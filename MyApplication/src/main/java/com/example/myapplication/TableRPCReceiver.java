package com.example.myapplication;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Created by dad9r on 8/7/13.
 */
public class TableRPCReceiver implements TableRPCIFace {

    private static final int MAX_THREADS = 8;
    private final int tenSeconds = 10000;

    private Logger log = Logger.getLogger("TableRPCReceiver");

    private TableActivity table;
    private SocketAcceptor acceptor;
    private ArrayList<SocketListener> listeners;
    private String ipAddress;
    private int port;

    public TableRPCReceiver(TableActivity tableActivity, int port) {
        this.port = port;
        table = tableActivity;
        openConnector(port);
    }

    private void openConnector(int port) {
        acceptor = new SocketAcceptor(port);
        Thread acceptorThread = new Thread(acceptor);
        acceptorThread.start();
        ipAddress = getMyIP();
    }

    public void discard(Card card) {
        table.insertCardBottom(card);
    }

    public Card draw() throws CardDeck.DeckExhaustedException {
        return table.getTopCard();
    }

    public void terminate() {
        acceptor.terminate();
        table.finish();
    }

    private class SocketAcceptor implements Runnable {

        private int port;
        private ServerSocket socket;

        public SocketAcceptor(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            try {
                log.info("Opening root socket");
                socket = new ServerSocket(port);
                listeners = new ArrayList<SocketListener>();

                while (socket.isBound()) {
                    log.info("Thread waiting for connections");
                    Socket client = socket.accept();
                    log.info("Thread got a connection");
                    if (listeners.size() < MAX_THREADS) {
                        log.info("opening new listener thread");
                        SocketListener newPlayer = new SocketListener(client);
                        listeners.add(newPlayer);
                        Thread clientThread = new Thread(newPlayer);
                        clientThread.start();
                        log.info("called start on listener, should be going");
                    }
                    else {
                        log.info("No threads left for listener, closing connection");
                        client.close();
                    }
                }
            } catch (IOException e) {
                // socket error, usually, so terminate
                TableRPCReceiver.this.terminate();
            }
        }

        public void terminate() {
            if (socket != null) {
                while (!listeners.isEmpty()) {
                    listeners.remove(0).terminate();
                }
                try {
                    socket.close();
                    socket = null;
                    log.info("terminating acceptor thread");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getMyIP() {
        WifiManager mgr = (WifiManager) table.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = mgr.getConnectionInfo();
        if (info.getNetworkId() != -1) {
            int ip = info.getIpAddress();
            return String.format("%d.%d.%d.%d",
                    (ip & 0xff),
                    ((ip >> 8) & 0xff),
                    ((ip >> 16) & 0xff),
                    ((ip >> 24) & 0xff));
        }
        return null;
    }

    public String getIP() {
        return ipAddress;
    }

    public String getPort() {
        return String.valueOf(port);
    }

    private class SocketListener implements Runnable {
        private Socket socket;
        private InputStream in;
        private OutputStream out;
        private ArrayList<Card> playersCards;

        public SocketListener(Socket socket) {
            this.socket = socket;
            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
                socket.setSoTimeout(tenSeconds);
                socket.setKeepAlive(true);
                playersCards = new ArrayList<Card>();
                log.info("listener ready to run");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            log.info("Starting up listener thread ");
            byte [] raw = new byte[12];
            int timeouts = 0;
            final int MAX_TIME_OUTS = 5; // 5 intervals of "timeout" seconds, timeout is set in constructor
            try {
                do {
                    int howMuch = 0;
                    try {
                        howMuch = in.read(raw);
                    }
                    catch (InterruptedIOException e) {
                        // socket timed out, the following tests will break or continue as needed
                        timeouts++;
                    }
                    if (howMuch == -1 || !socket.isConnected() || socket.isOutputShutdown() || socket.isInputShutdown() || timeouts > MAX_TIME_OUTS) break;
                    if (howMuch == 0) continue;

                    timeouts = 0;
                    Marshaller.CardOperation request = Marshaller.unmarshall(raw);
                    switch (request.op) {
                        case DRAW:
                            try {
                                log.info("Drawing");
                                Card result = draw();
                                log.info("  Writing result");
                                out.write(Marshaller.marshal(Marshaller.operationCode.DRAW, result));
                                playersCards.add(result);
                            } catch (CardDeck.DeckExhaustedException e) {
                                e.printStackTrace();
                                log.info("  no card, writing error");
                                out.write(Marshaller.marshal(Marshaller.operationCode.ERROR));
                            }
                            log.info("  done");
                            break;

                        case DISCARD:
                            log.info("Discarding");
                            if (request.card == null) {
                                log.info("  no card, writing error");
                                out.write(Marshaller.marshal(Marshaller.operationCode.ERROR));
                            }
                            else if (!playersCards.contains(request.card)) {
                                log.info("  trying to return a card you don't have, writing error");
                                out.write(Marshaller.marshal(Marshaller.operationCode.ERROR));
                            }
                            else {
                                discard(request.card);
                                log.info("  Writing result");
                                out.write(Marshaller.marshal(Marshaller.operationCode.DISCARD));
                                playersCards.remove(request.card);
                            }
                            log.info("  done");
                            break;

                        case SHUFFLE:
                            break;
                        case QUIT:
                            break;
                        case PLAY:
                            break;
                        case PING:
                            out.write(Marshaller.marshal(Marshaller.operationCode.PONG));
                            break;
                        case ERROR:
                            log.info("  writing error");
                            out.write(Marshaller.marshal(Marshaller.operationCode.ERROR));
                            log.info("done");
                            break;
                    }
                } while (socket.isConnected());

            } catch (IOException e) {
                e.printStackTrace();
            }
            terminate();
        }

        public void terminate() {
            listeners.remove(this);
            try {
                log.info("Closing listener socket ");
                while (!playersCards.isEmpty()) {
                    discard(playersCards.remove(0));
                }
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            in = null;
            out = null;
            socket = null;
        }
    }
}

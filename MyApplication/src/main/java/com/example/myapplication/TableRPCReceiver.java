package com.example.myapplication;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Created by dad9r on 8/7/13.
 */
public class TableRPCReceiver implements TableRPCIFace {

    private static final int MAX_THREADS = 8;

    private Logger log = Logger.getLogger("TableRPCReceiver");

    private TableActivity table;
    private SocketAcceptor acceptor;
    private SocketListener[] listeners;
    private String ipAddress;
    private int port;
    private int nextListener;

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
                nextListener = 0;
                listeners = new SocketListener[MAX_THREADS];

                while (socket.isBound()) {
                    log.info("Thread waiting for connections");
                    Socket client = socket.accept();
                    log.info("Thread got a connection");
                    if (nextListener < MAX_THREADS) {
                        log.info("opening new listener thread");
                        listeners[nextListener] = new SocketListener(client, nextListener);
                        Thread clientThread = new Thread(listeners[nextListener++]);
                        clientThread.start();
                        log.info("called start on listener, should be going");
                    }
                    else {
                        // TODO: enable defrag, or garbage collection
                        log.info("No threads left for listener, closing connection");
                        client.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void terminate() {
            if (socket != null) {
                try {
                    socket.close();
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

    public void terminate() {
        acceptor.terminate();
        for (int i = 0; i < MAX_THREADS; ++i) {
            if (listeners[i] != null) {
                listeners[i].terminate();
            }
        }
    }

    private class SocketListener implements Runnable {
        private int me;
        private Socket socket;
        private InputStream in;
        private OutputStream out;

        public SocketListener(Socket socket, int me) {
            this.socket = socket;
            this.me = me;
            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
                log.info("listener ready to run");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            log.info("Starting up listener thread " + me);
            byte [] raw = new byte[12];
            try {
                while (in.read(raw) > 0) {
                    log.info("Received request");
                    Marshaller.CardOperation request = Marshaller.unmarshall(raw);
                    switch (request.op) {
                        case DRAW:
                            try {
                                log.info("Drawing");
                                Card result = draw();
                                log.info("  Writing result");
                                out.write(Marshaller.marshal(Marshaller.operationCode.DRAW, result));
                            } catch (CardDeck.DeckExhaustedException e) {
                                e.printStackTrace();
                                log.info("  no card, writing error");
                                out.write(Marshaller.marshal(Marshaller.operationCode.ERROR));
                            }
                            log.info("  done");
                            break;

                        case DISCARD:
                            log.info("Discarding");
                            if (request.card != null) {
                                discard(request.card);
                                log.info("  Writing result");
                                out.write(Marshaller.marshal(Marshaller.operationCode.DISCARD));
                            } else {
                                log.info("  no card, writing error");
                                out.write(Marshaller.marshal(Marshaller.operationCode.ERROR));
                            }
                            log.info("  done");
                            break;

                        case SHUFFLE:
                            break;
                        case QUIT:
                            break;
                        case PLAY:
                            break;
                        case ERROR:
                            log.info("  writing error");
                            out.write(Marshaller.marshal(Marshaller.operationCode.ERROR));
                            log.info("done");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            terminate();
        }

        public void terminate() {
            listeners[me] = null;
            try {
                log.info("Closing listener socket " + me);
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

package com.example.myapplication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by dad9r on 8/7/13.
 */
public class TableRPCReceiver implements TableRPCIFace {

    private TableActivity table;
    private Thread acceptorThread;

    public TableRPCReceiver(TableActivity tableActivity, int port) {
        table = tableActivity;
        openConnector(port);
    }

    private void openConnector(int port) {
        acceptorThread = new Thread(new SocketAcceptor(port));
        acceptorThread.start();
    }

    public void discard(Card card) {
        table.insertCardBottom(card);
    }

    public Card draw() throws CardDeck.DeckExhaustedException {
        return table.getTopCard();
    }

    private class SocketAcceptor implements Runnable {

        int port;

        public SocketAcceptor(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            ServerSocket socket;
            try {
                socket = new ServerSocket(port);
                while (socket.isBound()) {
                    Socket client = socket.accept();
                    Thread clientThread = new Thread(new SocketListener(client));
                    clientThread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class SocketListener implements Runnable {
        private Socket socket;
        private InputStream in;
        private OutputStream out;

        public SocketListener(Socket socket) {
            this.socket = socket;
            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte [] raw = new byte[12];
            try {
                while (in.read(raw) > 0) {
                    Marshaller.CardOperation request = Marshaller.unmarshall(raw);
                    switch (request.op) {
                        case DRAW:
                            try {
                                Card result = draw();
                                out.write(Marshaller.marshal(Marshaller.operationCode.DRAW, result));
                            } catch (CardDeck.DeckExhaustedException e) {
                                e.printStackTrace();
                            }
                            out.write(Marshaller.marshal(Marshaller.operationCode.ERROR));
                            break;

                        case DISCARD:
                            if (request.card != null) {
                                discard(request.card);
                                out.write(Marshaller.marshal(Marshaller.operationCode.DISCARD));
                            } else {
                                out.write(Marshaller.marshal(Marshaller.operationCode.ERROR));
                            }
                            break;

                        case SHUFFLE:
                            break;
                        case QUIT:
                            break;
                        case PLAY:
                            break;
                        case ERROR:
                            out.write(Marshaller.marshal(Marshaller.operationCode.ERROR));
                    }
                }
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

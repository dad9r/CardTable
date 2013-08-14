package com.example.myapplication;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

/**
 * Created by dad9r on 8/8/13.
 */
public class TableRPCSender implements TableRPCIFace, Runnable {

    private final int threeSeconds = 3000;
    private final String ipAddress;
    private final int port;

    private InputStream socketIn;
    private OutputStream socketOut;
    private Socket socket;

    private final Object startLock = new Object();
    private final Object senderLock = new Object();

    private byte[] senderData;
    private byte[] resultData;

    private boolean opPending;
    private boolean opComplete;

    private Logger log = Logger.getLogger("TableRPCSender");

    public TableRPCSender(String ip, String port) throws IOException {
        this.ipAddress = ip;
        this.port = Integer.parseInt(port);

        synchronized (startLock) {
            Thread tableThread = new Thread(this);
            tableThread.start();
            try {
                startLock.wait();
            } catch (InterruptedException e) {
            }

            if (!stillConnected())
                throw new IOException("Couldn't connect, not running");
        }
    }

    public void requestTerminate() {
        synchronized (senderLock) {
            opPending = true;
            senderData = null;
            senderLock.notifyAll();
        }
    }

    @Override
    public void discard(Card card) {
        synchronized (senderLock) {
            senderData = Marshaller.marshal(Marshaller.operationCode.DISCARD, card);
            opPending = true;
            opComplete = false;
            senderLock.notify();

            while (!opComplete) {
                try {
                    senderLock.wait();
                } catch (InterruptedException e) {
                }
            }

            senderData = null;
            resultData = null;
        }
    }

    @Override
    public Card draw() {
        synchronized (senderLock) {
            senderData = Marshaller.marshal(Marshaller.operationCode.DRAW);
            opPending = true;
            opComplete = false;
            senderLock.notify();

            while (!opComplete) {
                try {
                    senderLock.wait();
                } catch (InterruptedException e) {
                }
            }

            Marshaller.CardOperation res = Marshaller.unmarshall(resultData);
            senderData = null;
            resultData = null;
            if (res.op == Marshaller.operationCode.DRAW) {
                return res.card;
            }
        }
        return null;
    }

    public boolean stillConnected() {
        return socket != null
                && socket.isConnected()
                && !socket.isInputShutdown()
                && !socket.isOutputShutdown();
    }

    @Override
    public void run() {
        synchronized (startLock) {
            try {
                socket = new Socket(ipAddress, port);
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                startLock.notify();
            }
        }
        if (socket == null || !socket.isConnected()) {
            log.info("Bad socket, not running");
            return;
        }

        try {
            log.info("Sender thread is running");
            socket.setSoTimeout(threeSeconds);
            socket.setKeepAlive(true);
            socketIn = socket.getInputStream();
            socketOut = socket.getOutputStream();

            opPending = false;

            synchronized (senderLock) {
                while (stillConnected()) {
                    while (!opPending) {
                        try {
                            senderLock.wait(threeSeconds);
                            if (!heartbeat()) {
                                terminate();
                                return;
                            }
                        } catch (InterruptedException e) {
                        }
                    }

                    if (senderData != null) {
                        send();
                        opComplete = true;
                        opPending = false;
                        log.info("Sending notify to caller");
                        senderLock.notify();
                    }
                    else {
                        senderLock.notifyAll();
                        break;
                    }
                }
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        terminate();
    }

    private boolean dead = false;
    private void terminate() {
        if (dead) return;
        try {
            log.info("Thread terminating");
            if (socketIn != null) socketIn.close();
            if (socketOut != null) socketOut.close();
            if (socket != null) socket.close();
            dead = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        socketIn = null;
        socketOut = null;
        socket = null;
    }

    private boolean heartbeat() {
        if (socket == null ||
                socket.isClosed() ||
                socket.isOutputShutdown() ||
                socket.isInputShutdown()) return false;

        final int MAX_TIME_OUTS = 5; // 15 seconds
        try {
            socketOut.write(Marshaller.marshal(Marshaller.operationCode.PING));
            resultData = new byte[12];
            int res = 0;
            int timeouts = 0;
            do {
                try {
                    res = socketIn.read(resultData);
                } catch (InterruptedIOException e) {
                    // times out, could be dead
                    timeouts++;
                }
            } while (res == 0 && timeouts < MAX_TIME_OUTS);

            if (res > 0) {
                return Marshaller.unmarshall(resultData).op.equals(Marshaller.operationCode.PONG);
            }
        } catch (IOException e) {
            // something went wrong with the socket, so tell them the heartbeat failed
        }
        return false;
    }

    private int send() {
        try {
            log.info("Sending data");
            socketOut.write(senderData);
            resultData = new byte[12];
            do {
                try {
                    int res = socketIn.read(resultData);
                    log.info("Data transferred");
                    return res;
                } catch (InterruptedIOException e) {
                    // read timed out, try again if still connected
                    log.info("timed out");
                }
            } while (socket.isConnected() && !socket.isInputShutdown() && !socket.isOutputShutdown());
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("Sending data failed");
        return -1;
    }
}

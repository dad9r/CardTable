package com.example.myapplication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

/**
 * Created by dad9r on 8/8/13.
 */
public class TableRPCSender implements TableRPCIFace, Runnable {

    private InputStream socketIn;
    private OutputStream socketOut;
    private Socket socket;

    private String ipAddress;
    private String port;

    private final Object senderLock = new Object();
    private byte[] senderData;
    private byte[] resultData;

    private boolean opPending;
    private boolean opComplete;

    private Logger log = Logger.getLogger("TableRPCSender");

    public TableRPCSender(String ip, String port) throws IOException {
        this.ipAddress = ip;
        this.port = port;
    }

    public void terminate () {
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
            log.info("waking thread");
            senderLock.notify();

            while (!opComplete) {
                try {
                    log.info("Caller Waiting");
                    senderLock.wait();
                } catch (InterruptedException e) {
                    log.info("Caller notified");
                }
            }
            log.info("Caller awake");

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
            log.info("waking thread");
            senderLock.notify();

            while (!opComplete) {
                try {
                    log.info("Caller Waiting");
                    senderLock.wait();
                } catch (InterruptedException e) {
                    log.info("Caller notified");
                }
            }
            log.info("Caller awake");

            Marshaller.CardOperation res = Marshaller.unmarshall(resultData);
            senderData = null;
            resultData = null;
            if (res.op == Marshaller.operationCode.DRAW) {
                return res.card;
            }
        }
        return null;
    }

    @Override
    public void run() {
        try {
            log.info("Sender thread is running");
            InetAddress addr = InetAddress.getByName(ipAddress);
            socket = new Socket(addr, Integer.parseInt(port));
            socketIn = socket.getInputStream();
            socketOut = socket.getOutputStream();

            opPending = false;

            synchronized (senderLock) {
                while (socket.isConnected()) {
                    while (!opPending) {
                        try {
                            log.info("Thread is Waiting");
                            senderLock.wait();
                        } catch (InterruptedException e) {
                            log.info("Thread notified");
                        }
                    }
                    log.info("Thread is awake");

                    if (senderData != null) {
                        send();
                        opComplete = true;
                        opPending = false;
                        log.info("Sending notify to caller");
                        senderLock.notify();
                    }
                    else {
                        socketIn.close();
                        socketOut.close();
                        socket.close();
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
        log.info("Thread terminating");
    }

    private int send() {
        try {
            log.info("Sending data");
            socketOut.write(senderData);
            resultData = new byte[12];
            int res = socketIn.read(resultData);
            log.info("Data transferred");
            return res;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
}

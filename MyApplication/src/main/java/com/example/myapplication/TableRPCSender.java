package com.example.myapplication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by dad9r on 8/8/13.
 */
public class TableRPCSender implements TableRPCIFace {

    private InputStream socketIn;
    private OutputStream socketOut;
    private Socket socket;

    public TableRPCSender(ConnectActivity.TableServerInfo serverInfo) {
        try {
            socket = new Socket(serverInfo.ipStr(), serverInfo.port);
            socketIn = socket.getInputStream();
            socketOut = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void discard(Card card) {
        try {
            socketOut.write(Marshaller.marshal(Marshaller.operationCode.DISCARD, card));
            byte[] raw = new byte[12];
            int result = socketIn.read(raw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Card draw() throws CardDeck.DeckExhaustedException {
        try {
            socketOut.write(Marshaller.marshal(Marshaller.operationCode.DRAW));
            byte[] raw = new byte[12];
            int result = socketIn.read(raw);
            if (result > 0) {
                Marshaller.CardOperation res = Marshaller.unmarshall(raw);
                if (res.op == Marshaller.operationCode.DRAW) {
                    return res.card;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}

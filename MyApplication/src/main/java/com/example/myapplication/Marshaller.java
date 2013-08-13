package com.example.myapplication;

import java.nio.ByteBuffer;

/**
 * Created by dad9r on 8/8/13.
 */
public class Marshaller {

    public static byte[] marshal(Marshaller.operationCode op) {
        return ByteBuffer.allocate(4)
                .putInt(op.ordinal())
                .array();
    }

    public static byte[] marshal(Marshaller.operationCode op, Card card) {
        return ByteBuffer.allocate(12)
                .putInt(op.ordinal())
                .putInt(card.getSuit().ordinal())
                .putInt(card.getValue().ordinal())
                .array();
    }

    public static CardOperation unmarshall(byte[] raw) {
        if (raw == null)
            return new CardOperation(operationCode.ERROR.ordinal());

        ByteBuffer buffer = ByteBuffer.wrap(raw);
        int op = buffer.getInt();
        if (buffer.hasRemaining()) {
            return new CardOperation(op, buffer.getInt(), buffer.getInt());
        }
        return new CardOperation(op);
    }

    public static class CardOperation {
        public operationCode op;
        public Card card;

        public CardOperation(int op) {
            this.op = operationCode.values()[op];
            this.card = null;
        }

        public CardOperation(int op, int suit, int value) {
            this.op = operationCode.values()[op];
            this.card = new Card(suit, value);
        }
    }

    public enum operationCode {
        DISCARD,
        DRAW,
        SHUFFLE,
        QUIT,
        PLAY,
        PING,
        PONG,
        ERROR
    }
}

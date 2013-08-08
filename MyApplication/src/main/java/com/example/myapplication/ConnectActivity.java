package com.example.myapplication;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by dad9r on 8/8/13.
 */
public class ConnectActivity extends ListActivity {

    private Intent handIntent;
    private ArrayAdapter<TableServerInfo> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new ArrayAdapter<TableServerInfo>(this, R.layout.activity_connect);
        setListAdapter(adapter);

        populateList();

        handIntent = new Intent(this, HandActivity.class);
    }

    private void populateList() {
        WifiManager wifiMgr = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiMgr.getConnectionInfo();
        if (info.getNetworkId() == -1) {
            // wifi is disabled or not connected
        }
        else {
            int myIP = info.getIpAddress();
            for (int i = 0; i < 256; ++i) {
                doScan((myIP % 256) + i);
            }
        }
    }

    private void doScan(int ip) {
        for (int port : MainActivity.availablePorts) {
            if (ping(ip, port)) {
                adapter.add(new TableServerInfo(ip, port));
            }
        }
    }

    private boolean ping(int ip, int port) {
        TableServerInfo info = new TableServerInfo(ip, port);

        InetAddress addr;
        Socket socket = null;
        InputStream in = null;
        OutputStream out = null;

        try {
            addr = InetAddress.getByName(info.ipStr());
            socket = new Socket(addr, port);
            in = socket.getInputStream();
            out = socket.getOutputStream();

            out.write(port);
            if (in.read() == port)
                return true;

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        TableServerInfo server = adapter.getItem(position);

        handIntent.putExtra("serverAddr", server.ipStr());
        handIntent.putExtra("serverPort", server.port);

        startActivity(handIntent);

        finish();
    }

    public class TableServerInfo {
        public int ip;
        public int port;

        public String ipStr () {
            return String.format("%d.%d.%d.%d",
                    (ip & 0xff),
                    (ip >> 8 & 0xff),
                    (ip >> 16 & 0xff),
                    (ip >> 24 & 0xff));
        }

        public TableServerInfo(int ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        public String toString() {
            return ipStr();
        }
    }
}
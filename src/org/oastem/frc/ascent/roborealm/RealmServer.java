/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.oastem.frc.ascent.roborealm;

import java.io.*;

import javax.microedition.io.Connector;
import javax.microedition.io.ServerSocketConnection;
import javax.microedition.io.SocketConnection;


/**
 * 
 * @author STEM
 **/
public class RealmServer {
    private static RealmServer instance;
    
    private SocketConnection sc;
    
    private String host = "localhost";
    private int port = 6060;
    
    private InputStream in;
    private PrintStream out;
    
    private RealmListener rl;
    private Thread reader;
    
    private RealmServer() {
        
    }
    
    public static RealmServer getInstance() {
        if (instance == null) instance = new RealmServer();
        return instance;
    }
    
    public void setHost(String newHost) {
        this.host = newHost;
    }
    
    public void setPort(int newPort) {
        this.port = newPort;
    }
    
    public void connect() throws IOException {
        sc = (SocketConnection) Connector.open("socket://" + host + ":" + port);
        sc.setSocketOption(SocketConnection.LINGER, 5);
        
        in = sc.openInputStream();
        
        this.reader = new Thread(new Runnable() {

            public void run() {
                startReadInternal();
            }
        });
        this.reader.start();
        
        out = new PrintStream(sc.openOutputStream());
    }
    
    private void startReadInternal() {
        try {
            String resp = "";
            
            int bufLen = 1024;
            char[] buf = new char[bufLen];
            int pos = 0;
            
            int consecZero = 0;
            final int ZERO_THRESHOLD = 10;
            
            InputStreamReader isr = new InputStreamReader(this.in);
            
            while (true) {
                int temp = 0;
                if (isr.ready()) temp = isr.read();
                if (pos == 0 && temp == 0) continue;
                
                if (temp != 0) consecZero = 0;
                else consecZero++;
                
                if (consecZero >= ZERO_THRESHOLD) {
                    resp = new String(buf).trim();
                    if (!resp.equals("")) {
                        this.rl.serverRespond(resp);
                        buf = new char[bufLen];
                        pos = 0;
                    }
                }
                
                buf[pos] = (char)temp;
                
                pos = ++pos % bufLen;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void write(String data) {
        out.print(data);
    }
    
    public void attachListener(RealmListener rl) {
        this.rl = rl;
    }
    
}
    
    

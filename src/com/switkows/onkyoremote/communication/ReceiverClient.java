package com.switkows.onkyoremote.communication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import android.util.Log;

/***
 * Provides TCP/IP Communication interface to Server (Receiver)
***/
public class ReceiverClient extends Eiscp {
   public static final String DEFAULT_IP_ADDR = "16.1.1.200";
   public static final int DEFAULT_TCP_PORT   = Eiscp.DEFAULT_EISCP_PORT;
   private final String ipAddr;
   private final int tcpPort;
   private boolean isRunning = false;
   
   private PrintWriter mSender;
//   private final OnMessageReceived messageListener;
   private Socket mClientSocket;

   public ReceiverClient(String ip_addr, int port) {
      super(ip_addr,port);
      ipAddr = ip_addr;
      tcpPort = port;
//      messageListener = listener;
      //FIXME - move to the thread?
//      try {
//         mClientSocket = new Socket(ipAddr, tcpPort);
//      } catch (Exception e) {
//         Log.e("ReceiverClient",e.toString());
//         e.printStackTrace();
//      }
   }

   //Override Eiscp methods to wrap execution in thread (to disconnect from UI)
   public void sendCommand(final int command) {
      new Thread() {
         public void run() {
            ReceiverClient.super.sendCommand(command);
         }
      }.start();
   }
   
   public boolean closeSocket() {
      new Thread() {
         public void run() {
            ReceiverClient.super.closeSocket();
         }
      }.start();
      return false;//FIXME - return value not truly returned to caller
   }
   
   public boolean connectSocketThread() {
      new Thread() {
         public void run() {
            ReceiverClient.super.connectSocket();
         }
      }.start();
      return true;//FIXME - return value not truly returned to caller
   }
//   public void run() {
//      if(mClientSocket != null && mClientSocket.isConnected()) {
//         try {
//            mSender = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mClientSocket.getOutputStream())),true);
//            BufferedReader mReceiver = new BufferedReader(new InputStreamReader(mClientSocket.getInputStream()));
//            while(isRunning) {
//               String message = mReceiver.readLine();
////               if(message != null && messageListener != null) {
////                  messageListener.onMessageReceived(message);
////               }
//            }
//         } catch (Exception e) {
//            Log.e("ReceiverClient","Connection not opened or message not received correctly: "+e.toString());
//         } finally {
//            try {
//               mClientSocket.close();
//            } catch (IOException e) {
//               Log.e("ReceiverClient","Connection could not be closed: "+e.toString());
//            }
//         }
//      } else {
//         Log.v("ReceiverClient","Socket not connected correctly...");
//      }
//   }
//   
//   public void sendMessage(String message) {
//      if(mSender != null && !mSender.checkError()) {
//         mSender.println(message);
//         mSender.flush();
//      }
//   }
//   
//   private interface OnMessageReceived {
//      public void onMessageReceived(String message);
//   }
}

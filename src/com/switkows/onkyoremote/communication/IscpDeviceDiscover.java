package com.switkows.onkyoremote.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Vector;

import android.content.Context;
import android.util.Log;

public class IscpDeviceDiscover {

   public final static String DEFAULT_ISCP_DISCOVERY_IP_ADDR   = "255.255.255.255";
   public final static int    DEFAULT_ISCP_DISCOVERY_UDP_PORT  = 60128;
   
   private Vector<ReceiverInfo> mReceivers;
   private IscpCommands mCommands;
   
   public void discover(final Context context, final Eiscp eiscp) {
      discover(null, -1,context,eiscp);
   }
   public void discover(String ip_addr, int port_num, final Context contfext, final Eiscp eISCPInterface) {
      IscpCommands commands = IscpCommands.getInstance();
      mCommands = commands;
      if(mReceivers==null)
         mReceivers = new Vector<ReceiverInfo>();
      if(ip_addr == null || ip_addr.equals(""))
         ip_addr = DEFAULT_ISCP_DISCOVERY_IP_ADDR;
      if(port_num <= 0)
         port_num = DEFAULT_ISCP_DISCOVERY_UDP_PORT;
      Log.d("TJS","Starting discover()");
      MulticastSocket socket = null;
      try {
         socket = new MulticastSocket(port_num);
      } catch (IOException e1) {
         e1.printStackTrace();
      }
      InetAddress group = null;
      try {
         group = InetAddress.getByName(ip_addr);
      } catch (UnknownHostException e) {
         e.printStackTrace();
      }
      
      if(socket != null && group != null) {
         //perform query, analyze response
         StringBuilder sb = Eiscp.getEiscpPacket(IscpCommands.getCommandStr(IscpCommands.SERVER_QUERY),'x');
         byte[] message = sb.toString().getBytes();
         DatagramPacket packet = new DatagramPacket(message, message.length, group, port_num);
         try {
            Log.d("TJS","Opening Socket");

            //configure socket
            socket.setBroadcast(true);
            socket.setSoTimeout(2000);//wait for a maximum of 2 seconds

            //send request to network
            socket.send(packet);

            //wait for response(s)
            //response is in format: '!1ECNnnnnnnnn/ppppp/dd/iiiiiiiiii
            //                              (<65)    (5)  (2) (< 13)
            byte[] responseRawData = new byte[91]; //maximum expected packet is 91 bytes
            DatagramPacket responsePacket = new DatagramPacket(responseRawData, responseRawData.length);
            //read two packets. first will be the broadcast we just sent, second will contain response data (in theory)
            boolean foundPacket = false;
            int i = 0;
            //FIXME - add delay, so we wait for others to respond (and collect results)
            while(!foundPacket && i < 10) {
               socket.receive(responsePacket);
               String returnedData = new String(responsePacket.getData());
               Vector<String> iscpMessages = Eiscp.parsePacketBytes(returnedData, false);
               String hostAddress = responsePacket.getAddress().getHostAddress();
               for(String iscpMessage : iscpMessages) {
                  //                  Log.v("TJSDiscover", "ISCP message : '"+iscpMessage+"'");
                  if(iscpMessage.contains("ECN") && !iscpMessage.substring(0,7).equals("ECNQSTN")) {
                     foundPacket = true;
//                     01-02 22:45:04.797: V/ReceiverClient(25361):     ECNTX-NR809/60128/DX/0009B0446FE5xxxxxxx

                     String[] parts = iscpMessage.split("/");
                     if(parts.length != 4) {
                        Log.e("TJSDiscover", "Packet received incorrectly: expected four /-separated pieces...");
                        Log.e("TJSDiscover", "full ISCP message : '"+iscpMessage+"'");
                     }
                     else {
                        String model = parts[0].substring(3);
                        String tcp_port = parts[1];
                        String region = parts[2];
                        String receiverId = (parts.length > 12) ? parts[3].substring(0,12) : parts[3];
                        ReceiverInfo receiver = new ReceiverInfo(hostAddress, tcp_port, model, region, receiverId);
                        mReceivers.add(receiver);
                        Log.d("TJS", receiver.printMe());
                     }
                  }
               }
            }

            //close socket
            socket.close();
         } catch (IOException e) {
            e.printStackTrace();
            Log.e("TJS", "IO exception : "+e);
         }
         
      }
      else
      {
         Log.e("TJS","Socket not created or network address not generated properly ("+socket+","+group+")");
      }
   }
   
   public String printAllReceivers() {
      String retStr = "";
      for(ReceiverInfo receiver : mReceivers)
         retStr += receiver.printMe() + "\n";
      return retStr;
   }

   public boolean receiversPresent() {
      return !mReceivers.isEmpty();
   }

   public ReceiverInfo getReceiver(int index) {
      if(mReceivers.isEmpty())
         return null;
      return mReceivers.get(index%mReceivers.size());
   }
}

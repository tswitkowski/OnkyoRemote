package com.switkows.onkyoremote.communication;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Vector;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

/***
 * Shim around Eiscp class, to provide usefull wrappers to better handle Android connection
***/
//FIXME - can/should I build this into a content provider? I'm guessing not (since it complicates queries a bit)
public class ReceiverClient extends Eiscp {
   public static final String DEFAULT_IP_ADDR = "10.1.1.45";
   public static final int DEFAULT_TCP_PORT   = Eiscp.DEFAULT_EISCP_PORT;
   public final CommandHandler mParent;
   private ReceiverInfo mInfo;
   private ServerListener mServerListener;

   public ReceiverClient(Fragment parent, ReceiverInfo info) {
      this(parent,info.getIpAddr(),info.getTcpPort());
      mInfo = info;
   }
   public ReceiverClient(Fragment parent,String ip_addr, int port) {
      super(ip_addr,port);
      try {
         mParent = (CommandHandler)parent;
      } catch(ClassCastException e) {
         throw new ClassCastException(parent.toString()+" must implement CommandHandler interface");
      }
      mInfo = new ReceiverInfo(ip_addr, String.valueOf(port), "TBD", "TBD", "TBD");
   }

   /**
    * Initializes variables (like power status, input selection, volume, etc)
    * and opens the socket to the server (AV Receiver)
    */
   public void initiateConnection() {
      new AsyncTask<Void,Void,Void>() {
         @Override
         protected Void doInBackground(Void... params) {
            //make connection
            boolean connected = connectSocket();
            mInfo.setConnected(connected);
            //Now, collect a bit of information of the current state from the server (AV Receiver)
            if(connected) {
               String[] commands = {String.valueOf(IscpCommands.POWER_QUERY),
                                 String.valueOf(IscpCommands.SOURCE_QUERY),
                                 String.valueOf(IscpCommands.VOLUME_QUERY),
                                 String.valueOf(IscpCommands.MUTE_QUERY)
                                 };
               Log.v("TJS","Connected..");
               QueryServerTask query;
               Log.v("TJS","Querying Receiver status (power, volume, source, mute)..");
               query = new QueryServerTask(ReceiverClient.this);
               query.execute(commands);
               //make sure to start this in the 'thread pool executor' since it will run
               //indefinitely. Otherwise it would have blocked all other threads (namely:
               //the above thread and any 'sendCommand' threads) from executing)
               mServerListener = new ServerListener(ReceiverClient.this);
               mServerListener.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void)null);
            }
            return null;
         }

         @Override
         protected void onPostExecute(Void result) {
            connectionStateChanged();
            super.onPostExecute(result);
         }
      }.execute();
   }

   public void connectionStateChanged() {
      Log.v("TJS","Calling connectionStateChanged : connected = "+isConnected());
      mInfo.setConnected(isConnected());
      if(mParent != null)
         ((CommandHandler)mParent).onConnectionChange(isConnected());
   }

   //Override Eiscp methods to wrap execution in thread (to disconnect from UI)
   public void sendCommand(final int command) {
      new AsyncTask<Void,Void,Void>() {
         @Override
         protected Void doInBackground(Void... params) {
            //do not send update down Eiscp if we are requesting to change the Source select to the same source
            boolean sourceChangeCommand = isSourceChange(command);
            if(!sourceChangeCommand || sendSourceChange(command))
               ReceiverClient.super.sendCommand(command);
            if(sourceChangeCommand)
               mInfo.setSource(command);
            return null;
         }

         @Override
         protected void onPostExecute(Void result) {
            connectionStateChanged();
            super.onPostExecute(result);
         }
      }.execute();
   }
   
   public boolean isSourceChange(final int command) {
      return command >= IscpCommands.SOURCE_DVR && command <= IscpCommands.SOURCE_SIRIUS;
   }
   
   public boolean sendSourceChange(final int command) {
      return command != mInfo.getSource();
   }

   /***
    * Runs on GUI thread. Gives feedback to GUI and stores state to our object
    * @param queryResult - message received from server (AV Receiver)
    */
   public void postQueryResults(String queryResult) {
//      Log.d("TJS","Query Result : '"+queryResult+"'...");
//      String[] resultParnts = queryResult.split("/\n");
//      if(queryResult.split(regularExpression))
      if(mParent != null ) {
         if(queryResult.contains("PWR")) {
            //Power state decode
            String resultStr = queryResult.substring(3, 5);
            int value = Integer.parseInt(resultStr);
            Log.v("TJS","Power query result = '"+value+"'");
            mParent.onPowerChange(value == 1);
            mInfo.setPoweredOn(value==1);
            connectionStateChanged();
         } else if(queryResult.contains("MVL")) {
            //Volume level decode
            String resultStr = queryResult.substring(3, 5);
            try {
               float value;
               value = (float)Integer.parseInt(resultStr,16);
               Log.v("TJS","Volume query result = '"+value+"'");
               setVolume(value);
               mInfo.setVolume(value);//FIXME - clean up
               mParent.onVolumeChange(value);
            } catch (NumberFormatException e) {
               //if we get an unknown number (ex. 'N/A' sometimes comes back?!)
               Log.v("ReceiverClient", "Unknown Volume command. Ignoring: "+queryResult);
            }
         } else if(queryResult.contains("AMT")) {
            //Muted status decode
            String resultStr = queryResult.substring(3, 5);
            int value = Integer.parseInt(resultStr);
            Log.v("TJS","Muted query result = '"+value+"'");
            mParent.onMuteChange(value == 1);
            mInfo.setMuted(value==1);
         } else if(queryResult.contains("SLI")) {
            //Source status decode
            String resultStr = queryResult.substring(0, 5);
//            int value = Integer.parseInt(resultStr);
//            Log.v("TJS","Input query result = '"+value+"'");
            if(IscpCommands.commandMapInverse_.containsKey(resultStr)) {
               int input = IscpCommands.commandMapInverse_.get(resultStr);
               mParent.onInputChange(input);
               mInfo.setSource(input);
            }
         } else {
            Log.d("ReceiverClient","Unhandled message: "+queryResult);
         }
      }
      Log.i("TJS","Got result in Background Thread: '" + queryResult + "'...");
      Log.d("TJS"," in hex                        : '" + Eiscp.convertStringToHex(queryResult)+"'...");
      mParent.onMessageReceived(null, queryResult);
   }


   public boolean closeSocket() {
      new AsyncTask<Void,Void,Void>() {
         @Override
         protected Void doInBackground(Void... params) {
            ReceiverClient.super.closeSocket();
            return null;
         };
         @Override
         protected void onPostExecute(Void result) {
            connectionStateChanged();
         }
      }.execute();
      return false;
   }

   public boolean getPoweredOn() {
      return mInfo.isPoweredOn();
   }
   //redirect messages to Log.e() instead of System.err
//   public static void errorMessage(String message) {
//      Log.e("ReceiverClient",message);
//   }
//
//   //redirect messages to Log.v() instead of System.out
//   public static void debugMessage(String message) {
//      Log.v("ReceiverClient",message);
//   }
   
   /***
    * Sends commands to the server in a background thread
    * Note : though it says it is a 'query', it does not actually collect
    *        information, it only sends the commands. You must install the
    *        ServerListener to read/interpret the Server response(s)
    * @author Trevor
    *
    */
   protected class QueryServerTask extends AsyncTask<String, Void, Void> {

      private final ReceiverClient mParent;
      public QueryServerTask(ReceiverClient parent) {
         mParent = parent;
      }
      @Override
      protected Void doInBackground(String... params) {
         for(int i = 0 ; i < params.length ; i++) {
            Log.v("TJSAsync","Sending query : " + params[i]);
            int command = Integer.parseInt(params[i]);
            mParent.sendCommand(command,false);
         }
         return null;
      }
   }

   /***
    * Async task which runs indefinitely(?) listening to server for status updates
    * 
    * @author Trevor
    *
    */
   public class ServerListener extends AsyncTask<Void,String,Void> {
      private final ReceiverClient mParent;
      public ServerListener(ReceiverClient parent) {
         mParent = parent;
      }

      @Override
      protected void onPostExecute(Void result) {
         // Note : This method should not be called, probably.
         Log.d("TJS","ServerListener has finished...");
         super.onPostExecute(result);
      }

      @Override
      protected void onProgressUpdate(String... values) {
         //Note : I think we should only ever get one string in this method
         for(String result : values) {
//            Log.i("TJS","Got result in Background Thread: '" + result + "'...");
            mParent.postQueryResults(result);
         }
         super.onProgressUpdate(values);
      }

      @Override
      protected Void doInBackground(Void... params) {
         DataInputStream iStream = mParent.getInputStream();
         Socket socket = mParent.getSocket();
         byte [] buffer = new byte[64];
         int numReceived = 0;
         boolean debugging = true;
         int totBytesReceived = 0;
         int packetCounter = 0;
         while(socket.isConnected()) {
            try {
               numReceived = iStream.read(buffer);
               if(numReceived > 0) {
                  totBytesReceived = 0;
                  StringBuilder msgBuffer = new StringBuilder("");
                  if (debugging) debugMessage(" Packet"+"["+packetCounter+"]:");
                  
                  /* Read ALL the incoming Bytes and buffer them */ 
                  // *******************************************
                  if (debugging) debugMessage("numBytesReceived = "+numReceived);
                  while(numReceived>0 )
                  {
                    totBytesReceived+=numReceived;
                    msgBuffer.append(new String(buffer));
                    buffer = new byte[32];
                    numReceived = 0;
                    if (iStream.available()>0)
                      numReceived = iStream.read(buffer);
                    if (debugging) System.out.print(" "+numReceived);
                  }
//                  if(debugging)
//                     debugMessage("numBytesReceived = "+numBytesReceived+", totBytesReceived = "+totBytesReceived+", message.length = "+msgBuffer.toString().length());

                  //push rest of parsing to sub-method (note: string MUST be totBytesReceived long)
                  Vector<String> currentMessages = parsePacketBytes(msgBuffer.toString().substring(0, totBytesReceived), false);
                  for(String message : currentMessages)
                     publishProgress(message);
                  Log.d("TJS","Packet data complete...");
               }
            } catch (IOException e) {
               Log.i("ReceiverClient","Socket closed. killing Listener thread");
               break;
            }
         }
         return null;
      }
   }
   
   /***
    * Interface for passing state information from the Client back to the Fragment/Activity(s)
    * @author Trevor
    *
    */
   public interface CommandHandler {
      public void onMessageSent(String message);
      public void onMessageReceived(String message, String response);
      public void onInputChange(int sourceVal);
      public void onPowerChange(boolean powered_on);
      public void onMuteChange(boolean muted);
      /***
       * Interface for communicating volume state changes to GUI (call on GUI thread only!)
       * @param volume - decimal volume value (0 = no sound, 100.0 = max sound)
       */
      public void onVolumeChange(float volume);
      public void onConnectionChange(boolean isConnected);
      
      //returns instance to ReceiverInfo struct (for initializing GUI with valid information)
      public ReceiverInfo getReceiverInfo();
   }
   
   public interface CommandSendCallbacks {
      public void sendCommand(int command, boolean sendIfOff);
      public void sendQueryCommand(int command);
      public boolean toggleConnection();
      public void setVolume(float volume);
   }
}

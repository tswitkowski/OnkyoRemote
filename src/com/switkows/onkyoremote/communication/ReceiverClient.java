package com.switkows.onkyoremote.communication;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

/***
 * Shim around Eiscp class, to provide usefull wrappers to better handle Android connection
***/
//FIXME - can/should I build this into a content provider? I'm guessing not (since it complicates queries a bit)
public class ReceiverClient extends Eiscp {
   public static final String DEFAULT_IP_ADDR = "16.1.1.200";
   public static final int DEFAULT_TCP_PORT   = Eiscp.DEFAULT_EISCP_PORT;
   public final Fragment mParent;

   public ReceiverClient(Fragment parent,String ip_addr, int port) {
      super(ip_addr,port);
      mParent = parent;
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
            //Now, collect a bit of information of the current state from the server (AV Receiver)
            if(connected) {
               Log.v("TJS","Connected..");
               QueryServerTask query;
               Log.v("TJS","Querying power status..");
               query = new QueryServerTask(ReceiverClient.this);
               query.execute(String.valueOf(POWER_QUERY));
               //FIXME - unify into one thread?
               Log.v("TJS","Querying source status..");
               query = new QueryServerTask(ReceiverClient.this);
               query.execute(String.valueOf(SOURCE_QUERY));
               //FIXME - unify into one thread?
               Log.v("TJS","Querying Volume Level..");
               query = new QueryServerTask(ReceiverClient.this);
               query.execute(String.valueOf(VOLUME_QUERY));
               //FIXME - unify into one thread?
               Log.v("TJS","Querying Mute status..");
               query = new QueryServerTask(ReceiverClient.this);
               query.execute(String.valueOf(MUTE_QUERY));
//               query.execute(String.valueOf(VOLUME_QUERY));
            }
            return null;
         }

         @Override
         protected void onPostExecute(Void result) {
            connectionStateChanged();//FIXME - will this execute too soon (i.e. before all queries are completed)?
            super.onPostExecute(result);
         }
      }.execute();
      ;
   }

   public void connectionStateChanged() {
      Log.v("TJS","Calling connectionStateChanged : connected = "+isConnected());
      if(mParent != null && mParent instanceof CommandHandler)
         ((CommandHandler)mParent).onConnectionChange(isConnected());
   }

   //Override Eiscp methods to wrap execution in thread (to disconnect from UI)
   public void sendCommand(final int command) {
      new AsyncTask<Void,Void,Void>() {
         @Override
         protected Void doInBackground(Void... params) {
            ReceiverClient.super.sendCommand(command);
            return null;
         }

         @Override
         protected void onPostExecute(Void result) {
            connectionStateChanged();//FIXME - will this execute too soon (i.e. before all queries are completed)?
            super.onPostExecute(result);
         }
      }.execute();
   }
   
   //FIXME - change to AsyncTask?
   public String sendQueryCommand(final int command, final boolean closeSocket) {
      QueryServerTask query = new QueryServerTask(this);
      query.execute(String.valueOf(command));
      return null;
   }
   
   /***
    * Runs on GUI thread. Gives feedback to GUI and stores state to our object
    * @param queryResult - message received from server (AV Receiver)
    */
   public void postQueryResults(String queryResult) {
      Log.d("TJS","Query Result : '"+queryResult+"'...");
//      String[] resultParnts = queryResult.split("/\n");
//      if(queryResult.split(regularExpression))
      if(mParent instanceof CommandHandler ) {
         CommandHandler parent = (CommandHandler)mParent;
         if(queryResult.contains("PWR")) {
            //Power state decode
            String resultStr = queryResult.substring(3, 5);
            int value = Integer.parseInt(resultStr);
            Log.v("TJS","Power query result = '"+value+"'");
            parent.onPowerChange(value == 1);
            setPoweredOn(value==1);
            connectionStateChanged();
         } else if(queryResult.contains("MVL")) {
            //Volume level decode
            String resultStr = queryResult.substring(3, 5);
            float value = (float)Integer.parseInt(resultStr,16);
            Log.v("TJS","Volume query result = '"+value+"'");
            setVolume(value);
            parent.onVolumeChange(value);
         } else if(queryResult.contains("AMT")) {
            //Muted status decode
            String resultStr = queryResult.substring(3, 5);
            int value = Integer.parseInt(resultStr);
            Log.v("TJS","Muted query result = '"+value+"'");
            parent.onMuteChange(value == 1);
            setMuted(value==1);
         } else if(queryResult.contains("SLI")) {
            //Source status decode
            String resultStr = queryResult.substring(0, 5);
//            int value = Integer.parseInt(resultStr);
//            Log.v("TJS","Input query result = '"+value+"'");
            if(commandMapInverse_.containsKey(resultStr)) {
               parent.onInputChange(commandMapInverse_.get(resultStr));
            }
         }
      }
   }

   //FIXME - use AsyncTask rather than Thread? this would allow publishing status back
   //to the caller (but I'm not sure this is really necessary for this application)
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
      return false;//FIXME - return value not truly returned to caller
   }

   //FIXME - use AsyncTask rather than Thread? this would allow publishing status back
   //to the caller (but I'm not sure this is really necessary for this application)
   //FIXME - upon connection, query for current power status & 
   public boolean connectSocketThread() {
      new Thread() {
         public void run() {
            ReceiverClient.super.connectSocket();
         }
      }.start();
      return true;//FIXME - return value not truly returned to caller
   }

   //redirect messages to Log.e() instead of System.err
   public void errorMessage(String message) {
      Log.e("ReceiverClient",message);
   }

   //redirect messages to Log.v() instead of System.out
   public void debugMessage(String message) {
      Log.v("ReceiverClient",message);
   }
   
   protected class QueryServerTask extends AsyncTask<String, Void, String> {

      private final ReceiverClient mParent;
      public QueryServerTask(ReceiverClient parent) {
         mParent = parent;
      }
      @Override
      protected String doInBackground(String... params) {
         //FIXME - pass in 'close' flag, somehow
         Log.v("TJSAsync","Sending query : " + params[0]);
         int command = Integer.parseInt(params[0]);
         return mParent.sendQueryCommand(command,false,false);
      }

      @Override
      protected void onPostExecute(String result) {
         // TODO Auto-generated method stub
         Log.v("TJSAsync","Got result: '" + result + "'...");
         mParent.postQueryResults(result);
         super.onPostExecute(result);
      }
   }
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
   }
}

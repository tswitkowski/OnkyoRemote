package com.switkows.onkyoremote;

import java.util.Vector;

import com.switkows.onkyoremote.communication.IscpCommands;
import com.switkows.onkyoremote.communication.IscpDeviceDiscover;
import com.switkows.onkyoremote.communication.ReceiverClient;
import com.switkows.onkyoremote.communication.ReceiverClient.CommandHandler;
import com.switkows.onkyoremote.communication.ReceiverClient.CommandSendCallbacks;
import com.switkows.onkyoremote.communication.ReceiverInfo;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

/***
 * This fragment is used to hold background information for the main application (and its fragments)
 * Also, it should house any threads/etc which may need to be retained across configuration changes
 * 1. Network discovery logic
 * 2. Active AV Receiver connections/state
 * 
 * I used the following blog as my guide:
 * http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html
 * @author Trevor
 *
 */
//FIXME - add list of Eiscp structures, to track 1+ receiver connections, so we retain their state across orientation changes?
public class ReceiverBackgroundFragment extends Fragment implements CommandHandler, CommandSendCallbacks {
   /**
    * Callback interface through which the fragment will report the
    * task's progress and results back to the Activity.
    */
   public static interface TaskCallbacks {
      public void onDiscoveryComplete();
   }
  
   private TaskCallbacks mCallbacks;
   private CommandHandler mCommandCallbacks;
//   private BackgroundListenerTask snooper; //background status message listener
   private IscpDeviceDiscover columbus; //discovery mechanism and state-keeper
   private Vector<ReceiverClient> mEiscpInterfaces;

   /**
    * Hold a reference to the parent Activity so we can report the
    * task's current progress and results. The Android framework 
    * will pass us a reference to the newly created Activity after 
    * each configuration change.
    */
   @Override
   public void onAttach(final Activity activity) {
     super.onAttach(activity);
     if(mEiscpInterfaces == null)
        mEiscpInterfaces = new Vector<ReceiverClient>();
     if(activity instanceof TaskCallbacks)
        mCallbacks = (TaskCallbacks) activity;
     else
        throw new ClassCastException(activity.toString()+" must implement TaskCallbacks");
     if(activity instanceof CommandHandler)
        mCommandCallbacks = (CommandHandler)activity;
     else
        throw new ClassCastException(activity.toString()+" must implement CommandHandler");
   }
  
   /**
    * This method will only be called once when the retained
    * Fragment is first created.
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
  
     // Retain this fragment across configuration changes.
     setRetainInstance(true);
  
      //launch discovery
      columbus = new IscpDeviceDiscover();
      new DiscoveryTask().execute(columbus);
   }
  
   /**
    * Set the callback to null so we don't accidentally leak the 
    * Activity instance.
    */
   @Override
   public void onDetach() {
     super.onDetach();
     mCallbacks = null;
   }

   
   //methods/etc not related to lifecycle:
   public String printAllReceivers() {
      if(columbus != null)
         return columbus.printAllReceivers();
      return "";
   }
   
   public ReceiverInfo getReceiver(int index) {
      if(columbus != null)
         return columbus.getReceiver(index);
      return null;
   }
   
   public boolean receiversPresent() {
      if(columbus != null)
         return columbus.receiversPresent();
      return false;
   }
   
   public ReceiverClient getReceiverClient(int index) {
      if(!mEiscpInterfaces.isEmpty())
         return (ReceiverClient)mEiscpInterfaces.get(index%mEiscpInterfaces.size());
      return null;
   }
   
   public void onDiscoveryComplete() {
      if(mCallbacks != null)
         mCallbacks.onDiscoveryComplete();
      if(columbus.receiversPresent() && mEiscpInterfaces.isEmpty()) {
         ReceiverInfo receiver = columbus.getReceiver(0);//FIXME - add configuration/dialog box/something
         ReceiverClient eiscpInstance = new ReceiverClient(this,receiver);
//         ReceiverClient eiscpInstance = new ReceiverClient(this,receiver.getIpAddr(), receiver.getTcpPort());
         eiscpInstance.initiateConnection();
         mEiscpInterfaces.add(eiscpInstance);
      }
   }
   
   /***
    * Simple AsyncTask implementation to wrap auto-discovery procedure into a thread
    * 
    * @author Trevor
    *
    */
   private class DiscoveryTask extends AsyncTask<IscpDeviceDiscover,Void,IscpDeviceDiscover> {
      @Override
      protected IscpDeviceDiscover doInBackground(IscpDeviceDiscover... params) {
         params[0].discover(null, null);
         return params[0];
      }
      @Override
      protected void onPostExecute(IscpDeviceDiscover result) {
         onDiscoveryComplete();
      }
   }
   

   @Override
   public void onMessageSent(String message) {
      if(mCommandCallbacks != null)
         mCommandCallbacks.onMessageSent(message);
   }

   @Override
   public void onMessageReceived(String message, String response) {
      if(mCommandCallbacks != null)
         mCommandCallbacks.onMessageReceived(message,ReceiverClient.messageToPrintable(response));
   }

   @Override
   public void onInputChange(int sourceVal) {
      if(mCommandCallbacks != null)
         mCommandCallbacks.onInputChange(sourceVal);
   }

   @Override
   public void onPowerChange(boolean powered_on) {
      if(mCommandCallbacks != null)
         mCommandCallbacks.onPowerChange(powered_on);
   }

   @Override
   public void onMuteChange(boolean muted) {
      if(mCommandCallbacks != null)
         mCommandCallbacks.onMuteChange(muted);
   }

   @Override
   public void onVolumeChange(float volume) {
      if(mCommandCallbacks != null)
         mCommandCallbacks.onVolumeChange(volume);
   }

   @Override
   public void onConnectionChange(boolean isConnected) {
      if(mCommandCallbacks != null)
         mCommandCallbacks.onConnectionChange(isConnected);
   }

   @Override
   public ReceiverInfo getReceiverInfo() {
      return getReceiver(0);
   }

   @Override
   public void sendCommand(int command, boolean sendIfOff) {
      ReceiverClient client = getReceiverClient(0);//FIXME - allow for control
      if(client!=null && (sendIfOff || client.getPoweredOn()))
         client.sendCommand(command);
   }

   @Override
   public void sendQueryCommand(int command) {
      // TODO Auto-generated method stub
      
   }

   @Override
   public boolean toggleConnection() {
      ReceiverClient client = getReceiverClient(0);//FIXME - allow for control
      if(client!=null) {
         if(client.isConnected()) {
            client.closeSocket();
            Log.v("TJS","Dis-connected");
         } else {
            client.initiateConnection();
            Log.v("TJS","Connected");
         }
         mCommandCallbacks.onConnectionChange(client.isConnected());
         return client.isConnected();
      }
      return false;
   }

   @Override
   public void setVolume(float volume) {
      ReceiverClient client = getReceiverClient(0);//FIXME - allow for control
      if(client!=null) {
         //set local state
         client.setVolume(volume);
         //now send command to server
         client.sendCommand(IscpCommands.VOLUME_SET);
      }
   }

   @Override
   public void setVolumeTracked(boolean isTracked) {
      ReceiverClient client = getReceiverClient(0);//FIXME - allow for control
      if(client!=null) {
         client.setVolumeFrozen(isTracked);
      }
   }
}

package com.switkows.onkyoremote;

import com.switkows.onkyoremote.communication.IscpDeviceDiscover;
import com.switkows.onkyoremote.communication.ReceiverInfo;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

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
public class ReceiverBackgroundFragment extends Fragment {
   /**
    * Callback interface through which the fragment will report the
    * task's progress and results back to the Activity.
    */
   public static interface TaskCallbacks {
      public void onDiscoveryComplete();
   }
  
   private TaskCallbacks mCallbacks;
   private IscpDeviceDiscover columbus; //discovery mechanism and state-keeper

   /**
    * Hold a reference to the parent Activity so we can report the
    * task's current progress and results. The Android framework 
    * will pass us a reference to the newly created Activity after 
    * each configuration change.
    */
   @Override
   public void onAttach(final Activity activity) {
     super.onAttach(activity);
     if(activity instanceof TaskCallbacks)
        mCallbacks = (TaskCallbacks) activity;
     else
        throw new ClassCastException(activity.toString()+" must implement TaskCallbacks");
     if(columbus == null) {
     }
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
         if(mCallbacks != null)
            mCallbacks.onDiscoveryComplete();
      }
   }
}

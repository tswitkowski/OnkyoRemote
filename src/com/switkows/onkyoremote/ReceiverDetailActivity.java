package com.switkows.onkyoremote;

import com.switkows.onkyoremote.communication.ReceiverClient;
import com.switkows.onkyoremote.communication.ReceiverInfo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;

/**
 * An activity representing a single Receiver detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ReceiverListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than a {@link ReceiverDetailFragment}.
 */
public class ReceiverDetailActivity extends FragmentActivity implements ReceiverBackgroundFragment.TaskCallbacks, ReceiverClient.CommandHandler, ReceiverClient.CommandSendCallbacks {

   //pointer to background fragment (passes data back and forth between the 'detail' fragment)
   private ReceiverBackgroundFragment mBackgroundFragment;

   //pointer to detail fragment (if we are a 'command' activity)
   private ReceiverDetailFragment mCommandFragment;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_receiver_detail);

      // Show the Up button in the action bar.
      getActionBar().setDisplayHomeAsUpEnabled(true);

      // savedInstanceState is non-null when there is fragment state
      // saved from previous configurations of this activity
      // (e.g. when rotating the screen from portrait to landscape).
      // In this case, the fragment will automatically be re-added
      // to its container so we don't need to manually add it.
      // For more information, see the Fragments API guide at:
      //
      // http://developer.android.com/guide/components/fragments.html
      //
      if(savedInstanceState == null) {
         // Create the detail fragment and add it to the activity
         // using a fragment transaction.
         Bundle arguments = new Bundle();
         String id = getIntent().getStringExtra(ReceiverDetailFragment.ARG_ITEM_ID);
         int idInt = Integer.parseInt(id);
         arguments.putString(ReceiverDetailFragment.ARG_ITEM_ID, id);
         arguments.putString(ReceiverDetailFragment.ARG_IP_ADDR, getIntent().getStringExtra(ReceiverDetailFragment.ARG_IP_ADDR));
         arguments.putInt(ReceiverDetailFragment.ARG_TCP_PORT, getIntent().getIntExtra(ReceiverDetailFragment.ARG_TCP_PORT,0));
         ReceiverDetailFragment fragment = new ReceiverDetailFragment();
         fragment.setArguments(arguments);
         getSupportFragmentManager().beginTransaction().add(R.id.receiver_detail_container, fragment).commit();
         if(idInt==1)
            mCommandFragment = fragment;
      }
      else
         mCommandFragment = (ReceiverDetailFragment)getSupportFragmentManager().findFragmentById(R.id.receiver_detail_container);

      //get pointer to background fragment
      //FIXME - change string (in all places) to a variable
      FragmentManager fm = getSupportFragmentManager();
      mBackgroundFragment = (ReceiverBackgroundFragment)fm.findFragmentByTag("background_frag");
      if(mBackgroundFragment == null) {
         Log.v("TJSDebug","Background task started...");
         mBackgroundFragment = new ReceiverBackgroundFragment();
         
         fm.beginTransaction().add(mBackgroundFragment, "background_frag").commit();
      }
      if(mCommandFragment != null)
         mCommandFragment.setReceiverInfo(getReceiverInfo());//update state of child fragment (mainly for orientation changes)
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch(item.getItemId()) {
         case android.R.id.home:
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, ReceiverListActivity.class));
            return true;
      }
      return super.onOptionsItemSelected(item);
   }

   @Override
   public void onMessageSent(String message) {
   }

   @Override
   public void onMessageReceived(String message, String response) {
   }

   @Override
   public void onInputChange(int sourceVal) {
      if(mCommandFragment!=null)
         mCommandFragment.onInputChange(sourceVal);
   }

   @Override
   public void onPowerChange(boolean powered_on) {
      if(mCommandFragment!=null)
         mCommandFragment.onPowerChange(powered_on);
   }

   @Override
   public void onMuteChange(boolean muted) {
      if(mCommandFragment!=null)
         mCommandFragment.onMuteChange(muted);
   }

   @Override
   public void onVolumeChange(float volume) {
      if(mCommandFragment!=null)
         mCommandFragment.onVolumeChange(volume);
   }

   @Override
   public void onConnectionChange(boolean isConnected) {
      if(mCommandFragment!=null)
         mCommandFragment.onConnectionChange(isConnected);
   }

   @Override
   public ReceiverInfo getReceiverInfo() {
      if(mCommandFragment!=null)
         return mBackgroundFragment.getReceiver(0);//FIXME - find index
      return null;
   }

   @Override
   public void sendCommand(int command, boolean sendIfOff) {
      mBackgroundFragment.sendCommand(command, sendIfOff);
   }

   @Override
   public void sendQueryCommand(int command) {
      mBackgroundFragment.sendQueryCommand(command);
   }

   @Override
   public boolean toggleConnection() {
      return mBackgroundFragment.toggleConnection();
   }

   @Override
   public void setVolume(float volume) {
      mBackgroundFragment.setVolume(volume);
   }

   @Override
   public void onDiscoveryComplete() {
      if(mCommandFragment != null)
         mCommandFragment.setReceiverInfo(getReceiverInfo()); 
   }

   @Override
   public void setVolumeTracked(boolean isTracked) {
      if(mBackgroundFragment != null)
         mBackgroundFragment.setVolumeTracked(isTracked); 
   }
}

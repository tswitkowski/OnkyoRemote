package com.switkows.onkyoremote;

import com.switkows.onkyoremote.communication.ReceiverInfo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.Toast;

/**
 * An activity representing a list of Receivers. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ReceiverDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a {@link ReceiverListFragment} and the item details
 * (if present) is a {@link ReceiverDetailFragment}.
 * <p>
 * This activity also implements the required {@link ReceiverListFragment.Callbacks} interface to listen for item
 * selections.
 */
public class ReceiverListActivity extends FragmentActivity implements ReceiverListFragment.Callbacks, ReceiverBackgroundFragment.TaskCallbacks {

   /**
    * Whether or not the activity is in two-pane mode, i.e. running on a tablet
    * device.
    */
   private boolean mTwoPane;
   
   private ReceiverBackgroundFragment mBackgroundFragment;
   

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_receiver_list);

      FragmentManager fm = getSupportFragmentManager();
      mBackgroundFragment = (ReceiverBackgroundFragment)fm.findFragmentByTag("background_frag");

      //if we don't already have the fragment, create it!
      if(mBackgroundFragment == null) {
         Log.v("TJSDebug","Background task started...");
         mBackgroundFragment = new ReceiverBackgroundFragment();
         
         fm.beginTransaction().add(mBackgroundFragment, "background_frag").commit();
      }


      if(findViewById(R.id.receiver_detail_container) != null) {
         // The detail container view will be present only in the
         // large-screen layouts (res/values-large and
         // res/values-sw600dp). If this view is present, then the
         // activity should be in two-pane mode.
         mTwoPane = true;

         Bundle arguments = new Bundle();
         arguments.putString(ReceiverDetailFragment.ARG_ITEM_ID, "1");
         mCommandFragment = new ReceiverDetailFragment();
         mCommandFragment.setArguments(arguments);

         // In two-pane mode, list items should be given the
         // 'activated' state when touched.
         ((ReceiverListFragment)fm.findFragmentById(R.id.receiver_list)).setActivateOnItemClick(true);
      }

      // TODO: If exposing deep links into your app, handle intents here.
   }

   private Fragment mLogFragment;
   private Fragment mCommandFragment;
   /**
    * Callback method from {@link ReceiverListFragment.Callbacks} indicating that the item with the given ID was
    * selected.
    */
   @Override
   public void onItemSelected(String id) {
      ReceiverInfo receiver = null;
      if(mBackgroundFragment != null && mBackgroundFragment.receiversPresent()) {
         receiver = mBackgroundFragment.getReceiver(0);//FIXME - add choice dialog
      }
      if(mTwoPane) {
         // In two-pane mode, show the detail view in this activity by
         // adding or replacing the detail fragment using a
         // fragment transaction.

         //re-use fragment if one has already been created. else, create a new one with the appropriate content
         Fragment fragment = null;
         int idInt = Integer.parseInt(id);
         if(idInt==0)
            fragment = mCommandFragment;
         else if(idInt==1)
            fragment = mLogFragment;
         if(fragment==null) {
            Bundle arguments = new Bundle();
            arguments.putString(ReceiverDetailFragment.ARG_ITEM_ID, id);
            if(receiver != null) {
               arguments.putString(ReceiverDetailFragment.ARG_IP_ADDR, receiver.getIpAddr());
               arguments.putInt(ReceiverDetailFragment.ARG_TCP_PORT, receiver.getTcpPort());
            }
            fragment = new ReceiverDetailFragment();
            fragment.setArguments(arguments);
            fragment.setRetainInstance(true);
            if(idInt==0)
               mCommandFragment = fragment;
            else if(idInt==1)
               mLogFragment = fragment;
         } else {
            Log.v("TJS","Restored saved fragment");
            //anything to do here?
         }
         getSupportFragmentManager().beginTransaction().replace(R.id.receiver_detail_container, fragment).commit();

      } else {
         // In single-pane mode, simply start the detail activity
         // for the selected item ID.
         Intent detailIntent = new Intent(this, ReceiverDetailActivity.class);
         detailIntent.putExtra(ReceiverDetailFragment.ARG_ITEM_ID, id);
         if(receiver != null) {
            detailIntent.putExtra(ReceiverDetailFragment.ARG_IP_ADDR, receiver.getIpAddr());
            detailIntent.putExtra(ReceiverDetailFragment.ARG_TCP_PORT, receiver.getTcpPort());
         }
         startActivity(detailIntent);
      }
   }

   public void onDiscoveryComplete() {
      if(mBackgroundFragment != null)
         Toast.makeText(this, mBackgroundFragment.printAllReceivers(), Toast.LENGTH_LONG).show();
   }
}

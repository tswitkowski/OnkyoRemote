package com.switkows.onkyoremote;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.switkows.onkyoremote.communication.Eiscp;
import com.switkows.onkyoremote.communication.ReceiverClient;
import com.switkows.onkyoremote.communication.ReceiverClient.CommandHandler;
import com.switkows.onkyoremote.dummy.DummyContent;

/**
 * A fragment representing a single Receiver detail screen.
 * This fragment is either contained in a {@link ReceiverListActivity} in two-pane mode (on tablets) or a
 * {@link ReceiverDetailActivity} on handsets.
 */
@SuppressLint("ValidFragment")
public class ReceiverDetailFragment extends Fragment implements CommandHandler {
   /**
    * The fragment argument representing the item ID that this fragment
    * represents.
    */
   public static final String     ARG_ITEM_ID = "item_id";
   //FIXME - split to different fragments (one for commands, one for console output
   //        but since this is a demo (i.e. non-final implementation), this is okay, maybe forever
   private boolean isCommandFragment;

   private ReceiverClient eISCPInterface;
   /**
    * The dummy content this fragment is presenting.
    */
   private DummyContent.DummyItem mItem;
   
   private FragmentActivity mParent;//pointer to parent (for callbacks, etc)

   /**
    * Mandatory empty constructor for the fragment manager to instantiate the
    * fragment (e.g. upon screen orientation changes).
    */
   
   public ReceiverDetailFragment() {}
   public ReceiverDetailFragment(FragmentActivity parent) {
      mParent = parent;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      if(getArguments().containsKey(ARG_ITEM_ID)) {
         // Load the dummy content specified by the fragment
         // arguments. In a real-world scenario, use a Loader
         // to load content from a content provider.
         String id = getArguments().getString(ARG_ITEM_ID);
         mItem = DummyContent.ITEM_MAP.get(id);
         if(id.equals("1"))
            isCommandFragment = true;
         else
            isCommandFragment = false;
      }
      if(isCommandFragment)
         setHasOptionsMenu(true);
   }

   private Menu mMenu;//save pointer, so we can overwrite icons/text later
   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.main_menu, menu);
      mMenu = menu;
      super.onCreateOptionsMenu(menu, inflater);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch(item.getItemId()) {
         case R.id.connectActionButton:
            toggleConnection();
            return true;
      }
      return super.onOptionsItemSelected(item);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View rootView;
      if(isCommandFragment) {
         rootView = inflater.inflate(R.layout.fragment_commands, container, false);
         //start connection
         eISCPInterface = new ReceiverClient(this,ReceiverClient.DEFAULT_IP_ADDR,ReceiverClient.DEFAULT_TCP_PORT);//FIXME - make IP address & port number configurable
         eISCPInterface.initiateConnection();
         //connect eventListeners to buttons
         View view;
         //FIXME - this fires once on initial load, which causes the receiver to turn on if it is currently off
         view = rootView.findViewById(R.id.inputSelector);
         view.setEnabled(false); //disable by default. once the connection to the server has been established and the value queried, the field will be enabled
         ((Spinner)view).setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
               //Note : only has an effect if the power is already on (this may not be desirable...
               //       I will later change to set the value of the Array to the current input
               //       value, on startup, so we don't turn on device upon app powerup)
               if(eISCPInterface.getPoweredOn())
                  eISCPInterface.sendCommand(Eiscp.SOURCE_DVR + arg2);//FIXME - apply proper addition. This seems to work, but it's not very robust
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
         });

         //dynamically add buttons (saves copy/paste in layout file as well as attaching event handlers)
         LinearLayout layout = (LinearLayout)rootView.findViewById(R.id.buttons);
         String[] labels = {"Power On","Power Off","Volume Up", "Volume Down", "Mute/Unmute", "Up", "Down",
                            "Left", "Right", "Enter", "Exit", "Menu"};
         final int[] commands = {Eiscp.POWER_ON, Eiscp.POWER_OFF, Eiscp.VOLUME_UP, Eiscp.VOLUME_DOWN, Eiscp.MUTE_TOGGLE, Eiscp.DIRECTION_UP, Eiscp.DIRECTION_DOWN,
                                 Eiscp.DIRECTION_LEFT, Eiscp.DIRECTION_RIGHT, Eiscp.BUTTON_ENTER, Eiscp.BUTTON_EXIT, Eiscp.BUTTON_MENU};
         for(int i=0 ; i < labels.length ; i++) {
            Button button = new Button(container.getContext());
            button.setText(labels[i]);
            button.setId(commands[i]);
            button.setOnClickListener(new OnClickListener() {
               @Override
               public void onClick(View v) {
                  eISCPInterface.sendCommand(v.getId());
               }
            });
            layout.addView(button);
         }
      }
      else {
         rootView = inflater.inflate(R.layout.fragment_receiver_detail, container, false);
   
         // Show the dummy content as text in a TextView.
         if(mItem != null) {
            ((TextView)rootView.findViewById(R.id.receiver_detail)).setText(mItem.content);
         }
      }

      return rootView;
   }

   @Override
   public void onMessageSent(String message) {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void onMessageReceived(String message, String response) {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void onInputChange(int sourceVal) {
      // TODO Auto-generated method stub
      Spinner view = (Spinner)this.getView().findViewById(R.id.inputSelector);
//      view.setEnabled(true);//FIXME - only set to TRUE if it was previously false?
      Log.v("TJS","Received "+sourceVal);
      view.setSelection(sourceVal-Eiscp.SOURCE_DVR);//FIXME - apply proper addition. This seems to work, but it's not very robust
   }

   @Override
   public void onPowerChange(boolean powered_on) {
      // TODO Auto-generated method stub
   }

   public void toggleConnection() {
      if(eISCPInterface != null) {
         if(eISCPInterface.isConnected()) {
            eISCPInterface.closeSocket();
            Log.v("TJS","Dis-connected");
         } else {
            eISCPInterface.initiateConnection();
            Log.v("TJS","Connected");
         }
      }
   }

   @Override
   public void onConnectionChange(boolean isConnected) {
      if(mParent != null && mParent instanceof CommandHandler)
         ((CommandHandler)mParent).onConnectionChange(isConnected);
      //overwrite menu icon
      //FIXME - for some reason, on a one-pane configuration, this is triggered before mMenu is defined...
      if(mMenu != null) {
         MenuItem item = mMenu.findItem(R.id.connectActionButton);
         if(isConnected) {
            item.setIcon(android.R.drawable.presence_online);
            item.setTitle("Disconnect from Server");
         } else {
            item.setIcon(android.R.drawable.stat_notify_sync_noanim);
            item.setTitle("Connect to Server");
         }
      }
      View view = this.getView().findViewById(R.id.inputSelector);
      view.setEnabled(isConnected);
   }
   @Override
   public void onMuteChange(boolean muted) {
      // TODO Auto-generated method stub
      
   }
}

package com.switkows.onkyoremote;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.switkows.onkyoremote.communication.Eiscp;
import com.switkows.onkyoremote.communication.ReceiverClient;
import com.switkows.onkyoremote.dummy.DummyContent;

/**
 * A fragment representing a single Receiver detail screen.
 * This fragment is either contained in a {@link ReceiverListActivity} in two-pane mode (on tablets) or a
 * {@link ReceiverDetailActivity} on handsets.
 */
public class ReceiverDetailFragment extends Fragment {
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

   /**
    * Mandatory empty constructor for the fragment manager to instantiate the
    * fragment (e.g. upon screen orientation changes).
    */
   public ReceiverDetailFragment() {
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
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View rootView;
      if(isCommandFragment) {
         rootView = inflater.inflate(R.layout.fragment_commands, container, false);
         //start connection
         eISCPInterface = new ReceiverClient(ReceiverClient.DEFAULT_IP_ADDR,ReceiverClient.DEFAULT_TCP_PORT);//FIXME - make IP address & port number configurable
         eISCPInterface.connectSocketThread();
         //connect eventListeners to buttons
         Button button;
         button = (Button)rootView.findViewById(R.id.powerOffButton);
         button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               eISCPInterface.sendCommand(Eiscp.POWER_OFF);
               Log.v("TJS","Sent power-off");
            }
         });
         button = (Button)rootView.findViewById(R.id.powerOnButton);
         button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               eISCPInterface.sendCommand(Eiscp.POWER_ON);
               Log.v("TJS","Sent power-on");
            }
         });
         button = (Button)rootView.findViewById(R.id.inputAuxButton);
         button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               eISCPInterface.sendCommand(Eiscp.SOURCE_AUX);
               Log.v("TJS","Set source to Aux");
            }
         });
         button = (Button)rootView.findViewById(R.id.inputBdButton);
         button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               eISCPInterface.sendCommand(Eiscp.SOURCE_BLURAY);
               Log.v("TJS","Set source to BluRay");
            }
         });
         button = (Button)rootView.findViewById(R.id.connectButton);
         button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               if(eISCPInterface.isConnected()) {
                  eISCPInterface.closeSocket();
                  Log.v("TJS","Dis-connected");
               } else {
                  eISCPInterface.connectSocketThread();
                  Log.v("TJS","Connected");
               }
            }
         });
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
   
//   private CommandHandler messageHandler = new CommandHandler();
   
   public interface CommandHandler {
      public void onMessageSent(String message);
      public void onMessageReceived(String message, String response);
   }
}

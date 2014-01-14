/*
 *  $URL: svn://svn/open/trunk/projects/WebARTS/ca/bc/webarts/tools/eiscp/Eiscp.java $
 *  $Author: tgutwin $
 *  $Revision: 598 $
 *  $Date: 2013-01-29 20:55:30 -0800 (Tue, 29 Jan 2013) $
 */
/*
 *
 *  Written by Tom Gutwin - WebARTS Design.
 *  Copyright (C) 2012 WebARTS Design, North Vancouver Canada
 *  http://www.webarts.bc.ca
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without_ even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */


package com.switkows.onkyoremote.communication;
//TJS - I found this code from: https://sites.google.com/a/webarts.ca/toms-blog/Blog/new-blog-items/javaeiscp-integraserialcontrolprotocol
//      At the time, i could not find the SVN version (or at least, i could not find the web-viewable version)
//package ca.bc.webarts.tools.eiscp;


import java.io.*;
import java.net.*;
import java.util.Vector;

import android.util.Log;

/**
 *  A class that wraps the comunication to Onkyo/Integra devices using the
 *  ethernet Integra Serial Control Protocal (eISCP). This class uses class
 *  constants and commandMaps to help handling of the many iscp Commands.
 *  <br />
 *  The Message packet looks like:<br />
 *  <img src="http://tom.webarts.ca/_/rsrc/1320209141605/Blog/new-blog-items/javaeiscp-integraserialcontrolprotocol/eISCP-Packet.png" border="1"/>
 *  <br /> See also <a href="http://tom.webarts.ca/Blog/new-blog-items/javaeiscp-integraserialcontrolprotocol" > tom.webarts.ca</a> writeup.
 *
 * @author     Tom Gutwin P.Eng
 */
public class Eiscp
{
  /**  A holder for this clients System File Separator.  */
  public final static String SYSTEM_FILE_SEPERATOR = File.separator;

  /**  A holder for this clients System line termination separator.  */
  public final static String SYSTEM_LINE_SEPERATOR =
                                           System.getProperty("line.separator");

  /**  The VM classpath (used in some methods)..  */
  public static String CLASSPATH = System.getProperty("class.path");

  /**  The users home directory.  */
  public static String USERHOME = System.getProperty("user.home");

  /**  The users pwd directory.  */
  public static String USERDIR = System.getProperty("user.dir");

  /** default receiver IP Address. **/
  private static final String DEFAULT_EISCP_IP = "16.1.1.200";
  /** Instantiated class IP for the receiver to communicate with. **/
  private String receiverIP_ = DEFAULT_EISCP_IP;

  /** default eISCP port. **/
  protected static final int DEFAULT_EISCP_PORT = 60128;
  /** Instantiated class Port for the receiver to communicate with. **/
  private int receiverPort_ = DEFAULT_EISCP_PORT;

  /** the socket for communication - the protocol spec says to use one socket connection AND HOLD ONTO IT for re-use. **/
  private Socket eiscpSocket_ = null;
  public Socket getSocket() { return eiscpSocket_;}
  /** the timeout in ms for socket reads. **/
  private static final int socketTimeOut_ = 500;
  private ObjectOutputStream out_ = null;
  private DataInputStream in_ = null;
  public DataInputStream getInputStream() {return in_;}


  private boolean connected_ = false;

  @SuppressWarnings("unused")
  private static IscpCommands iscp_ = IscpCommands.getInstance();

  /** Var to hold the volume level to or from a message. **/
  private float volume_ = 32;
  
  /** Constructor that takes your receivers ip and port,  gets all the class command 
   * constants set-up along with their command lookup maps (commandNameMap_ and commandMap_) . 
   **/
  public Eiscp(String ip, int eiscpPort)
  {
    //initCommandMap();
    if (ip==null || ip.equals("")) 
      receiverIP_=DEFAULT_EISCP_IP;
    else 
      receiverIP_=ip;
    if (eiscpPort<1 ) 
      receiverPort_=DEFAULT_EISCP_PORT;
    else 
      receiverPort_=eiscpPort;
  }

  //simple method which returns whether the socket is currently connected or not
  public boolean isConnected() {
     return connected_;
  }

  //overwriteable method which prints a message to STDERR by default
  public static void errorMessage(String message) {
//     System.err.println(message);
     Log.e("EISCP", message);
  }

  //overwriteable method which prints a message to STDOUT by default
  public static void debugMessage(String message) {
//     System.out.println(message);
     Log.d("EISCP", message);
  }
  /** Makes Chocolate glazed doughnuts. **/
  public void setReceiverIP( String ip) { receiverIP_ = ip;}
  /** Makes Sprinkle doughnuts. **/
  public String getReceiverIP() {return receiverIP_;}
  /** Makes mini doughnuts. **/
  public void setReceiverPort( int port) { receiverPort_ = port;}
  /** Makes glazed doughnuts. **/
  public int getReceiverPort() {return receiverPort_;}

  /**
   * Connects to the receiver by opening a socket connection through the DEFaULT IP and DEFAULT eISCP port.
   **/
   public boolean connectSocket() { return connectSocket(null, -1);}


  /**
   * Connects to the receiver by opening a socket connection through the eISCP port.
   **/
  public boolean connectSocket(String ip, int eiscpPort)
  {
    if (ip==null || ip.equals("")) ip=receiverIP_;
    if (eiscpPort<1 ) eiscpPort=receiverPort_;
    
    if (eiscpSocket_==null || !connected_ || !eiscpSocket_.isConnected())
    try
    {
      //1. creating a socket to connect to the server
      eiscpSocket_ = new Socket(ip, eiscpPort);
      debugMessage("Connected to "+ip+" on port "+eiscpPort);
      //2. get Input and Output streams
      out_ = new ObjectOutputStream(eiscpSocket_.getOutputStream());
      in_ = new DataInputStream(eiscpSocket_.getInputStream());

      //debugMessage("out_Init");
      out_.flush();
      // debugMessage("inInit");
      connected_ = true;
    }
    catch(UnknownHostException unknownHost)
    {
      errorMessage("You are trying to connect to an unknown host!");
    }
    catch(IOException ioException)
    {
      errorMessage("Can't Connect: "+ioException.getMessage());
    }
    return connected_;
  }

  /**
   * Closes the socket connection.
   * @return true if the closed succesfully
   **/
  public boolean closeSocket()
  {
    //4: Closing connection
    try
    {
      boolean acted = false;
      if (in_!=null) {in_.close();in_=null;acted = true;}
      if (out_!=null) {out_.close();out_=null;acted = true;}
      if (eiscpSocket_!=null) {eiscpSocket_.close();eiscpSocket_=null;acted = true;}
      if (acted) debugMessage("closed connections");
      connected_ = false;
    }
    catch(IOException ioException)
    {
      ioException.printStackTrace();
    }
    return connected_;
  }


  /** Converts an ascii decimal String to a hex  String.
   * @param str holding the string to convert to HEX
   * @return a string holding the HEX representation of the passed in decimal str.
   **/
  public static String convertStringToHex(String str)
  {
     return convertStringToHex( str, false);
  }


  /** Converts an ascii decimal String to a hex  String.
   * @param str holding the string to convert to HEX
   * @param dumpOut flag to turn some debug output on/off
   * @return a string holding the HEX representation of the passed in str.
   **/
  public static String convertStringToHex(String str,  boolean dumpOut)
  {
    char[] chars = str.toCharArray();
    String out_put = "";

    if (dumpOut) debugMessage("    Ascii: "+str);
    if (dumpOut) System.out.print("    Hex: ");
    StringBuffer hex = new StringBuffer();
    for(int i = 0; i < chars.length; i++)
    {
      out_put = Integer.toHexString((int)chars[i]);
      if (out_put.length()==1) hex.append("0");
      hex.append(out_put);
      if (dumpOut) System.out.print("0x"+(out_put.length()==1?"0":"")+ out_put+" ");
    }
    if (dumpOut) debugMessage("");

    return hex.toString();
  }


  /** Converts an HEX number String to its decimal equivalent.
   * @param String holding the Hex Number string to convert to decimal
   * @param boolean flag to turn some debug output on/off
   * @return an int holding the decimal equivalent of the passed in HEX numberStr.
   **/
  public static int convertHexNumberStringToDecimal(char[] chars,  boolean dumpOut) {
     String out_put = "";

    //if (dumpOut) debugMessage("      AsciiHex: 0x"+chars);
    if (dumpOut) System.out.print(  "       Decimal: ");

     StringBuffer hex = new StringBuffer();
     String hexInt = new String();
     for(int i = 0; i < chars.length; i++)
     {
       out_put = Integer.toHexString((int)chars[i]);
       if (out_put.length()==1) hex.append("0");
       hex.append(out_put);
       if (dumpOut) System.out.print((out_put.length()==1?"0":"")+ out_put);
     }
     hexInt = ""+(Integer.parseInt( hex.toString(), 16));
     if (dumpOut) debugMessage("");
     if (dumpOut) debugMessage( "      Decimal: "+hexInt.toString());

     return Integer.parseInt(hexInt.toString());
  }
  public static int convertHexNumberStringToDecimal(String str,  boolean dumpOut)
  {
    char[] chars = str.toCharArray();
    return convertHexNumberStringToDecimal(chars, dumpOut);
  }


  /**
    * Wraps a command in a eiscp data message (data characters).
    *
    * @param command must be one of the Command Class Constants from the eiscp.Eiscp.Command class.
    * @return StringBuffer holing the full iscp message packet
    **/
  public StringBuilder getEiscpMessage(int command)
  {
    String cmdStr = "";
    if (command==IscpCommands.VOLUME_SET)
      cmdStr = getVolumeCmdStr();
    else
      cmdStr = IscpCommands.getCommandStr(command);
    char unitType = '1';
    if(command == IscpCommands.SERVER_QUERY)
       unitType = 'x';
    StringBuilder packet = getEiscpPacket(cmdStr, unitType);
    debugMessage("  eISCP msg size: "+packet.length() +"(0x"+Integer.toHexString(packet.length()) +") chars");
    return packet;
  }

  /**
   * Wraps a command (ISCP message) into an eISCP packet
   * @param command - ISCP message (a piece of it)
   * @param unitType - unit type. normally '1', but for some messages (Server Query, for example) it varies
   * @return string containing TCP/UDP packet payload
   */
  public static StringBuilder getEiscpPacket(String command, char unitType)
  {
    StringBuilder sb = new StringBuilder();
    int eiscpDataSize = command.length() + 2 ; // this is the eISCP data size
    int eiscpMsgSize = eiscpDataSize + 1 + 16 ; // this is the eISCP data size

    /* This is where I construct the entire message
        character by character. Each char is represented by a 2 disgit hex value */
    sb.append("ISCP");
    // the following are all in HEX representing one char

    // 4 char Big Endian Header
    char padding = (char)Integer.parseInt("00", 16); 
    sb.append(padding);
    sb.append(padding);
    sb.append(padding);
    sb.append((char)Integer.parseInt("10", 16));

    // 4 char  Big Endian data size
    sb.append(padding);
    sb.append(padding);
    sb.append(padding);
    // the official ISCP docs say this is supposed to be just the data size  (eiscpDataSize)
    // ** BUT **
    // It only works if you send the size of the entire Message size (eiscpMsgSize)
    sb.append((char)Integer.parseInt(Integer.toHexString(eiscpMsgSize), 16));

    // eiscp_version = "01";
    sb.append((char)Integer.parseInt("01", 16));

    // 3 chars reserved = "00"+"00"+"00";
    sb.append(padding);
    sb.append(padding);
    sb.append(padding);

    //  eISCP data
    // Start Character
    sb.append("!");

    // eISCP data - unittype char '1' is receiver
    sb.append(unitType);

    // eISCP data - 3 char command and param    ie PWR01
    sb.append(command);

    // msg end - EOF
    sb.append((char)Integer.parseInt("0D", 16));

    debugMessage("  eISCP data size: "+eiscpDataSize +"(0x"+Integer.toHexString(eiscpDataSize) +") chars");

    return sb;
  }


  /**
    * Sends to command to the receiver and does not wait for a reply.
    *
    * @param command must be one of the Command Class Constants from the eiscp.Eiscp.Command class.
    **/
  public void sendCommand(int command)
  {
    sendCommand(command, false);
  }


  /**
    * Sends to command to the receiver and does not wait for a reply.
    *
    * @param command must be one of the Command Class Constants from the eiscp.Eiscp.Command class.
    * @param closeSocket flag to close the connection when done or leave it open.
    **/
  public void sendCommand(int command, boolean closeSocket)
  {
    StringBuilder sb = getEiscpMessage(command);

    if(connectSocket())
    {
      try
      {
        debugMessage("  sending "+sb.length() +" chars: ");
        convertStringToHex(sb.toString(), true);
        //out_.writeObject(sb.toString());
        //out_.writeChars(sb.toString());
        out_.writeBytes(sb.toString());  // <--- This is the one that works
        //out_.writeBytes(convertStringToHex(sb.toString(), false));
        //out_.writeChars(convertStringToHex(sb.toString(), false));
        out_.flush();
        debugMessage("sent!" );
      }
      catch(IOException ioException)
      {
        ioException.printStackTrace();
      }
    }
    if (closeSocket) closeSocket();
  }


  /**
    * Sends to command to the receiver and then waits for the response(s). The responses often have nothing to do with the command sent
    * so this method can filter them to return only the responses related to the command sent.
    *
    * @param command must be one of the Command Class Constants from the eiscp.Eiscp.Command class.
    * @param closeSocket flag to close the connection when done or leave it open.
    * @param returnAll flags if all response packetMessages are returned, if no then ONLY the ones related to the command requested
    * @return the response to the command
    **/
  public String sendQueryCommand(int command, boolean closeSocket, boolean returnAll)
  {
    String retVal = "";

    /* Send The Command and then... */
    sendCommand(command,false);
    //sleep(50); // docs say so
     
    /* now listen for the response. */
    Vector <String> rv = readQueryResponses();
    String currResponse = "";
    for (int i=0; i < rv.size(); i++)
    {
      currResponse = (String) rv.elementAt(i);
      /* Send ALL responses OR just the one related to the commad sent??? */
      if (returnAll || currResponse.startsWith(IscpCommands.getCommandStr(command).substring(0,3))) {
        retVal+= currResponse+"\n";
        debugMessage("Accepting  message: '"+currResponse+"'");
      }
      else
         debugMessage("Filtering message: '"+currResponse+"'");
    }
    
    //FIXME - handle case where retVal is null string (i.e. readQueryResponses() return nothing, or returns something but not something that corresponds with query)
    if (closeSocket) closeSocket();
    
    return retVal ;
  }


  /**
   * This method reads ALL responses (possibly more than one) after a query command.
   * @return an array of the data portion of the response messages only - There might be more than one response message received.
   **/
  public Vector <String> readQueryResponses()
  {
    boolean debugging = false;
    Vector <String> retVal = new Vector <String> ();
    byte [] responseBytes = new byte[32] ;
//    String currResponse = "";
    int numBytesReceived = 0;
    int totBytesReceived = 0;
//    int i=0;
    int packetCounter=0;

    if(connected_)
    {
      try
      {
        if (debugging) debugMessage("\nReading Response Packet");
        eiscpSocket_.setSoTimeout(socketTimeOut_); // this must be set or the following read will BLOCK / hang the method when the messages are done
        
        while((numBytesReceived = in_.read(responseBytes))>0)
        {
          totBytesReceived = 0;
          StringBuilder msgBuffer = new StringBuilder("");
          if (debugging) debugMessage(" Packet"+"["+packetCounter+"]:");
          
          /* Read ALL the incoming Bytes and buffer them */ 
          // *******************************************
          if (debugging) debugMessage("numBytesReceived = "+numBytesReceived);
          while(numBytesReceived>0 )
          {
            totBytesReceived+=numBytesReceived;
            msgBuffer.append(new String(responseBytes));
            responseBytes = new byte[32];
            numBytesReceived = 0;
            if (in_.available()>0)
              numBytesReceived = in_.read(responseBytes);
            if (debugging) System.out.print(" "+numBytesReceived);
          }
//          if(debugging)
//             debugMessage("numBytesReceived = "+numBytesReceived+", totBytesReceived = "+totBytesReceived+", message.length = "+msgBuffer.toString().length());

          //push rest of parsing to sub-method (note: string MUST be totBytesReceived long)
          retVal.addAll(parsePacketBytes(msgBuffer.toString().substring(0, totBytesReceived), false));
        }

      }
      catch( java.net.SocketTimeoutException  noMoreDataException)
      {
        if (debugging) debugMessage("Response Done: " );
      }
      catch(EOFException  eofException)
      {
        debugMessage("received: \""+retVal+"\"" );
      }
      catch(IOException ioException)
      {
        ioException.printStackTrace();
      }
    }
    else
      debugMessage("!!Not Connected to Receive ");    
    return retVal;
  }

  public static Vector<String> parsePacketBytes(String packetData, boolean debugging) {
     Vector <String> retVal = new Vector <String> ();
     int packetCounter=0;
     int headerSizeDecimal;
     int dataSizeDecimal = 0;
     char endChar1 ='!';// NR-5008 response sends 3 chars to terminate the packet - 0x1a 0x0d 0x0a
     char endChar2 ='!';
     char endChar3 ='!';
     
     char [] responseChars = packetData.toCharArray(); // use the charArray to step through
     int totBytesReceived = responseChars.length;
     int responseByteCnt = 0;
//     char versionChar = '1';
//     char dataStartChar = '!';
//     char dataUnitChar = '1';
     
     // loop through all the chars and split out the dataMessages
     while (responseByteCnt< totBytesReceived)
     {
       /* read Header */
       // 1st 4 chars are the leadIn
        //FIXME - parse 4 bytes. they should always equal 'ISCP'. assert error if they do not match
       responseByteCnt+=4;
       
       // read headerSize
       if(debugging) {
          responseByteCnt += 4;//in lieu of decoding bytes
          char [] headerSizeBytes = {responseChars[responseByteCnt++],
                                     responseChars[responseByteCnt++],
                                     responseChars[responseByteCnt++],
                                     responseChars[responseByteCnt++]} ;
           headerSizeDecimal = convertHexNumberStringToDecimal(new String(headerSizeBytes),debugging);
           if (debugging) debugMessage(" -HeaderSize-"+headerSizeDecimal);
       } else {
          //in lieu of decoding size bytes, 
          responseByteCnt += 4;
       }
       // 4 char Big Endian data size
       char [] dataSizeBytes = { responseChars[responseByteCnt++],
                                 responseChars[responseByteCnt++],
                                 responseChars[responseByteCnt++],
                                 responseChars[responseByteCnt++]} ;
       dataSizeDecimal = convertHexNumberStringToDecimal(new String(dataSizeBytes),debugging);
       if (debugging) debugMessage(" -DataSize-"+dataSizeDecimal);
                                 
       // version
//       versionChar = responseChars[responseByteCnt++];
       responseByteCnt++;
       
       // 3 reserved bytes
       responseByteCnt+=3;
       int dataByteCnt = 0;
       
       // Now the data message
//       dataStartChar = responseChars[responseByteCnt++]; // parse and throw away (like parsley)
//       dataUnitChar = responseChars[responseByteCnt++]; // dito
       responseByteCnt += 2;
       char [] dataMessage;
       if(dataSizeDecimal > 200) {
          dataMessage = new char[1];
          errorMessage("Framing error: data_size calculated to be: "+dataSizeDecimal+". Aborting packet parse...");
          errorMessage("Remaining message: '"+packetData.substring(responseByteCnt-14)+"'");
          debugMessage("Start of message within packet: " + (responseByteCnt-14));
          debugMessage("Full Packet: '"+packetData+"'");
          debugMessage("Full Packet(hex):"+convertStringToHex(packetData)+"");
          for(String message : retVal)
             debugMessage("   Successfully parsed : " + message);
       } else
          dataMessage = new char [dataSizeDecimal];
       
       /* Get the dataMessage from this response */
       // NR-5008 response sends 3 chars to terminate the packet - so DON't include them in the message
       while( dataByteCnt < (dataSizeDecimal-5) && responseByteCnt< (totBytesReceived-3))
       {
         dataMessage[dataByteCnt++] = responseChars[responseByteCnt++];
       }
       if (debugging) debugMessage(" -DataMessage-");
       if (debugging) debugMessage("    "+(new String(dataMessage))+ "\n");
       retVal.addElement(new String(dataMessage));
       
       // Read the end packet char(s) "[EOF]"
       // [EOF]       End of File    ASCII Code 0x1A
       // NOTE: the end of packet char (0x1A) for a response message is DIFFERENT that the sent message
       // NOTE: ITs also different than what is in the Onkyo eISCP docs
       // NR-5008 sends 3 chars to terminate the packet - 0x1a 0x0d 0x0a
       if(responseByteCnt + 3 < responseChars.length){ 
          endChar1 = responseChars[responseByteCnt++];
          endChar2 = responseChars[responseByteCnt++];
          endChar3 = responseChars[responseByteCnt++];
          if (endChar1 == (char)Integer.parseInt("1A", 16) &&
              endChar2 == (char)Integer.parseInt("0D", 16) &&
              endChar3 == (char)Integer.parseInt("0A", 16) 
             ) {
             if (debugging)
                debugMessage(" EndOfPacket["+packetCounter+"]\n");
          }
          else
             break;//if we don't find the correct end of packet frame, abort
          //trim padding bytes (my receiver (TX-NR809) seems to add extra words to increase ISCP message to 16-byte boundaries?)
          while(responseByteCnt < responseChars.length && responseChars[responseByteCnt]==(char)Integer.parseInt("0",16)) {
             responseByteCnt++;//throw away padding zeros
          }
       } else {
          if (debugging)
             debugMessage(" EndOfPacket["+packetCounter+"]\n");
          break;
       }
       packetCounter++;
     }
     if(retVal.size() > 1)
        Log.v("TJS","Returned > 1 ISCP message in this iteration! " + retVal.size());
     return retVal;
  }

  /** This method creates the set volume command based on the passed value. **/
  public String getVolumeCmdStr(){return IscpCommands.getVolumeCmdStr((int)volume_);}


  /** This method takes the  3 character response from the USB Play status query (NETUSB_PLAY_STATUS_QUERY) and creates a human readable String. 
   * NET/USB Play Status QUERY returns one of 3 letters - PRS.<oL>
   * <LI>p -> Play Status<ul><li>"S": STOP</li><li>"P": Play</li><li>"p": Pause</li><li>"F": FF</li><li>"R": FastREW</li></ul></LI>
   * <LI>r -> Repeat Status<ul><li>"-": Off</li><li>"R": All</li><li>"F": Folder</li><li>"1": Repeat 1</li></ul></LI>
   * <LI>s -> Shuffle Status<ul><li>"-": Off</li><li>"S": All</li><li>"A": Album</li><li>"F": Folder</li></ul></LI></oL>
   * @param queryResponses is the entire response packet with the oneOf3 char reply embedded in it.
  **/
  public   String decipherUsbPlayStatusResponse(String queryResponses)
  {
    String [] responses = queryResponses.split("[\n]");
    String retVal = "NETUSB_PLAY_STATUS_QUERY response: "+ queryResponses.trim();
    String queryResponse = "";
    for (int i=0; i< responses.length; i++)
    {
      queryResponse = responses[i];
      if (queryResponse.substring(3,4).equals("P") )
      {
        retVal += "\n  Play Status: ";
        if (queryResponse.substring(5).equals("S") )
          retVal +="Stop";
        else if (queryResponse.substring(5).equals("P") )
          retVal +="Play";
        else if (queryResponse.substring(5).equals("p") )
          retVal +="Pause";
        else if (queryResponse.substring(5).equals("F") )
          retVal +="FastForward";
        else if (queryResponse.substring(5).equals("R") )
          retVal +="FastRewind";
        else retVal+= "NotSpecified";
      }
  
      if (queryResponse.substring(3,4).equals("R") )
      {
        retVal += "\n  Repeat Status: ";
        if (queryResponse.substring(5).equals("-") )
          retVal +="Off";
        else if (queryResponse.substring(5).equals("R") )
          retVal +="All";
        else if (queryResponse.substring(5).equals("F") )
          retVal +="Folder";
        else if (queryResponse.substring(5).equals("1") )
          retVal +="1 song";
        else retVal+= "NotSpecified";
      }
  
      if (queryResponse.substring(3,4).equals("S") )
      {
        retVal += "\n  Schuffle Status: ";
        if (queryResponse.trim().substring(5).equals("-") )
          retVal +="Off";
        else if (queryResponse.trim().substring(5).equals("S") )
          retVal +="All";
        else if (queryResponse.trim().substring(5).equals("A") )
          retVal +="Album";
        else if (queryResponse.trim().substring(5).equals("F") )
          retVal +="Folder";
        else retVal+= "NotSpecified";
      }
    }
    
    return retVal;
  }

  /** get the class volume_.
   * @return the volume_
   **/
  public float getVolume()
  {
    return volume_;
  }


  /** sets the class volume_.
   * @param volume the value to set the class volume_
   **/
  public void setVolume(float volume)
  {
    volume_ = volume;
  }

} // class

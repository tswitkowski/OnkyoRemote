/*
 *  $URL: svn://svn/open/trunk/projects/WebARTS/ca/bc/webarts/tools/eiscp/Eiscp.java $
 *  $Author: tgutwin $
 *  $Revision: 590 $
 *  $Date: 2012-12-09 15:19:16 -0800 (Sun, 09 Dec 2012) $
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


import android.annotation.SuppressLint;
import java.io.*;
import java.net.*;
import java.util.HashMap;
//import java.util.TreeSet;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

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

  /**  The users home ditrectory.  */
  public static String USERHOME = System.getProperty("user.home");

  /**  The users pwd ditrectory.  */
  public static String USERDIR = System.getProperty("user.dir");

  /**  A holder This classes name (used when logging).  */
  private static String CLASSNAME = "ca.bc.webarts.tools.eiscp.Eiscp";

  /**  Class flag signifying if the initUtil method has been called  */
//  private static boolean classInit = false;

  /** default receiver IP Address. **/
  private static final String DEFAULT_EISCP_IP = "16.1.1.200";
  /** Instantiated class IP for the receiver to communicate with. **/
  private String receiverIP_ = DEFAULT_EISCP_IP;

  /** default eISCP port. **/
  protected static final int DEFAULT_EISCP_PORT = 60128;
  /** Instantiated class Port for the receiver to communicate with. **/
  private int receiverPort_ = DEFAULT_EISCP_PORT;

  /** the socket for communication - the protocol spec says to use one socket connection AND HOLD ONTO IT for re-use. **/
  private static Socket eiscpSocket_ = null;
  /** the timeout in ms for socket reads. **/
  private static int socketTimeOut_ = 500;
  private static ObjectOutputStream out_ = null;
  private static DataInputStream in_ = null;
  private boolean connected_ = false;

  /** Maps the class contant vars to the eiscp command string. **/
  private static HashMap<Integer, String> commandMap_ = null;

  /** Maps a Readable string to a corresponding class var. **/
  private static HashMap<String, Integer> commandNameMap_ = null;

  /** Var to hold the volume level to or from a message. **/
  private static int volume_ = 32;

  private static StringBuffer helpMsg_ = new StringBuffer(SYSTEM_LINE_SEPERATOR);

  /** Simple class Constructor (using deafult IP and port) that gets all the class command constants set-up along with their command lookup maps (commandNameMap_ and commandMap_) . **/
  public Eiscp()
  {
    initCommandMap();
  }


  /** Constructor that takes your receivers ip and default port, gets all the class command 
   * constants set-up along with their command lookup maps (commandNameMap_ and commandMap_) . 
   **/
  public Eiscp(String ip)
  {
    initCommandMap();
    if (ip==null || ip.equals("")) 
      receiverIP_=DEFAULT_EISCP_IP;
    else 
      receiverIP_=ip;
    receiverPort_=DEFAULT_EISCP_PORT;
  }


  /** Constructor that takes your receivers ip and port,  gets all the class command 
   * constants set-up along with their command lookup maps (commandNameMap_ and commandMap_) . 
   **/
  public Eiscp(String ip, int eiscpPort)
  {
    initCommandMap();
    if (ip==null || ip.equals("")) 
      receiverIP_=DEFAULT_EISCP_IP;
    else 
      receiverIP_=ip;
    if (eiscpPort<1 ) 
      receiverPort_=DEFAULT_EISCP_PORT;
    else 
      receiverPort_=eiscpPort;
  }

  public boolean isConnected() {
     return connected_;
  }
  /** Makes Chocolate glazed doughnuts. **/
  public void setReceiverIP( String ip) { receiverIP_ = ip;}
  /** Makes Sprinkle doughnuts. **/
  public String getReceiverIP() {return receiverIP_;};
  /** Makes mini doughnuts. **/
  public void setReceiverPort( int port) { receiverPort_ = port;}
  /** Makes glazed doughnuts. **/
  public int getReceiverPort() {return receiverPort_;};

  /**
   * Connects to the receiver by opening a socket connection through the DEFaULT IP and DEFAULT eISCP port.
   **/
   public boolean connectSocket() { return connectSocket(null, -1);}


  /**
   * Connects to the receiver by opening a socket connection through the DEFAULT eISCP port.
   **/
   public boolean connectSocket(String ip) { return connectSocket(ip,-1);}


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
      System.out.println("Connected to "+ip+" on port "+eiscpPort);
      //2. get Input and Output streams
      out_ = new ObjectOutputStream(eiscpSocket_.getOutputStream());
      in_ = new DataInputStream(eiscpSocket_.getInputStream());

      //System.out.println("out_Init");
      out_.flush();
      // System.out.println("inInit");
      connected_ = true;
    }
    catch(UnknownHostException unknownHost)
    {
      System.err.println("You are trying to connect to an unknown host!");
    }
    catch(IOException ioException)
    {
      System.err.println("Can't Connect: "+ioException.getMessage());
    }
    return connected_;
  }


  /**
   * Tests the Connection to the receiver by opening a socket connection through the DEFaULT IP and DEFAULT eISCP port.
   **/
   public boolean testConnection() { return testConnection(DEFAULT_EISCP_IP,DEFAULT_EISCP_PORT);}


  /**
   * test the connection to the receiver by opening a socket connection through the eISCP port AND THEN CLOSES it if it was not already open.
   * @return true if already connected or can connect, and false if can't connect
   **/
  public boolean testConnection(String ip, int eiscpPort)
  {
    boolean retVal = false;
    if (ip==null || ip.equals("")) ip=DEFAULT_EISCP_IP;
    if (eiscpPort==0 ) eiscpPort=DEFAULT_EISCP_PORT;

    if (connected_)
    {
      // test existing connection
      if (eiscpSocket_.isConnected()) retVal = true;
    }
    else
    {
      // test a new connection
      try
      {
        //1. creating a socket to connect to the server
        eiscpSocket_ = new Socket(ip, eiscpPort);
        if (eiscpSocket_!=null) eiscpSocket_.close();
        retVal = true;
      }
      catch(UnknownHostException unknownHost)
      {
        System.err.println("You are trying to connect to an unknown host!");
      }
      catch(IOException ioException)
      {
        System.err.println("Can't Connect: "+ioException.getMessage());
      }
    }
    return retVal;
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
      if (acted) System.out.println("closed connections");
      connected_ = false;
    }
    catch(IOException ioException)
    {
      ioException.printStackTrace();
    }
    return connected_;
  }


  /** Converts an ascii decimal String to a hex  String.
   * @param String holding the string to convert to HEX
   * @return a string holding the HEX representation of the passed in decimal str.
   **/
  public static String convertStringToHex(String str)
  {
     return convertStringToHex( str, false);
  }


  /** Converts an ascii decimal String to a hex  String.
   * @param String holding the string to convert to HEX
   * @param boolean flag to turn some debug output on/off
   * @return a string holding the HEX representation of the passed in str.
   **/
  public static String convertStringToHex(String str,  boolean dumpOut)
  {
    char[] chars = str.toCharArray();
    String out_put = "";

    if (dumpOut) System.out.println("    Ascii: "+str);
    if (dumpOut) System.out.print("    Hex: ");
    StringBuffer hex = new StringBuffer();
    for(int i = 0; i < chars.length; i++)
    {
      out_put = Integer.toHexString((int)chars[i]);
      if (out_put.length()==1) hex.append("0");
      hex.append(out_put);
      if (dumpOut) System.out.print("0x"+(out_put.length()==1?"0":"")+ out_put+" ");
    }
    if (dumpOut) System.out.println("");

    return hex.toString();
  }


  /** Converts an HEX number String to its decimal equivalent.
   * @param String holding the Hex Number string to convert to decimal
   * @param boolean flag to turn some debug output on/off
   * @return an int holding the decimal equivalent of the passed in HEX numberStr.
   **/
  public static int convertHexNumberStringToDecimal(String str,  boolean dumpOut)
  {
    char[] chars = str.toCharArray();
    String out_put = "";

    if (dumpOut) System.out.println("        Ascii: "+str);
    if (dumpOut) System.out.print(  "          Hex: 0x");
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
    if (dumpOut) System.out.println("");
    if (dumpOut) System.out.println( "      Decimal: "+hexInt.toString());

    return Integer.parseInt(hexInt.toString());
  }


  /** Converts a hex byte to an ascii String.
   * @param byte holding the HEX string to convert back to decimal
   * @return a string holding the HEX representation of the passed in str.
   **/
  public static String convertHexToString(byte hex)
  {
    byte [] bytes = {hex};
    return convertHexToString( new String(bytes), false);
  }

  /** Converts a hex String to an ascii String.
   * @param String holding the HEX string to convert back to decimal
   * @return a string holding the HEX representation of the passed in str.
   **/
  public static String convertHexToString(String hex)
  {
    return convertHexToString( hex, false);
  }


  /** Converts a hex String to an ascii String.
   * @param String holding the HEX string to convert backk to decimal
   * @param boolean flag to turn some debug output on/off
   * @return a string holding the HEX representation of the passed in str.
   **/
  public static String convertHexToString(String hex,  boolean dumpOut)
  {

    StringBuilder sb = new StringBuilder();
    StringBuilder temp = new StringBuilder();
    String out_put = "";

    if (dumpOut) System.out.print("    Hex: ");
    //49204c6f7665204a617661 split into two characters 49, 20, 4c...
    for( int i=0; i<hex.length()-1; i+=2 ){

        //grab the hex in pairs
        out_put = hex.substring(i, (i + 2));
        if (dumpOut) System.out.print("0x"+out_put+" ");
        //convert hex to decimal
        int decimal = Integer.parseInt(out_put, 16);
        //convert the decimal to character
        sb.append((char)decimal);

        temp.append(decimal);
    }
    if (dumpOut) System.out.println("    Decimal : " + temp.toString());

    return sb.toString();
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
    if (command==VOLUME_SET)
      cmdStr = getVolumeCmdStr();
    else
      cmdStr = (String) commandMap_.get(Integer.valueOf(command));

    StringBuilder sb = new StringBuilder();
    int eiscpDataSize = ((String)commandMap_.get(Integer.valueOf(command))).length() + 2 ; // this is the eISCP data size
    int eiscpMsgSize = eiscpDataSize + 1 + 16 ; // this is the eISCP data size

    /* This is where I construct the entire message
        character by character. Each char is represented by a 2 disgit hex value */
    sb.append("ISCP");
    // the following are all in HEX representing one char

    // 4 char Big Endian Header
    sb.append((char)Integer.parseInt("00", 16));
    sb.append((char)Integer.parseInt("00", 16));
    sb.append((char)Integer.parseInt("00", 16));
    sb.append((char)Integer.parseInt("10", 16));

    // 4 char  Big Endian data size
    sb.append((char)Integer.parseInt("00", 16));
    sb.append((char)Integer.parseInt("00", 16));
    sb.append((char)Integer.parseInt("00", 16));
    // the official ISCP docs say this is supposed to be just the data size  (eiscpDataSize)
    // ** BUT **
    // It only works if you send the size of the entire Message size (eiscpMsgSize)
    sb.append((char)Integer.parseInt(Integer.toHexString(eiscpMsgSize), 16));

    // eiscp_version = "01";
    sb.append((char)Integer.parseInt("01", 16));

    // 3 chars reserved = "00"+"00"+"00";
    sb.append((char)Integer.parseInt("00", 16));
    sb.append((char)Integer.parseInt("00", 16));
    sb.append((char)Integer.parseInt("00", 16));

    //  eISCP data
    // Start Character
    sb.append("!");

    // eISCP data - unittype char '1' is receiver
    sb.append("1");

    // eISCP data - 3 char command and param    ie PWR01
    sb.append(cmdStr);

    // msg end - EOF
    sb.append((char)Integer.parseInt("0D", 16));

    System.out.println("  eISCP data size: "+eiscpDataSize +"(0x"+Integer.toHexString(eiscpDataSize) +") chars");
    System.out.println("  eISCP msg size: "+sb.length() +"(0x"+Integer.toHexString(sb.length()) +") chars");

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
        System.out.println("  sending "+sb.length() +" chars: ");
        convertStringToHex(sb.toString(), true);
        //out_.writeObject(sb.toString());
        //out_.writeChars(sb.toString());
        out_.writeBytes(sb.toString());  // <--- This is the one that works
        //out_.writeBytes(convertStringToHex(sb.toString(), false));
        //out_.writeChars(convertStringToHex(sb.toString(), false));
        out_.flush();
        System.out.println("sent!" );
      }
      catch(IOException ioException)
      {
        ioException.printStackTrace();
      }
    }
    if (closeSocket) closeSocket();
  }


  /**
    * Sends to command to the receiver and then waits for the response(s) <br />and returns all response packetMessages <br />and closes the socket.
    *
    * @param command must be one of the Command Class Constants from the eiscp.Eiscp.Command class.
    * @return the response to the command
    **/
  public String sendQueryCommand(int command)
  {
    return sendQueryCommand( command,  true, true);
  }
  
  
  /**
    * Sends to command to the receiver and then waits for the response(s) <br />and returns all response packetMessages.
    *
    * @param command must be one of the Command Class Constants from the eiscp.Eiscp.Command class.
    * @param closeSocket flag to close the connection when done or leave it open.
    * @return the response to the command
    **/
  public String sendQueryCommand(int command, boolean closeSocket)
  {
    return sendQueryCommand( command,  closeSocket, true);
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
      if (returnAll || currResponse.startsWith(getCommandStr(command).substring(0,3)))
        retVal+= currResponse+"\n";
    }
    
    if (closeSocket) closeSocket();
    
    return retVal ;
  }


  /** This method reads ALL responses (possibly more than one) after a query command. 
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
//    int headerSizeDecimal;
    int dataSizeDecimal = 0;
    char endChar1 ='!';// NR-5008 response sends 3 chars to terminate the packet - 0x1a 0x0d 0x0a
    char endChar2 ='!';
    char endChar3 ='!';

    if(connected_)
    {
      try
      {
        if (debugging) System.out.println("\nReading Response Packet");
        eiscpSocket_.setSoTimeout(socketTimeOut_); // this must be set or the following read will BLOCK / hang the method when the messages are done
        
        while((numBytesReceived = in_.read(responseBytes))>0)
        {
          totBytesReceived = 0;
          StringBuilder msgBuffer = new StringBuilder("");
          if (debugging) System.out.print( " Packet"+"["+packetCounter+"]:");
          
          /* Read ALL the incoming Bytes and buffer them */ 
          // *******************************************
          if (debugging) System.out.print(""+numBytesReceived);
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
          if (debugging) System.out.println();
          convertStringToHex(msgBuffer.toString(), debugging);
          
          /* Response is done... process it into dataMessages */
          // *******************************************
          char [] responseChars = msgBuffer.toString().toCharArray(); // use the charArray to step through
          int responseByteCnt = 0;
//          char versionChar = '1';
//          char dataStartChar = '!';
//          char dataUnitChar = '1';
          
          // loop through all the chars and split out the dataMessages
          while (responseByteCnt< totBytesReceived)
          {
            /* read Header */
            // 1st 4 chars are the leadIn
            responseByteCnt+=4;
            
            // read headerSize
//            char [] headerSizeBytes = {responseChars[responseByteCnt++],
//                                       responseChars[responseByteCnt++],
//                                       responseChars[responseByteCnt++],
//                                       responseChars[responseByteCnt++]} ;
            // 4 char Big Endian data size
            char [] dataSizeBytes = { responseChars[responseByteCnt++],
                                      responseChars[responseByteCnt++],
                                      responseChars[responseByteCnt++],
                                      responseChars[responseByteCnt++]} ;
            if (debugging) System.out.println(" -HeaderSize-");
//            headerSizeDecimal = convertHexNumberStringToDecimal(new String(headerSizeBytes),debugging);
            if (debugging) System.out.println(" -DataSize-");
            dataSizeDecimal = convertHexNumberStringToDecimal(new String(dataSizeBytes),debugging);
                                      
            // version
//            versionChar = responseChars[responseByteCnt++];
            
            // 3 reserved bytes
            responseByteCnt+=3;
            int dataByteCnt = 0;
            
            // Now the data message
//            dataStartChar = responseChars[responseByteCnt++]; // parse and throw away (like parsley)
//            dataUnitChar = responseChars[responseByteCnt++]; // dito
            char [] dataMessage = new char [dataSizeDecimal];
            
            /* Get the dataMessage from this response */
            // NR-5008 response sends 3 chars to terminate the packet - so DON't include them in the message
            while( dataByteCnt < (dataSizeDecimal-3) && responseByteCnt< (totBytesReceived-3))
            {
              dataMessage[dataByteCnt++] = responseChars[responseByteCnt++];
            }
            if (debugging) System.out.println(" -DataMessage-");
            if (debugging) System.out.println("    "+(new String(dataMessage))+ "\n");
            retVal.addElement(new String(dataMessage));
            
            // Read the end packet char(s) "[EOF]"
            // [EOF]			End of File		ASCII Code 0x1A
            // NOTE: the end of packet char (0x1A) for a response message is DIFFERENT that the sent message
            // NOTE: ITs also different than what is in the Onkyo eISCP docs
            // NR-5008 sends 3 chars to terminate the packet - 0x1a 0x0d 0x0a
            endChar1 = responseChars[responseByteCnt++];
            endChar2 = responseChars[responseByteCnt++];
            endChar3 = responseChars[responseByteCnt++];
            if (endChar1 == (char)Integer.parseInt("1A", 16) &&
                endChar2 == (char)Integer.parseInt("0D", 16) &&
                endChar3 == (char)Integer.parseInt("0A", 16) 
               ) if (debugging) System.out.println(" EndOfPacket["+packetCounter+"]\n");
            packetCounter++;
          }// 
          
        }

      }
      catch( java.net.SocketTimeoutException  noMoreDataException)
      {
        if (debugging) System.out.println("Response Done: " );
      }
      catch(EOFException  eofException)
      {
        System.out.println("received: \""+retVal+"\"" );
      }
      catch(IOException ioException)
      {
        ioException.printStackTrace();
      }
    }
    else
      System.out.println("!!Not Connected to Receive ");    
    return retVal;
  }


  /** This method creates the set volume command based on the passed value. **/
  public static  String getVolumeCmdStr(){return "MVL"+Integer.toHexString(volume_);}


  /** This method takes the  3 character response from the USB Play status query (NETUSB_PLAY_STATUS_QUERY) and creates a human readable String. 
   * NET/USB Play Status QUERY returns 3 letters - PRS.<oL>
   * <LI>p -> Play Status<ul><li>"S": STOP</li><li>"P": Play</li><li>"p": Pause</li><li>"F": FF</li><li>"R": FastREW</li></ul></LI>
   * <LI>r -> Repeat Status<ul><li>"-": Off</li><li>"R": All</li><li>"F": Folder</li><li>"1": Repeat 1</li></ul></LI>
   * <LI>s -> Shuffle Status<ul><li>"-": Off</li><li>"S": All</li><li>"A": Album</li><li>"F": Folder</li></ul></LI></oL>
   * @param queryResponses is the entire response packet with the 3 char reply embedded in it.
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


  /**
   *  A method to simply abstract the Try/Catch required to put the current
   *  thread to sleep for the specified time in ms.
   *
   * @param  waitTime  the sleep time in milli seconds (ms).
   * @return           boolean value specifying if the sleep completed (true) or
   *      was interupted (false).
   */
  public boolean sleep(long waitTime)
  {
    boolean retVal = true;
    /*
     *  BLOCK for the spec'd time
     */
    try
    {
      Thread.sleep(waitTime);
    }
    catch (InterruptedException iex)
    {
      retVal = false;
    }
    return retVal;
  }


  /** gets the help as a String. 
   * @return the helpMsg in String form
   **/
  private static String getHelpMsgStr() {return getHelpMsg().toString();}


  /** initializes and gets the helpMsg_
  class var. 
   * @return the class var helpMsg_
   **/
  private static StringBuffer getHelpMsg()
  {
    helpMsg_ = new StringBuffer(SYSTEM_LINE_SEPERATOR);
    helpMsg_.append("---  WebARTS Eiscp Class  -----------------------------------------------------");
    helpMsg_.append(SYSTEM_LINE_SEPERATOR);
    helpMsg_.append("---  $Revision: 590 $ $Date: 2012-12-09 15:19:16 -0800 (Sun, 09 Dec 2012) $ ---");
    helpMsg_.append(SYSTEM_LINE_SEPERATOR);
    helpMsg_.append("-------------------------------------------------------------------------------");
    helpMsg_.append(SYSTEM_LINE_SEPERATOR);
    helpMsg_.append("WebARTS Eiscp Class");
    helpMsg_.append(SYSTEM_LINE_SEPERATOR);
    helpMsg_.append("SYNTAX:");
    helpMsg_.append(SYSTEM_LINE_SEPERATOR);
    helpMsg_.append("   java ");
    helpMsg_.append(CLASSNAME);
    helpMsg_.append(" command [commandArgs]");
    helpMsg_.append(SYSTEM_LINE_SEPERATOR);
    helpMsg_.append(SYSTEM_LINE_SEPERATOR);
    helpMsg_.append("Available Methods:");
    /* now add all the commands available */
    TreeSet <String> ts = new TreeSet<String>(commandNameMap_.keySet());
    Iterator<String> it =ts.tailSet("").iterator();
    while( it.hasNext())
    {
      helpMsg_.append(SYSTEM_LINE_SEPERATOR);
      helpMsg_.append("-->   "+it.next());
    }
    helpMsg_.append(SYSTEM_LINE_SEPERATOR);
    helpMsg_.append("---------------------------------------------------------");
    helpMsg_.append("----------------------");
    helpMsg_.append(SYSTEM_LINE_SEPERATOR);

    return helpMsg_;
  }


  /**  
   * Class main commandLine entry method.
   **/
  public static void main(String [] args)
  {
//    final String methodName = CLASSNAME + ": main()";
    Eiscp instance = new Eiscp(DEFAULT_EISCP_IP, DEFAULT_EISCP_PORT);

    /* Simple way af parsing the args */
    if (args ==null || args.length<1)
      System.out.println(getHelpMsgStr());
    else
    {
      if (args[0].equals("test"))
      {
        System.out.println("Testing Eiscp");
        instance.sendQueryCommand(VOLUME_QUERY);
        instance.sendCommand(MUTE);
        instance.sleep(750);
        instance.sendCommand(UNMUTE);
      }
      else
      {
        // Parse the command
        int command = -1;
        String commandStr = "";
        // TODO: Set up a loop to handle multiple commands/args in one run with one socket connection
        command =instance.getCommand(args[0].toUpperCase());  //returns -1 if not found
        commandStr=instance.getCommandStr(command);
        
        /* Special case VOLUME_SET command needs to parse a parameter. */
        if ( command == VOLUME_SET ) instance.setVolume(Integer.parseInt(args[1]));
        System.out.println("command: "+commandStr);

        String queryResponse = "";
        if (command!=-1)
          
          /* It is a query command so send AND parse response */
          if(args[0].endsWith("QUERY") )   
          {
            //send the command and get the response
            queryResponse = instance.sendQueryCommand(command, true, false);
            System.out.print("Responses: \n" +queryResponse);
            if (queryResponse!=null && !queryResponse.equals(""))
            {
              System.out.println(instance.getCommandName(queryResponse.trim()));
              if (command==NETUSB_PLAY_STATUS_QUERY)
              {
                System.out.println(instance.decipherUsbPlayStatusResponse(queryResponse));
              }
            }
            else
              System.out.println("\n"+ args[0]+"("+commandStr +") response: EMPTY");
          }
          
          /* It is a basic change setting command (with no response) */
          else 
          {
            instance.sendCommand(command); //send the command
          }
      }
      instance.closeSocket();
    }
  } // main


  /** get the class volume_.
   * @return the volume_
   **/
  public int getVolume()
  {
    return volume_;
  }


  /** sets the class volume_.
   * @param volume the value to set the class volume_
   **/
  public void setVolume(int volume)
  {
    volume_ = volume;
  }


  /** searches for the commandName that is associated with the passed command.
   * @param commandStr the iscp command to get a commandName key
   * @return the commandNameMap_ key
  **/
  public String getCommandName(String commandStr)
  {
    String retVal = "";
    int command = -1;
    for (int i=0; i< eCnt; i++)
    {
      if (((String)commandMap_.get(i)).equals(commandStr)) command = i;
    }
    if (command!=-1)
    {
      Iterator<String> it = commandNameMap_.keySet().iterator();
      int curr = -1;
      String currStr = "";
      while (it.hasNext())
      {
        currStr = (String)it.next();
        curr = commandNameMap_.get(currStr);
        if (curr==command) retVal = currStr;
      }
    }

    return retVal;
  }


  /** searches for the command constant that is associated with the passed command name.
   * @param commandStr a string representation of command referencing the iscp command 
   * @return the iscp command constant reference
   **/
  public int getCommand(String commandName)
  {
    return  commandNameMap_.get(commandName);
  }


  /** searches for the command that is associated with the passed command constant.
   * @param command the command referencing the iscp command constant
   * @return the iscp command string (example 'SLI10')
   **/
  public String getCommandStr(int command)
  {
    return  commandMap_.get(command);
  }


  /** searches for the command that is associated with the passed commandName.
   * @param commandName the commandName key referencing the iscp command str
   * @return the iscp command string (example 'SLI10')
   **/
  public String getCommandStr(String commandName)
  {
    int command = -1;
    if (commandNameMap_.containsKey(commandName));
      command = commandNameMap_.get(commandName);
    return  commandMap_.get(command);
  }


  /** Initializes all the class constants (commandNameMap_ & commandMap_ ) that help with processing the commands.
  **/
  @SuppressLint("UseSparseArrays")
private void initCommandMap()
  {
    commandNameMap_ = new HashMap<String, Integer>(eCnt);
    commandMap_ = new HashMap<Integer, String>(eCnt);

    commandNameMap_.put("POWER_OFF", POWER_OFF);
    commandNameMap_.put("POWER_ON", POWER_ON);
    commandNameMap_.put("POWER_QUERY", POWER_QUERY);
    commandNameMap_.put("UNMUTE", UNMUTE);
    commandNameMap_.put("MUTE", MUTE);
    commandNameMap_.put("MUTE_QUERY", MUTE_QUERY);
    commandNameMap_.put("VOLUME_UP", VOLUME_UP);
    commandNameMap_.put("VOLUME_DOWN", VOLUME_DOWN);
    commandNameMap_.put("VOLUME_QUERY", VOLUME_QUERY);
    commandNameMap_.put("VOLUME_SET", VOLUME_SET);
    commandNameMap_.put("SET_VOLUME", VOLUME_SET);
    commandNameMap_.put("VOLUME", VOLUME_SET);
    commandNameMap_.put("SOURCE_DVR", SOURCE_DVR);
    commandNameMap_.put("SOURCE_SATELLITE", SOURCE_SATELLITE);
    commandNameMap_.put("SOURCE_GAME", SOURCE_GAME);
    commandNameMap_.put("SOURCE_AUXILIARY", SOURCE_AUX);
    commandNameMap_.put("SOURCE_AUX", SOURCE_AUX);
    commandNameMap_.put("SOURCE_VIDEO5", SOURCE_VIDEO5);
    commandNameMap_.put("SOURCE_COMPUTER", SOURCE_COMPUTER);
    commandNameMap_.put("SOURCE_PC", SOURCE_COMPUTER);
    commandNameMap_.put("SOURCE_BLURAY", SOURCE_BLURAY);
    commandNameMap_.put("SOURCE_TAPE1", SOURCE_TAPE1);
    commandNameMap_.put("SOURCE_TAPE2", SOURCE_TAPE2);
    commandNameMap_.put("SOURCE_PHONO", SOURCE_PHONO);
    commandNameMap_.put("SOURCE_CD", SOURCE_CD);
    commandNameMap_.put("SOURCE_FM", SOURCE_FM);
    commandNameMap_.put("SOURCE_AM", SOURCE_AM);
    commandNameMap_.put("SOURCE_TUNER", SOURCE_TUNER);
    commandNameMap_.put("SOURCE_MUSICSERVER", SOURCE_MUSICSERVER);
    commandNameMap_.put("SOURCE_INTERETRADIO", SOURCE_INTERETRADIO);
    commandNameMap_.put("SOURCE_USB", SOURCE_USB);
    commandNameMap_.put("SOURCE_USB_BACK", SOURCE_USB_BACK);
    commandNameMap_.put("SOURCE_NETWORK", SOURCE_NETWORK);
    commandNameMap_.put("SOURCE_MULTICH", SOURCE_MULTICH);
    commandNameMap_.put("SOURCE_SIRIUS", SOURCE_SIRIUS);
    commandNameMap_.put("SOURCE_UP", SOURCE_UP);
    commandNameMap_.put("SOURCE_DOWN", SOURCE_DOWN);
    commandNameMap_.put("SOURCE_QUERY", SOURCE_QUERY);
    commandNameMap_.put("VIDEO_WIDE_AUTO", VIDEO_WIDE_AUTO);
    commandNameMap_.put("VIDEO_WIDE_43", VIDEO_WIDE_43);
    commandNameMap_.put("VIDEO_WIDE_FULL", VIDEO_WIDE_FULL);
    commandNameMap_.put("VIDEO_WIDE_ZOOM", VIDEO_WIDE_ZOOM);
    commandNameMap_.put("VIDEO_WIDE_WIDEZOOM", VIDEO_WIDE_WIDEZOOM);
    commandNameMap_.put("VIDEO_WIDE_SMARTZOOM", VIDEO_WIDE_SMARTZOOM);
    commandNameMap_.put("VIDEO_WIDE_NEXT", VIDEO_WIDE_NEXT);
    commandNameMap_.put("VIDEO_WIDE_QUERY", VIDEO_WIDE_QUERY);
    commandNameMap_.put("LISTEN_MODE_STEREO", LISTEN_MODE_STEREO);
    commandNameMap_.put("LISTEN_MODE_ALCHANSTEREO", LISTEN_MODE_ALCHANSTEREO);
    commandNameMap_.put("LISTEN_MODE_AUDYSSEY_DSX", LISTEN_MODE_AUDYSSEY_DSX);
    commandNameMap_.put("LISTEN_MODE_PLII_MOVIE_DSX", LISTEN_MODE_PLII_MOVIE_DSX);
    commandNameMap_.put("LISTEN_MODE_PLII_MUSIC_DSX", LISTEN_MODE_PLII_MUSIC_DSX);
    commandNameMap_.put("LISTEN_MODE_PLII_GAME_DSX", LISTEN_MODE_PLII_GAME_DSX);
    commandNameMap_.put("LISTEN_MODE_NEO_CINEMA_DSX", LISTEN_MODE_NEO_CINEMA_DSX);
    commandNameMap_.put("LISTEN_MODE_NEO_MUSIC_DSX", LISTEN_MODE_NEO_MUSIC_DSX);
    commandNameMap_.put("LISTEN_MODE_NEURAL_SURROUND_DSX", LISTEN_MODE_NEURAL_SURROUND_DSX);
    commandNameMap_.put("LISTEN_MODE_NEURAL_DIGITAL_DSX", LISTEN_MODE_NEURAL_DIGITAL_DSX);
    commandNameMap_.put("LISTEN_MODE_QUERY", LISTEN_MODE_QUERY);
    commandNameMap_.put("ZONE2_POWER_ON",         ZONE2_POWER_ON);
    commandNameMap_.put("ZONE2_POWER_SBY",        ZONE2_POWER_SBY);
    commandNameMap_.put("ZONE2_POWER_QUERY",      ZONE2_POWER_QUERY);
    commandNameMap_.put("ZONE2_SOURCE_DVR",       ZONE2_SOURCE_DVR);
    commandNameMap_.put("ZONE2_SOURCE_SATELLITE", ZONE2_SOURCE_SATELLITE);
    commandNameMap_.put("ZONE2_SOURCE_GAME",      ZONE2_SOURCE_GAME);
    commandNameMap_.put("ZONE2_SOURCE_AUX",       ZONE2_SOURCE_AUX);
    commandNameMap_.put("ZONE2_SOURCE_VIDEO5",    ZONE2_SOURCE_VIDEO5);
    commandNameMap_.put("ZONE2_SOURCE_COMPUTER",  ZONE2_SOURCE_COMPUTER);
    commandNameMap_.put("ZONE2_SOURCE_BLURAY",    ZONE2_SOURCE_BLURAY);
    commandNameMap_.put("ZONE2_SOURCE_QUERY",     ZONE2_SOURCE_QUERY);
    //commandNameMap_.put("ZONE2_SOURCE_OFF",       ZONE2_SOURCE_OFF); // not supported

    commandNameMap_.put("NETUSB_OP_PLAY"     , NETUSB_OP_PLAY);
    commandNameMap_.put("NETUSB_OP_STOP"     , NETUSB_OP_STOP);
    commandNameMap_.put("NETUSB_OP_PAUSE"    , NETUSB_OP_PAUSE);
    commandNameMap_.put("NETUSB_OP_TRACKUP"  , NETUSB_OP_TRACKUP);
    commandNameMap_.put("NETUSB_OP_TRACKDWN" , NETUSB_OP_TRACKDWN);
    commandNameMap_.put("NETUSB_OP_FF"       , NETUSB_OP_FF);
    commandNameMap_.put("NETUSB_OP_REW"      , NETUSB_OP_REW);
    commandNameMap_.put("NETUSB_OP_REPEAT"   , NETUSB_OP_REPEAT);
    commandNameMap_.put("NETUSB_OP_RANDOM"   , NETUSB_OP_RANDOM);
    commandNameMap_.put("NETUSB_OP_DISPLAY"  , NETUSB_OP_DISPLAY);
    commandNameMap_.put("NETUSB_OP_RIGHT"    , NETUSB_OP_RIGHT);
    commandNameMap_.put("NETUSB_OP_LEFT"     , NETUSB_OP_LEFT);
    commandNameMap_.put("NETUSB_OP_UP"       , NETUSB_OP_UP);
    commandNameMap_.put("NETUSB_OP_DOWN"     , NETUSB_OP_DOWN);
    commandNameMap_.put("NETUSB_OP_SELECT"   , NETUSB_OP_SELECT);
    commandNameMap_.put("NETUSB_OP_1"        , NETUSB_OP_1);
    commandNameMap_.put("NETUSB_OP_2"        , NETUSB_OP_2);
    commandNameMap_.put("NETUSB_OP_3"        , NETUSB_OP_3);
    commandNameMap_.put("NETUSB_OP_4"        , NETUSB_OP_4);
    commandNameMap_.put("NETUSB_OP_5"        , NETUSB_OP_5);
    commandNameMap_.put("NETUSB_OP_6"        , NETUSB_OP_6);
    commandNameMap_.put("NETUSB_OP_7"        , NETUSB_OP_7);
    commandNameMap_.put("NETUSB_OP_8"        , NETUSB_OP_8);
    commandNameMap_.put("NETUSB_OP_9"        , NETUSB_OP_9);
    commandNameMap_.put("NETUSB_OP_0"        , NETUSB_OP_0);
    commandNameMap_.put("NETUSB_OP_DELETE"   , NETUSB_OP_DELETE);
    commandNameMap_.put("NETUSB_OP_CAPS"     , NETUSB_OP_CAPS);
    commandNameMap_.put("NETUSB_OP_SETUP"    , NETUSB_OP_SETUP);
    commandNameMap_.put("NETUSB_OP_RETURN"   , NETUSB_OP_RETURN);
    commandNameMap_.put("NETUSB_OP_CHANUP"   , NETUSB_OP_CHANUP);
    commandNameMap_.put("NETUSB_OP_CHANDWN"  , NETUSB_OP_CHANDWN);
    commandNameMap_.put("NETUSB_OP_MENU"     , NETUSB_OP_MENU);
    commandNameMap_.put("NETUSB_OP_TOPMENU"  , NETUSB_OP_TOPMENU);
    commandNameMap_.put("NETUSB_SONG_ARTIST_QUERY" ,NETUSB_SONG_ARTIST_QUERY);
    commandNameMap_.put("NETUSB_SONG_ALBUM_QUERY" , NETUSB_SONG_ALBUM_QUERY);
    commandNameMap_.put("NETUSB_SONG_TITLE_QUERY" , NETUSB_SONG_TITLE_QUERY);
    commandNameMap_.put("NETUSB_SONG_ELAPSEDTIME_QUERY", NETUSB_SONG_ELAPSEDTIME_QUERY);
    commandNameMap_.put("NETUSB_SONG_TRACK_QUERY", NETUSB_SONG_TRACK_QUERY);
    commandNameMap_.put("NETUSB_PLAY_STATUS_QUERY" , NETUSB_PLAY_STATUS_QUERY);

    commandMap_.put(POWER_OFF, "PWR00");
    commandMap_.put(POWER_ON , "PWR01");
    commandMap_.put(POWER_QUERY , "PWRQSTN");
    commandMap_.put(UNMUTE      , "AMT00");
    commandMap_.put(MUTE        , "AMT01");
    commandMap_.put(MUTE_QUERY  , "AMTQSTN");
    commandMap_.put(VOLUME_UP   , "MVLUP");
    commandMap_.put(VOLUME_DOWN , "MVLDOWN");
    commandMap_.put(VOLUME_QUERY , "MVLQSTN");
    commandMap_.put(VOLUME_SET , "MVL");
    commandMap_.put(SOURCE_DVR , "SLI00");
    commandMap_.put(SOURCE_SATELLITE , "SLI01");
    commandMap_.put(SOURCE_GAME , "SLI02");
    commandMap_.put(SOURCE_AUX , "SLI03");
    commandMap_.put(SOURCE_VIDEO5 , "SLI04");
    commandMap_.put(SOURCE_COMPUTER , "SLI05");
    commandMap_.put(SOURCE_BLURAY   , "SLI10");
    commandMap_.put(SOURCE_TAPE1    , "SLI20");
    commandMap_.put(SOURCE_TAPE2    , "SLI21");
    commandMap_.put(SOURCE_PHONO    , "SLI22");
    commandMap_.put(SOURCE_CD       , "SLI23");
    commandMap_.put(SOURCE_FM    , "SLI24");
    commandMap_.put(SOURCE_AM    , "SLI25");
    commandMap_.put(SOURCE_TUNER    , "SLI26");
    commandMap_.put(SOURCE_MUSICSERVER    , "SLI27");
    commandMap_.put(SOURCE_INTERETRADIO   , "SLI28");
    commandMap_.put(SOURCE_USB   , "SLI29");
    commandMap_.put(SOURCE_USB_BACK   , "SLI2A");
    commandMap_.put(SOURCE_NETWORK   , "SLI2C");
    commandMap_.put(SOURCE_MULTICH    , "SLI30");
    commandMap_.put(SOURCE_SIRIUS    , "SLI32");
    commandMap_.put(SOURCE_UP    , "SLIUP");
    commandMap_.put(SOURCE_DOWN    , "SLIDOWN");
    commandMap_.put(SOURCE_QUERY    , "SLIQSTN");
    commandMap_.put(VIDEO_WIDE_AUTO    , "VWM00");
    commandMap_.put(VIDEO_WIDE_43    , "VWM01");
    commandMap_.put(VIDEO_WIDE_FULL    , "VWM02");
    commandMap_.put(VIDEO_WIDE_ZOOM    , "VWM03");
    commandMap_.put(VIDEO_WIDE_WIDEZOOM    , "VWM04");
    commandMap_.put(VIDEO_WIDE_SMARTZOOM    , "VWM05");
    commandMap_.put(VIDEO_WIDE_NEXT    , "VWMUP");
    commandMap_.put(VIDEO_WIDE_QUERY    , "VWMQSTN");
    commandMap_.put(LISTEN_MODE_STEREO    , "LMD00");
    commandMap_.put(LISTEN_MODE_ALCHANSTEREO    , "LMD0C");
    commandMap_.put(LISTEN_MODE_AUDYSSEY_DSX    , "LMD16");
    commandMap_.put(LISTEN_MODE_PLII_MOVIE_DSX    , "LMDA0");
    commandMap_.put(LISTEN_MODE_PLII_MUSIC_DSX    , "LMDA1");
    commandMap_.put(LISTEN_MODE_PLII_GAME_DSX    , "LMDA2");
    commandMap_.put(LISTEN_MODE_NEO_CINEMA_DSX    , "LMDA3");
    commandMap_.put(LISTEN_MODE_NEO_MUSIC_DSX    , "LMDA4");
    commandMap_.put(LISTEN_MODE_NEURAL_SURROUND_DSX    , "LMDA5");
    commandMap_.put(LISTEN_MODE_NEURAL_DIGITAL_DSX    , "LMDA6");
    commandMap_.put(LISTEN_MODE_QUERY   , "LMDQSTN");
    commandMap_.put(ZONE2_POWER_ON, "ZPW01");
    commandMap_.put(ZONE2_POWER_SBY, "ZPW00");
    commandMap_.put(ZONE2_POWER_QUERY, "ZPWQSTN");
    commandMap_.put(ZONE2_SOURCE_DVR, "SLZ00");
    commandMap_.put(ZONE2_SOURCE_SATELLITE, "SLZ01");
    commandMap_.put(ZONE2_SOURCE_GAME, "SLZ02");
    commandMap_.put(ZONE2_SOURCE_AUX, "SLZ03");
    commandMap_.put(ZONE2_SOURCE_VIDEO5, "SLZ04");
    commandMap_.put(ZONE2_SOURCE_COMPUTER, "SLZ05");
    commandMap_.put(ZONE2_SOURCE_BLURAY, "SLZ10");
    commandMap_.put(ZONE2_SOURCE_QUERY, "SLZQSTN");
    //commandMap_.put(ZONE2_SOURCE_OFF, "SLZ7F"); // not supported

    commandMap_.put(NETUSB_OP_PLAY     , "NTCPLAY");
    commandMap_.put(NETUSB_OP_STOP     , "NTCSTOP");
    commandMap_.put(NETUSB_OP_PAUSE    , "NTCPAUSE");
    commandMap_.put(NETUSB_OP_TRACKUP  , "NTCTRUP");
    commandMap_.put(NETUSB_OP_TRACKDWN , "NTCTRDN");
    commandMap_.put(NETUSB_OP_FF       , "NTCFF");
    commandMap_.put(NETUSB_OP_REW      , "NTCREW");
    commandMap_.put(NETUSB_OP_REPEAT   , "NTCREPEAT");
    commandMap_.put(NETUSB_OP_RANDOM   , "NTCRANDOM");
    commandMap_.put(NETUSB_OP_DISPLAY  , "NTCDISPLAY");
    commandMap_.put(NETUSB_OP_RIGHT    , "NTCRIGHT");
    commandMap_.put(NETUSB_OP_LEFT     , "NTCLEFT");
    commandMap_.put(NETUSB_OP_UP       , "NTCUP");
    commandMap_.put(NETUSB_OP_DOWN     , "NTCDOWN");
    commandMap_.put(NETUSB_OP_SELECT   , "NTCSELECT");
    commandMap_.put(NETUSB_OP_1        , "NTC1");
    commandMap_.put(NETUSB_OP_2        , "NTC2");
    commandMap_.put(NETUSB_OP_3        , "NTC3");
    commandMap_.put(NETUSB_OP_4        , "NTC4");
    commandMap_.put(NETUSB_OP_5        , "NTC5");
    commandMap_.put(NETUSB_OP_6        , "NTC6");
    commandMap_.put(NETUSB_OP_7        , "NTC7");
    commandMap_.put(NETUSB_OP_8        , "NTC8");
    commandMap_.put(NETUSB_OP_9        , "NTC9");
    commandMap_.put(NETUSB_OP_0        , "NTC0");
    commandMap_.put(NETUSB_OP_DELETE   , "NTCDELETE");
    commandMap_.put(NETUSB_OP_CAPS     , "NTCCAPS");
    commandMap_.put(NETUSB_OP_SETUP    , "NTCSETUP");
    commandMap_.put(NETUSB_OP_RETURN   , "NTCRETURN");
    commandMap_.put(NETUSB_OP_CHANUP   , "NTCCHUP");
    commandMap_.put(NETUSB_OP_CHANDWN  , "NTCCHDN");
    commandMap_.put(NETUSB_OP_MENU     , "NTCMENU");
    commandMap_.put(NETUSB_OP_TOPMENU  , "NTCTOP");
    commandMap_.put(NETUSB_SONG_ARTIST_QUERY , "NATQSTN");
    commandMap_.put(NETUSB_SONG_ALBUM_QUERY , "NALQSTN");
    commandMap_.put(NETUSB_SONG_TITLE_QUERY , "NTIQSTN");
    commandMap_.put(NETUSB_SONG_ELAPSEDTIME_QUERY, "NTMQSTN");
    commandMap_.put(NETUSB_SONG_TRACK_QUERY, "NTRQSTN");
    commandMap_.put(NETUSB_PLAY_STATUS_QUERY , "NSTQSTN");
  }


      private static int eCnt = 0;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int POWER_OFF = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int POWER_ON  = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int POWER_QUERY  = eCnt++;

      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int UNMUTE       = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int MUTE         = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int MUTE_QUERY    = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int VOLUME_UP    = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int VOLUME_DOWN  = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int VOLUME_QUERY = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int VOLUME_SET   = eCnt++;

      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int SOURCE_DVR  = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int SOURCE_SATELLITE  = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int SOURCE_GAME  = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int SOURCE_AUX = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int SOURCE_VIDEO5  = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int SOURCE_COMPUTER  = eCnt++;
      //public static final int SOURCE_VIDEO6    = eCnt++;
      //public static final int SOURCE_VIDEO7    = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int SOURCE_BLURAY    = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int SOURCE_TAPE1     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int SOURCE_TAPE2     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int SOURCE_PHONO     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int SOURCE_CD        = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int SOURCE_FM     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int SOURCE_AM     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int SOURCE_TUNER     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int SOURCE_MUSICSERVER     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int SOURCE_INTERETRADIO    = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int SOURCE_USB    = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int SOURCE_USB_BACK    = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int SOURCE_NETWORK    = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int SOURCE_MULTICH     = eCnt++;
      //public static final int SOURCE_XM     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int SOURCE_SIRIUS     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int SOURCE_UP     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int SOURCE_DOWN     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int SOURCE_QUERY     = eCnt++;

      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int VIDEO_WIDE_AUTO     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int VIDEO_WIDE_43     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int VIDEO_WIDE_FULL     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int VIDEO_WIDE_ZOOM     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int VIDEO_WIDE_WIDEZOOM     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int VIDEO_WIDE_SMARTZOOM     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int VIDEO_WIDE_NEXT     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int VIDEO_WIDE_QUERY     = eCnt++;

      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int LISTEN_MODE_STEREO     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int LISTEN_MODE_ALCHANSTEREO     = eCnt++;

      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int LISTEN_MODE_AUDYSSEY_DSX     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int LISTEN_MODE_PLII_MOVIE_DSX     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int LISTEN_MODE_PLII_MUSIC_DSX     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int LISTEN_MODE_PLII_GAME_DSX     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int LISTEN_MODE_NEO_CINEMA_DSX     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int LISTEN_MODE_NEO_MUSIC_DSX     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int LISTEN_MODE_NEURAL_SURROUND_DSX     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int LISTEN_MODE_NEURAL_DIGITAL_DSX     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int LISTEN_MODE_QUERY    = eCnt++;

      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int ZONE2_POWER_ON     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int ZONE2_POWER_SBY     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int ZONE2_POWER_QUERY     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int ZONE2_SOURCE_DVR     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int ZONE2_SOURCE_SATELLITE     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int ZONE2_SOURCE_GAME     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int ZONE2_SOURCE_AUX     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int ZONE2_SOURCE_VIDEO5     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int ZONE2_SOURCE_COMPUTER     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int ZONE2_SOURCE_BLURAY     = eCnt++;
      /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int ZONE2_SOURCE_QUERY     = eCnt++;

       /** Command Class Constant mapped to its corresponding iscp command. **/
      public static final int NETUSB_OP_PLAY     = eCnt++;
      public static final int NETUSB_OP_STOP     = eCnt++;
      public static final int NETUSB_OP_PAUSE    = eCnt++;
      public static final int NETUSB_OP_TRACKUP  = eCnt++;
      public static final int NETUSB_OP_TRACKDWN = eCnt++;
      public static final int NETUSB_OP_FF       = eCnt++;
      public static final int NETUSB_OP_REW      = eCnt++;
      public static final int NETUSB_OP_REPEAT   = eCnt++;
      public static final int NETUSB_OP_RANDOM   = eCnt++;
      public static final int NETUSB_OP_DISPLAY  = eCnt++;
      public static final int NETUSB_OP_RIGHT    = eCnt++;
      public static final int NETUSB_OP_LEFT     = eCnt++;
      public static final int NETUSB_OP_UP       = eCnt++;
      public static final int NETUSB_OP_DOWN     = eCnt++;
      public static final int NETUSB_OP_SELECT   = eCnt++;
      public static final int NETUSB_OP_1        = eCnt++;
      public static final int NETUSB_OP_2        = eCnt++;
      public static final int NETUSB_OP_3        = eCnt++;
      public static final int NETUSB_OP_4        = eCnt++;
      public static final int NETUSB_OP_5        = eCnt++;
      public static final int NETUSB_OP_6        = eCnt++;
      public static final int NETUSB_OP_7        = eCnt++;
      public static final int NETUSB_OP_8        = eCnt++;
      public static final int NETUSB_OP_9        = eCnt++;
      public static final int NETUSB_OP_0        = eCnt++;
      public static final int NETUSB_OP_DELETE   = eCnt++;
      public static final int NETUSB_OP_CAPS     = eCnt++;
      public static final int NETUSB_OP_SETUP    = eCnt++;
      public static final int NETUSB_OP_RETURN   = eCnt++;
      public static final int NETUSB_OP_CHANUP   = eCnt++;
      public static final int NETUSB_OP_CHANDWN  = eCnt++;
      public static final int NETUSB_OP_MENU     = eCnt++;
      public static final int NETUSB_OP_TOPMENU  = eCnt++;
      public static final int NETUSB_SONG_ARTIST_QUERY = eCnt++;
      public static final int NETUSB_SONG_ALBUM_QUERY = eCnt++;
      public static final int NETUSB_SONG_TITLE_QUERY = eCnt++;
      /** NET/USB Time Info (Elapsed time/Track Time Max 99:59). **/
      public static final int NETUSB_SONG_ELAPSEDTIME_QUERY = eCnt++;
      /** NET/USB Track Info (Current Track/Toral Track Max 9999). **/
      public static final int NETUSB_SONG_TRACK_QUERY = eCnt++;

      /** NET/USB Play Status QUERY (3 letters - PRS).<UL>
       * <LI>p -> Play Status: "S": STOP, "P": Play, "p": Pause, "F": FF, "R": FREW</LI>
       * <LI>r -> Repeat Status: "-": Off, "R": All, "F": Folder, "1": Repeat 1</LI>
       * <LI>s -> Shuffle Status: "-": Off, "S": All , "A": Album, "F": Folder</LI></UL>
       **/
      public static final int NETUSB_PLAY_STATUS_QUERY = eCnt++; //NET/USB Track Info (Current Track/Toral Track Max 9999)


} // class

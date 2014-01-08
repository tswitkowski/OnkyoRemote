/*
 *  $URL: svn://svn/open/trunk/projects/WebARTS/ca/bc/webarts/tools/eiscp/IscpCommands.java $
 *  $Author: $
 *  $Revision: $
 *  $Date: $
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

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.Iterator;

/**
 *  A class that wraps the Onkyo/Integra Serial Control Protocol  (eISCP) messages that can be sent in a packet.
 *  This class uses class constants and commandMaps to help handling of the many iscp Commands.
 *  <br /><br />
 *  The Message packet looks like:<br />
 *  <img src="http://tom.webarts.ca/_/rsrc/1320209141605/Blog/new-blog-items/javaeiscp-integraserialcontrolprotocol/eISCP-Packet.png" border="1"/>
 *  <br /> See also <a href="http://tom.webarts.ca/Blog/new-blog-items/javaeiscp-integraserialcontrolprotocol" > tom.webarts.ca</a> writeup.
 *
 * @author     Tom Gutwin P.Eng
 */
public class IscpCommands
{
	/**  A holder for this clients System File Separator.  */
	//public final static String SYSTEM_FILE_SEPERATOR = File.separator;

	/**  A holder for this clients System line termination separator.  */
	public final static String SYSTEM_LINE_SEPERATOR = System.getProperty("line.separator");

	/**  The VM classpath (used in some methods)..  */
	public static String CLASSPATH = System.getProperty("class.path");

	/**  The users home ditrectory.  */
	public static String USERHOME = System.getProperty("user.home");

	/**  The users pwd ditrectory.  */
	public static String USERDIR = System.getProperty("user.dir");

	/** Maps the class contant vars to the eiscp command string. **/
	private static HashMap<Integer, String> commandMap_ = null;
	public static HashMap<String, Integer> commandMapInverse_ = null;

	/** Maps a Readable string to a corresponding class var. **/
	private static HashMap<String, Integer> commandNameMap_ = null;

	private static IscpCommands instance_ = null;


	private IscpCommands()
	{
		initCommandMap();
		instance_ = this;
	}

	/** Singleton method to ensure all is setup. **/
	public static IscpCommands getInstance()
	{
    if (instance_!=null)
      return instance_;
    else
      return new IscpCommands();
  }

	/**
	 * Gets an iterator of all commandNames.
	 * @return the commandNames iterator as Strings
	 **/
	public Iterator<String> getIterator()
  {
		TreeSet <String> ts = new TreeSet<String>(commandNameMap_.keySet());
		Iterator<String> it =ts.tailSet("").iterator();
		return it;
  }

	/** searches for the commandName that is associated with the passed command.
	 * @param commandStr the iscp command to get a commandName key
	 * @return the commandNameMap_ key
	**/
	public static String getCommandName(String commandStr)
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
	 * @param commandName a string representation of command referencing the iscp command
	 * @return the iscp command constant reference
	 **/
	public static int getCommand(String commandName)
	{
		return  commandNameMap_.get(commandName);
	}


	/** searches for the command that is associated with the passed command constant.
	 * @param command the command referencing the iscp command constant
	 * @return the iscp command string (example 'SLI10')
	 **/
	public static String getCommandStr(int command)
	{
		return  commandMap_.get(command);
	}


	/** searches for the command that is associated with the passed commandName.
	 * @param commandName the commandName key referencing the iscp command str
	 * @return the iscp command string (example 'SLI10')
	 **/
	public static String getCommandStr(String commandName)
	{
		int command = -1;
		if (commandNameMap_.containsKey(commandName))
			command = commandNameMap_.get(commandName);
		return  commandMap_.get(command);
	}

  /** This method creates the set volume command based on the passed value. **/
  public static String getVolumeCmdStr(int volume){return "MVL"+Integer.toHexString(volume);}


	/** Initializes all the class constants (commandNameMap_ & commandMap_ ) that help with processing the commands.
	**/
	@SuppressLint("UseSparseArrays")
	private void initCommandMap()
	{
		commandNameMap_ = new HashMap<String, Integer>(eCnt);
		commandMap_ = new HashMap<Integer, String>(eCnt);
		commandMapInverse_ = new HashMap<String, Integer>(eCnt);

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
		commandNameMap_.put("OSD_UP" , DIRECTION_UP);
		commandNameMap_.put("OSD_DOWN" , DIRECTION_DOWN);
		commandNameMap_.put("OSD_LEFT" , DIRECTION_LEFT);
		commandNameMap_.put("OSD_RIGHT" , DIRECTION_RIGHT);
		commandNameMap_.put("OSD_ENTER" , BUTTON_ENTER);
		commandNameMap_.put("OSD_EXIT" , BUTTON_EXIT);
		commandNameMap_.put("OSD_MENU" , BUTTON_MENU);
		commandNameMap_.put("SERVER_QUERY" , SERVER_QUERY);

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
		commandMap_.put(DIRECTION_UP, "OSDUP");
		commandMap_.put(DIRECTION_DOWN, "OSDDOWN");
		commandMap_.put(DIRECTION_LEFT, "OSDLEFT");
		commandMap_.put(DIRECTION_RIGHT, "OSDRIGHT");
		commandMap_.put(BUTTON_ENTER, "OSDENTER");
		commandMap_.put(BUTTON_EXIT, "OSDEXIT");
		commandMap_.put(BUTTON_MENU, "OSDMENU");
		commandMap_.put(SERVER_QUERY, "ECNQSTN");
		
        Set<Integer> keys = commandMap_.keySet();
        for(Iterator<Integer> iterator = keys.iterator(); iterator.hasNext();) {
          Integer key = (Integer)iterator.next();
          commandMapInverse_.put(commandMap_.get(key), key);
        }
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

      //Movement commands

      public static final int BUTTON_0       = eCnt++;
      public static final int BUTTON_1       = eCnt++;
      public static final int BUTTON_2       = eCnt++;
      public static final int BUTTON_3       = eCnt++;
      public static final int BUTTON_4       = eCnt++;
      public static final int BUTTON_5       = eCnt++;
      public static final int BUTTON_6       = eCnt++;
      public static final int BUTTON_7       = eCnt++;
      public static final int BUTTON_8       = eCnt++;
      public static final int BUTTON_9       = eCnt++;
      public static final int DIRECTION_UP   = eCnt++;
      public static final int DIRECTION_DOWN = eCnt++;
      public static final int DIRECTION_LEFT = eCnt++;
      public static final int DIRECTION_RIGHT= eCnt++;
      public static final int BUTTON_ENTER   = eCnt++;
      public static final int BUTTON_EXIT    = eCnt++;
      public static final int BUTTON_MENU    = eCnt++;

      public static final int SERVER_QUERY   = eCnt++;

} // class

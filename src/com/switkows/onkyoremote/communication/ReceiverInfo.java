package com.switkows.onkyoremote.communication;

/***
 * Simple structure to track information and state of an AV receiver
 * I want to keep this out of the Eiscp class, so we can retain the information across
 * application changes. This way we reduce network traffic, since we won't need to
 * re-learn the information on Android orientation changes, etc.
 * @author Trevor
 *
 */
public class ReceiverInfo {
   //Model information (found during 'discovery', never changes
   private final String ipAddr;
   private final int    tcpPort;
   private final String modelName;
   private final String region;
   private final String id;
   
   //Dynamic information
   private float   volume;
   private boolean poweredOn;
   private boolean muted;
   private boolean isConnected;
   private int     source;
   
   public ReceiverInfo(String ip, String port, String model, String region, String id) {
      ipAddr = ip;
      tcpPort = Integer.parseInt(port);
      modelName = model;
      this.region = region;
      this.id = id;
   }
   
   public String getIpAddr() {
      return ipAddr;
   }
   public int getTcpPort() {
      return tcpPort;
   }
   public String getModelName() {
      return modelName;
   }
   public String getRegion() {
      return region;
   }
   public String getId() {
      return id;
   }
   public float getVolume() {
      return volume;
   }

   public void setVolume(float volume) {
      this.volume = volume;
   }

   public boolean isPoweredOn() {
      return poweredOn;
   }

   public void setPoweredOn(boolean poweredOn) {
      this.poweredOn = poweredOn;
   }

   public boolean isMuted() {
      return muted;
   }

   public void setMuted(boolean muted) {
      this.muted = muted;
   }

   public boolean isConnected() {
      return isConnected;
   }

   public void setConnected(boolean isConnected) {
      this.isConnected = isConnected;
   }

   public int getSource() {
      return source;
   }

   public void setSource(int source) {
      this.source = source;
   }

   public String printMe() {
      return "Receiver info: IP="+ipAddr+":"+tcpPort+", model='"+modelName+"', region='"+region+"', id='"+id+"'";         
   }
}
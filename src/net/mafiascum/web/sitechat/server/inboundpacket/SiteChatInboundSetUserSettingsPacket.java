package net.mafiascum.web.sitechat.server.inboundpacket;

public class SiteChatInboundSetUserSettingsPacket extends SiteChatInboundPacket {

  protected boolean compact;
  protected boolean animateAvatars;
  protected String timestamp;
  protected boolean invisible;
  protected boolean emoji;
  protected int sort;
  
  public boolean getCompact() {
    return compact;
  }
  
  public void setCompact(boolean compact) {
    this.compact = compact;
  }
  
  public SiteChatInboundPacketType getType() {
    
    return SiteChatInboundPacketType.setUserSettings;
  }

  public boolean getAnimateAvatars() {
    return animateAvatars;
  }

  public void setAnimateAvatars(boolean animateAvatars) {
    this.animateAvatars = animateAvatars;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }
  
  public boolean getInvisible() {
    return invisible;
  }
  
  public void setInvisible(boolean invisible) {
    this.invisible = invisible;
  }

  public boolean getEmoji() {
    return emoji;
  }

  public void setEmoji(boolean emoji) {
    this.emoji = emoji;
  }

  public int getSort() {
    return sort;
  }

  public void setSort(int sort) {
    this.sort = sort;
  }
}

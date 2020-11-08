package net.mafiascum.web.sitechat.server;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.mafiascum.jdbc.DataObject;
import net.mafiascum.jdbc.IsNewDataObject;
import net.mafiascum.jdbc.StoreDataObjectSQLBuilder;
import net.mafiascum.jdbc.Table;
import net.mafiascum.util.QueryUtil;

@Table(tableName="siteChatUserSettings")
public class SiteChatUserSettings implements IsNewDataObject, DataObject {

  public static final String USER_ID_COLUMN = "user_id";
  public static final String COMPACT_COLUMN = "compact";
  public static final String ANIMATE_AVATARS_COLUMN = "animate_avatars";
  public static final String TIMESTAMP_FORMAT_COLUMN = "timestamp_format";
  public static final String INVISIBLE_COLUMN = "invisible";
  public static final String DISABLE_EMOJI_COLUMN = "disable_emoji";
  public static final String SORT_OPTION_COLUMN = "sort_option";
  
  protected int userId;
  protected boolean compact;
  protected boolean animateAvatars;
  protected String timestampFormat;
  protected boolean invisible;
  protected boolean disable_emoji;
  protected int sort_option;
  
  boolean isNew;

  public SiteChatUserSettings() {
    setIsNew(true);
    setCompact(false);
    setAnimateAvatars(true);
    setTimestampFormat("");
    setInvisible(false);
    setEmoji(false);
    setSortOption(0);
  }
  
  public boolean isNew() {
    return isNew;
  }
  public void setIsNew(boolean isNew) {
    this.isNew = isNew;
  }
  public int getUserId() {
    return userId;
  }
  public void setUserId(int userId) {
    this.userId = userId;
  }
  public boolean getCompact() {
    return compact;
  }
  public void setCompact(boolean compact) {
    this.compact = compact;
  }
  public boolean getAnimateAvatars() {
    return animateAvatars;
  }
  public void setAnimateAvatars(boolean animateAvatars) {
    this.animateAvatars = animateAvatars;
  }
  public String getTimestampFormat() {
    return timestampFormat;
  }
  public void setTimestampFormat(String timestampFormat) {
    this.timestampFormat = timestampFormat;
  }
  public boolean getInvisible() {
    return invisible;
  }
  public void setInvisible(boolean invisible) {
    this.invisible = invisible;
  }
  public boolean getEmoji() {
    return disable_emoji;
  }
  public void setEmoji(boolean emoji) {
    this.disable_emoji = emoji;
  }
  public int getSortOption() {
    return sort_option;
  }
  public void setSortOption(int sortOption) {
    this.sort_option = sortOption;
  }
  
  public void loadFromResultSet(ResultSet resultSet) throws SQLException {
    setIsNew(false);
    
    QueryUtil queryUtil = QueryUtil.get();
    
    setUserId(resultSet.getInt(USER_ID_COLUMN));
    setCompact(queryUtil.getIntBoolean(resultSet, COMPACT_COLUMN));
    setAnimateAvatars(queryUtil.getIntBoolean(resultSet, ANIMATE_AVATARS_COLUMN));
    setTimestampFormat(resultSet.getString(TIMESTAMP_FORMAT_COLUMN));
    setInvisible(queryUtil.getIntBoolean(resultSet, INVISIBLE_COLUMN));
    setEmoji(queryUtil.getIntBoolean(resultSet, DISABLE_EMOJI_COLUMN));
    setSortOption(resultSet.getInt(SORT_OPTION_COLUMN));
  }
  
  public void store(Connection connection) throws SQLException {
    
    QueryUtil queryUtil = QueryUtil.get();
    
    StoreDataObjectSQLBuilder builder = new StoreDataObjectSQLBuilder(queryUtil.getTableName(SiteChatUserSettings.class));

    builder
    .put(COMPACT_COLUMN, getCompact())
    .put(USER_ID_COLUMN, getUserId())
    .put(ANIMATE_AVATARS_COLUMN, getAnimateAvatars())
    .put(TIMESTAMP_FORMAT_COLUMN, getTimestampFormat())
    .put(INVISIBLE_COLUMN, getInvisible())
    .put(DISABLE_EMOJI_COLUMN, getEmoji())
    .put(SORT_OPTION_COLUMN, getSortOption())
    .putPrimaryKey(USER_ID_COLUMN, isNew() ? null : getUserId());
    
    builder.execute(connection, this);
    
    setIsNew(false);
  }
}

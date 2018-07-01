package net.mafiascum.web.sitechat.server;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import net.mafiascum.jdbc.BatchInsertStatement;
import net.mafiascum.phpbb.log.ForumLog;
import net.mafiascum.phpbb.usergroup.UserGroup;
import net.mafiascum.util.MSUtil;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversation;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationMessage;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationType;

public class SiteChatUtil extends MSUtil {

  private static SiteChatUtil INSTANCE;
  
  private SiteChatUtil() {
    
  }
  
  public static synchronized SiteChatUtil get() {
    
    if(INSTANCE == null) {
      
      INSTANCE = new SiteChatUtil();
      INSTANCE.init();
    }
    
    return INSTANCE;
  }
  
  public final int MAX_SITE_CHAT_CONVERSATION_MESSAGE_LENGTH = 255;
  public final int MAX_SITE_CHAT_CONVERSATION_NAME_LENGTH = 40;
  public final int MAX_MESSAGES_PER_CONVERSATION_CACHE = 100;
  public final int BANNED_USERS_GROUP_ID = 13662;
  
  protected Logger logger = Logger.getLogger(SiteChatUtil.class.getName());
  
  public UserGroup getUserGroup(Connection connection, int userId, int groupId) throws SQLException {
    return queryUtil.executeStatement(connection, statement -> queryUtil.retrieveDataObject(statement, "user_id=" + userId + " AND group_id=" + groupId, UserGroup.class));
  }
  
  public void deleteUserGroup(Connection connection, int userId, int groupId) throws SQLException {
    String sql = " DELETE FROM " + queryUtil.getTableName(UserGroup.class)
               + " WHERE " + sqlUtil.escapeQuoteColumnName(UserGroup.USER_ID_COLUMN) + "=" + userId
               + " AND " + sqlUtil.escapeQuoteColumnName(UserGroup.GROUP_ID_COLUMN) + "=" + groupId;
    
    queryUtil.executeStatement(connection, statement -> statement.executeUpdate(sql));
  }
  
  public void putUserGroup(Connection connection, UserGroup userGroup) throws SQLException {
    userGroup.store(connection);
  }
  
  public Map<Integer, SiteChatUser> loadSiteChatUserMap(Connection connection) throws SQLException {
    return queryUtil.executeStatement(connection, statement -> queryUtil.retrieveDataObjectMap(statement, "1", SiteChatUser.class, SiteChatUser::getId));
  }
  
  public List<SiteChatConversation> getSiteChatConversations(Connection connection) throws SQLException {
    return queryUtil.executeStatement(connection, statement -> queryUtil.retrieveDataObjectList(statement, "1", SiteChatConversation.class));
  }
  
  public SiteChatConversation getSiteChatConversation(Connection connection, int siteChatConversationId) throws SQLException {
    return queryUtil.executeStatement(connection, statement -> queryUtil.retrieveDataObject(statement, "id=" + siteChatConversationId, SiteChatConversation.class));
  }
  
  public SiteChatConversation getSiteChatConversation(Connection connection, String siteChatConversationName) throws SQLException {
    return queryUtil.executeStatement(connection, statement -> queryUtil.retrieveDataObject(statement, "name=" + sqlUtil.escapeQuoteString(siteChatConversationName), SiteChatConversation.class));
  }
  
  public void putSiteChatConversation(Connection connection, SiteChatConversation siteChatConversation) throws SQLException {
    siteChatConversation.store(connection);
  }
  
  public boolean authenticateUserLogin(Connection connection, int userId, String sessionId) throws SQLException {
    
    return queryUtil.executeStatement(connection, statement -> {
      String sql;
      
      sql = " SELECT 1"
          + " FROM phpbb_sessions"
          + " WHERE session_id = " + sqlUtil.escapeQuoteString(sessionId)
          + " AND session_user_id = " + userId;
    
      return queryUtil.hasAtLeastOneRow(statement, sql);
    });
  }

  public void putNewSiteChatConversationMessages(List<SiteChatConversationMessage> siteChatConversationMessages, BatchInsertStatement batchInsertStatement) throws SQLException {
    
    if(siteChatConversationMessages.isEmpty())
      return;
    
    siteChatConversationMessages.get(0).setBatchInsertStatementColumns(batchInsertStatement);
    
    batchInsertStatement.start();
    
    for(SiteChatConversationMessage siteChatConversationMessage : siteChatConversationMessages) {
      
      siteChatConversationMessage.addToBatchInsertStatement(batchInsertStatement);
    }
    
    batchInsertStatement.finish();
  }
  
  public void putNewSiteChatConversationMessages(Connection connection, List<SiteChatConversationMessage> siteChatConversationMessages) throws SQLException {
    for(SiteChatConversationMessage message : siteChatConversationMessages) {
      try {
        BatchInsertStatement batchInsertStatement = new BatchInsertStatement(connection, "siteChatConversationMessage", 1);
        putNewSiteChatConversationMessages(new ArrayList<>(Arrays.asList(message)), batchInsertStatement);
      }
      catch(Exception exception) {
        logger.error("Error storing message.", exception);
      }
    }
  }
  
  public void putSiteChatConversationMessage(Connection connection, SiteChatConversationMessage message) throws SQLException {
    message.store(connection);
  }
  
  public int getTopSiteChatConversationMessageId(Connection connection) throws SQLException {
    
    String sql = " SELECT MAX(id)"
               + " FROM `" + queryUtil.getTableName(SiteChatConversationMessage.class) + "`";
    
    return queryUtil.getSingleIntValueResult(connection, sql);
  }
  
  public int getNumberOfSiteChatConversationMessages(Connection connection) throws SQLException {
    
    String sql = " SELECT COUNT(*)"
               + " FROM `" + queryUtil.getTableName(SiteChatConversationMessage.class) + "`";
    
    return queryUtil.getSingleIntValueResult(connection, sql);
  }
  
  public List<UserGroup> getBanUserGroups(Connection connection) throws SQLException {
    String criteria = sqlUtil.escapeQuoteColumnName(UserGroup.GROUP_ID_COLUMN) + "=" + BANNED_USERS_GROUP_ID;
    return queryUtil.retrieveDataObjectList(connection, criteria, UserGroup.class);
  }
  
  public void addUserToUserGroup(Connection connection, int userId, int userGroupId, Long autoRemoveTime) throws SQLException {
    new UserGroup(true, userGroupId, userId, false, false, autoRemoveTime == null ? 0 : autoRemoveTime.intValue()).storeInsertIgnore(connection, true);
  }
  
  public List<SiteChatConversationMessage> loadSiteChatConversationMessagesForConversation(Connection connection, int siteChatConversationId, int numberToLoad, Integer oldestMessageId) throws SQLException {
    
    String criteria = " " + sqlUtil.escapeQuoteColumnName(SiteChatConversationMessage.SITE_CHAT_CONVERSATION_ID_COLUMN) + "=" + siteChatConversationId
                    + (oldestMessageId == null ? "" : (" AND " + sqlUtil.escapeQuoteColumnName(SiteChatConversationMessage.ID_COLUMN) + " < " + oldestMessageId));
    
    return queryUtil.retrieveDataObjectList(connection, criteria, "id DESC", String.valueOf(numberToLoad), SiteChatConversationMessage.class, false);
  }

  public List<SiteChatConversationMessage> loadSiteChatConversationMessagesForPrivateConversation(Connection connection, int userId1, int userId2, int numberToLoad, Integer oldestMessageId) throws SQLException {
    
    String criteria =     "((recipient_user_id=" + userId1 + " AND user_id=" + userId2 + ")"
                    + " OR (recipient_user_id=" + userId2 + " AND user_id=" + userId1 + "))"
                    + (oldestMessageId == null ? "" : (" AND id < " + oldestMessageId));
    
    return queryUtil.retrieveDataObjectList(connection.createStatement(), criteria, "id+0 DESC", String.valueOf(numberToLoad), SiteChatConversationMessage.class, false);
  }
  
  public int getConversationUniqueIdentifier(String conversationUniqueKey) {
    
    return Integer.valueOf(conversationUniqueKey.substring(1));
  }
  
  public char getConversationSymbol(String conversationUniqueKey) {
    
    return conversationUniqueKey.charAt(0);
  }
  
  public SiteChatConversationType getSiteChatConversationTypeBySymbol(char symbol) {
    
    Iterator<SiteChatConversationType> iter = SiteChatConversationType.getSetIterator();
    while(iter.hasNext()) {
      
      SiteChatConversationType siteChatConversationType = iter.next();
      if(siteChatConversationType.getSymbol() == symbol) {
        
        return siteChatConversationType;
      }
    }
    
    return null;
  }
  
  public String getPrivateMessageHistoryKey(int userId1, int userId2) {
    
    return userId1 < userId2 ? userId1 + "_" + userId2 : userId2 + "_" + userId1;
  }
  
  public String generateConversationAuthCode(int siteChatUserId, int siteChatConversationId, String siteChatConversationPasswordSha1) {
    
    return stringUtil.getSHA1(String.valueOf(siteChatUserId) + String.valueOf(siteChatConversationId) + siteChatConversationPasswordSha1);
  }
  
  public void putSiteChatUserSettings(Connection connection, SiteChatUserSettings userSettings) throws SQLException {
    userSettings.store(connection);
  }
  
  public SiteChatUserSettings getSiteChatUserSettings(Connection connection, int userId) throws SQLException {
    String criteria = sqlUtil.escapeQuoteColumnName(SiteChatUserSettings.USER_ID_COLUMN) + "=" + userId;
    return queryUtil.retrieveDataObject(connection, criteria, SiteChatUserSettings.class);
  }
  
  public List<SiteChatUserSettings> getSiteChatUserSettingsList(Connection connection) throws SQLException {
    return queryUtil.retrieveDataObjectList(connection, null, SiteChatUserSettings.class);
  }
  
  public void putSiteChatIgnore(Connection connection, SiteChatIgnore ignore) throws SQLException {
    ignore.store(connection);
  }
  
  public void removeSiteChatIgnore(Connection connection, int userId, int ignoredUserId) throws SQLException {
    String sql = " DELETE FROM " + queryUtil.getEscapedTableName(SiteChatIgnore.class)
               + " WHERE " + sqlUtil.escapeQuoteColumnName(SiteChatIgnore.USER_ID_COLUMN) + "=" + userId
               + " AND " + sqlUtil.escapeQuoteColumnName(SiteChatIgnore.IGNORED_USER_ID_COLUMN) + "=" + ignoredUserId;
    queryUtil.executeStatementNoResult(connection, statement -> statement.executeUpdate(sql));
  }
  
  public List<SiteChatIgnore> getSiteChatIgnores(Connection connection) throws SQLException {
    return queryUtil.retrieveDataObjectList(connection, null, SiteChatIgnore.class);
  }
  
  public void putForumLog(Connection connection, ForumLog forumLog) throws SQLException {
    forumLog.store(connection);
  }
  
  public List<UserGroup> getUserGroups(Connection connection) throws SQLException {
    return queryUtil.retrieveDataObjectList(connection, null, UserGroup.class);
  }
  
  public void createTablesIfNecessary(Connection connection) throws SQLException {
    String sql;
    
    sql = "CREATE TABLE IF NOT EXISTS " + queryUtil.getEscapedTableName(SiteChatUserSettings.class) + " (" + 
          sqlUtil.escapeQuoteColumnName(SiteChatUserSettings.USER_ID_COLUMN) + " mediumint(8) unsigned NOT NULL," + 
          sqlUtil.escapeQuoteColumnName(SiteChatUserSettings.COMPACT_COLUMN) + " tinyint(3) unsigned NOT NULL," + 
          sqlUtil.escapeQuoteColumnName(SiteChatUserSettings.ANIMATE_AVATARS_COLUMN) + " tinyint(3) unsigned NOT NULL," + 
          sqlUtil.escapeQuoteColumnName(SiteChatUserSettings.TIMESTAMP_FORMAT_COLUMN) + " varchar(32) COLLATE utf8_bin NOT NULL DEFAULT ''," + 
          sqlUtil.escapeQuoteColumnName(SiteChatUserSettings.INVISIBLE_COLUMN) + " tinyint(3) unsigned NOT NULL DEFAULT '0'," + 
          sqlUtil.escapeQuoteColumnName(SiteChatUserSettings.DISABLE_EMOJI_COLUMN) + " tinyint(3) unsigned NOT NULL DEFAULT '0'," + 
          sqlUtil.escapeQuoteColumnName(SiteChatUserSettings.SORT_OPTION_COLUMN) + " tinyint(3) unsigned NOT NULL DEFAULT '0'," + 
          "  PRIMARY KEY (" + sqlUtil.escapeQuoteColumnName(SiteChatUserSettings.USER_ID_COLUMN) + ")" + 
          " ) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin;";
    
    queryUtil.executeUpdate(connection, sql);
    
    sql = "CREATE TABLE IF NOT EXISTS " + queryUtil.getEscapedTableName(SiteChatIgnore.class) + " (" + 
        sqlUtil.escapeQuoteColumnName(SiteChatIgnore.ID_COLUMN) + " int(11) unsigned NOT NULL AUTO_INCREMENT," + 
        sqlUtil.escapeQuoteColumnName(SiteChatIgnore.USER_ID_COLUMN) + " mediumint(8) unsigned NOT NULL," + 
        sqlUtil.escapeQuoteColumnName(SiteChatIgnore.IGNORED_USER_ID_COLUMN) + " mediumint(8) unsigned NOT NULL," + 
        sqlUtil.escapeQuoteColumnName(SiteChatIgnore.CREATED_DATETIME_COLUMN) + " datetime NOT NULL," + 
        "  PRIMARY KEY (" + sqlUtil.escapeQuoteColumnName(SiteChatIgnore.ID_COLUMN) + ")," + 
        "  UNIQUE KEY `user_id` (" + sqlUtil.escapeQuoteColumnName(SiteChatIgnore.USER_ID_COLUMN) + "," + sqlUtil.escapeQuoteColumnName(SiteChatIgnore.IGNORED_USER_ID_COLUMN) + ")" + 
        ") ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin;";
    
    queryUtil.executeUpdate(connection, sql);
    
    sql = "CREATE TABLE IF NOT EXISTS " + queryUtil.getEscapedTableName(SiteChatConversationMessage.class) + " (" + 
        sqlUtil.escapeQuoteColumnName(SiteChatConversationMessage.ID_COLUMN) + " int(11) unsigned NOT NULL AUTO_INCREMENT," + 
        sqlUtil.escapeQuoteColumnName(SiteChatConversationMessage.SITE_CHAT_CONVERSATION_ID_COLUMN) + " int(11) unsigned DEFAULT NULL," + 
        sqlUtil.escapeQuoteColumnName(SiteChatConversationMessage.USER_ID_COLUMN) + " mediumint(8) unsigned NOT NULL," + 
        sqlUtil.escapeQuoteColumnName(SiteChatConversationMessage.CREATED_DATETIME_COLUMN) + " datetime NOT NULL," + 
        sqlUtil.escapeQuoteColumnName(SiteChatConversationMessage.MESSAGE_COLUMN) + " varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL," + 
        sqlUtil.escapeQuoteColumnName(SiteChatConversationMessage.RECIPIENT_USER_ID_COLUMN) + " mediumint(8) unsigned DEFAULT NULL," + 
        "  PRIMARY KEY (" + sqlUtil.escapeQuoteColumnName(SiteChatConversationMessage.ID_COLUMN) + ")," + 
        "  KEY `user_id` (" + sqlUtil.escapeQuoteColumnName(SiteChatConversationMessage.USER_ID_COLUMN) + ")," + 
        "  KEY `site_chat_conversation_id` (" + sqlUtil.escapeQuoteColumnName(SiteChatConversationMessage.SITE_CHAT_CONVERSATION_ID_COLUMN) + ")" + 
        ") ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin;";
    
    queryUtil.executeUpdate(connection, sql);
    
    sql = "CREATE TABLE IF NOT EXISTS " + queryUtil.getEscapedTableName(SiteChatConversation.class) + " (" + 
        sqlUtil.escapeQuoteColumnName(SiteChatConversation.ID_COLUMN) + " int(11) unsigned NOT NULL AUTO_INCREMENT," + 
        sqlUtil.escapeQuoteColumnName(SiteChatConversation.NAME_COLUMN) + " varchar(40) COLLATE utf8_bin NOT NULL," + 
        sqlUtil.escapeQuoteColumnName(SiteChatConversation.CREATED_DATETIME_COLUMN) + " datetime NOT NULL," + 
        sqlUtil.escapeQuoteColumnName(SiteChatConversation.CREATED_BY_USER_ID_COLUMN) + " mediumint(8) unsigned NOT NULL," + 
        sqlUtil.escapeQuoteColumnName(SiteChatConversation.PASSWORD_COLUMN) + " varchar(40) COLLATE utf8_bin DEFAULT NULL," + 
        "  PRIMARY KEY (" + sqlUtil.escapeQuoteColumnName(SiteChatConversation.ID_COLUMN) + ")," + 
        "  UNIQUE KEY `name` (" + sqlUtil.escapeQuoteColumnName(SiteChatConversation.NAME_COLUMN) + ")" + 
        ") ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin;";
    
    queryUtil.executeUpdate(connection, sql);
  }
}

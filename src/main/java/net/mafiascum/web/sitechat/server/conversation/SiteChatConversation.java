package net.mafiascum.web.sitechat.server.conversation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import net.mafiascum.jdbc.BatchInsertStatement;
import net.mafiascum.jdbc.BatchInsertable;
import net.mafiascum.jdbc.DataObject;
import net.mafiascum.jdbc.StoreDataObjectSQLBuilder;
import net.mafiascum.jdbc.Table;
import net.mafiascum.util.QueryUtil;
import net.mafiascum.web.misc.DataObjectWithIntID;

@Table(tableName="siteChatConversation")
public class SiteChatConversation extends DataObjectWithIntID implements DataObject, BatchInsertable {

  public static final String ID_COLUMN = "id";
  public static final String NAME_COLUMN = "name";
  public static final String CREATED_DATETIME_COLUMN = "created_datetime";
  public static final String CREATED_BY_USER_ID_COLUMN = "created_by_user_id";
  public static final String PASSWORD_COLUMN = "password";
  
  protected Date createdDatetime;
  protected int createdByUserId;
  protected String name;
  protected String password;
  
  public SiteChatConversation(int id, Date createdDatetime, int createdByUserId, String name, String password) {
    this(createdDatetime, createdByUserId, name, password);
    setId(id);
  }
  
  public SiteChatConversation(Date createdDatetime, int createdByUserId, String name, String password) {
    this();
    setCreatedDatetime(createdDatetime);
    setCreatedByUserId(createdByUserId);
    setName(name);
    setPassword(password);
  }
  
  public SiteChatConversation() {
    
    id = NEW;
  }
  
  public Date getCreatedDatetime() {
    return createdDatetime;
  }

  public void setCreatedDatetime(Date createdDatetime) {
    this.createdDatetime = createdDatetime;
  }

  public int getCreatedByUserId() {
    return createdByUserId;
  }

  public void setCreatedByUserId(int createdByUserId) {
    this.createdByUserId = createdByUserId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public String getPassword() {
    return password;
  }
  
  public void setPassword(String password) {
    this.password = password;
  }
  
  public void loadFromResultSet(ResultSet resultSet) throws SQLException {
    
    setId(resultSet.getInt(ID_COLUMN));
    setName(resultSet.getString(NAME_COLUMN));
    setCreatedDatetime(resultSet.getTimestamp(CREATED_DATETIME_COLUMN));
    setCreatedByUserId(resultSet.getInt(CREATED_BY_USER_ID_COLUMN));
    setPassword(resultSet.getString(PASSWORD_COLUMN));
  }
  
  public void store(Connection connection) throws SQLException {
    
    QueryUtil.get().executeStatement(connection, statement -> {
      
      StoreDataObjectSQLBuilder builder = new StoreDataObjectSQLBuilder(QueryUtil.get().getTableName(getClass()));

      builder.put(NAME_COLUMN, getName())
             .put(CREATED_DATETIME_COLUMN, getCreatedDatetime())
             .put(CREATED_BY_USER_ID_COLUMN, getCreatedByUserId())
             .put(PASSWORD_COLUMN, getPassword())
             .putPrimaryKey(ID_COLUMN, isNew() ? null : getId());
      
      builder.execute(statement, this);
      
      return null;
    });
  }
  
  public void setBatchInsertStatementColumns(BatchInsertStatement batchInsertStatement) throws SQLException {
    
    batchInsertStatement.addField(ID_COLUMN);
    batchInsertStatement.addField(NAME_COLUMN);
    batchInsertStatement.addField(CREATED_DATETIME_COLUMN);
    batchInsertStatement.addField(CREATED_BY_USER_ID_COLUMN);
    batchInsertStatement.addField(PASSWORD_COLUMN);
  }
  
  public void addToBatchInsertStatement(BatchInsertStatement batchInsertStatement) throws SQLException {
    
    batchInsertStatement.beginEntry();
    
    batchInsertStatement.putInteger(isNew() ? null : getId());
    batchInsertStatement.putString(getName());
    batchInsertStatement.putDate(getCreatedDatetime());
    batchInsertStatement.putInt(getCreatedByUserId());
    batchInsertStatement.putString(getPassword());
    
    batchInsertStatement.endEntry();
  }
}

package net.mafiascum.provider;

import java.sql.Connection;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.mafiascum.environment.Environment;
import net.mafiascum.jdbc.ConnectionPool;
import net.mafiascum.util.MiscUtil;
import net.mafiascum.util.StringUtil;

import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Provider {

  protected String mysqlUrl;
  protected String mysqlUsername;
  protected String mysqlPassword;
  protected String docRoot;
  protected Environment environment;
  protected int maxConnections;
  
  protected ConnectionPool connectionPool = null;
  protected MiscUtil miscUtil;
  protected StringUtil stringUtil;
  
  protected final long MILLIS_BETWEEN_CONNECTION_RENEWAL = 300000;
  
  public Provider() {
    setMiscUtil(MiscUtil.get());
    setStringUtil(StringUtil.get());
  }
  
  public Connection getConnection() throws Exception {

    return connectionPool.openConnection();
  }
  
  public void setupConnectionPool() {
    
    connectionPool = new ConnectionPool(getMaxConnections(), mysqlUrl, mysqlUsername, mysqlPassword);
    connectionPool.setup();
  }
  private static final Logger logger = LogManager.getLogger(Provider.class.getName());
  public void loadConfiguration(String configurationFilePath) throws Exception {
    
    logger.info("CONFIG PATH: " + configurationFilePath);
    Configuration config = miscUtil.loadPropertiesResource(configurationFilePath);

    mysqlUrl = config.getString("Mysql.Main.Url");
    mysqlUsername = config.getString("Mysql.Main.Username");
    mysqlPassword = config.getString("Mysql.Main.Password");
    maxConnections = config.getInt("Mysql.Main.MaxConnections");
    environment = Environment.getEnumByAbbreviatedName(config.getString("Environment"));
    
    if(environment == null)
      environment = Environment.prod;
  }
  
  public void shutdownConnectionPool() {
    connectionPool.shutdown();
  }
  
  public String getDocRoot() {
    
    return docRoot;
  }
  
  public void setDocRoot(String docRoot) {
    
    this.docRoot = docRoot;
  }
  
  public int getMaxConnections() {
    
    return maxConnections;
  }
  
  public void setMaxConnections(int maxConnections) {
    
    this.maxConnections = maxConnections;
  }

  public MiscUtil getMiscUtil() {
    return miscUtil;
  }

  public void setMiscUtil(MiscUtil miscUtil) {
    this.miscUtil = miscUtil;
  }

  public StringUtil getStringUtil() {
    return stringUtil;
  }

  public void setStringUtil(StringUtil stringUtil) {
    this.stringUtil = stringUtil;
  }

  public Environment getEnvironment() {
    return environment;
  }

  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  public String getMysqlUrl() {
    return mysqlUrl;
  }

  public void setMysqlUrl(String mysqlUrl) {
    this.mysqlUrl = mysqlUrl;
  }
  
  public String getDatabaseName() {
    
    Pattern pattern = Pattern.compile("://.*?/([^?]*).*$");
    Matcher matcher = pattern.matcher(mysqlUrl);
    
    if(matcher.find()) {
      return matcher.group(1);
    }
    
    return null;
  }
}

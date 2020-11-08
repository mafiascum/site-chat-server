package net.mafiascum.provider;

import org.junit.Assert;
import org.junit.Test;

public class ProviderTest {

  @Test
  public void testGetDatabaseName() {
    
    Provider provider = new Provider();
    provider.setMysqlUrl("jdbc:mysql://msdev/ms_phpbb3?useUnicode=yes&characterEncoding=UTF-8&jdbcCompliantTruncation=false&zeroDateTimeBehavior=convertToNull&allowMultiQueries=true&rewriteBatchedStatements=true");
    
    Assert.assertEquals("ms_phpbb3", provider.getDatabaseName());
    
    provider.setMysqlUrl("jdbc:mysql://msdev/ms_phpbb3");
    Assert.assertEquals("ms_phpbb3", provider.getDatabaseName());
  }
}

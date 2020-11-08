package net.mafiascum.testcase;

import java.util.Map;

import net.mafiascum.web.sitechat.server.SiteChatUser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FirstTestCase extends TestCase {
  
  private static final Logger logger = LogManager.getLogger(FirstTestCase.class.getName());
  
  public void execute() throws Exception {
    
    queryUtil.executeConnectionNoResult(provider, connection -> {
      
      //Test getting user map.
      Map<Integer, SiteChatUser> siteChatUserMap = siteChatUtil.loadSiteChatUserMap(provider.getConnection());
      logger.info("Site Chat User Map Size: " + siteChatUserMap.size());
      siteChatUserMap = null;
      
    });
  }
}

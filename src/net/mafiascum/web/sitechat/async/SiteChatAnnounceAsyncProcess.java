package net.mafiascum.web.sitechat.async;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.log4j.Logger;

import net.mafiascum.util.MiscUtil;
import net.mafiascum.util.StringUtil;
import net.mafiascum.web.sitechat.server.SiteChatMessageProcessor;
import net.mafiascum.web.sitechat.server.SiteChatUser;
import net.mafiascum.web.sitechat.server.SiteChatUtil;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationMessage;
import net.mafiascum.web.sitechat.server.conversation.SiteChatConversationWithUserList;
import net.mafiascum.web.sitechat.server.outboundpacket.SiteChatOutboundNewMessagePacket;

public class SiteChatAnnounceAsyncProcess extends SiteChatAsyncProcess {
  
  private static final Logger logger = Logger.getLogger(SiteChatAnnounceAsyncProcess.class.getName());

  public SiteChatAnnounceAsyncProcess(long miliSecondsBetweenRun) {
    super(miliSecondsBetweenRun);
  }
  
  protected void fillOperations() {
    operations.add(new SiteChatAsyncProcessOperation(true, processor -> {
      
      SiteChatUser user = processor.getSiteChatUser("SITECHAT ANNOUNCE BOT");
      SiteChatConversationWithUserList conversationWithUserList = processor.getConversationWithUserList("Lobby");
      
      if(user == null)
        return;
      
      if(conversationWithUserList == null)
        return;
      
      String message = pickMessage();
      
      if(message == null)
        return;
      
      process(processor, user.getId(), conversationWithUserList.getSiteChatConversation().getId(), message);
    }));
  }
  
  protected String pickMessage() throws FileNotFoundException {
    
    final String FILE_PATH = "/tmp/site-chat-messages.txt";
    File file = new File(FILE_PATH);
    
    if(!file.exists())
      return null;
    
    Scanner scanner = new Scanner(file);
    String content = scanner.useDelimiter("\\Z").next();
    scanner.close();
    
    List<String> messageList = StringUtil.get().buildListFromString(content, "\n");
    
    if(messageList.isEmpty())
      return null;
    
    int numberOfLines = messageList.size();
    int index = MiscUtil.get().getRandomNumberInRange(0, numberOfLines - 1);
    
    return messageList.get(index);
  }
  
  public void process(SiteChatMessageProcessor processor, final int USER_ID, int conversationId, String message) throws Exception {

    SiteChatUtil siteChatUtil = SiteChatUtil.get();
    //SiteChatInboundSendMessagePacket sendMessagePacket = new Gson().fromJson(siteChatInboundPacketJson, SiteChatInboundSendMessagePacket.class);
    SiteChatConversationWithUserList siteChatConversationWithUserList = null;
    
    processor.updateUserActivity(USER_ID);
    processor.updateUserNetworkActivity(USER_ID);
    
    siteChatConversationWithUserList = processor.getSiteChatConversationWithUserList(conversationId);
    
    if(siteChatConversationWithUserList == null) {
    
      logger.error("No Site Chat Conversation could be found.");
      return;//Conversation does not exist.
    }
    
    //Truncate long messages.
    if(message.length() > siteChatUtil.MAX_SITE_CHAT_CONVERSATION_MESSAGE_LENGTH) {
      
      message = (message.substring(0, siteChatUtil.MAX_SITE_CHAT_CONVERSATION_MESSAGE_LENGTH));
    }
    
    
    SiteChatConversationMessage messageObject = processor.recordSiteChatConversationMessage(USER_ID, conversationId, null, message);
    
    messageObject = messageObject.clone();
    //message.setMessage(stringUtil.escapeHTMLCharacters(message.getMessage()));
      
    sendOutboundMessage(siteChatConversationWithUserList, USER_ID, processor, null, messageObject);
  }
  
  protected void sendOutboundMessage(
      SiteChatConversationWithUserList siteChatConversationWithUserList,
      int userId,
      SiteChatMessageProcessor processor,
      SiteChatUser siteChatRecipientUser,
      SiteChatConversationMessage message
  ) throws IOException {
    
    Set<Integer> sendToUserIdSet;
    
    //Send the message to all users in the conversation(including the user who sent it).
    SiteChatOutboundNewMessagePacket siteChatOutboundNewMessagePacket = new SiteChatOutboundNewMessagePacket();
    siteChatOutboundNewMessagePacket.setSiteChatConversationMessage(message);
    
    //Build recipient user ID set.
    if(siteChatConversationWithUserList != null)
      sendToUserIdSet = siteChatConversationWithUserList.getUserIdSet();
    else {
      sendToUserIdSet = new HashSet<Integer>();
      sendToUserIdSet.add(siteChatRecipientUser.getId());
      sendToUserIdSet.add(userId);
    }
    
    processor.sendOutboundPacketToUsers(sendToUserIdSet, siteChatOutboundNewMessagePacket, null);    
  }
}

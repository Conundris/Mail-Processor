package com.jbr.pbm.mail;

import com.jbr.pbm.serverProperties.MailProperties;
import com.sun.mail.imap.protocol.FLAGS;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.mail.ImapIdleChannelAdapter;
import org.springframework.integration.mail.ImapMailReceiver;

@Configuration
@EnableIntegration
public class Mail {

  private static final Logger logger = LoggerFactory.getLogger(Mail.class);

  @Autowired
  private EmailParser emailParser;

  @Autowired
  private MailProperties mailProperties;

  @Bean
  public ImapMailReceiver mailReceiver() {
    ImapMailReceiver mailReceiver = new ImapMailReceiver(mailProperties.getMailReceiverString());
    mailReceiver.setJavaMailProperties(mailProperties.getIMAPProperties());
    mailReceiver.setShouldDeleteMessages(false);
    mailReceiver.setShouldMarkMessagesAsRead(true);
    //Default UserFlag: spring-integration-mail-adapter
    mailReceiver.setUserFlag("pbm-processed");

    return mailReceiver;
  }

  @Bean
  public ImapIdleChannelAdapter mailAdapter() {
    ImapIdleChannelAdapter imapIdleChannelAdapter = new ImapIdleChannelAdapter(mailReceiver());
    imapIdleChannelAdapter.setAutoStartup(true);
    imapIdleChannelAdapter.setOutputChannel(directChannel());
    imapIdleChannelAdapter.setShouldReconnectAutomatically(true);
    return imapIdleChannelAdapter;
  }

  @Bean
  public DirectChannel directChannel() {
    DirectChannel directChannel = new DirectChannel();

    directChannel.subscribe(message -> {
      MimeMessage mimeMessage = (MimeMessage) message.getPayload();
      Folder fallbackFolder = mimeMessage.getFolder();
      try (Folder folder = mimeMessage.getFolder()) {
        //https://jira.spring.io/browse/INT-4299
        folder.open(Folder.READ_WRITE);
        fallbackFolder = folder;

        Message reopenedMessage = getMessageWorkaround(folder, mimeMessage);
        MailMessage processedMessage = emailParser.processMail(reopenedMessage);
        if (processedMessage != null) {
          emailParser.sendMessage(reopenedMessage);
        }
      } catch (MessagingException e) {
        logger.error("Exception Caught: {}", e.getMessage(), e);
        logger.debug("{}", "Trying to reset received mail.");
        // Delete Flags on received message, to reset its status.
        // Flags deleted: Seen, pbm-processed
        try {
          fallbackFolder.open(Folder.READ_WRITE);
          Message reopenedMessage = getMessageWorkaround(fallbackFolder, mimeMessage);
          reopenedMessage.setFlags(reopenedMessage.getFlags(), false);
        } catch (MessagingException e1) {
          logger.debug("Email couldn't be reset", e);
          logger.error("Email couldn't be processed", e);
        }
      }
    });
    return directChannel;
  }

  public Message getMessageWorkaround(Folder folder, MimeMessage originalMessage)
      throws MessagingException {

    if (originalMessage.getMessageNumber() == 0) {
      FLAGS flags = (FLAGS) originalMessage.getFlags();
      return folder.getMessage(flags.msgno);
    } else {
      return folder.getMessage(originalMessage.getMessageNumber());
    }
  }
}

package com.jbr.pbm.mail;

import com.jbr.pbm.serverProperties.MailProperties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(MailProperties.class)
public class MailSender {

  private static final Logger logger = LoggerFactory.getLogger(MailSender.class);

  @Autowired
  private MailProperties mailProperties;

  public void informInvalidSubjectLine(Message message) {

    Session session = Session.getInstance(mailProperties.getSMTPProperties(),
        new javax.mail.Authenticator() {
          @Override
          protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(mailProperties.getEmailAddress(), mailProperties.getPassword());
          }
        });

    try {
      Message mailMessage = new MimeMessage(session);
      mailMessage.setFrom(new InternetAddress(mailProperties.getEmailAddress()));
      mailMessage.setRecipients(Message.RecipientType.TO,
          InternetAddress.parse(message.getFrom()[0].toString()));
      mailMessage.setSubject("!Attention - Message Processing Failed");
      mailMessage.setText("The following mail subject failed to process. \n\n- " + message.getSubject());

      Transport.send(mailMessage);

    } catch (MessagingException e) {
      logger.warn("Invalid subject line E-mail couldn't be sent.", e);
      logger.error("Exception caught: {}", e.getMessage(), e);
    }
  }
}
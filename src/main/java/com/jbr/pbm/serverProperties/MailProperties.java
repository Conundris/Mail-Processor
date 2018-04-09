package com.jbr.pbm.serverProperties;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pbm")
@EnableConfigurationProperties
public class MailProperties {

  @Value("${pbm.mail.store.protocol}")
  private String protocol;
  @Value("${pbm.mail.store.protocolV}")
  private String protocolV;
  @Value("${pbm.mail.imaps.host}")
  private String host;
  @Value("${pbm.mail.hostV}")
  private String hostV;
  @Value("${pbm.mail.imaps.port}")
  private String port;
  @Value("${pbm.mail.portV}")
  private String portV;
  @Value("${pbm.mail.imaps.ssl.enable}")
  private String sslEnable;
  @Value("${pbm.mail.imaps.ssl.enableV}")
  private String sslEnableV;
  @Value("${pbm.mail.imaps.timeout}")
  private String timeout;
  @Value("${pbm.mail.imaps.timeoutV}")
  private String timeoutV;
  @Value("${pbm.mail.username}")
  private String username;
  @Value("${pbm.mail.emailAddress}")
  private String emailAddress;
  @Value("${pbm.mail.password}")
  private String pass;
  @Value("${pbm.mail.smtp.auth}")
  private String smtpAuth;
  @Value("${pbm.mail.smtp.starttls.enable}")
  private String enable;
  @Value("${pbm.mail.smtp.host}")
  private String smtpHost;
  @Value("${pbm.smtp.port}")
  private String smtpPort;

  @Bean
  public Properties getIMAPProperties() {
    Properties properties = new Properties();
    properties.put(protocol, protocolV);
    properties.put(host, hostV);
    properties.put(portV, portV);
    properties.put(sslEnable, sslEnableV);
    properties.put(timeout, timeoutV);
    return properties;
  }

  @Bean
  public Properties getSMTPProperties() {
    Properties properties = new Properties();
    properties.put("mail.smtp.auth", smtpAuth);
    properties.put("mail.smtp.starttls.enable", enable);
    properties.put("mail.smtp.host", smtpHost);
    properties.put("mail.smtp.port", smtpPort);
    return properties;
  }

  @Bean
  public String getUsername() {
    return username;
  }

  @Bean
  public String getPassword() {
    return pass;
  }

  @Bean
  public String getEmailAddress() { return emailAddress; }

  @Bean
  public String getMailReceiverString() {
    String receiverString =
        protocolV
            + "://"
            + username
            + ":"
            + pass
            + "@"
            + hostV
            + ":"
            + portV
            + "/INBOX";

    return receiverString;
  }
}
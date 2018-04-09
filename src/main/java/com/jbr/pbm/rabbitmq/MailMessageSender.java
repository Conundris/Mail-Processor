package com.jbr.pbm.rabbitmq;

import com.jbr.pbm.mail.MailMessage;
import com.google.gson.Gson;
import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(RabbitMqConfig.class)
public class MailMessageSender {

  private static final Logger logger = LoggerFactory.getLogger(MailMessageSender.class);

  private final RabbitTemplate rabbitTemplate;

  private SlackApi api = new SlackApi(
      "<api>");


  @Autowired
  private RabbitMqConfig rabbitMqConfig;

  @Autowired
  public MailMessageSender(final RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  // Send's Message to Console, Splunk and RabbitMQ
  public void sendMessage(MailMessage mailMessage) throws AmqpException {
      logger.info("Mail Processed - Sending Message Now\n" + new Gson().toJson(mailMessage));
      rabbitTemplate
          .convertAndSend(rabbitMqConfig.exchangeName, rabbitMqConfig.routingKey, mailMessage);
      api.call(new SlackMessage("Processed Mail: " + new Gson().toJson(mailMessage)));
  }
}
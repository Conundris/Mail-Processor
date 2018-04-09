package com.jbr.pbm.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

@Configuration
@EnableRabbit
@ConfigurationProperties(prefix = "pbm")
@EnableConfigurationProperties
public class RabbitMqConfig implements RabbitListenerConfigurer {

  private static final Logger logger = LoggerFactory.getLogger(RabbitMqConfig.class);

  @Value("${pbm.rabbitmq.exchangename}")
  public String exchangeName;
  @Value("${pbm.rabbitmq.queuename}")
  public String queueSpecificName;
  @Value("${pbm.rabbitmq.routingkey}")
  public String routingKey;

  @Bean
  public TopicExchange appExchange() {
    return new TopicExchange(exchangeName);
  }

  @Bean
  public Queue appQueueSpecific() {
    return new Queue(queueSpecificName);
  }

  @Bean
  public Binding declareBindingSpecific() {
    return BindingBuilder.bind(appQueueSpecific()).to(appExchange()).with(routingKey);
  }

  @Bean
  public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {

    logger.info(connectionFactory.getHost());
    logger.info(connectionFactory.getUsername());

    final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
    return rabbitTemplate;
  }

  @Bean
  public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  @Bean
  public DefaultMessageHandlerMethodFactory messageHandlerMethodFactory() {
    DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
    factory.setMessageConverter(consumerJackson2MessageConverter());
    return factory;
  }

  @Override
  public void configureRabbitListeners(final RabbitListenerEndpointRegistrar registrar) {
    registrar.setMessageHandlerMethodFactory(messageHandlerMethodFactory());
  }

  @Bean
  public MappingJackson2MessageConverter consumerJackson2MessageConverter() {
    return new MappingJackson2MessageConverter();
  }
}

package no.kristiania.echocare.feedback.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration for Feedback Service
 *
 * Feedback Service is a PUBLISHER only — it does NOT create queues.
 * Consumers (like playlist-service) create their own queues and bind to this exchange.
 *
 * Publishes feedback.submitted events to feedback.exchange
 */
@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.feedback}")
    private String feedbackExchange;

    @Value("${rabbitmq.routing-key.feedback-submitted}")
    private String feedbackSubmittedRoutingKey;

    /**
     * Feedback Exchange - used for publishing feedback events
     * Consumers (like playlist-service) will create their own queues and bind to this exchange
     */
    @Bean
    public TopicExchange feedbackExchange() {

        return new TopicExchange(feedbackExchange);
    }


    @Bean
    public MessageConverter jsonMessageConverter() {

        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}


package no.kristiania.echocare.playlist.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration for Playlist Service
 *
 * This service is primarily a CONSUMER that listens to events from other services.
 * It acts as an event-driven orchestrator that reacts to changes in the system.
 *
**/

@Configuration
public class RabbitMQConfig {

    // ========== Profile Exchange Configuration ==========
    @Value("${rabbitmq.exchange.profile}")
    private String profileExchange;

    // ========== Feedback Exchange Configuration ==========
    @Value("${rabbitmq.exchange.feedback}")
    private String feedbackExchange;

    // ========== Queue Configuration ==========
    @Value("${rabbitmq.queue.profile-events}")
    private String profileEventsQueue;

    @Value("${rabbitmq.queue.feedback-events}")
    private String feedbackEventsQueue;

    // ========== Routing Key Configuration ==========
    @Value("${rabbitmq.routing-key.profile-created}")
    private String profileCreatedRoutingKey;

    @Value("${rabbitmq.routing-key.profile-updated}")
    private String profileUpdatedRoutingKey;

    @Value("${rabbitmq.routing-key.feedback-submitted}")
    private String feedbackSubmittedRoutingKey;

    @Bean
    public TopicExchange profileExchange() {
        return new TopicExchange(profileExchange);
    }

    @Bean
    public Queue profileEventsQueue() {
        return new Queue(profileEventsQueue, true); // durable = true
    }

    @Bean
    public Binding profileCreatedBinding() {
        return BindingBuilder
                .bind(profileEventsQueue())
                .to(profileExchange())
                .with(profileCreatedRoutingKey);
    }

    @Bean
    public Binding profileUpdatedBinding() {
        return BindingBuilder
                .bind(profileEventsQueue())
                .to(profileExchange())
                .with(profileUpdatedRoutingKey);
    }

    @Bean
    public TopicExchange feedbackExchange() {

        return new TopicExchange(feedbackExchange);
    }


    @Bean
    public Queue feedbackEventsQueue() {

        return new Queue(feedbackEventsQueue, true); // durable = true
    }

    @Bean
    public Binding feedbackSubmittedBinding() {
        return BindingBuilder
                .bind(feedbackEventsQueue())
                .to(feedbackExchange())
                .with(feedbackSubmittedRoutingKey);
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


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
 * This service is a PUBLISHER that produces feedback-related events.
 * When feedback is submitted by caregivers, events are published to the feedback.exchange.
 *
 * Architecture:
 * - Exchange: feedback.exchange (Topic Exchange)
 * - Queue: feedback.submitted.queue
 * - Routing Key: feedback.submitted
 *
 * Other services (like playlist-service) can subscribe to feedback events
 * to adapt and improve their recommendations based on caregiver feedback.
 */
@Configuration
public class RabbitMQConfig {

    // ========== Exchange Configuration ==========
    @Value("${rabbitmq.exchange.feedback}")
    private String feedbackExchange;

    // ========== Queue Configuration ==========
    @Value("${rabbitmq.queue.feedback-submitted}")
    private String feedbackSubmittedQueue;

    // ========== Routing Key Configuration ==========
    @Value("${rabbitmq.routing-key.feedback-submitted}")
    private String feedbackSubmittedRoutingKey;

    @Bean
    public TopicExchange feedbackExchange() {
        return new TopicExchange(feedbackExchange);
    }

    @Bean
    public Queue feedbackSubmittedQueue() {
        return new Queue(feedbackSubmittedQueue, true); // durable = true
    }

    @Bean
    public Binding feedbackSubmittedBinding() {
        return BindingBuilder
                .bind(feedbackSubmittedQueue())
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


package no.kristiania.echocare.profile.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ========== Exchange Configuration ==========
    @Value("${rabbitmq.exchange.profile}")
    private String profileExchange;

    // ========== Queue Configuration ==========
    @Value("${rabbitmq.queue.profile-created}")
    private String profileCreatedQueue;

    @Value("${rabbitmq.queue.profile-updated}")
    private String profileUpdatedQueue;

    // ========== Routing Key Configuration ==========
    @Value("${rabbitmq.routing-key.profile-created}")
    private String profileCreatedRoutingKey;

    @Value("${rabbitmq.routing-key.profile-updated}")
    private String profileUpdatedRoutingKey;


    @Bean
    public TopicExchange profileExchange() {
        return new TopicExchange(profileExchange);
    }


    @Bean
    public Queue profileCreatedQueue() {
        return new Queue(profileCreatedQueue, true); // durable = true
    }

    @Bean
    public Queue profileUpdatedQueue() {
        return new Queue(profileUpdatedQueue, true); // durable = true
    }

    @Bean
    public Binding profileCreatedBinding() {
        return BindingBuilder
                .bind(profileCreatedQueue())
                .to(profileExchange())
                .with(profileCreatedRoutingKey);
    }

    @Bean
    public Binding profileUpdatedBinding() {
        return BindingBuilder
                .bind(profileUpdatedQueue())
                .to(profileExchange())
                .with(profileUpdatedRoutingKey);
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


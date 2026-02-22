package no.kristiania.echocare.profile.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Configuration for Profile Service.
 *
 * Profile Service is a PUBLISHER only — it does NOT create queues.
 * Consumers (like playlist-service) create their own queues and bind to this exchange.
 *
 * On startup, this config removes any stale queues (profile.created.queue,
 * profile.updated.queue) that may have been left over from older builds.
 */
@Slf4j
@Configuration
public class RabbitMQConfig {

    // ========== Exchange Configuration ==========
    @Value("${rabbitmq.exchange.profile}")
    private String profileExchange;

    // ========== Routing Key Configuration (for publishing) ==========
    @Value("${rabbitmq.routing-key.profile-created}")
    private String profileCreatedRoutingKey;

    @Value("${rabbitmq.routing-key.profile-updated}")
    private String profileUpdatedRoutingKey;

    /**
     * Profile Exchange - used for publishing profile events
     * Consumers (like playlist-service) will create their own queues and bind to this exchange
     */
    @Bean
    public TopicExchange profileExchange() {
        return new TopicExchange(profileExchange);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    /**
     * JSON message converter for RabbitMQ
     */
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

    /**
     * Remove stale queues on startup.
     * Older builds of profile-service incorrectly created profile.created.queue and
     * profile.updated.queue. These queues steal messages from the exchange because
     * they are bound with the same routing keys, but nobody consumes from them.
     */
    @Bean
    public CommandLineRunner staleQueueCleanup(RabbitAdmin rabbitAdmin) {
        return args -> {
            String[] staleQueues = {"profile.created.queue", "profile.updated.queue"};
            for (String queueName : staleQueues) {
                try {
                    boolean deleted = rabbitAdmin.deleteQueue(queueName);
                    if (deleted) {
                        log.warn("Deleted stale queue '{}' — this queue should not exist. "
                                + "Profile Service is a publisher only.", queueName);
                    }
                } catch (Exception e) {
                    // Queue doesn't exist — this is the expected state
                    log.debug("Queue '{}' does not exist (expected)", queueName);
                }
            }
        };
    }
}


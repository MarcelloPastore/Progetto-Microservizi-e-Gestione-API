package it.newunimol.comunicazioni.config;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${app.rabbitmq.queue.materiale}")
    private String materialeQueue;

    @Value("${app.rabbitmq.queue.compiti}")
    private String compitiQueue;

    @Value("${app.rabbitmq.queue.esami}")
    private String esamiQueue;

    @Bean
    public Queue materialeQueue() {
        return new Queue(materialeQueue, true);
    }

    @Bean
    public Queue compitiQueue() {
        return new Queue(compitiQueue, true);
    }

    @Bean
    public Queue esamiQueue() {
        return new Queue(esamiQueue, true);
    }
}

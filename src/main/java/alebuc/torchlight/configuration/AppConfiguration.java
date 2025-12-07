package alebuc.torchlight.configuration;

import alebuc.torchlight.consumer.KafkaEventConsumer;
import alebuc.torchlight.controller.MainScreenController;
import alebuc.torchlight.controller.TopicPaneContentController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class AppConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public ZoneId zoneId() {
        return ZoneId.systemDefault();
    }

    @Bean
    public KafkaEventConsumer kafkaEventConsumer(KafkaProperties kafkaProperties) {
        return new KafkaEventConsumer(kafkaProperties);
    }

    @Bean
    public MainScreenController mainScreenController(KafkaEventConsumer kafkaEventConsumer) {
        return new MainScreenController(kafkaEventConsumer);
    }

    @Bean
    public TopicPaneContentController topicPaneContentController(KafkaEventConsumer kafkaEventConsumer) {
        return new TopicPaneContentController(kafkaEventConsumer);
    }

}

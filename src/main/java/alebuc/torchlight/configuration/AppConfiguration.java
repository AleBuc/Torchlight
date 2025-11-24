package alebuc.torchlight.configuration;

import alebuc.torchlight.consumer.KafkaEventConsumer;
import alebuc.torchlight.controller.MainScreenController;
import alebuc.torchlight.controller.TopicPaneContentController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

    @Bean
    public KafkaEventConsumer kafkaEventConsumer() {
        return new KafkaEventConsumer();
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

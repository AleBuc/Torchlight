package alebuc.torchlight.configuration;

import alebuc.torchlight.consumer.KafkaEventConsumer;
import alebuc.torchlight.controller.MainScreenController;
import alebuc.torchlight.controller.TopicPaneContentController;
import alebuc.torchlight.scene.ConsumerScene;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

    @Bean
    public KafkaEventConsumer kafkaEventConsumer() {
        return new KafkaEventConsumer();
    }

    @Bean
    public ConsumerScene consumerScene(KafkaEventConsumer kafkaEventConsumer) {
        return new ConsumerScene(kafkaEventConsumer);
    }

    @Bean
    public MainScreenController mainScreenController(KafkaEventConsumer kafkaEventConsumer, ConsumerScene consumerScene) {
        return new MainScreenController(kafkaEventConsumer, consumerScene);
    }

    @Bean
    public TopicPaneContentController topicPaneContentController(ConsumerScene consumerScene) {
        return new TopicPaneContentController(consumerScene);
    }

}

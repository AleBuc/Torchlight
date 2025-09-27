package alebuc.torchlight.configuration;

import alebuc.torchlight.consumer.KafkaEventConsumer;
import alebuc.torchlight.controller.MainScreenController;
import alebuc.torchlight.controller.TopicPaneContentController;
import alebuc.torchlight.scene.ConsumerScene;
import alebuc.torchlight.utils.ValueUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

    @Bean
    public KafkaEventConsumer kafkaEventConsumer() {
        return new KafkaEventConsumer();
    }

    @Bean
    public ConsumerScene consumerScene(KafkaEventConsumer kafkaEventConsumer, ValueUtils valueUtils) {
        return new ConsumerScene(kafkaEventConsumer, valueUtils);
    }

    @Bean
    public MainScreenController mainScreenController(KafkaEventConsumer kafkaEventConsumer, ConsumerScene consumerScene) {
        return new MainScreenController(kafkaEventConsumer, consumerScene);
    }

    @Bean
    public TopicPaneContentController topicPaneContentController(ConsumerScene consumerScene) {
        return new TopicPaneContentController(consumerScene);
    }

    @Bean
    public ValueUtils valueUtils() {
        return new ValueUtils();
    }

}

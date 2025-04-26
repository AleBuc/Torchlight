package alebuc.torchlight;


import alebuc.torchlight.configuration.KafkaProperties;
import alebuc.torchlight.model.Event;
import javafx.application.Platform;
import javafx.scene.control.TableView;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class KafkaEventConsumer {

    private final Logger log = LoggerFactory.getLogger(KafkaEventConsumer.class);

    private final KafkaConsumer<String, String> consumer;

    /**
     * Initializes the consumer and assign topic and partitions.
     */
    public KafkaEventConsumer() {
        log.info("Starting consumer.");
        Properties kafkaProperties = KafkaProperties.getProperties();
        consumer = new KafkaConsumer<>(kafkaProperties);
        Map<String, List<PartitionInfo>> listTopics = consumer.listTopics();
        List<PartitionInfo> topicPartitionInfos = listTopics.get(kafkaProperties.getProperty("topic-name"));
        List<TopicPartition> partitions = new ArrayList<>();
        for (PartitionInfo partitionInfo : topicPartitionInfos) {
            partitions.add(new TopicPartition(partitionInfo.topic(), partitionInfo.partition()));
        }
        if (!partitions.isEmpty()) {
            consumer.assign(partitions);
        }
    }

    /**
     * Adds Kafka events to a given list.
     *
     * @param eventTableView list to populate
     */
    public void processEvents(TableView<Event<String, String>> eventTableView) {
        while (true) {
            try {
                ConsumerRecords<String, String> records = consumer.poll(Duration.of(100, ChronoUnit.MILLIS));
                for (ConsumerRecord<String, String> consumerRecord : records) {
                    Platform.runLater(() -> {
                        Event<String, String> event = new Event<>(consumerRecord);
                        eventTableView.getItems().add(event);
                        log.info(event.toString());
                    });
                }
            } catch (WakeupException wakeupException) {
                log.info("Consumer waking up.");
            } catch (Exception exception) {
                log.error("Exception occurred: ", exception);
            }
        }
    }

    /**
     * Stops the consumer.
     */
    public void close() {
        consumer.close();
    }
}

package alebuc.torchlight;


import alebuc.torchlight.configuration.KafkaProperties;
import javafx.application.Platform;
import javafx.scene.control.ListView;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Properties;

public class KafkaEventConsumer {

    private final Logger log = LoggerFactory.getLogger(KafkaEventConsumer.class);

    private final KafkaConsumer<String, String> consumer;

    public KafkaEventConsumer() {
        log.info("Starting consumer.");
        Properties kafkaProperties = KafkaProperties.getProperties();
        consumer = new KafkaConsumer<>(kafkaProperties);
        consumer.subscribe(Collections.singletonList(kafkaProperties.getProperty("topic-name")));
        //fixme give unique GroupID and remove reading commit
    }

    public void processEvents(ListView<String> eventListView) {
        while (true) {
            try {
                ConsumerRecords<String, String> records = consumer.poll(Duration.of(100, ChronoUnit.MILLIS));
                for (ConsumerRecord<String, String> consumerRecord : records) {
                    Platform.runLater(() -> {
                        String recordContent = String.format("Partition: %d, Offset: %d, Key: %s, Value: %s", consumerRecord.partition(), consumerRecord.offset(), consumerRecord.key(), consumerRecord.value());
                        eventListView.getItems().add(recordContent);
                        log.info(recordContent);
                    });
                }
            } catch (WakeupException wakeupException) {
                log.info("Consumer waking up.");
            } catch (Exception exception) {
                log.error("Exception occurred: ", exception);
            }
        }
    }

    public void close() {
        consumer.close();
    }
}

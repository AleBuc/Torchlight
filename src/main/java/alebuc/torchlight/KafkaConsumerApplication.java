package alebuc.torchlight;


import alebuc.torchlight.configuration.KafkaProperties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Properties;

public class KafkaConsumerApplication {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerApplication.class);
    private static final Properties kafkaProperties = KafkaProperties.getProperties();

    public static void main(String[] args) {
        log.info("Starting consumer.");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(kafkaProperties);

        Thread currentThread = Thread.currentThread();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Begin shutdown by waking up.");
            consumer.wakeup();
            try {
                currentThread.join();
            } catch (InterruptedException exception) {
                log.error("Exception happened: ", exception);
                Thread.currentThread().interrupt();
            }
        }));

        try {
            consumer.subscribe(List.of("testTopic"));
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.of(100, ChronoUnit.MILLIS));
                for (ConsumerRecord<String, String> consumerRecord : records) {
                    log.info("Partition: {}, Offset: {}, Key: {}, Value: {}", consumerRecord.partition(), consumerRecord.offset(), consumerRecord.key(), consumerRecord.value());
                }
            }
        }catch (WakeupException wakeupException) {
            log.info("Consumer waking up.");
        }catch (Exception exception) {
            log.error("Exception occurred: ", exception);
        } finally {
            consumer.close();
            log.info("Consumer closed.");
        }
    }
}

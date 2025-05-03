package alebuc.torchlight.consumer;


import alebuc.torchlight.configuration.KafkaProperties;
import alebuc.torchlight.model.Partition;
import alebuc.torchlight.model.Topic;
import javafx.application.Platform;
import javafx.scene.control.ListView;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class KafkaEventConsumer {

    private final Logger log = LoggerFactory.getLogger(KafkaEventConsumer.class);

    private final KafkaConsumer<String, String> consumer;
    Map<String, Topic> topicByName;

    /**
     * Initializes the consumer and assign topic and partitions.
     */
    public KafkaEventConsumer() {
        log.info("Starting consumer.");
        Properties kafkaProperties = KafkaProperties.getProperties();
        consumer = new KafkaConsumer<>(kafkaProperties);
    }

    /**
     * Adds Kafka events to a given list.
     *
     * @param eventListView list to populate
     */
    public void processEvents(String topicName, ListView<String> eventListView) {
        Topic topic = topicByName.get(topicName);
        List<TopicPartition> partitions = new ArrayList<>();
        for (Partition partition : topic.getPartitions()) {
            partitions.add(new TopicPartition(topicName, partition.getIndex()));
        }
        if (!partitions.isEmpty()) {
            consumer.assign(partitions);
        }
        log.info("Start consumption of topic.");
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

    /**
     * Stops the consumer.
     */
    public void close() {
        consumer.close();
    }

    public List<Topic> getTopics() {
        Map<String, List<PartitionInfo>> topicMap = consumer.listTopics();
        Map<String, List<TopicPartition>> topicPartitionsByTopic = topicMap.entrySet().stream()
                .map(entry -> {
                    String topicName = entry.getKey();
                    return entry.getValue().stream().map(partitionInfo -> new TopicPartition(topicName, partitionInfo.partition())).toList();
                })
                .collect(Collectors.toMap(partitions -> partitions.getFirst().topic(), Function.identity()));
        List<Topic> topics = new ArrayList<>();
        for (Map.Entry<String, List<TopicPartition>> entry : topicPartitionsByTopic.entrySet()) {
            Topic.TopicBuilder topicBuilder = Topic.builder();
            topicBuilder.name(entry.getKey());
            Map<TopicPartition, Long> minOffsetByPartition = getMinOffsets(entry.getValue());
            Map<TopicPartition, Long> maxOffsetByPartition = getMaxOffsets(entry.getValue());
            for (TopicPartition topicPartition : entry.getValue()) {
                Partition partition = new Partition(topicPartition.partition(), minOffsetByPartition.get(topicPartition), maxOffsetByPartition.get(topicPartition));
                topicBuilder.partition(partition);
            }
            topics.add(topicBuilder.build());
        }
        this.topicByName = topics.stream().collect(Collectors.toMap(Topic::getName, Function.identity()));
        return topics;
    }

    private Map<TopicPartition, Long> getMinOffsets(List<TopicPartition> topicPartitions) {
        consumer.assign(topicPartitions);
        consumer.seekToBeginning(Collections.emptySet());
        return topicPartitions.stream().collect(Collectors.toMap(Function.identity(), consumer::position));
    }

    private Map<TopicPartition, Long> getMaxOffsets(List<TopicPartition> topicPartitions) {
        consumer.assign(topicPartitions);
        consumer.seekToEnd(Collections.emptySet());
        return topicPartitions.stream().collect(Collectors.toMap(Function.identity(), consumer::position));
    }
}

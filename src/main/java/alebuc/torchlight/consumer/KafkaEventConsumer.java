package alebuc.torchlight.consumer;


import alebuc.torchlight.configuration.KafkaProperties;
import alebuc.torchlight.model.Event;
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

    private static final Properties KAFKA_PROPERTIES = KafkaProperties.getProperties();
    private final Logger log = LoggerFactory.getLogger(KafkaEventConsumer.class);

    private final KafkaConsumer<String, String> menuConsumer;
    Map<String, Topic> topicByName;
    private final Set<UUID> stageIdForConsumerStopping = new HashSet<>();

    /**
     * Initializes the consumer and assign topic and partitions.
     */
    public KafkaEventConsumer() {
        log.info("Starting consumer.");
        menuConsumer = new KafkaConsumer<>(KAFKA_PROPERTIES);
    }

    /**
     * Adds Kafka events to a given list. New Kafka consumer is created each time to be multi-thread safe.
     *
     * @param eventListView list to populate
     */
    public void processEvents(UUID stageId, String topicName, ListView<Event<?,?>> eventListView) {
        Topic topic = topicByName.get(topicName);
        List<TopicPartition> partitions = new ArrayList<>();
        for (Partition partition : topic.getPartitions()) {
            partitions.add(new TopicPartition(topicName, partition.getIndex()));
        }
        if (partitions.isEmpty()) {
            log.warn("No partition found for topic {}", topicName);
            return;
        }
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(KAFKA_PROPERTIES)) {
            consumer.assign(partitions);
            log.info("Start consumption of topic {}.", topicName);
            while (!stageIdForConsumerStopping.contains(stageId)) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.of(500, ChronoUnit.MILLIS));
                for (ConsumerRecord<String, String> consumerRecord : records) {
                    Platform.runLater(() -> {
                        Event<?, ?> event = new Event<>(consumerRecord);
                        eventListView.getItems().add(event);
                        log.info(event.toString());
                    });
                }
            }
            consumer.wakeup();
            consumer.unsubscribe();
        } catch (WakeupException _) {
            log.info("Consumer waking up.");
        } catch (Exception e) {
            log.error("Exception occurred: ", e);
        }
        stageIdForConsumerStopping.remove(stageId);
    }

    /**
     * Retrieves broker topic list.
     *
     * @return a list of {@link Topic}
     */
    public List<Topic> getTopics() {
        Map<String, List<PartitionInfo>> topicMap = menuConsumer.listTopics();
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
        menuConsumer.assign(topicPartitions);
        menuConsumer.seekToBeginning(Collections.emptySet());
        return topicPartitions.stream().collect(Collectors.toMap(Function.identity(), menuConsumer::position));
    }

    private Map<TopicPartition, Long> getMaxOffsets(List<TopicPartition> topicPartitions) {
        menuConsumer.assign(topicPartitions);
        menuConsumer.seekToEnd(Collections.emptySet());
        return topicPartitions.stream().collect(Collectors.toMap(Function.identity(), menuConsumer::position));
    }

    /**
     * Precise the consumer is no more used and the consumption can be stopped.
     *
     * @param stageId consumer stage ID
     */
    public void stopTopicConsumption(UUID stageId) {
        stageIdForConsumerStopping.add(stageId);
    }
}

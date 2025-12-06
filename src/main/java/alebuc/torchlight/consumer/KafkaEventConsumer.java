package alebuc.torchlight.consumer;


import alebuc.torchlight.configuration.KafkaProperties;
import alebuc.torchlight.model.Event;
import alebuc.torchlight.model.EventFilter;
import alebuc.torchlight.model.Partition;
import alebuc.torchlight.model.Topic;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
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
    public KafkaEventConsumer(Clock clock, ZoneId zoneId) {
        log.info("Starting consumer.");
        this.menuConsumer = new KafkaConsumer<>(KAFKA_PROPERTIES);
    }

    /**
     * Consumes events from a specified Kafka topic and processes them by adding the events to the provided list.
     * The method assigns the consumer to the topic's partitions and processes records until the specified stage ID is marked for stopping.
     *
     * @param stageId     the unique identifier for the consumer's lifecycle stage, used to control when to stop consumption
     * @param topicName   the name of the Kafka topic to consume events from
     * @param eventList   the list to which processed events will be added
     * @param eventFilter the filter containing start and end instants to filter events by timestamp
     */
    public void processEvents(UUID stageId,
                              String topicName,
                              List<Event<?, ?>> eventList,
                              EventFilter eventFilter,
                              LongProperty totalEventsCount) {
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
                        totalEventsCount.set(totalEventsCount.get() + 1);
                        boolean afterStart = (eventFilter.startInstant() == null) || !event.getTimestamp().isBefore(eventFilter.startInstant());
                        boolean beforeEnd = (eventFilter.endInstant() == null) || !event.getTimestamp().isAfter(eventFilter.endInstant());

                        if (afterStart && beforeEnd) {
                            eventList.add(event);
                            log.info(event.toString());
                        }
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

package alebuc.torchlight.consumer;


import alebuc.torchlight.configuration.KafkaProperties;
import alebuc.torchlight.model.DataType;
import alebuc.torchlight.model.Event;
import alebuc.torchlight.model.EventFilter;
import alebuc.torchlight.model.Partition;
import alebuc.torchlight.model.Topic;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class KafkaEventConsumer {


    private final KafkaConsumer<String, String> menuConsumer;
    private final KafkaProperties kafkaProperties;
    Map<String, Topic> topicByName;
    private final Set<UUID> stageIdForConsumerStopping = new HashSet<>();

    /**
     * Initializes the consumer and assign topic and partitions.
     */
    public KafkaEventConsumer(KafkaProperties kafkaProperties) {
        log.info("Starting consumer.");
        this.kafkaProperties = kafkaProperties;
        this.menuConsumer = new KafkaConsumer<>(kafkaProperties.getProperties());
    }

    /**
     * Processes events from a specified Kafka topic, filtering them based on the provided event filter,
     * and adds the filtered events to the provided event list. Additionally, it keeps track of the total
     * number of events processed.
     *
     * @param stageId          unique identifier of the current consumer stage, used to control when to stop consumption.
     * @param topicName        name of the Kafka topic from which events will be consumed.
     * @param eventList        list in which the filtered events will be added after processing.
     * @param eventFilter      filter criteria to include or exclude events based on their timestamp.
     * @param totalEventsCount a mutable counter to track the total number of processed events.
     * @param keyType          data type of the event key for deserialization.
     * @param valueType        data type of the event value for deserialization.
     */
    public void processEvents(UUID stageId,
                              String topicName,
                              List<Event<?, ?>> eventList,
                              EventFilter eventFilter,
                              LongProperty totalEventsCount,
                              DataType keyType,
                              DataType valueType) {
        if (kafkaProperties == null) {
            log.error("Kafka properties not found.");
            return;
        }
        Topic topic = topicByName.get(topicName);
        List<TopicPartition> partitions = new ArrayList<>();
        for (Partition partition : topic.getPartitions()) {
            partitions.add(new TopicPartition(topicName, partition.getIndex()));
        }
        if (partitions.isEmpty()) {
            log.warn("No partition found for topic {}", topicName);
            return;
        }
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(kafkaProperties.getProperties())) {
            consumer.assign(partitions);
            log.info("Start consumption of topic {}.", topicName);
            while (!stageIdForConsumerStopping.contains(stageId)) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.of(500, ChronoUnit.MILLIS));
                for (ConsumerRecord<String, String> consumerRecord : records) {
                    Platform.runLater(() -> {
                        Event<?, ?> event = new Event<>(consumerRecord, keyType, valueType);
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

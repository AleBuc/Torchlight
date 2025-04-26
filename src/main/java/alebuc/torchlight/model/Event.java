package alebuc.torchlight.model;

import lombok.Value;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.time.Instant;

@Value
public class Event<K, V>{

    ConsumerRecord<K, V> consumerRecord;
    String topic;
    int partition;
    long offset;
    Instant timestamp;
    K key;
    V value;

    public Event(ConsumerRecord<K, V> consumerRecord) {
        this.consumerRecord = consumerRecord;
        this.topic = consumerRecord.topic();
        this.partition = consumerRecord.partition();
        this.offset = consumerRecord.offset();
        this.timestamp= Instant.ofEpochMilli(consumerRecord.timestamp());
        this.key = consumerRecord.key();
        this.value = consumerRecord.value();
    }

    @Override
    public String toString() {
        return String.format("Partition: %d, Offset: %d, Key: %s, Value: %s", this.getPartition(), this.getOffset(), this.getKey(), this.getValue());
    }
}

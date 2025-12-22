package alebuc.torchlight.model;

import lombok.NonNull;
import lombok.Value;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;

import java.time.Instant;

@Value
public class Event<K, V>{

    ConsumerRecord<K, V> consumerRecord;
    String topic;
    int partition;
    long offset;
    Instant timestamp;
    DataType keyType;
    DataType valueType;
    K key;
    V value;
    Headers headers;

    public Event(@NonNull ConsumerRecord<K, V> consumerRecord, DataType keyType, DataType valueType) {
        this.consumerRecord = consumerRecord;
        this.topic = consumerRecord.topic();
        this.partition = consumerRecord.partition();
        this.offset = consumerRecord.offset();
        this.timestamp= Instant.ofEpochMilli(consumerRecord.timestamp());
        this.keyType = keyType;
        this.valueType = valueType;
        this.key = consumerRecord.key();
        this.value = consumerRecord.value();
        this.headers = consumerRecord.headers();
    }

    @Override
    public String toString() {
        return String.format(
                "Partition: %d, Offset: %d, Key: %s, Value: %s",
                this.getPartition(),
                this.getOffset(),
                keyType.getIsInstanceOf().apply(this.getKey()) ? this.getKey().toString() : "Deserialization error",
                valueType.getIsInstanceOf().apply(this.getValue()) ? this.getValue().toString() : "Deserialization error"
        );
    }
}

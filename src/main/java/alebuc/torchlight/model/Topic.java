package alebuc.torchlight.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.apache.kafka.common.PartitionInfo;

import java.util.List;

@Builder
@Value
public class Topic {
    String name;
    @Singular
    List<Partition> partitions;

    public int getPartitionsCount() {
        return this.partitions.size();
    }

    public long getEventCount() {
        return this.partitions.stream().mapToLong(partition -> partition.getOffsetMax()-partition.getOffsetMin()).sum();
    }
}

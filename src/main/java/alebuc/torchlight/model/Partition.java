package alebuc.torchlight.model;

import lombok.Value;

@Value
public class Partition {
    int index;
    long offsetMin;
    long offsetMax;
}

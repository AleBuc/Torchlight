package alebuc.torchlight.model.consumer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum StartChoice {
    NOW("Now", false),
    TODAY("Today", false),
    YESTERDAY("Yesterday", false),
    SPECIFIC_DATE("Specific Date", true),
    EARLIEST("Earliest", false),;
    public static final StartChoice DEFAULT = NOW;

    private final String name;
    private final boolean showDatePicker;
}

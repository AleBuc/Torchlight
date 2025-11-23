package alebuc.torchlight.model.consumer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EndChoice {
    NO_LIMIT("No Limit", false),
    NOW("Now", false),
    TODAY("Today", false),
    YESTERDAY("Yesterday", false),
    SPECIFIC_DATE("Specific Date", true);
    public static final EndChoice DEFAULT = NO_LIMIT;

    private final String name;
    private final boolean showDatePicker;
}

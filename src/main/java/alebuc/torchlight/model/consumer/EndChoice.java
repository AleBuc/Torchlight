package alebuc.torchlight.model.consumer;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Clock;
import java.time.Instant;
import java.util.function.Function;

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
    private final Function<Clock, Instant> getInstant = clock -> switch (this) {
        case NOW -> clock.instant();
        case TODAY -> clock.instant().truncatedTo(java.time.temporal.ChronoUnit.DAYS);
        case YESTERDAY -> clock.instant().minus(1, java.time.temporal.ChronoUnit.DAYS).truncatedTo(java.time.temporal.ChronoUnit.DAYS);
        case SPECIFIC_DATE, NO_LIMIT -> null;
    };

}

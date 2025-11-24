package alebuc.torchlight.model.consumer;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Clock;
import java.time.Instant;
import java.util.function.Function;

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
    private final Function<Clock, Instant> getInstant = clock -> switch (this) {
        case NOW -> clock.instant();
        case TODAY -> clock.instant().truncatedTo(java.time.temporal.ChronoUnit.DAYS);
        case YESTERDAY -> clock.instant().minus(1, java.time.temporal.ChronoUnit.DAYS).truncatedTo(java.time.temporal.ChronoUnit.DAYS);
        case SPECIFIC_DATE, EARLIEST -> null;
    };
}

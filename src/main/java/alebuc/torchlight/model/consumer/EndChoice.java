package alebuc.torchlight.model.consumer;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Clock;
import java.time.Instant;
import java.util.function.Function;

/**
 * An enumeration representing choices for determining the end point of an event or process.
 * Each choice is associated with a name, a flag indicating whether a date picker should be displayed,
 * and a function to calculate the ending {@link Instant} based on the selected choice.
 * <p>
 * Enum Values:
 * - NO_LIMIT: Represents no specific end point. Does not show a date picker.
 * - NOW: Represents the current instant. Does not show a date picker.
 * - TODAY: Represents the start of the current day. Does not show a date picker.
 * - YESTERDAY: Represents the start of the previous day. Does not show a date picker.
 * - SPECIFIC_DATE: Provides the ability to specify a custom date. Displays a date picker.
 * <p>
 * The `DEFAULT` value is set to NO_LIMIT.
 * <p>
 * Attributes:
 * - name: A user-friendly name for the end choice.
 * - showDatePicker: Indicates whether a date picker should be displayed for the corresponding choice.
 * - instant: A functional mapping of the corresponding choice to a calculated {@link Instant}.
 */
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
    private final Function<Clock, Instant> instant = clock -> switch (this) {
        case NOW -> clock.instant();
        case TODAY -> clock.instant().truncatedTo(java.time.temporal.ChronoUnit.DAYS);
        case YESTERDAY ->
                clock.instant().minus(1, java.time.temporal.ChronoUnit.DAYS).truncatedTo(java.time.temporal.ChronoUnit.DAYS);
        case SPECIFIC_DATE, NO_LIMIT -> null;
    };

}

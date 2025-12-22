package alebuc.torchlight.model.consumer;

import javafx.util.StringConverter;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Clock;
import java.time.Instant;
import java.util.function.Function;

/**
 * An enumeration representing choices for determining the start point of an event or process.
 * Each choice is associated with a name, a flag indicating whether a date picker should be displayed,
 * and a function to calculate the starting {@link Instant} based on the selected choice.
 * <p>
 * Enum Values:
 * - NOW: Represents the current instant. Does not show a date picker.
 * - TODAY: Represents the start of the current day. Does not show a date picker.
 * - YESTERDAY: Represents the start of the previous day. Does not show a date picker.
 * - SPECIFIC_DATE: Provides the ability to specify a custom date. Displays a date picker.
 * - EARLIEST: Represents the earliest possible instant. Does not show a date picker.
 * <p>
 * The `DEFAULT` value is set to NOW.
 * <p>
 * Attributes:
 * - name: A user-friendly name for the start choice.
 * - showDatePicker: Indicates whether a date picker should be displayed for the corresponding choice.
 * - instant: A functional mapping of the corresponding choice to a calculated {@link Instant}.
 */
@AllArgsConstructor
@Getter
public enum StartChoice {
    NOW("Now", false),
    TODAY("Today", false),
    YESTERDAY("Yesterday", false),
    SPECIFIC_DATE("Specific Date", true),
    EARLIEST("Earliest", false),
    ;
    public static final StartChoice DEFAULT = NOW;

    private final String name;
    private final boolean showDatePicker;
    private final Function<Clock, Instant> instant = clock -> switch (this) {
        case NOW -> clock.instant();
        case TODAY -> clock.instant().truncatedTo(java.time.temporal.ChronoUnit.DAYS);
        case YESTERDAY ->
                clock.instant().minus(1, java.time.temporal.ChronoUnit.DAYS).truncatedTo(java.time.temporal.ChronoUnit.DAYS);
        case SPECIFIC_DATE, EARLIEST -> null;
    };

    public static StringConverter<StartChoice> getStringConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(StartChoice choice) {
                return choice == null ? "" : choice.getName();
            }

            @Override
            public StartChoice fromString(String string) {
                if (string == null) {
                    return null;
                }
                for (StartChoice c : StartChoice.values()) {
                    if (c.getName().equals(string)) {
                        return c;
                    }
                }
                return null;
            }
        };
    }
}

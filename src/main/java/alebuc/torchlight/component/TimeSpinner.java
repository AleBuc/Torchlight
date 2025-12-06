package alebuc.torchlight.component;

import javafx.geometry.Pos;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * A custom spinner for selecting and displaying {@link LocalTime} values.
 * The spinner allows the user to increment or decrement the time in minute intervals.
 * The time is displayed in the format "HH:mm:ss".
 * <p>
 * This class extends {@link Spinner} and uses a custom {@link SpinnerValueFactory}
 * to control the formatting and incrementing behavior for {@link LocalTime}.
 * <p>
 * Key Features:
 * - Editable text field for direct time input.
 * - Custom time formatting using "HH:mm:ss" pattern.
 * - Automatically resets to the current time if the field is null.
 * - Supports incrementing and decrementing time in configurable minute steps.
 * <p>
 * Usage:
 * Instantiate the component, optionally providing an initial {@link LocalTime}.
 * If no initial value is provided, the current system time is used, truncated to seconds precision.
 * <p>
 * Behavior:
 * - When incrementing, the time increases in minute intervals.
 * - When decrementing, the time decreases in minute intervals.
 * - If invalid input is provided, the spinner retains its current value.
 * <p>
 * Note:
 * The text field alignment is set to {@link Pos#CENTER_RIGHT} to improve UX for time input.
 */
public class TimeSpinner extends Spinner<LocalTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public TimeSpinner() {
        this(LocalTime.now().withNano(0));
    }

    public TimeSpinner(LocalTime time) {
        setEditable(true);
        getEditor().setAlignment(Pos.CENTER_RIGHT);
        SpinnerValueFactory<LocalTime> valueFactory = new SpinnerValueFactory<>() {
            {
                setConverter(new StringConverter<>() {
                    @Override
                    public String toString(LocalTime time) {
                        return (time == null) ? "" : FORMATTER.format(time);
                    }

                    @Override
                    public LocalTime fromString(String string) {
                        if (string == null || string.isBlank()) {
                            return null;
                        }
                        try {
                            return LocalTime.parse(string, FORMATTER);
                        } catch (DateTimeParseException e) {
                            return getValue();
                        }
                    }
                });
                setValue(time);
            }

            @Override
            public void decrement(int steps) {
                if (getValue() == null) {
                    setValue(LocalTime.now().withNano(0));
                } else {
                    setValue(getValue().minusMinutes(steps));
                }
            }

            @Override
            public void increment(int steps) {
                if (getValue() == null) {
                    setValue(LocalTime.now().withNano(0));
                } else {
                    setValue(getValue().plusMinutes(steps));
                }
            }
        };
        setValueFactory(valueFactory);
    }
}

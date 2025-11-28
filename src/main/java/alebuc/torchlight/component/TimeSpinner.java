package alebuc.torchlight.component;

import javafx.geometry.Pos;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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

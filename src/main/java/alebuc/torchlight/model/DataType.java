package alebuc.torchlight.model;

import alebuc.torchlight.utils.ValueUtils;
import javafx.util.StringConverter;
import lombok.Getter;

import java.util.function.Function;

@Getter
public enum DataType {
    STRING("String", o -> o instanceof String),
    INTEGER("Integer", o -> o instanceof Integer),
    LONG("Long", o -> o instanceof Long),
    FLOAT("Float", o -> o instanceof Float),
    DOUBLE("Double", o -> o instanceof Double),
    JSON("JSON", o -> o instanceof String s && ValueUtils.isValidJson(s));
    public static final DataType DEFAULT = STRING;
    private final String name;
    private final Function<Object, Boolean> isInstanceOf;

    DataType(String name, Function<Object, Boolean> isInstanceOf) {
        this.name = name;
        this.isInstanceOf = isInstanceOf;
    }

    public static StringConverter<DataType> getStringConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(DataType type) {
                return type == null ? "" : type.getName();
            }

            @Override
            public DataType fromString(String string) {
                if (string == null) {
                    return null;
                }
                for (DataType t : DataType.values()) {
                    if (t.getName().equals(string)) {
                        return t;
                    }
                }
                return null;
            }
        };
    }

}

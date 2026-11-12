package alebuc.torchlight.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ValueUtilsTest {

    @Test
    void stringOf_jsonString_isPrettyPrinted() {
        assertThat(ValueUtils.stringOf("{\"id\":1,\"tags\":[\"a\",\"b\"],\"nested\":{\"ok\":true,\"none\":null}}"))
                .isEqualToNormalizingNewlines("""
                        {
                          "id" : 1,
                          "tags" : [ "a", "b" ],
                          "nested" : {
                            "ok" : true,
                            "none" : null
                          }
                        }""");
    }

    @Test
    void stringOf_nonJsonString_isReturnedUnchanged() {
        assertThat(ValueUtils.stringOf("plain text")).isEqualTo("plain text");
        assertThat(ValueUtils.stringOf("{\"unterminated\": ")).isEqualTo("{\"unterminated\": ");
    }

    @Test
    void stringOf_otherValues_useStringValueOf() {
        assertThat(ValueUtils.stringOf(42L)).isEqualTo("42");
        assertThat(ValueUtils.stringOf(null)).isEqualTo("null");
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "[1, 2]", "{\"key\": \"value\"}", "\"text\"", "42", "true"})
    void isValidJson_validJson(String value) {
        assertThat(ValueUtils.isValidJson(value)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "plain text", "{", "{\"key\": }", "{} trailing", "[1] [2]"})
    void isValidJson_invalidJson(String value) {
        assertThat(ValueUtils.isValidJson(value)).isFalse();
    }
}

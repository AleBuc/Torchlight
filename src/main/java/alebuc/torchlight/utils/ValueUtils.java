package alebuc.torchlight.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@RequiredArgsConstructor
public class ValueUtils {
    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .build();
    private static final ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();

    public static String stringOf(Object object) {
        if (object instanceof String string){
            if (isValidJson(string)) {
                try {
                    JsonNode json = objectMapper.readTree(string);
                    return writer.writeValueAsString(json);
                } catch (JacksonException e) {
                    log.error("Error during JSON value deserialization.",e);
                    return string;
                }
            }
            return string;
        }
        return String.valueOf(object);
    }

    public static boolean isValidJson(String string) {
        if (StringUtils.isBlank(string)) {
            return false;
        }
        try {
            objectMapper.readTree(string);
        } catch (JacksonException e) {
            return false;
        }
        return true;
    }
}

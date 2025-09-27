package alebuc.torchlight.utils;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ValueUtils {
    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    private final ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();

    public String stringOf(Object object) {
        if (object instanceof String string){
            if (isValidJson(string)) {
                try {
                    JsonNode json = objectMapper.readTree(string);
                    return writer.writeValueAsString(json);
                } catch (JsonProcessingException e) {
                    log.error("Error during JSON value deserialization.",e);
                    return string;
                }
            }
            return string;
        }
        return String.valueOf(object);
    }

    public boolean isValidJson(String string) {
        try {
            objectMapper.readTree(string);
        } catch (JacksonException e) {
            return false;
        }
        return true;
    }
}

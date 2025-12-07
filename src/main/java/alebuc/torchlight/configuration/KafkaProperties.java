package alebuc.torchlight.configuration;

import alebuc.torchlight.model.login.LoginType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
@RequiredArgsConstructor
public class KafkaProperties {
    private final String brokerUrl;
    private final LoginType loginType;
    private final String username;
    private final String secret;

    /**
     * Gets Kafka cluster properties
     *
     * @return retrieved properties
     */
    public Properties getProperties() {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(false));
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return properties;
    }

    private Properties readPropertiesFile() {
        Properties properties = new Properties();
        try (InputStream is = KafkaProperties.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (is == null) {
                throw new IOException("Property file 'application.properties' not found in the classpath.");
            }
            properties.load(is);
        } catch (IOException e) {
            log.error("Failed to load application.properties", e);
        }
        checkProperties(properties);
        return properties;
    }

    private void checkProperties(Properties properties) {
        List<String> missingPropertyList = new ArrayList<>();
        checkAndAddMissingPropertyToList(properties, missingPropertyList, brokerUrl);
        if (loginType.equals(LoginType.CLIENT_CREDENTIALS)){
        checkAndAddMissingPropertyToList(properties, missingPropertyList, username);
        checkAndAddMissingPropertyToList(properties, missingPropertyList, secret);
        }
        if (!missingPropertyList.isEmpty()) {
            if (missingPropertyList.size() == 1) {
                throw new IllegalArgumentException(String.format("Required property '%s' is missing.", missingPropertyList.getFirst()));
            } else {
                throw new IllegalArgumentException(String.format("Required properties '%s' are missing.", missingPropertyList));
            }
        }
    }

    private static void checkAndAddMissingPropertyToList(Properties propertiesToCheck, List<String> missingPropertyList, String propertyKey) {
        if (!propertiesToCheck.containsKey(propertyKey)) {
            missingPropertyList.add(propertyKey);
        }
    }
}

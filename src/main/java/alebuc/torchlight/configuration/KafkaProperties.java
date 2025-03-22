package alebuc.torchlight.configuration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KafkaProperties {
    private static final Logger log = LoggerFactory.getLogger(KafkaProperties.class);
    private static final String CLUSTER_URL = "cluster.url";
    private static final String CLUSTER_CREDENTIALS_USERNAME = "cluster.credentials.username";
    private static final String CLUSTER_CREDENTIALS_PASSWORD = "cluster.credentials.password";
    private static final String TOPIC_NAME = "topic-name";

    public static Properties getProperties() {
        Properties properties = new Properties();
        Properties readenProperties = readPropertiesFile();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, readenProperties.getProperty(CLUSTER_URL));
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "Torchlight" + Instant.now());
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.setProperty(TOPIC_NAME, readenProperties.getProperty(TOPIC_NAME));
        return properties;
    }

    private static Properties readPropertiesFile() {
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

    private static void checkProperties(Properties properties) {
        List<String> missingPropertyList = new ArrayList<>();
        checkAndAddMissingPropertyToList(properties, missingPropertyList, CLUSTER_URL);
//        checkAndAddMissingPropertyToList(properties, missingPropertyList, CLUSTER_CREDENTIALS_USERNAME);
//        checkAndAddMissingPropertyToList(properties, missingPropertyList, CLUSTER_CREDENTIALS_PASSWORD);
        checkAndAddMissingPropertyToList(properties, missingPropertyList, TOPIC_NAME);
        if (!missingPropertyList.isEmpty()) {
            if (missingPropertyList.size() == 1) {
                throw new IllegalArgumentException(String.format("Required property '%s' is missing in the application.properties file.", missingPropertyList.getFirst()));
            } else {
                throw new IllegalArgumentException(String.format("Required properties '%s' are missing in the application.properties file.", missingPropertyList));
            }
        }
    }

    private static void checkAndAddMissingPropertyToList(Properties propertiesToCheck, List<String> missingPropertyList, String propertyKey) {
        if (!propertiesToCheck.containsKey(propertyKey)) {
            missingPropertyList.add(propertyKey);
        }
    }
}

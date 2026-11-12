package alebuc.torchlight.model.login;

/**
 * A saved connection profile, as stored in {@code ~/.torchlight/login.json}.
 * <p>
 * The secret is deliberately not part of this model: it is never written to disk
 * and must be typed again on each connection.
 *
 * @param name     unique profile name, used as the profile key
 * @param host     Kafka broker host
 * @param port     Kafka broker port
 * @param type     authentication type
 * @param username username for {@link LoginType#CLIENT_CREDENTIALS}, {@code null} otherwise
 */
public record Login(
        String name,
        String host,
        int port,
        LoginType type,
        String username
) {
}

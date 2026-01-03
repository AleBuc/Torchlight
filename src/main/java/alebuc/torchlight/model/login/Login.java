package alebuc.torchlight.model.login;

public record Login(
        String id,
        String host,
        int port,
        LoginType type,
        String username,
        String secret
) {
}

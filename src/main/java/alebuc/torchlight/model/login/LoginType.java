package alebuc.torchlight.model.login;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LoginType {
    NONE("None"),
    CLIENT_CREDENTIALS("Client Credentials");
    public static final LoginType DEFAULT = NONE;

    private final String name;
}

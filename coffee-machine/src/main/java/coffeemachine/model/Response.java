package coffeemachine.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Response {
    private final boolean isSuccess;
    private final String message;

    public static Response success(final String message) {
        return new Response(true, message);
    }

    public static Response error(final String message) {
        return new Response(false, message);
    }
}

package ai.holo.wdyt.common.exception;

public class UsernameAlreadyExistingException extends RuntimeException {

    public UsernameAlreadyExistingException() {
        super("Username already exists");
    }
}

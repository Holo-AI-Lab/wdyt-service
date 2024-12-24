package ai.holo.wdyt.common.exception;

public class InvalidImageException extends RuntimeException {

    public InvalidImageException() {
        super("We could’t detect a person. Please try again!");
    }
}

package ai.holo.wdyt.common.exception;

public class InsufficientCreditException extends RuntimeException {
    public InsufficientCreditException(Long userId) {
        super(String.format("User with id %d has insufficient credit", userId));
    }
}

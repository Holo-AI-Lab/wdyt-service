package ai.holo.wdyt.common.exception;

public class ParameterValidationException extends RuntimeException {

    public ParameterValidationException(String validationMessage) {
        super(validationMessage);
    }
}

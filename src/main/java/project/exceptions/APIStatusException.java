package project.Exceptions;

public class APIStatusException extends Exception {
    public APIStatusException(String message) {
        super(message);
    }

    public APIStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}
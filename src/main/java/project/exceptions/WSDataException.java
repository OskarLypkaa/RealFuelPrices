package project.Exceptions;

public class WSDataException extends Exception {
    public WSDataException(String message) {
        super(message);
    }

    public WSDataException(String message, Throwable cause) {
        super(message, cause);
    }
}

package cz.muni.fi.pv168.warehouse.exceptions;

/**
 * Represents common program exception, mostly SQLExceptions.
 * @author Oliver Mrázik & Martin Zaťko
 * @version 2014-03-25
 */
public class MethodFailureException extends Throwable {

    public MethodFailureException(String msg) {
        super(msg);
    }

    public MethodFailureException(Throwable cause) {
        super(cause);
    }

    public MethodFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}

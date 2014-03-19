package cz.muni.fi.pv168.warehouse.exceptions;

/**
 * Created by Natrezim on 14.3.2014.
 */
public class ShelfSecurityException extends Exception {
    public ShelfSecurityException(String msg) {
        super(msg);
    }

    public ShelfSecurityException(Throwable cause) {
        super(cause);
    }

    public ShelfSecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}

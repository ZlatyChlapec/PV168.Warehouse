package cz.muni.fi.pv168.warehouse.exceptions;

/**
 * Created by Natrezim on 14.3.2014.
 */
public class ShelfCapacityException extends Throwable {
    public ShelfCapacityException(String msg) {
        super(msg);
    }

    public ShelfCapacityException(Throwable cause) {
        super(cause);
    }

    public ShelfCapacityException(String message, Throwable cause) {
        super(message, cause);
    }
}

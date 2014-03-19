package cz.muni.fi.pv168.warehouse.exceptions;

/**
 * Created by Natrezim on 14.3.2014.
 */
public class ShelfWeightException extends Exception{
    public ShelfWeightException(String msg) {
        super(msg);
    }

    public ShelfWeightException(Throwable cause) {
        super(cause);
    }

    public ShelfWeightException(String message, Throwable cause) {
        super(message, cause);
    }
}

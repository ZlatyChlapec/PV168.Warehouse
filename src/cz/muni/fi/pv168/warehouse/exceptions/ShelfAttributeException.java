package cz.muni.fi.pv168.warehouse.exceptions;

/**
 * Represents exception when shelf have some wrong attributes.
 * @author Oliver Mrázik & Martin Zaťko
 * @version 2014-03-29
 */
public class ShelfAttributeException extends Exception {

    public ShelfAttributeException(String msg) {
        super(msg);
    }

    public ShelfAttributeException(Throwable cause) {
        super(cause);
    }

    public ShelfAttributeException(String message, Throwable cause) {
        super(message, cause);
    }
}

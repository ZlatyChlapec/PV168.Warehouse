package cz.muni.fi.pv168.warehouse.exceptions;

/**
 * Created by Natrezim on 22. 3. 2014.
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

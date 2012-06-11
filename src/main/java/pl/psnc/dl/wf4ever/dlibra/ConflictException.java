/**
 * 
 */
package pl.psnc.dl.wf4ever.dlibra;

import pl.psnc.dlibra.service.DuplicatedValueException;

/**
 * A value that is being added to RODL already exists.
 * 
 * @author piotrek
 * 
 */
public class ConflictException extends Exception {

    /** id. */
    private static final long serialVersionUID = -3060338267583511733L;


    /**
     * Constructor.
     * 
     * @param message
     *            exception message
     */
    public ConflictException(String message) {
        super(message);
    }


    /**
     * Constructor.
     * 
     * @param e
     *            dLibra exception
     */
    public ConflictException(DuplicatedValueException e) {
        this(String.format("%s (%s: %s)", e.getMessage(), e.getKey(), e.getValue()));
    }

}

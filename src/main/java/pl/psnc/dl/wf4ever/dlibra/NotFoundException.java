package pl.psnc.dl.wf4ever.dlibra;

import pl.psnc.dlibra.service.IdNotFoundException;

/**
 * Requested resource does not exist.
 * 
 * @author piotrek
 * 
 */
public class NotFoundException extends Exception {

    /** id. */
    private static final long serialVersionUID = -3060338267583511733L;


    /**
     * Constructor.
     * 
     * @param message
     *            exception message
     */
    public NotFoundException(String message) {
        super(message);
    }


    /**
     * Constructor.
     * 
     * @param e
     *            dLibra exception
     */
    public NotFoundException(IdNotFoundException e) {
        this(String.format("%s (%s)", e.getMessage(), e.getNotFoundId()));
    }

}

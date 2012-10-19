package pl.psnc.dl.wf4ever.dlibra;

/**
 * Operation failed because the access was denied.
 * 
 * @author piotrekhol
 * 
 */
public class AccessDeniedException extends Exception {

    /** id. */
    private static final long serialVersionUID = 2157102296863571887L;


    /**
     * Constructor.
     * 
     * @param message
     *            message
     */
    public AccessDeniedException(String message) {
        super(message);
    }


    /**
     * Constructor.
     * 
     * @param message
     *            message
     * @param e
     *            parent exception
     */
    public AccessDeniedException(String message, Exception e) {
        super(message, e);
    }
}

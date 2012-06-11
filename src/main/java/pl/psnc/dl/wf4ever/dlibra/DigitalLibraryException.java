package pl.psnc.dl.wf4ever.dlibra;

/**
 * An RODL exception.
 * 
 * @author piotrhol
 * 
 */
public class DigitalLibraryException extends Exception {

    /** id. */
    private static final long serialVersionUID = 8004921765200303834L;


    /**
     * Constructor.
     * 
     * @param e
     *            base exception
     */
    public DigitalLibraryException(Exception e) {
        super(e);
    }

}

/**
 * 
 */
package pl.psnc.dl.wf4ever.dlibra;

import pl.psnc.dlibra.service.IdNotFoundException;

/**
 * @author piotrek
 * 
 */
public class NotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3060338267583511733L;

	public NotFoundException(String message) {
		super(message);
	}

	public NotFoundException(IdNotFoundException e) {
		this(String.format("%s (%s)", e.getMessage(), e.getNotFoundId()));
	}

}

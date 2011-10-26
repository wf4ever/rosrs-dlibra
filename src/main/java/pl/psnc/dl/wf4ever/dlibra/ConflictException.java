/**
 * 
 */
package pl.psnc.dl.wf4ever.dlibra;

import pl.psnc.dlibra.service.DuplicatedValueException;

/**
 * @author piotrek
 * 
 */
public class ConflictException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3060338267583511733L;

	public ConflictException(String message) {
		super(message);
	}

	public ConflictException(DuplicatedValueException e) {
		this(String.format("%s (%s: %s)", e.getMessage(), e.getKey(), e.getValue()));
	}

}

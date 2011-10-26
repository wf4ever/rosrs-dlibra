/**
 * 
 */
package pl.psnc.dl.wf4ever.dlibra;

import java.util.Date;

/**
 * @author piotrek
 *
 */
public class Snapshot {
	
	private long id;
	
	private boolean published;
	
	private Date creationDate;

	/**
	 * @param id
	 * @param published
	 * @param creationDate
	 */
	public Snapshot(long id, boolean published, Date creationDate) {
		this.id = id;
		this.published = published;
		this.creationDate = creationDate;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @return the published
	 */
	public boolean isPublished() {
		return published;
	}

	/**
	 * @return the creationDate
	 */
	public Date getCreationDate() {
		return creationDate;
	}

}

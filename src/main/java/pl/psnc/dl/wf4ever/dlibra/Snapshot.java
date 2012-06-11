/**
 * 
 */
package pl.psnc.dl.wf4ever.dlibra;

import java.util.Date;

/**
 * Represents a version of an RO.
 * 
 * @author piotrek
 * 
 */
public class Snapshot {

    /** snapshot id. */
    private final long id;

    /** if the version is published (indexed) or not. */
    private final boolean published;

    /** snapshor creation date. */
    private final Date creationDate;


    /**
     * Consructor.
     * 
     * @param id
     *            id
     * @param published
     *            published
     * @param creationDate
     *            creation date
     */
    public Snapshot(long id, boolean published, Date creationDate) {
        this.id = id;
        this.published = published;
        this.creationDate = creationDate;
    }


    public long getId() {
        return id;
    }


    public boolean isPublished() {
        return published;
    }


    public Date getCreationDate() {
        return creationDate;
    }

}

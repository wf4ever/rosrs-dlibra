package pl.psnc.dl.wf4ever.dlibra;

import org.joda.time.DateTime;

/**
 * File metadata.
 * 
 * @author piotrhol
 * 
 */
public class ResourceInfo {

    /** file name. */
    private final String name;

    /** file checksum. */
    private final String checksum;

    /** file checksum method. */
    private final String digestMethod;

    /** file size in bytes. */
    private final long sizeInBytes;

    /** last modification date. */
    private final DateTime lastModified;


    /**
     * Constructor.
     * 
     * @param name
     *            file name
     * @param checksum
     *            checksum
     * @param sizeInBytes
     *            size in bytes
     * @param digestMethod
     *            i.e. MD5, SHA1
     * @param lastModified
     *            date of last modification
     */
    public ResourceInfo(String name, String checksum, long sizeInBytes, String digestMethod, DateTime lastModified) {
        this.name = name;
        this.checksum = checksum;
        this.sizeInBytes = sizeInBytes;
        this.digestMethod = digestMethod;
        this.lastModified = lastModified;
    }


    public String getName() {
        return name;
    }


    public String getChecksum() {
        return checksum;
    }


    public long getSizeInBytes() {
        return sizeInBytes;
    }


    public String getDigestMethod() {
        return digestMethod;
    }


    public DateTime getLastModified() {
        return lastModified;
    }

}

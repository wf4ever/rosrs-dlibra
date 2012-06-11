package pl.psnc.dl.wf4ever.dlibra;

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
     */
    public ResourceInfo(String name, String checksum, long sizeInBytes, String digestMethod) {
        this.name = name;
        this.checksum = checksum;
        this.sizeInBytes = sizeInBytes;
        this.digestMethod = digestMethod;
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

}

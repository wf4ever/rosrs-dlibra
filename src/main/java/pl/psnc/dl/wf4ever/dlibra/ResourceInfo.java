/**
 * 
 */
package pl.psnc.dl.wf4ever.dlibra;

/**
 * @author piotrhol
 * 
 */
public class ResourceInfo {

    private final String name;

    private final String checksum;

    private final String digestMethod;

    private final long sizeInBytes;


    /**
     * @param name
     * @param checksum
     * @param sizeInBytes
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


    /**
     * @return the digestMethod
     */
    public String getDigestMethod() {
        return digestMethod;
    }

}

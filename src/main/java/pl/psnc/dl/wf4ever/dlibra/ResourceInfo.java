/**
 * 
 */
package pl.psnc.dl.wf4ever.dlibra;

/**
 * @author piotrhol
 *
 */
public class ResourceInfo
{

	private final String name;

	private final String checksum;

	private final long sizeInBytes;


	/**
	 * @param name
	 * @param checksum
	 * @param sizeInBytes
	 */
	public ResourceInfo(String name, String checksum, long sizeInBytes)
	{
		this.name = name;
		this.checksum = checksum;
		this.sizeInBytes = sizeInBytes;
	}


	public String getName()
	{
		return name;
	}


	public String getChecksum()
	{
		return checksum;
	}


	public long getSizeInBytes()
	{
		return sizeInBytes;
	}

}

package pl.psnc.dl.wf4ever;

/**
 * 
 * @author nowakm
 *
 */
public final class Constants
{

	/**
	 * No instances allowed
	 */
	private Constants()
	{
		// nop
	}

	public final static String MANIFEST_FILENAME = "manifest.rdf";

	public static final String RDF_XML_MIME_TYPE = "application/rdf+xml";

	/**
	 * Used for accessing DLibraDataSource stored in HttpRequest
	 */
	public static final String DLIBRA_DATA_SOURCE = "dLibraDataSource";

	public static final long EDITION_QUERY_PARAM_DEFAULT = 0L;

	public static final String EDITION_QUERY_PARAM_DEFAULT_STRING = "0";

}

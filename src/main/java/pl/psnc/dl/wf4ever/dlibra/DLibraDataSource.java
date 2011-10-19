package pl.psnc.dl.wf4ever.dlibra;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import pl.psnc.dlibra.content.ContentServer;
import pl.psnc.dlibra.metadata.DirectoryId;
import pl.psnc.dlibra.metadata.LibCollectionId;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.mgmt.DLStaticServiceResolver;
import pl.psnc.dlibra.mgmt.UserServiceResolver;
import pl.psnc.dlibra.search.server.SearchServer;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.dlibra.user.UserManager;

/**
 * 
 * @author nowakm, piotrhol
 * 
 */
public class DLibraDataSource
{

	@SuppressWarnings("unused")
	private final static Logger logger = Logger
			.getLogger(DLibraDataSource.class);

	public static final DirectoryId ROOT_DIRECTORY_ID = new DirectoryId(1L);

	public final static int BUFFER_SIZE = 4096;

	private final UserServiceResolver serviceResolver;

	private final String userLogin;

	private final ContentServer contentServer;

	private final UserManager userManager;

	private final MetadataServer metadataServer;

	private final UsersHelper usersHelper;

	private final PublicationsHelper publicationsHelper;

	private final EditionHelper editionHelper;

	private final FilesHelper filesHelper;

	private final ManifestHelper manifestHelper;

	private final AttributesHelper attributesHelper;

	private final DirectoryId workspacesContainerDirectoryId;

	private final SearchServer searchServer;

	private final LibCollectionId collectionId;


	public DLibraDataSource(UserServiceResolver userServiceResolver,
			String userLogin, long workspacesContainerDirectoryId,
			long collectionId)
		throws RemoteException, DLibraException
	{
		this.serviceResolver = userServiceResolver;
		this.userLogin = userLogin;
		this.workspacesContainerDirectoryId = new DirectoryId(
				workspacesContainerDirectoryId);
		this.collectionId = new LibCollectionId(collectionId);

		metadataServer = DLStaticServiceResolver.getMetadataServer(
			serviceResolver, null);

		contentServer = DLStaticServiceResolver.getContentServer(
			serviceResolver, null);

		userManager = DLStaticServiceResolver.getUserServer(serviceResolver,
			null).getUserManager();

		searchServer = DLStaticServiceResolver.getSearchServer(serviceResolver,
			null);

		usersHelper = new UsersHelper(this);
		publicationsHelper = new PublicationsHelper(this);
		filesHelper = new FilesHelper(this);
		manifestHelper = new ManifestHelper(this);
		attributesHelper = new AttributesHelper(this);
		editionHelper = new EditionHelper(this);
	}


	UserServiceResolver getServiceResolver()
	{
		return serviceResolver;
	}


	public ContentServer getContentServer()
	{
		return contentServer;
	}


	UserManager getUserManager()
	{
		return userManager;
	}


	public MetadataServer getMetadataServer()
	{
		return metadataServer;
	}


	public UsersHelper getUsersHelper()
	{
		return usersHelper;
	}


	public PublicationsHelper getPublicationsHelper()
	{
		return publicationsHelper;
	}


	public FilesHelper getFilesHelper()
	{
		return filesHelper;
	}


	public ManifestHelper getManifestHelper()
	{
		return manifestHelper;
	}


	public AttributesHelper getAttributesHelper()
	{
		return attributesHelper;
	}


	String getUserLogin()
	{
		return userLogin;
	}


	DirectoryId getWorkspacesContainerDirectoryId()
	{
		return workspacesContainerDirectoryId;
	}


	public SearchServer getSearchServer()
	{
		return searchServer;
	}


	public LibCollectionId getCollectionId()
	{
		return collectionId;
	}


	public EditionHelper getEditionHelper()
	{
		return editionHelper;
	}


	public boolean isAdmin()
		throws RemoteException, IdNotFoundException, DLibraException
	{
		return usersHelper.isAdmin(userLogin);
	}

}

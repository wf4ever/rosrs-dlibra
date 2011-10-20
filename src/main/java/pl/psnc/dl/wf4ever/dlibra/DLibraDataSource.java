package pl.psnc.dl.wf4ever.dlibra;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.DigitalLibrary;
import pl.psnc.dl.wf4ever.DigitalLibraryException;
import pl.psnc.dl.wf4ever.UserProfile;
import pl.psnc.dlibra.content.ContentServer;
import pl.psnc.dlibra.metadata.AbstractPublicationInfo;
import pl.psnc.dlibra.metadata.DirectoryId;
import pl.psnc.dlibra.metadata.Edition;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.LibCollectionId;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.PublicationInfo;
import pl.psnc.dlibra.mgmt.DLStaticServiceResolver;
import pl.psnc.dlibra.mgmt.UserServiceResolver;
import pl.psnc.dlibra.search.server.SearchServer;
import pl.psnc.dlibra.service.AuthorizationToken;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.DuplicatedValueException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.dlibra.service.ServiceUrl;
import pl.psnc.dlibra.system.UserInterface;
import pl.psnc.dlibra.user.User;
import pl.psnc.dlibra.user.UserManager;

/**
 * 
 * @author nowakm, piotrhol
 * 
 */
public class DLibraDataSource
	implements DigitalLibrary
{

	@SuppressWarnings("unused")
	private final static Logger logger = Logger
			.getLogger(DLibraDataSource.class);

	public static final DirectoryId ROOT_DIRECTORY_ID = new DirectoryId(1L);

	public final static int BUFFER_SIZE = 4096;

	private final UserServiceResolver serviceResolver;

	private final String userLogin;

	private final String userPassword;

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


	public DLibraDataSource(String host, int port,
			long workspacesContainerDirectoryId, long collectionId,
			String userLogin, String password)
		throws RemoteException, DLibraException, MalformedURLException,
		UnknownHostException
	{
		AuthorizationToken authorizationToken = new AuthorizationToken(
				userLogin, password);
		serviceResolver = new UserServiceResolver(new ServiceUrl(
				InetAddress.getByName(host), UserInterface.SERVICE_TYPE, port),
				authorizationToken);

		this.userLogin = userLogin;
		this.userPassword = password;
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


	ContentServer getContentServer()
	{
		return contentServer;
	}


	UserManager getUserManager()
	{
		return userManager;
	}


	MetadataServer getMetadataServer()
	{
		return metadataServer;
	}


	UsersHelper getUsersHelper()
	{
		return usersHelper;
	}


	PublicationsHelper getPublicationsHelper()
	{
		return publicationsHelper;
	}


	FilesHelper getFilesHelper()
	{
		return filesHelper;
	}


	ManifestHelper getManifestHelper()
	{
		return manifestHelper;
	}


	AttributesHelper getAttributesHelper()
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


	SearchServer getSearchServer()
	{
		return searchServer;
	}


	LibCollectionId getCollectionId()
	{
		return collectionId;
	}


	public EditionHelper getEditionHelper()
	{
		return editionHelper;
	}


	@Override
	public UserProfile getUserProfile()
		throws DigitalLibraryException, IdNotFoundException
	{
		User user;
		try {
			user = userManager.getUserData(userLogin);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (RemoteException | DLibraException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
		return new UserProfile(userLogin, userPassword, user.getName(),
				userLogin.equals("wfadmin"));
	}


	@Override
	public List<String> getResourcePaths(String workspaceId,
			String researchObjectId, String versionId, String folder)
		throws DigitalLibraryException, IdNotFoundException
	{
		EditionId editionId;
		try {
			editionId = getEditionHelper().getLastEditionId(researchObjectId,
				versionId);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (RemoteException | DLibraException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
		return getResourcePaths(workspaceId, researchObjectId, versionId,
			folder, editionId.getId());
	}


	@Override
	public List<String> getResourcePaths(String workspaceId,
			String researchObjectId, String versionId, String folder,
			long editionId)
		throws DigitalLibraryException, IdNotFoundException
	{
		try {
			return getFilesHelper().getFilePathsInFolder(
				new EditionId(editionId), folder);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (RemoteException | DLibraException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
	}


	@Override
	public String getFileMetadata(String workspaceId, String researchObjectId,
			String versionId, String folder, URI baseURI)
		throws DigitalLibraryException, IdNotFoundException
	{
		EditionId editionId;
		try {
			editionId = getEditionHelper().getLastEditionId(researchObjectId,
				versionId);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (RemoteException | DLibraException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
		return getFileMetadata(workspaceId, researchObjectId, versionId,
			folder, editionId.getId(), baseURI);
	}


	@Override
	public String getFileMetadata(String workspaceId, String researchObjectId,
			String versionId, String folder, long editionId, URI baseURI)
		throws DigitalLibraryException, IdNotFoundException
	{
		try {
			return getFilesHelper().getFileMetadata(new EditionId(editionId),
				folder, baseURI.toString());
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (RemoteException | DLibraException | TransformerException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
	}


	@Override
	public InputStream getZippedFolder(String workspaceId,
			String researchObjectId, String versionId, String folder)
		throws DigitalLibraryException, IdNotFoundException
	{
		EditionId editionId;
		try {
			editionId = getEditionHelper().getLastEditionId(researchObjectId,
				versionId);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (RemoteException | DLibraException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
		return getZippedFolder(workspaceId, researchObjectId, versionId,
			folder, editionId.getId());
	}


	@Override
	public InputStream getZippedFolder(String workspaceId,
			String researchObjectId, String versionId, String folder,
			long editionId)
		throws DigitalLibraryException, IdNotFoundException
	{
		try {
			return getFilesHelper().getZippedFolder(new EditionId(editionId),
				folder);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (RemoteException | DLibraException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
	}


	@Override
	public InputStream getFileContents(String workspaceId,
			String researchObjectId, String versionId, String filePath)
		throws DigitalLibraryException, IdNotFoundException
	{
		EditionId editionId;
		try {
			editionId = getEditionHelper().getLastEditionId(researchObjectId,
				versionId);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (RemoteException | DLibraException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
		return getFileContents(workspaceId, researchObjectId, versionId,
			filePath, editionId.getId());
	}


	@Override
	public InputStream getFileContents(String workspaceId,
			String researchObjectId, String versionId, String filePath,
			long editionId)
		throws DigitalLibraryException, IdNotFoundException
	{
		try {
			return getFilesHelper().getFileContents(new EditionId(editionId),
				filePath);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (RemoteException | DLibraException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
	}


	@Override
	public String getFileMimeType(String workspaceId, String researchObjectId,
			String versionId, String filePath)
		throws DigitalLibraryException, IdNotFoundException
	{
		EditionId editionId;
		try {
			editionId = getEditionHelper().getLastEditionId(researchObjectId,
				versionId);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (RemoteException | DLibraException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
		return getFileMimeType(workspaceId, researchObjectId, versionId,
			filePath, editionId.getId());
	}


	@Override
	public String getFileMimeType(String workspaceId, String researchObjectId,
			String versionId, String filePath, long editionId)
		throws DigitalLibraryException, IdNotFoundException
	{
		try {
			return getFilesHelper().getFileMimeType(new EditionId(editionId),
				filePath);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (RemoteException | DLibraException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
	}


	@Override
	public void createOrUpdateFile(URI versionUri, String workspaceId,
			String researchObjectId, String versionId, String filePath,
			InputStream inputStream, String type)
		throws DigitalLibraryException, IdNotFoundException
	{
		try {
			getFilesHelper().createOrUpdateFile(versionUri, researchObjectId,
				versionId, filePath, inputStream, type);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (IOException | DLibraException | TransformerException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
	}


	@Override
	public void deleteFile(URI versionUri, String workspaceId,
			String researchObjectId, String versionId, String filePath)
		throws DigitalLibraryException, IdNotFoundException
	{
		try {
			getFilesHelper().deleteFile(versionUri, researchObjectId,
				versionId, filePath);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (IOException | DLibraException | TransformerException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
	}


	@Override
	public List<String> getResearchObjectIds(String workspaceId)
		throws DigitalLibraryException, IdNotFoundException
	{
		List<AbstractPublicationInfo> infos;
		try {
			infos = getPublicationsHelper().listUserGroupPublications(
				Publication.PUB_GROUP_MID);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (RemoteException | DLibraException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
		List<String> ids = new ArrayList<String>();
		for (AbstractPublicationInfo info : infos) {
			ids.add(info.getLabel());
		}
		return ids;
	}


	@Override
	public List<String> getVersionIds(String workspaceId,
			MultivaluedMap<String, String> queryParameters)
		throws DigitalLibraryException, IdNotFoundException
	{
		List<AbstractPublicationInfo> infos;
		try {
			infos = getPublicationsHelper().listUserPublications(
				queryParameters);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (RemoteException | DLibraException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
		List<String> ids = new ArrayList<String>();
		for (AbstractPublicationInfo info : infos) {
			ids.add(info.getLabel());
		}
		return ids;
	}


	@Override
	public void createResearchObject(String workspaceId, String researchObjectId)
		throws DigitalLibraryException, IdNotFoundException
	{
		try {
			getPublicationsHelper().createGroupPublication(workspaceId,
				researchObjectId);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (RemoteException | DLibraException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
	}


	@Override
	public List<String> getVersionIds(String workspaceId,
			String researchObjectId)
		throws DigitalLibraryException, IdNotFoundException
	{
		List<PublicationInfo> infos;
		try {
			infos = getPublicationsHelper().listPublicationsInGroup(
				researchObjectId);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (RemoteException | DLibraException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
		List<String> ids = new ArrayList<String>();
		for (AbstractPublicationInfo info : infos) {
			ids.add(info.getLabel());
		}
		return ids;
	}


	@Override
	public void createVersion(String workspaceId, String researchObjectId,
			String version, URI resourceUri)
		throws DigitalLibraryException, IdNotFoundException
	{
		try {
			getPublicationsHelper().createPublication(researchObjectId,
				version, null, resourceUri);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (IOException | DLibraException | TransformerException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
	}


	@Override
	public void createVersion(String workspaceId, String researchObjectId,
			String version, String baseVersion, URI resourceUri)
		throws DigitalLibraryException, IdNotFoundException
	{
		try {
			getPublicationsHelper().createPublication(researchObjectId,
				version, baseVersion, resourceUri);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (IOException | DLibraException | TransformerException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
	}


	@Override
	public void deleteResearchObject(String workspaceId, String researchObjectId)
		throws DigitalLibraryException, IdNotFoundException
	{
		try {
			getPublicationsHelper().deleteGroupPublication(researchObjectId);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (RemoteException | DLibraException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
	}


	@Override
	public void createUser(String userId, String password)
		throws DigitalLibraryException, IdNotFoundException,
		DuplicatedValueException
	{
		try {
			getUsersHelper().createUser(userId, password);
		}
		catch (IdNotFoundException | DuplicatedValueException e) {
			throw e;
		}
		catch (RemoteException | DLibraException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
	}


	@Override
	public boolean userExists(String userId)
		throws DigitalLibraryException, IdNotFoundException
	{
		try {
			return getUsersHelper().userExists(userId);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (RemoteException | DLibraException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
	}


	@Override
	public void deleteUser(String userId)
		throws DigitalLibraryException, IdNotFoundException
	{
		try {
			getUsersHelper().deleteUser(userId);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (RemoteException | DLibraException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
	}


	@Override
	public Set<Edition> getEditionList(String workspaceId,
			String researchObjectId, String versionId)
		throws DigitalLibraryException, IdNotFoundException
	{
		try {
			return getEditionHelper().getEditionList(researchObjectId,
				versionId);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (RemoteException | DLibraException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
	}


	@Override
	public InputStream getManifest(String workspaceId, String researchObjectId,
			String versionId)
		throws DigitalLibraryException, IdNotFoundException
	{
		EditionId editionId;
		try {
			editionId = getEditionHelper().getLastEditionId(researchObjectId,
				versionId);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (RemoteException | DLibraException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
		return getManifest(workspaceId, researchObjectId, versionId,
			editionId.getId());
	}


	@Override
	public InputStream getManifest(String workspaceId, String researchObjectId,
			String versionId, long editionId)
		throws DigitalLibraryException, IdNotFoundException
	{
		try {
			return getManifestHelper().getManifest(new EditionId(editionId));
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (IOException | DLibraException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
	}


	@Override
	public InputStream getZippedVersion(String workspaceId,
			String researchObjectId, String versionId)
		throws DigitalLibraryException, IdNotFoundException
	{
		EditionId editionId;
		try {
			editionId = getEditionHelper().getLastEditionId(researchObjectId,
				versionId);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (RemoteException | DLibraException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
		return getZippedVersion(workspaceId, researchObjectId, versionId,
			editionId.getId());
	}


	@Override
	public InputStream getZippedVersion(String workspaceId,
			String researchObjectId, String versionId, long editionId)
		throws DigitalLibraryException, IdNotFoundException
	{
		try {
			return getPublicationsHelper().getZippedPublication(
				new EditionId(editionId));
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (RemoteException | DLibraException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
	}


	@Override
	public void publishVersion(String workspaceId, String researchObjectId,
			String versionId)
		throws DigitalLibraryException, IdNotFoundException
	{
		try {
			getPublicationsHelper().publishPublication(researchObjectId,
				versionId);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (RemoteException | DLibraException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
	}


	@Override
	public void unpublishVersion(String workspaceId, String researchObjectId,
			String versionId)
		throws DigitalLibraryException, IdNotFoundException
	{
		try {
			getPublicationsHelper().unpublishPublication(researchObjectId,
				versionId);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (RemoteException | DLibraException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
	}


	@Override
	public void updateManifest(URI versionUri, String researchObjectId,
			String versionId, ByteArrayInputStream body)
		throws DigitalLibraryException, IncorrectManifestException,
		IdNotFoundException
	{
		try {
			getManifestHelper().updateManifest(versionUri, researchObjectId,
				versionId, body);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (IOException | DLibraException | TransformerException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
	}


	@Override
	public EditionId createEdition(String workspaceId, String versionName,
			String researchObjectId, String versionId)
		throws DigitalLibraryException, IdNotFoundException
	{
		try {
			return getEditionHelper().createEdition(versionName,
				researchObjectId, versionId);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (RemoteException | DLibraException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
	}


	@Override
	public void deleteVersion(String workspaceId, String researchObjectId,
			String versionId, URI versionURI)
		throws DigitalLibraryException, IdNotFoundException
	{
		try {
			getPublicationsHelper().deletePublication(researchObjectId,
				versionId, versionURI);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (IOException | DLibraException | TransformerException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
	}


	@Override
	public List<String> getWorkspaceIds()
		throws DigitalLibraryException, IdNotFoundException
	{
		List<AbstractPublicationInfo> infos;
		try {
			infos = getPublicationsHelper().listUserGroupPublications(
				Publication.PUB_GROUP_ROOT);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (RemoteException | DLibraException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
		List<String> ids = new ArrayList<String>();
		for (AbstractPublicationInfo info : infos) {
			ids.add(info.getLabel());
		}
		return ids;
	}


	@Override
	public void createWorkspace(String workspaceId)
		throws DigitalLibraryException, IdNotFoundException
	{
		try {
			getPublicationsHelper().createGroupPublication(workspaceId);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (RemoteException | DLibraException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
	}


	@Override
	public void deleteWorkspace(String workspaceId)
		throws DigitalLibraryException, IdNotFoundException
	{
		try {
			getPublicationsHelper().deleteGroupPublication(workspaceId);
		}
		catch (IdNotFoundException e) {
			throw e;
		}
		catch (RemoteException | DLibraException e) {
			throw new DigitalLibraryException(e.getMessage());
		}
	}
}

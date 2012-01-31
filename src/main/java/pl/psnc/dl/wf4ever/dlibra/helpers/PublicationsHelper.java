/**
 * 
 */
package pl.psnc.dl.wf4ever.dlibra.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import pl.psnc.dlibra.common.Info;
import pl.psnc.dlibra.common.InputFilter;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.AbstractPublicationInfo;
import pl.psnc.dlibra.metadata.DirectoryFilter;
import pl.psnc.dlibra.metadata.DirectoryId;
import pl.psnc.dlibra.metadata.DirectoryInfo;
import pl.psnc.dlibra.metadata.DirectoryManager;
import pl.psnc.dlibra.metadata.Edition;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.ElementId;
import pl.psnc.dlibra.metadata.ElementInfo;
import pl.psnc.dlibra.metadata.File;
import pl.psnc.dlibra.metadata.FileManager;
import pl.psnc.dlibra.metadata.GroupPublicationInfo;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.PublicationFilter;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.PublicationInfo;
import pl.psnc.dlibra.metadata.PublicationManager;
import pl.psnc.dlibra.metadata.Version;
import pl.psnc.dlibra.metadata.VersionId;
import pl.psnc.dlibra.service.AccessDeniedException;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.DuplicatedValueException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.dlibra.user.User;
import pl.psnc.dlibra.user.UserManager;

/**
 * @author piotrhol
 * 
 */
public class PublicationsHelper
{

	private final static Logger logger = Logger.getLogger(PublicationsHelper.class);

	private final DLibraDataSource dLibra;

	private final PublicationManager publicationManager;

	private final DirectoryManager directoryManager;

	private final UserManager userManager;

	private final FileManager fileManager;


	public PublicationsHelper(DLibraDataSource dLibraDataSource)
		throws RemoteException
	{
		this.dLibra = dLibraDataSource;

		publicationManager = dLibraDataSource.getMetadataServer().getPublicationManager();
		directoryManager = dLibraDataSource.getMetadataServer().getDirectoryManager();
		userManager = dLibraDataSource.getUserManager();
		fileManager = dLibraDataSource.getMetadataServer().getFileManager();
	}


	/**
	 * Returns list of all group publications (ROs) of the current user.
	 * 
	 * @return
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	public List<AbstractPublicationInfo> listUserGroupPublications(byte groupState)
		throws RemoteException, DLibraException
	{
		DirectoryId workspaceDir = getWorkspaceDirectoryId();
		Collection<Info> resultInfos = directoryManager.getObjects(
			new DirectoryFilter(null, workspaceDir).setGroupStatus(groupState).setState(
				(byte) (Publication.PUB_STATE_ALL - Publication.PUB_STATE_PERMANENT_DELETED)),
			new OutputFilter(ElementInfo.class, List.class)).getResultInfos();

		ArrayList<AbstractPublicationInfo> result = new ArrayList<AbstractPublicationInfo>();
		for (Info info : resultInfos) {
			if (info instanceof GroupPublicationInfo) {
				result.add((GroupPublicationInfo) info);
			}
		}
		return result;
	}


	/**
	 * Creates a new group publication (workspace) for the current user.
	 * 
	 * @param groupPublicationName
	 * @return
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	public PublicationId createGroupPublication(String groupPublicationName)
		throws RemoteException, DLibraException
	{
		DirectoryId parent = getWorkspaceDirectoryId();
		try {
			getGroupId(groupPublicationName);
			throw new DuplicatedValueException(null, "Group publication already exists", groupPublicationName);
		}
		catch (IdNotFoundException e) {
			// OK - group does not exist
		}

		Publication publication = new Publication(parent);
		publication.setGroupStatus(Publication.PUB_GROUP_ROOT);
		publication.setName(groupPublicationName);

		return publicationManager.createPublication(publication);
	}


	/**
	 * Creates a new group publication (RO) for the current user.
	 * 
	 * @param groupPublicationName
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	public void createGroupPublication(String parentName, String publicationName)
		throws RemoteException, DLibraException
	{
		PublicationId groupId = getGroupId(parentName);
		try {
			getPublicationId(groupId, publicationName);
			throw new DuplicatedValueException(null, "RO Version already exists", parentName + "/" + publicationName);
		}
		catch (IdNotFoundException e) {
			// OK - the publication does not exist
		}

		Publication publication = getNewPublication(publicationName, groupId, Publication.PUB_GROUP_MID);
		PublicationId publicationId = publicationManager.createPublication(publication);
		logger.debug(String.format("Created publication %s with id %s", publication.getName(), publicationId));
	}


	/**
	 * Deletes a group publication (workspace or RO) for the current user.
	 * 
	 * @param groupPublicationName
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	public void deleteGroupPublication(String groupPublicationName)
		throws RemoteException, DLibraException
	{
		PublicationId groupId = getGroupId(groupPublicationName);
		publicationManager.removePublication(groupId, true, "Group publication removed");
	}


	/**
	 * Returns list of all publications (versions) for given group publication (RO).
	 * 
	 * @param groupPublicationName
	 * @return
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	public List<PublicationInfo> listPublicationsInGroup(String groupPublicationName)
		throws RemoteException, DLibraException
	{
		PublicationId groupId = getGroupId(groupPublicationName);

		InputFilter in = new PublicationFilter(null, groupId).setGroupStatus(Publication.PUB_GROUP_LEAF)
				.setPublicationState((byte) (Publication.PUB_STATE_ALL - Publication.PUB_STATE_PERMANENT_DELETED));
		OutputFilter out = new OutputFilter(AbstractPublicationInfo.class, List.class);
		Collection<Info> resultInfos = publicationManager.getObjects(in, out).getResultInfos();

		ArrayList<PublicationInfo> result = new ArrayList<PublicationInfo>();
		for (Info info : resultInfos) {
			result.add((PublicationInfo) info);
		}
		return result;
	}


	/**
	 * Creates new publication (version) in a group publication (RO).
	 * <p>
	 * If basePublicationName is not null, then the new publication is a copy of base
	 * publication.
	 * 
	 * @param groupPublicationName
	 * @param publicationName
	 * @param basePublicationName
	 *            Optional name of base publication to copy from
	 * @throws DLibraException
	 * @throws IOException
	 * @throws TransformerException
	 */
	public void createPublication(String groupPublicationName, String publicationName, String basePublicationName,
			InputStream mainFileContent, String mainFilePath, String mainFileMimeType)
		throws DLibraException, IOException, TransformerException
	{
		PublicationId groupId = getGroupId(groupPublicationName);
		try {
			getPublicationId(groupId, publicationName);
			throw new DuplicatedValueException(null, "RO Version already exists", groupPublicationName + "/"
					+ publicationName);
		}
		catch (IdNotFoundException e) {
			// OK - the publication does not exist
		}

		Publication publication = getNewPublication(publicationName, groupId, Publication.PUB_GROUP_LEAF);
		PublicationId publicationId = publicationManager.createPublication(publication);
		logger.debug(String.format("Created publication %s with id %s", publication.getName(), publicationId));
		if (basePublicationName != null && !basePublicationName.isEmpty()) {
			PublicationId basePublicationId = getPublicationId(groupId, basePublicationName);
			preparePublicationAsACopy(groupPublicationName, publicationName, publicationId, basePublicationName,
				basePublicationId);
		}
		else {
			preparePublicationAsNew(groupPublicationName, publicationName, publicationId, mainFileContent,
				mainFilePath, mainFileMimeType);
		}

		dLibra.getMetadataServer()
				.getLibCollectionManager()
				.addToCollections(Arrays.asList(dLibra.getCollectionId()), Arrays.asList((ElementId) publicationId),
					false);
	}


	public void publishPublication(String groupPublicationName, String publicationName)
		throws RemoteException, DLibraException
	{
		unpublishPublication(groupPublicationName, publicationName);

		Edition edition = dLibra.getEditionHelper().getLastEdition(groupPublicationName, publicationName);
		edition.setPublished(true);
		publicationManager.setEditionData(edition);
	}


	public void unpublishPublication(String groupPublicationName, String publicationName)
		throws RemoteException, DLibraException
	{
		Set<Edition> editions = dLibra.getEditionHelper().getEditionList(groupPublicationName, publicationName);
		for (Edition edition : editions) {
			if (edition.isPublished()) {
				edition.setPublished(false);
				publicationManager.setEditionData(edition);
			}
		}
	}


	private Publication getNewPublication(String publicationName, PublicationId groupId, byte groupStatus)
		throws RemoteException, DLibraException
	{
		Publication publication = new Publication(null, getWorkspaceDirectoryId());
		publication.setParentPublicationId(groupId);
		publication.setName(publicationName);
		publication.setPosition(0);
		publication.setGroupStatus(groupStatus);
		publication.setSecured(false);
		publication.setState(Publication.PUB_STATE_ACTUAL);
		return publication;
	}


	private EditionId preparePublicationAsNew(String groupPublicationName, String publicationName,
			PublicationId publicationId, InputStream mainFileContent, String mainFilePath, String mainFileMimeType)
		throws DLibraException, AccessDeniedException, IdNotFoundException, RemoteException, TransformerException,
		IOException
	{
		Date creationDate = new Date();

		File mainFile = new File(mainFileMimeType, publicationId, "/" + mainFilePath);
		Version createdVersion = fileManager.createVersion(mainFile, 0, creationDate, "");
		OutputStream output = dLibra.getContentServer().getVersionOutputStream(createdVersion.getId());
		try {
			IOUtils.copy(mainFileContent, output);
		}
		finally {
			mainFileContent.close();
			output.close();
		}

		publicationManager.setMainFile(publicationId, createdVersion.getFileId());

		EditionId editionId = dLibra.getEditionHelper().createEdition(publicationName, publicationId,
			new VersionId[] {});

		publicationManager.addEditionVersion(editionId, createdVersion.getId());

		return editionId;
	}


	private void preparePublicationAsACopy(String groupPublicationName, String publicationName,
			PublicationId publicationId, String basePublicationName, PublicationId basePublicationId)
		throws RemoteException, DLibraException, AccessDeniedException, IdNotFoundException, IOException,
		TransformerException
	{
		VersionId[] copyVersions = dLibra.getFilesHelper().copyVersions(basePublicationId, publicationId);
		Edition edition = new Edition(null, publicationId, false);
		edition.setName(publicationName);
		publicationManager.createEdition(edition, copyVersions);
	}


	/**
	 * Deletes publication (version) from a group publication (RO).
	 * 
	 * @param groupPublicationName
	 * @param publicationName
	 * @throws DLibraException
	 * @throws IOException
	 * @throws TransformerException
	 */
	public void deletePublication(String groupPublicationName, String publicationName)
		throws DLibraException, IOException, TransformerException
	{
		PublicationId publicationId = getPublicationId(getGroupId(groupPublicationName), publicationName);

		publicationManager.removePublication(publicationId, true, "Research Object Version removed.");
	}


	PublicationId getGroupId(String groupPublicationName)
		throws RemoteException, DLibraException
	{
		if (getWorkspaceDirectoryId() != null)
			return getGroupId(groupPublicationName, getWorkspaceDirectoryId());
		else
			return getGroupId(groupPublicationName, dLibra.getWorkspacesContainerDirectoryId());
	}


	PublicationId getGroupId(String groupPublicationName, DirectoryId directoryId)
		throws RemoteException, DLibraException
	{
		Collection<Info> resultInfos = directoryManager.getObjects(
			new DirectoryFilter(null, directoryId).setGroupStatus(
				(byte) (Publication.PUB_GROUP_ROOT | Publication.PUB_GROUP_MID)).setState(
				(byte) (Publication.PUB_STATE_ALL - Publication.PUB_STATE_PERMANENT_DELETED)),
			new OutputFilter(Info.class, List.class)).getResultInfos();
		for (Info info : resultInfos) {
			if (info instanceof GroupPublicationInfo && info.getLabel().equals(groupPublicationName)) {
				return (PublicationId) info.getId();
			}
			if (info instanceof DirectoryInfo) {
				try {
					return getGroupId(groupPublicationName, (DirectoryId) info.getId());
				}
				catch (IdNotFoundException e) {
					// keep searching
				}
			}
		}
		throw new IdNotFoundException(groupPublicationName);
	}


	PublicationId getPublicationId(PublicationId groupId, String publicationName)
		throws RemoteException, DLibraException
	{
		Collection<Info> resultInfos = publicationManager.getObjects(
			new PublicationFilter(null, groupId).setGroupStatus(Publication.PUB_GROUP_ALL).setPublicationState(
				(byte) (Publication.PUB_STATE_ALL - Publication.PUB_STATE_PERMANENT_DELETED)),
			new OutputFilter(AbstractPublicationInfo.class)).getResultInfos();
		for (Info info : resultInfos) {
			if (info.getLabel().equals(publicationName)) {
				return (PublicationId) info.getId();
			}
		}
		throw new IdNotFoundException(publicationName);
	}


	public PublicationId getPublicationId(String groupPublicationName, String publicationName)
		throws RemoteException, DLibraException
	{
		return getPublicationId(getGroupId(groupPublicationName), publicationName);

	}


	private DirectoryId getWorkspaceDirectoryId()
		throws RemoteException, DLibraException
	{
		User userData = userManager.getUserData(dLibra.getUserLogin());
		return userData.getHomedir();
	}


	/**
	 * Returns input stream for a zipped content of a publication.
	 * 
	 * @param groupPublicationName
	 * @param publicationName
	 * @return
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	public InputStream getZippedPublication(String groupPublicationName, String publicationName)
		throws RemoteException, DLibraException
	{
		return dLibra.getFilesHelper().getZippedFolder(
			dLibra.getEditionHelper().getLastEditionId(groupPublicationName, publicationName), null);
	}


	public InputStream getZippedPublication(EditionId editionId)
		throws RemoteException, DLibraException
	{
		return dLibra.getFilesHelper().getZippedFolder(editionId, null);
	}

}

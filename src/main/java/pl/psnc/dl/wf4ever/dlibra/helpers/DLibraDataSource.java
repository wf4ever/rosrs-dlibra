package pl.psnc.dl.wf4ever.dlibra.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.dlibra.ConflictException;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibrary;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
import pl.psnc.dl.wf4ever.dlibra.ResourceInfo;
import pl.psnc.dl.wf4ever.dlibra.Snapshot;
import pl.psnc.dl.wf4ever.dlibra.UserProfile;
import pl.psnc.dl.wf4ever.dlibra.UserProfile.Role;
import pl.psnc.dlibra.content.ContentServer;
import pl.psnc.dlibra.metadata.AbstractPublicationInfo;
import pl.psnc.dlibra.metadata.DirectoryId;
import pl.psnc.dlibra.metadata.Edition;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.LibCollectionId;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.Publication;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.PublicationInfo;
import pl.psnc.dlibra.mgmt.DLStaticServiceResolver;
import pl.psnc.dlibra.mgmt.UserServiceResolver;
import pl.psnc.dlibra.service.AccessDeniedException;
import pl.psnc.dlibra.service.AuthorizationToken;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.DuplicatedValueException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.dlibra.service.ServiceUrl;
import pl.psnc.dlibra.system.UserInterface;
import pl.psnc.dlibra.user.User;
import pl.psnc.dlibra.user.UserManager;

import com.google.common.collect.Multimap;

/**
 * 
 * @author nowakm, piotrhol
 * 
 */
public class DLibraDataSource implements DigitalLibrary {

    @SuppressWarnings("unused")
    private final static Logger logger = Logger.getLogger(DLibraDataSource.class);

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

    private final AttributesHelper attributesHelper;

    private final DirectoryId workspacesContainerDirectoryId;

    private final LibCollectionId collectionId;


    public DLibraDataSource(String host, int port, long workspacesContainerDirectoryId, long collectionId,
            String userLogin, String password)
            throws RemoteException, DLibraException, MalformedURLException, UnknownHostException {
        AuthorizationToken authorizationToken = new AuthorizationToken(userLogin, password);
        serviceResolver = new UserServiceResolver(new ServiceUrl(InetAddress.getByName(host),
                UserInterface.SERVICE_TYPE, port), authorizationToken);

        this.userLogin = userLogin;
        this.userPassword = password;
        this.workspacesContainerDirectoryId = new DirectoryId(workspacesContainerDirectoryId);
        this.collectionId = new LibCollectionId(collectionId);

        metadataServer = DLStaticServiceResolver.getMetadataServer(serviceResolver, null);

        contentServer = DLStaticServiceResolver.getContentServer(serviceResolver, null);

        userManager = DLStaticServiceResolver.getUserServer(serviceResolver, null).getUserManager();

        usersHelper = new UsersHelper(this);
        publicationsHelper = new PublicationsHelper(this);
        filesHelper = new FilesHelper(this);
        editionHelper = new EditionHelper(this);
        attributesHelper = new AttributesHelper(this);
    }


    /**
     * @return the attributesHelper
     */
    AttributesHelper getAttributesHelper() {
        return attributesHelper;
    }


    UserServiceResolver getServiceResolver() {
        return serviceResolver;
    }


    ContentServer getContentServer() {
        return contentServer;
    }


    UserManager getUserManager() {
        return userManager;
    }


    MetadataServer getMetadataServer() {
        return metadataServer;
    }


    UsersHelper getUsersHelper() {
        return usersHelper;
    }


    PublicationsHelper getPublicationsHelper() {
        return publicationsHelper;
    }


    FilesHelper getFilesHelper() {
        return filesHelper;
    }


    String getUserLogin() {
        return userLogin;
    }


    DirectoryId getWorkspacesContainerDirectoryId() {
        return workspacesContainerDirectoryId;
    }


    LibCollectionId getCollectionId() {
        return collectionId;
    }


    EditionHelper getEditionHelper() {
        return editionHelper;
    }


    @Override
    public UserProfile getUserProfile()
            throws DigitalLibraryException, NotFoundException {
        User user;
        try {
            user = userManager.getUserData(userLogin);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
        // FIXME should be based on sth else than login
        UserProfile.Role role;
        if (userLogin.equals("wfadmin"))
            role = Role.ADMIN;
        else if (userLogin.equals("wf4ever_reader"))
            role = Role.PUBLIC;
        else
            role = Role.AUTHENTICATED;
        return new UserProfile(userLogin, userPassword, user.getName(), role);
    }


    @Override
    public List<String> getResourcePaths(String workspaceId, String researchObjectId, String versionId, String folder)
            throws DigitalLibraryException, NotFoundException {
        try {
            EditionId editionId = getEditionHelper().getLastEditionId(researchObjectId, versionId);
            return getResourcePaths(workspaceId, researchObjectId, versionId, folder, editionId.getId());
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public List<String> getResourcePaths(String workspaceId, String researchObjectId, String versionId, String folder,
            long editionId)
            throws DigitalLibraryException, NotFoundException {
        try {
            return getFilesHelper().getFilePathsInFolder(new EditionId(editionId), folder);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public InputStream getZippedFolder(String workspaceId, String researchObjectId, String versionId, String folder)
            throws DigitalLibraryException, NotFoundException {
        try {
            EditionId editionId = getEditionHelper().getLastEditionId(researchObjectId, versionId);
            return getZippedFolder(workspaceId, researchObjectId, versionId, folder, editionId.getId());
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public InputStream getZippedFolder(String workspaceId, String researchObjectId, String versionId, String folder,
            long editionId)
            throws DigitalLibraryException, NotFoundException {
        try {
            return getFilesHelper().getZippedFolder(new EditionId(editionId), folder);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public InputStream getFileContents(String workspaceId, String researchObjectId, String versionId, String filePath)
            throws DigitalLibraryException, NotFoundException {
        try {
            EditionId editionId = getEditionHelper().getLastEditionId(researchObjectId, versionId);
            return getFileContents(workspaceId, researchObjectId, versionId, filePath, editionId.getId());
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public InputStream getFileContents(String workspaceId, String researchObjectId, String versionId, String filePath,
            long editionId)
            throws DigitalLibraryException, NotFoundException {
        try {
            return getFilesHelper().getFileContents(new EditionId(editionId), filePath);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public String getFileMimeType(String workspaceId, String researchObjectId, String versionId, String filePath)
            throws DigitalLibraryException, NotFoundException {
        try {
            EditionId editionId = getEditionHelper().getLastEditionId(researchObjectId, versionId);
            return getFileMimeType(workspaceId, researchObjectId, versionId, filePath, editionId.getId());
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public String getFileMimeType(String workspaceId, String researchObjectId, String versionId, String filePath,
            long editionId)
            throws DigitalLibraryException, NotFoundException {
        try {
            return getFilesHelper().getFileMimeType(new EditionId(editionId), filePath);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public boolean fileExists(String workspaceId, String researchObjectId, String versionId, String filePath)
            throws NotFoundException, DigitalLibraryException {
        try {
            EditionId editionId = getEditionHelper().getLastEditionId(researchObjectId, versionId);
            return getFilesHelper().fileExists(editionId, filePath);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public ResourceInfo createOrUpdateFile(String workspaceId, String researchObjectId, String versionId,
            String filePath, InputStream inputStream, String type)
            throws DigitalLibraryException, NotFoundException, AccessDeniedException {
        try {
            return getFilesHelper().createOrUpdateFile(researchObjectId, versionId, filePath, inputStream, type);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (IOException | DLibraException | TransformerException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public void deleteFile(String workspaceId, String researchObjectId, String versionId, String filePath)
            throws DigitalLibraryException, NotFoundException {
        try {
            getFilesHelper().deleteFile(researchObjectId, versionId, filePath);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (IOException | DLibraException | TransformerException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public List<String> getResearchObjectIds(String workspaceId)
            throws DigitalLibraryException, NotFoundException {
        List<AbstractPublicationInfo> infos;
        try {
            infos = getPublicationsHelper().listUserGroupPublications(Publication.PUB_GROUP_MID);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
        List<String> ids = new ArrayList<String>();
        for (AbstractPublicationInfo info : infos) {
            ids.add(info.getLabel());
        }
        return ids;
    }


    @Override
    public void createResearchObject(String workspaceId, String researchObjectId)
            throws DigitalLibraryException, NotFoundException, ConflictException {
        try {
            getPublicationsHelper().createGroupPublication(workspaceId, researchObjectId);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (DuplicatedValueException e) {
            throw new ConflictException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public List<String> getVersionIds(String workspaceId, String researchObjectId)
            throws DigitalLibraryException, NotFoundException {
        List<PublicationInfo> infos;
        try {
            infos = getPublicationsHelper().listPublicationsInGroup(researchObjectId);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
        List<String> ids = new ArrayList<String>();
        for (AbstractPublicationInfo info : infos) {
            ids.add(info.getLabel());
        }
        return ids;
    }


    @Override
    public void createVersion(String workspaceId, String researchObjectId, String version, InputStream mainFileContent,
            String mainFilePath, String mainFileMimeType)
            throws DigitalLibraryException, NotFoundException, ConflictException {
        try {
            getPublicationsHelper().createPublication(researchObjectId, version, null, mainFileContent, mainFilePath,
                mainFileMimeType);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (DuplicatedValueException e) {
            throw new ConflictException(e);
        } catch (IOException | DLibraException | TransformerException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public void createVersion(String workspaceId, String researchObjectId, String version, String baseVersion)
            throws DigitalLibraryException, NotFoundException {
        try {
            getPublicationsHelper().createPublication(researchObjectId, version, baseVersion, null, null, null);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (IOException | DLibraException | TransformerException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public void deleteResearchObject(String workspaceId, String researchObjectId)
            throws DigitalLibraryException, NotFoundException {
        try {
            getPublicationsHelper().deleteGroupPublication(researchObjectId);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public boolean createUser(String userId, String password, String username)
            throws DigitalLibraryException, NotFoundException, ConflictException {
        try {
            return getUsersHelper().createUser(userId, password, username);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (DuplicatedValueException e) {
            throw new ConflictException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public boolean userExists(String userId)
            throws DigitalLibraryException {
        try {
            return getUsersHelper().userExists(userId);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public void deleteUser(String userId)
            throws DigitalLibraryException, NotFoundException {
        try {
            getUsersHelper().deleteUser(userId);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public Set<Snapshot> getEditionList(String workspaceId, String researchObjectId, String versionId)
            throws DigitalLibraryException, NotFoundException {
        try {
            Set<Edition> eds = getEditionHelper().getEditionList(researchObjectId, versionId);
            LinkedHashSet<Snapshot> snaps = new LinkedHashSet<Snapshot>();
            for (Edition e : eds) {
                snaps.add(new Snapshot(e.getId().getId(), e.isPublished(), e.getCreationDate()));
            }
            return snaps;
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public InputStream getZippedVersion(String workspaceId, String researchObjectId, String versionId)
            throws DigitalLibraryException, NotFoundException {
        try {
            EditionId editionId = getEditionHelper().getLastEditionId(researchObjectId, versionId);
            return getZippedVersion(workspaceId, researchObjectId, versionId, editionId.getId());
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public InputStream getZippedVersion(String workspaceId, String researchObjectId, String versionId, long editionId)
            throws DigitalLibraryException, NotFoundException {
        try {
            return getPublicationsHelper().getZippedPublication(new EditionId(editionId));
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public void publishVersion(String workspaceId, String researchObjectId, String versionId)
            throws DigitalLibraryException, NotFoundException {
        try {
            getPublicationsHelper().publishPublication(researchObjectId, versionId);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public void unpublishVersion(String workspaceId, String researchObjectId, String versionId)
            throws DigitalLibraryException, NotFoundException {
        try {
            getPublicationsHelper().unpublishPublication(researchObjectId, versionId);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public Snapshot createEdition(String workspaceId, String editionName, String researchObjectId, String versionId)
            throws DigitalLibraryException, NotFoundException {
        try {
            return new Snapshot(getEditionHelper().createEdition(editionName, researchObjectId, versionId).getId(),
                    false, null);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public void deleteVersion(String workspaceId, String researchObjectId, String versionId)
            throws DigitalLibraryException, NotFoundException {
        try {
            getPublicationsHelper().deletePublication(researchObjectId, versionId);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (IOException | DLibraException | TransformerException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public List<String> getWorkspaceIds()
            throws DigitalLibraryException, NotFoundException {
        List<AbstractPublicationInfo> infos;
        try {
            infos = getPublicationsHelper().listUserGroupPublications(Publication.PUB_GROUP_ROOT);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
        List<String> ids = new ArrayList<String>();
        for (AbstractPublicationInfo info : infos) {
            ids.add(info.getLabel());
        }
        return ids;
    }


    @Override
    public void createWorkspace(String workspaceId)
            throws DigitalLibraryException, NotFoundException, ConflictException {
        try {
            PublicationId id = getPublicationsHelper().createGroupPublication(workspaceId);
            getUsersHelper().grantReadAccessToPublication(id);

        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (DuplicatedValueException e) {
            throw new ConflictException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public void deleteWorkspace(String workspaceId)
            throws DigitalLibraryException, NotFoundException {
        try {
            getPublicationsHelper().deleteGroupPublication(workspaceId);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public void storeAttributes(String workspaceId, String researchObjectId, String versionId,
            Multimap<URI, Object> roAttributes)
            throws NotFoundException, DigitalLibraryException {
        try {
            getAttributesHelper().storeAttributes(workspaceId, researchObjectId, versionId, roAttributes);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }

    }

}

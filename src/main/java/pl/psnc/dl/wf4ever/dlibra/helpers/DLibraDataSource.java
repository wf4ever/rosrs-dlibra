package pl.psnc.dl.wf4ever.dlibra.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;

import pl.psnc.dl.wf4ever.common.ResearchObject;
import pl.psnc.dl.wf4ever.common.ResourceInfo;
import pl.psnc.dl.wf4ever.common.UserProfile;
import pl.psnc.dl.wf4ever.common.UserProfile.Role;
import pl.psnc.dl.wf4ever.dlibra.ConflictException;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibrary;
import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
import pl.psnc.dlibra.content.ContentServer;
import pl.psnc.dlibra.metadata.AbstractPublicationInfo;
import pl.psnc.dlibra.metadata.DirectoryId;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.LibCollectionId;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.PublicationInfo;
import pl.psnc.dlibra.mgmt.DLStaticServiceResolver;
import pl.psnc.dlibra.mgmt.UnavailableServiceException;
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
    private final static Logger LOGGER = Logger.getLogger(DLibraDataSource.class);

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
        return getUserProfile(userLogin);
    }


    @Override
    public UserProfile getUserProfile(String login)
            throws DigitalLibraryException, NotFoundException {
        User user;
        try {
            user = userManager.getUserData(login);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
        // FIXME should be based on sth else than login
        UserProfile.Role role;
        if (login.equals("wfadmin")) {
            role = Role.ADMIN;
        } else if (login.equals("wf4ever_reader")) {
            role = Role.PUBLIC;
        } else {
            role = Role.AUTHENTICATED;
        }
        return new UserProfile(user.getLogin(), user.getName(), role);
    }


    @Override
    public List<String> getResourcePaths(ResearchObject ro, String folder)
            throws DigitalLibraryException, NotFoundException {
        try {
            return getFilesHelper().getFilePathsInFolder(ro, folder);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public InputStream getZippedFolder(ResearchObject ro, String folder)
            throws DigitalLibraryException, NotFoundException {
        try {
            return getFilesHelper().getZippedFolder(ro, folder);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public InputStream getFileContents(ResearchObject ro, String filePath)
            throws DigitalLibraryException, NotFoundException {
        try {
            return getFilesHelper().getFileContents(ro, filePath);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public boolean fileExists(ResearchObject ro, String filePath)
            throws DigitalLibraryException {
        try {
            return getFilesHelper().fileExists(ro, filePath);
        } catch (IdNotFoundException e) {
            return false;
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public ResourceInfo createOrUpdateFile(ResearchObject ro, String filePath, InputStream inputStream, String type)
            throws DigitalLibraryException, NotFoundException, AccessDeniedException {
        try {
            return getFilesHelper().createOrUpdateFile(ro, filePath, inputStream, type);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (IOException | DLibraException | TransformerException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public ResourceInfo getFileInfo(ResearchObject ro, String filePath)
            throws NotFoundException, DigitalLibraryException, AccessDeniedException {
        try {
            return getFilesHelper().getFileInfo(ro, filePath);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (IOException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public void deleteFile(ResearchObject ro, String filePath)
            throws DigitalLibraryException, NotFoundException {
        try {
            getFilesHelper().deleteFile(ro, filePath);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (IOException | DLibraException | TransformerException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public void createResearchObject(ResearchObject ro, InputStream mainFileContent, String mainFilePath,
            String mainFileMimeType)
            throws DigitalLibraryException, NotFoundException, ConflictException {
        try {
            createWorkspaceGroupPublication(ro);
            createRoGroupPublication(ro);
            createVersionPublication(ro, mainFileContent, mainFilePath, mainFileMimeType);
            getPublicationsHelper().publishPublication(ro);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (IOException | DLibraException | TransformerException e) {
            throw new DigitalLibraryException(e);
        }
    }


    private void createVersionPublication(ResearchObject ro, InputStream mainFileContent, String mainFilePath,
            String mainFileMimeType)
            throws RemoteException, DLibraException, IOException, TransformerException, ConflictException {
        if (getDlROVersionId(ro) == 0) {
            PublicationId verId = getPublicationsHelper().createVersionPublication(new PublicationId(getDlROId(ro)),
                ro.getId(), "v1", mainFileContent, mainFilePath, mainFileMimeType);
            ro.setDlROVersionId(verId.getId());
        } else {
            throw new ConflictException(ro.getUri().toString());
        }
    }


    private void createRoGroupPublication(ResearchObject ro)
            throws RemoteException, DLibraException {
        if (getDlROId(ro) == 0) {
            PublicationId roId = getPublicationsHelper().createROGroupPublication(
                new PublicationId(ro.getDlWorkspaceId()), ro.getId());
            ro.setDlROId(roId.getId());
        }
    }


    private void createWorkspaceGroupPublication(ResearchObject ro)
            throws RemoteException, DLibraException, IdNotFoundException, AccessDeniedException,
            UnavailableServiceException {
        if (getDlWorkspaceId(ro) == 0) {
            PublicationId workspaceId = getPublicationsHelper().createWorkspaceGroupPublication("default");
            getUsersHelper().grantReadAccessToPublication(workspaceId);
            ro.setDlWorkspaceId(workspaceId.getId());
        }
    }


    /**
     * Return workspace id loading it if necessary.
     * 
     * @param ro
     *            RO
     * @return dLibra workspace id
     * @throws DLibraException
     * @throws RemoteException
     */
    long getDlWorkspaceId(ResearchObject ro)
            throws RemoteException, DLibraException {
        if (ro.getDlWorkspaceId() == 0) {
            PublicationId publicationId = getPublicationsHelper().getGroupId("default");
            ro.setDlWorkspaceId(publicationId != null ? publicationId.getId() : 0);
        }
        return ro.getDlWorkspaceId();
    }


    /**
     * Return RO id loading it if necessary.
     * 
     * @param ro
     *            RO
     * @return dLibra RO id
     * @throws DLibraException
     * @throws RemoteException
     */
    long getDlROId(ResearchObject ro)
            throws RemoteException, DLibraException {
        if (ro.getDlROId() == 0) {
            PublicationId publicationId = getPublicationsHelper().getPublicationId(
                new PublicationId(getDlWorkspaceId(ro)), ro.getId());
            ro.setDlROId(publicationId != null ? publicationId.getId() : 0);
        }
        return ro.getDlROId();
    }


    /**
     * Return RO version id loading it if necessary.
     * 
     * @param ro
     *            RO
     * @return dLibra RO version id or 0 if not found
     * @throws DLibraException
     * @throws RemoteException
     */
    long getDlROVersionId(ResearchObject ro)
            throws RemoteException, DLibraException {
        if (ro.getDlROVersionId() == 0) {
            PublicationId roId = new PublicationId(getDlROId(ro));
            PublicationId publicationId = getPublicationsHelper().getPublicationId(roId, "v1");
            ro.setDlROVersionId(publicationId != null ? publicationId.getId() : 0);
        }
        return ro.getDlROVersionId();
    }


    /**
     * Return RO version last edition id loading it if necessary.
     * 
     * @param ro
     *            RO
     * @return dLibra RO version last edition id or 0 if not found
     * @throws DLibraException
     * @throws RemoteException
     */
    long getDlEditionId(ResearchObject ro)
            throws RemoteException, DLibraException {
        if (ro.getDlEditionId() == 0) {
            long versionIdLong = getDlROVersionId(ro);
            EditionId editionId = (EditionId) editionHelper.getLastEdition(new PublicationId(versionIdLong)).getId();
            ro.setDlEditionId(editionId != null ? editionId.getId() : 0);
        }
        return ro.getDlEditionId();
    }


    @Override
    public void deleteResearchObject(ResearchObject ro)
            throws DigitalLibraryException, NotFoundException, DLibraException, IOException, TransformerException {
        getPublicationsHelper().deleteVersionPublication(ro);
        List<PublicationInfo> vers = getPublicationsHelper().listPublicationsInROGroupPublication(
            new PublicationId(ro.getDlROVersionId()));
        if (vers.isEmpty()) {
            getPublicationsHelper().deleteGroupPublication(new PublicationId(ro.getDlROId()));
            List<AbstractPublicationInfo> ros = getPublicationsHelper().listROGroupPublications();
            if (ros.isEmpty()) {
                getPublicationsHelper().deleteGroupPublication(new PublicationId(ro.getDlWorkspaceId()));
            }
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
    public InputStream getZippedResearchObject(ResearchObject ro)
            throws DigitalLibraryException, NotFoundException {
        try {
            return getPublicationsHelper().getZippedPublication(ro);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public void storeAttributes(ResearchObject ro, Multimap<URI, Object> roAttributes)
            throws NotFoundException, DigitalLibraryException {
        try {
            getAttributesHelper().storeAttributes(ro, roAttributes);
        } catch (IdNotFoundException e) {
            throw new NotFoundException(e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }

    }

}

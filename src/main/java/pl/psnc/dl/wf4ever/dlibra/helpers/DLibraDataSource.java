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

import pl.psnc.dl.wf4ever.dl.AccessDeniedException;
import pl.psnc.dl.wf4ever.dl.ConflictException;
import pl.psnc.dl.wf4ever.dl.DigitalLibrary;
import pl.psnc.dl.wf4ever.dl.DigitalLibraryException;
import pl.psnc.dl.wf4ever.dl.NotFoundException;
import pl.psnc.dl.wf4ever.dl.ResourceMetadata;
import pl.psnc.dl.wf4ever.dl.UserMetadata;
import pl.psnc.dl.wf4ever.dl.UserMetadata.Role;
import pl.psnc.dl.wf4ever.dlibra.hibernate.HibernateUtil;
import pl.psnc.dl.wf4ever.dlibra.hibernate.ResearchObject;
import pl.psnc.dlibra.content.ContentServer;
import pl.psnc.dlibra.metadata.AbstractPublicationInfo;
import pl.psnc.dlibra.metadata.DirectoryId;
import pl.psnc.dlibra.metadata.Edition;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.LibCollectionId;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.PublicationInfo;
import pl.psnc.dlibra.mgmt.DLStaticServiceResolver;
import pl.psnc.dlibra.mgmt.UnavailableServiceException;
import pl.psnc.dlibra.mgmt.UserServiceResolver;
import pl.psnc.dlibra.service.AuthorizationToken;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.dlibra.service.ServiceUrl;
import pl.psnc.dlibra.system.UserInterface;
import pl.psnc.dlibra.user.User;
import pl.psnc.dlibra.user.UserManager;

import com.google.common.collect.Multimap;

/**
 * Implementation of the digital library interface based on dLibra.
 * 
 * @author piotrhol
 * 
 */
public class DLibraDataSource implements DigitalLibrary {

    /** logger. */
    @SuppressWarnings("unused")
    private static final Logger LOGGER = Logger.getLogger(DLibraDataSource.class);

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


    /**
     * Constructor.
     * 
     * @param host
     *            dLibra server host
     * @param port
     *            dLibra server RMI port
     * @param workspacesContainerDirectoryId
     *            dLibra directory id in which all content is stored
     * @param collectionId
     *            id of collection that will have all published ROs
     * @param userLogin
     *            user login
     * @param password
     *            user password
     * @throws DigitalLibraryException
     *             internal dLibra error
     * @throws IOException
     *             error connecting to dLibra
     */
    public DLibraDataSource(String host, int port, long workspacesContainerDirectoryId, long collectionId,
            String userLogin, String password)
            throws DigitalLibraryException, IOException {
        try {
            AuthorizationToken authorizationToken = new AuthorizationToken(userLogin, password);
            serviceResolver = new UserServiceResolver(new ServiceUrl(InetAddress.getByName(host),
                    UserInterface.SERVICE_TYPE, port), authorizationToken);

            this.userLogin = userLogin;
            this.workspacesContainerDirectoryId = new DirectoryId(workspacesContainerDirectoryId);
            this.collectionId = new LibCollectionId(collectionId);

            metadataServer = DLStaticServiceResolver.getMetadataServer(serviceResolver, null);
            contentServer = DLStaticServiceResolver.getContentServer(serviceResolver, null);
            userManager = DLStaticServiceResolver.getUserServer(serviceResolver, null).getUserManager();
        } catch (DLibraException e) {
            throw new DigitalLibraryException(e);
        } catch (MalformedURLException | UnknownHostException e) {
            throw new IOException(e);
        }

        usersHelper = new UsersHelper(this);
        publicationsHelper = new PublicationsHelper(this);
        filesHelper = new FilesHelper(this);
        editionHelper = new EditionHelper(metadataServer.getPublicationManager());
        attributesHelper = new AttributesHelper(this);
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


    /**
     * Get the profile of the user that is logged in to dLibra.
     * 
     * @return user profile
     * @throws NotFoundException
     *             user profile not found
     * @throws DigitalLibraryException
     *             dLibra exception
     */
    public UserMetadata getUserProfile()
            throws DigitalLibraryException, NotFoundException {
        return getUserProfile(userLogin);
    }


    /**
     * Get the profile of a user.
     * 
     * @param login
     *            user login
     * @return user profile
     * @throws NotFoundException
     *             user profile not found
     * @throws DigitalLibraryException
     *             dLibra exception
     */
    public UserMetadata getUserProfile(String login)
            throws DigitalLibraryException, NotFoundException {
        User user;
        try {
            user = userManager.getUserData(login);
        } catch (IdNotFoundException e) {
            throw new NotFoundException("User profile not found", e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
        // FIXME should be based on sth else than login
        UserMetadata.Role role;
        if (login.equals("wfadmin")) {
            role = Role.ADMIN;
        } else if (login.equals("wf4ever_reader")) {
            role = Role.PUBLIC;
        } else {
            role = Role.AUTHENTICATED;
        }
        return new UserMetadata(user.getLogin(), user.getName(), role);
    }


    @Override
    public InputStream getZippedFolder(URI uri, String folder)
            throws DigitalLibraryException, NotFoundException {
        HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
        try {
            ResearchObject ro = ResearchObject.create(uri);
            InputStream result = filesHelper.getZippedFolder(ro, folder);
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
            return result;
        } catch (IdNotFoundException e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw new NotFoundException("Something was not found", e);
        } catch (RemoteException | DLibraException e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw new DigitalLibraryException(e);
        } catch (Throwable e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw e;
        }
    }


    @Override
    public InputStream getFileContents(URI uri, String filePath)
            throws DigitalLibraryException, NotFoundException {
        try {
            HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
            ResearchObject ro = ResearchObject.create(uri);
            InputStream result = filesHelper.getFileContents(ro, filePath);
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
            return result;
        } catch (IdNotFoundException e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw new NotFoundException("Something was not found", e);
        } catch (RemoteException | DLibraException e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw new DigitalLibraryException(e);
        } catch (Throwable e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw e;
        }
    }


    @Override
    public boolean fileExists(URI uri, String filePath)
            throws DigitalLibraryException {
        try {
            HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
            ResearchObject ro = ResearchObject.create(uri);
            boolean result = filesHelper.fileExists(ro, filePath);
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
            return result;
        } catch (IdNotFoundException e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            return false;
        } catch (RemoteException | DLibraException e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw new DigitalLibraryException(e);
        } catch (Throwable e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw e;
        }
    }


    @Override
    public ResourceMetadata createOrUpdateFile(URI uri, String filePath, InputStream inputStream, String type)
            throws DigitalLibraryException, NotFoundException, AccessDeniedException {
        try {
            HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
            ResearchObject ro = ResearchObject.create(uri);
            ResourceMetadata result = filesHelper.createOrUpdateFile(ro, filePath, inputStream, type);
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
            return result;
        } catch (IdNotFoundException e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw new NotFoundException("Something was not found", e);
        } catch (pl.psnc.dlibra.service.AccessDeniedException e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw new AccessDeniedException(e.getMessage(), e);
        } catch (IOException | DLibraException | TransformerException e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw new DigitalLibraryException(e);
        } catch (Throwable e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw e;
        }
    }


    @Override
    public ResourceMetadata getFileInfo(URI uri, String filePath)
            throws NotFoundException, DigitalLibraryException, AccessDeniedException {
        try {
            HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
            ResearchObject ro = ResearchObject.create(uri);
            ResourceMetadata result = filesHelper.getFileInfo(ro, filePath);
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
            return result;
        } catch (IdNotFoundException e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw new NotFoundException("Something was not found", e);
        } catch (pl.psnc.dlibra.service.AccessDeniedException e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw new AccessDeniedException(e.getMessage(), e);
        } catch (IOException | DLibraException e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw new DigitalLibraryException(e);
        } catch (Throwable e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw e;
        }
    }


    @Override
    public void deleteFile(URI uri, String filePath)
            throws DigitalLibraryException, NotFoundException {
        try {
            HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
            ResearchObject ro = ResearchObject.create(uri);
            filesHelper.deleteFile(ro, filePath);
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        } catch (IdNotFoundException e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw new NotFoundException("Something was not found", e);
        } catch (IOException | DLibraException | TransformerException e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw new DigitalLibraryException(e);
        } catch (Throwable e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw e;
        }
    }


    @Override
    public void createResearchObject(URI uri, InputStream mainFileContent, String mainFilePath, String mainFileMimeType)
            throws DigitalLibraryException, ConflictException, AccessDeniedException {
        try {
            HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
            ResearchObject ro = ResearchObject.create(uri);
            createWorkspaceGroupPublication(ro);
            createRoGroupPublication(ro);
            createVersionPublication(ro);
            createEdition(ro, mainFileContent, mainFilePath, mainFileMimeType);
            publicationsHelper.publishPublication(ro);
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        } catch (IOException | DLibraException | TransformerException e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw new DigitalLibraryException(e);
        } catch (Throwable e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw e;
        }
    }


    private void createEdition(ResearchObject ro, InputStream mainFileContent, String mainFilePath,
            String mainFileMimeType)
            throws AccessDeniedException, IdNotFoundException, RemoteException, DLibraException, TransformerException,
            IOException {
        EditionId editionId = publicationsHelper.preparePublicationAsNew(getRoId(ro), new PublicationId(
                getDlROVersionId(ro)), mainFileContent, mainFilePath, mainFileMimeType);
        ro.setDlEditionId(editionId.getId());
    }


    private void createVersionPublication(ResearchObject ro)
            throws RemoteException, DLibraException, IOException, TransformerException, ConflictException {
        if (getDlROVersionId(ro) != 0 && publicationsHelper.publicationExists(new PublicationId(getDlROVersionId(ro)))) {
            throw new ConflictException(ro.getUri().toString());
        }
        PublicationId verId = publicationsHelper.createVersionPublication(new PublicationId(getDlROId(ro)), "v1");
        ro.setDlROVersionId(verId.getId());
    }


    private void createRoGroupPublication(ResearchObject ro)
            throws RemoteException, DLibraException {
        if (getDlROId(ro) != 0 && !publicationsHelper.publicationExists(new PublicationId(getDlROId(ro)))) {
            ro.setDlROId(0);
            ro.setDlROVersionId(0);
            ro.setDlEditionId(0);
        }
        if (getDlROId(ro) == 0) {
            PublicationId roId = publicationsHelper.createROGroupPublication(new PublicationId(ro.getDlWorkspaceId()),
                getRoId(ro));
            ro.setDlROId(roId.getId());
        }
    }


    private void createWorkspaceGroupPublication(ResearchObject ro)
            throws RemoteException, DLibraException, IdNotFoundException, AccessDeniedException,
            UnavailableServiceException {
        if (getDlWorkspaceId(ro) != 0 && !publicationsHelper.publicationExists(new PublicationId(getDlWorkspaceId(ro)))) {
            ro.setDlWorkspaceId(0);
            ro.setDlROId(0);
            ro.setDlROVersionId(0);
            ro.setDlEditionId(0);
        }
        if (getDlWorkspaceId(ro) == 0) {
            PublicationId workspaceId = publicationsHelper.createWorkspaceGroupPublication("default");
            usersHelper.grantReadAccessToPublication(workspaceId);
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
            PublicationId publicationId = publicationsHelper.getGroupId("default");
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
            PublicationId publicationId = publicationsHelper.getPublicationId(new PublicationId(getDlWorkspaceId(ro)),
                getRoId(ro));
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
            PublicationId publicationId = publicationsHelper.getPublicationId(roId, "v1");
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
            Edition edition = (Edition) editionHelper.getLastEdition(new PublicationId(versionIdLong));
            ro.setDlEditionId(edition != null ? edition.getId().getId() : 0);
        }
        return ro.getDlEditionId();
    }


    @Override
    public void deleteResearchObject(URI uri)
            throws DigitalLibraryException, NotFoundException {
        try {
            HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
            ResearchObject ro = ResearchObject.create(uri);
            ro.delete();
            publicationsHelper.deleteVersionPublication(ro);
            List<PublicationInfo> vers = publicationsHelper.listPublicationsInROGroupPublication(new PublicationId(ro
                    .getDlROId()));
            if (vers.isEmpty()) {
                publicationsHelper.deleteGroupPublication(new PublicationId(ro.getDlROId()));
                List<AbstractPublicationInfo> ros = publicationsHelper.listROGroupPublications();
                if (ros.isEmpty()) {
                    publicationsHelper.deleteGroupPublication(new PublicationId(ro.getDlWorkspaceId()));
                }
            }
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        } catch (IdNotFoundException e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
            throw new NotFoundException("Something was not found", e);
        } catch (IOException | DLibraException e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw new DigitalLibraryException(e);
        } catch (Throwable e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw e;
        }

    }


    @Override
    public boolean createOrUpdateUser(String userId, String password, String username)
            throws DigitalLibraryException {
        try {
            return usersHelper.createOrUpdateUser(userId, password, username);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public boolean userExists(String userId)
            throws DigitalLibraryException {
        try {
            return usersHelper.userExists(userId);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public void deleteUser(String userId)
            throws DigitalLibraryException, NotFoundException {
        try {
            usersHelper.deleteUser(userId);
        } catch (IdNotFoundException e) {
            throw new NotFoundException("Something was not found", e);
        } catch (RemoteException | DLibraException e) {
            throw new DigitalLibraryException(e);
        }
    }


    @Override
    public InputStream getZippedResearchObject(URI uri)
            throws DigitalLibraryException, NotFoundException {
        try {
            HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
            ResearchObject ro = ResearchObject.create(uri);
            InputStream result = filesHelper.getZippedFolder(ro, null);
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
            return result;
        } catch (IdNotFoundException e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw new NotFoundException("Something was not found", e);
        } catch (RemoteException | DLibraException e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw new DigitalLibraryException(e);
        } catch (Throwable e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw e;
        }
    }


    @Override
    public void storeAttributes(URI uri, Multimap<URI, Object> roAttributes)
            throws NotFoundException, DigitalLibraryException {
        try {
            HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
            ResearchObject ro = ResearchObject.create(uri);
            attributesHelper.storeAttributes(ro, roAttributes);
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
        } catch (IdNotFoundException e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw new NotFoundException("Something was not found", e);
        } catch (RemoteException | DLibraException e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw new DigitalLibraryException(e);
        } catch (Throwable e) {
            HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
            throw e;
        }

    }


    private static String getRoId(ResearchObject ro) {
        if (ro.getUri().getPath() == null) {
            return null;
        }
        String segments[] = ro.getUri().getPath().split("/");
        return segments[segments.length - 1];
    }

}

/**
 * 
 */
package pl.psnc.dl.wf4ever.dlibra.helpers;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import pl.psnc.dlibra.metadata.Directory;
import pl.psnc.dlibra.metadata.DirectoryId;
import pl.psnc.dlibra.metadata.DirectoryManager;
import pl.psnc.dlibra.metadata.Language;
import pl.psnc.dlibra.metadata.MetadataServer;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.mgmt.DLStaticServiceResolver;
import pl.psnc.dlibra.mgmt.UnavailableServiceException;
import pl.psnc.dlibra.mgmt.UserServiceResolver;
import pl.psnc.dlibra.service.AccessDeniedException;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;
import pl.psnc.dlibra.user.Actor;
import pl.psnc.dlibra.user.ActorId;
import pl.psnc.dlibra.user.DirectoryRightId;
import pl.psnc.dlibra.user.LibCollectionRightId;
import pl.psnc.dlibra.user.LibraryRightId;
import pl.psnc.dlibra.user.PublicationRightId;
import pl.psnc.dlibra.user.RightOperation;
import pl.psnc.dlibra.user.User;
import pl.psnc.dlibra.user.UserId;
import pl.psnc.dlibra.user.UserManager;
import pl.psnc.dlibra.user.UserServer;

/**
 * @author piotrhol
 * 
 */
public class UsersHelper
{

	private final DLibraDataSource dLibra;

	private final DirectoryManager directoryManager;

	private final UserServiceResolver serviceResolver;

	private final UserManager userManager;


	public UsersHelper(DLibraDataSource dLibraDataSource)
		throws RemoteException
	{
		this.dLibra = dLibraDataSource;

		directoryManager = dLibraDataSource.getMetadataServer().getDirectoryManager();
		serviceResolver = dLibraDataSource.getServiceResolver();
		userManager = dLibraDataSource.getUserManager();
	}


	/**
	 * Creates user in dLibra, equivalent to workspace in ROSRS.
	 * 
	 * @param login
	 * @param password
	 * @param username
	 * @return
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	public boolean createUser(String login, String password, String username)
		throws RemoteException, DLibraException
	{
		// check if user already exists
		boolean created;
		User user;
		UserId userId;
		try {
			user = userManager.getUserData(login);
			created = false;
		}
		catch (IdNotFoundException e) {
			user = new User(username);
			created = true;
		}

		DirectoryId workspaceDir = createDirectory(login);

		user.setPassword(password);
		user.setHomedir(workspaceDir);
		user.setType(Actor.USER);
		user.setLogin(login);
		user.setEmail(login);

		if (created) {
			userId = userManager.createUser(user);
		}
		else {
			userManager.setUserData(user);
			userId = user.getId();
		}

		List<ActorId> usersWithRead = Arrays.asList(userId, getPublicUserId());

		UserServer userServer = DLStaticServiceResolver.getUserServer(serviceResolver, null);
		userServer.getRightManager().setDirectoryRights(workspaceDir, Arrays.asList((ActorId) userId),
			new RightOperation(DirectoryRightId.PUBLICATION_MGMT, RightOperation.ADD));
		// directory access rights
		userServer.getRightManager().setDirectoryRights(DLibraDataSource.ROOT_DIRECTORY_ID, usersWithRead,
			new RightOperation(DirectoryRightId.DIRECTORY_ACCESS, RightOperation.ADD));
		userServer.getRightManager().setDirectoryRights(dLibra.getWorkspacesContainerDirectoryId(), usersWithRead,
			new RightOperation(DirectoryRightId.DIRECTORY_ACCESS, RightOperation.ADD));
		userServer.getRightManager().setDirectoryRights(workspaceDir, usersWithRead,
			new RightOperation(DirectoryRightId.DIRECTORY_ACCESS, RightOperation.ADD));
		// add to collection
		userServer.getRightManager().setLibCollectionRights(dLibra.getCollectionId(), Arrays.asList((ActorId) userId),
			new RightOperation(LibCollectionRightId.COLLECTION_CONTENT_MGMT, RightOperation.ADD));
		// modify attributes
		userServer.getRightManager().setLibraryRights(usersWithRead,
			new RightOperation(LibraryRightId.ATTRIBUTES_MGMT, RightOperation.ADD));
		return created;
	}


	private DirectoryId createDirectory(String name)
		throws RemoteException, DLibraException
	{
		MetadataServer metadataServer = DLStaticServiceResolver.getMetadataServer(serviceResolver, null);
		Directory directory = new Directory(null, dLibra.getWorkspacesContainerDirectoryId());
		for (String language : metadataServer.getLanguageManager().getLanguageNames(Language.LAN_INTERFACE)) {
			directory.setLanguageName(language);
			directory.setName(name);
		}
		return directoryManager.createDirectory(directory);
	}


	/**
	 * Deletes user from dLibra, equivalent to workspace in ROSRS.
	 * 
	 * @param login
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	public void deleteUser(String login)
		throws RemoteException, DLibraException
	{
		// TODO do we really want to permanently remove the user and its
		// directory?

		User userData = userManager.getUserData(login);

		directoryManager.removeDirectory(userData.getHomedir(), true, "Workspace removed from RO SRS");

		userManager.removeUser(userData.getId());
	}


	public boolean userExists(String login)
		throws RemoteException, DLibraException
	{
		try {
			userManager.getUserData(login);
			return true;
		}
		catch (IdNotFoundException e) {
			return false;
		}
	}


	/**
	 * @param id
	 * @throws RemoteException
	 * @throws DLibraException
	 * @throws IdNotFoundException
	 * @throws AccessDeniedException
	 * @throws UnavailableServiceException
	 */
	public void grantReadAccessToPublication(PublicationId id)
		throws RemoteException, DLibraException, IdNotFoundException, AccessDeniedException,
		UnavailableServiceException
	{
		ActorId publicUserId = getPublicUserId();

		DLStaticServiceResolver
				.getUserServer(serviceResolver, null)
				.getRightManager()
				.setPublicationRights(id, Arrays.asList(publicUserId),
					new RightOperation(PublicationRightId.PUBLICATION_READ, true, RightOperation.ADD));
	}


	/**
	 * @return
	 * @throws RemoteException
	 * @throws DLibraException
	 */
	private ActorId getPublicUserId()
		throws RemoteException, DLibraException
	{
		return userManager.getActorId("wf4ever_reader");
	}

}

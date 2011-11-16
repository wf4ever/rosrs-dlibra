/**
 * 
 */
package pl.psnc.dl.wf4ever.dlibra;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import pl.psnc.dl.wf4ever.dlibra.helpers.DLibraDataSource;
import pl.psnc.dlibra.service.DLibraException;

/**
 * @author piotrhol
 *
 */
public class BasicTest
{

	private String host;

	private int port;

	private long workspacesDirectory;

	private long collectionId;

	private static final String USERNAME = "John Doe";

	private static final String ADMIN_ID = "wfadmin";

	private static final String ADMIN_PASSWORD = "wfadmin!!!";

	private static final String USER_ID = "test-" + new Date().getTime();

	private static final String USER_PASSWORD = "password";


	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp()
		throws Exception
	{
		InputStream inputStream = BasicTest.class.getClassLoader()
				.getResourceAsStream("connection.properties");
		Properties properties = new Properties();
		properties.load(inputStream);
		host = properties.getProperty("host");
		port = Integer.parseInt(properties.getProperty("port"));
		workspacesDirectory = Long.parseLong(properties
				.getProperty("workspacesDir"));
		collectionId = Long.parseLong(properties.getProperty("collectionId"));
	}


	/**
	 * Test method for {@link pl.psnc.dl.wf4ever.dlibra.helpers.DLibraDataSource#createVersion(java.lang.String, java.lang.String, java.lang.String, java.net.URI)}.
	 * @throws DLibraException 
	 * @throws UnknownHostException 
	 * @throws MalformedURLException 
	 * @throws RemoteException 
	 * @throws DigitalLibraryException 
	 * @throws ConflictException 
	 * @throws NotFoundException 
	 */
	@Test
	public final void testCreateVersionStringStringStringURI()
		throws RemoteException, MalformedURLException, UnknownHostException,
		DLibraException, DigitalLibraryException, NotFoundException,
		ConflictException
	{
		DLibraDataSource dlA = new DLibraDataSource(host, port,
				workspacesDirectory, collectionId, ADMIN_ID, ADMIN_PASSWORD);
		dlA.createUser(USER_ID, USER_PASSWORD, USERNAME);
		DLibraDataSource dl = new DLibraDataSource(host, port,
				workspacesDirectory, collectionId, USER_ID, USER_PASSWORD);
		dl.createWorkspace("w");
		dl.createResearchObject("w", "r");
		dl.createVersion("w", "r", "v");
		dl.deleteWorkspace("w");
		dlA = new DLibraDataSource(host, port, workspacesDirectory,
				collectionId, ADMIN_ID, ADMIN_PASSWORD);
		dlA.deleteUser(USER_ID);
	}


	@Test
	public final void testGetUserProfile()
		throws RemoteException, MalformedURLException, UnknownHostException,
		DLibraException, DigitalLibraryException, NotFoundException,
		ConflictException
	{
		DLibraDataSource dlA = new DLibraDataSource(host, port,
				workspacesDirectory, collectionId, ADMIN_ID, ADMIN_PASSWORD);
		dlA.createUser(USER_ID, USER_PASSWORD, USERNAME);
		DLibraDataSource dl = new DLibraDataSource(host, port,
				workspacesDirectory, collectionId, USER_ID, USER_PASSWORD);
		UserProfile user = dl.getUserProfile();
		Assert.assertEquals("User login is equal", USER_ID, user.getLogin());
		Assert.assertEquals("User name is equal", USERNAME, user.getName());
		Assert.assertEquals("User password is equal", USER_PASSWORD,
			user.getPassword());
		dlA = new DLibraDataSource(host, port, workspacesDirectory,
				collectionId, ADMIN_ID, ADMIN_PASSWORD);
		dlA.deleteUser(USER_ID);
	}
}

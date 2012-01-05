/**
 * 
 */
package pl.psnc.dl.wf4ever.dlibra;

import static org.junit.Assert.fail;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import pl.psnc.dl.wf4ever.dlibra.helpers.DLibraDataSource;
import pl.psnc.dlibra.service.DLibraException;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

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


	@After
	public void tearDown()
	{
		try {
			DLibraDataSource dl = new DLibraDataSource(host, port,
					workspacesDirectory, collectionId, USER_ID, USER_PASSWORD);
			dl.deleteWorkspace("w");
		}
		catch (Exception e) {

		}
		try {
			DLibraDataSource dlA = new DLibraDataSource(host, port,
					workspacesDirectory, collectionId, ADMIN_ID, ADMIN_PASSWORD);
			dlA.deleteUser(USER_ID);
		}
		catch (Exception e) {

		}

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
	}


	@Test
	public final void testCreateDuplicateVersion()
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
		try {
			dl.createVersion("w", "r", "v");
			fail("Should throw some exception");
		}
		catch (ConflictException e) {
			// good
		}
		catch (Exception e) {
			fail("Threw a wrong exception: " + e.getClass().toString());
		}
	}


	@Test
	public final void testCreateDuplicateRO()
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
		try {
			dl.createResearchObject("w", "r");
			fail("Should throw some exception");
		}
		catch (ConflictException e) {
			// good
		}
		catch (Exception e) {
			fail("Threw a wrong exception: " + e.getClass().toString());
		}
	}


	@Test
	public final void testStoreAttributes()
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
		Multimap<URI, Object> atts = HashMultimap.create();
		atts.put(URI.create("a"), "foo");
		atts.put(URI.create("a"), "bar");
		atts.put(URI.create("b"), "lorem ipsum");
		dl.storeAttributes("w", "r", "v", atts);
	}
}

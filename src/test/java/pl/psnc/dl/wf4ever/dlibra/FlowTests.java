/**
 * 
 */
package pl.psnc.dl.wf4ever.dlibra;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pl.psnc.dl.wf4ever.dlibra.helpers.DLibraDataSource;
import pl.psnc.dlibra.service.IdNotFoundException;

/**
 * @author piotrek
 * 
 */
public class FlowTests {

	private static String host;

	private static int port;

	private static long workspacesDirectory;

	private static long collectionId;

	private static final String ADMIN_ID = "wfadmin";

	private static final String ADMIN_PASSWORD = "wfadmin!!!";

	private static final String USER_ID = "test-" + new Date().getTime();

	private static final String USER_PASSWORD = "password";

	private DLibraDataSource dl;

	private static final FileRecord[] files = new FileRecord[3];

	private static final String w = "w";
	private static final String r = "r";
	private static final String v = "v";
	private static final String v2 = "v2";

	private static final URI versionURI = URI.create("http://example.com/workspaces/w/ros/r/v");
	private static final URI versionURI2 = URI.create("http://example.com/workspaces/w/ros/r/v2");

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		InputStream inputStream = FlowTests.class.getClassLoader().getResourceAsStream("connection.properties");
		Properties properties = new Properties();
		properties.load(inputStream);
		host = properties.getProperty("host");
		port = Integer.parseInt(properties.getProperty("port"));
		workspacesDirectory = Long.parseLong(properties.getProperty("workspacesDir"));
		collectionId = Long.parseLong(properties.getProperty("collectionId"));
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		dl = new DLibraDataSource(host, port, workspacesDirectory, collectionId, ADMIN_ID, ADMIN_PASSWORD);
		dl.createUser(USER_ID, USER_PASSWORD);
		dl = new DLibraDataSource(host, port, workspacesDirectory, collectionId, USER_ID, USER_PASSWORD);
		dl.createWorkspace("w");
		dl.createResearchObject("w", "r");
		dl.createVersion("w", "r", "v", versionURI);

		files[0] = new FileRecord("file1.txt", "", "file1.txt", "text/plain");
		files[1] = new FileRecord("file2.txt", "dir/", "dir/file2.txt", "text/plain");
		files[2] = new FileRecord("file3.jpg", "testdir/", "testdir/file3.jpg", "image/jpg");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		dl.deleteWorkspace("w");
		dl = new DLibraDataSource(host, port, workspacesDirectory, collectionId, ADMIN_ID, ADMIN_PASSWORD);
		dl.deleteUser(USER_ID);
	}

	@Test
	public final void testAddingResources() throws IdNotFoundException, DigitalLibraryException, IOException {
		createOrUpdateFile(files[0]);
		createOrUpdateFile(files[1]);
		getZippedVersion();
		getFileContent(files[0]);
		getFileContent(files[1]);
		getZippedFolder(files[1]);
		createOrUpdateFile(files[0]);
		createOrUpdateFile(files[1]);
		createVersionAsCopy();
		deleteFile(files[0]);
		deleteFile(files[1]);
		checkNoFile(files[0]);
		checkNoFile(files[1]);
	}

	private void checkNoFile(FileRecord file) throws DigitalLibraryException, IdNotFoundException {
		try {
			dl.deleteFile(versionURI, w, r, v, file.path);
			fail("Deleted file doesn't throw IdNotFoundException");
		} catch (IdNotFoundException e) {
			// good
		}
	}

	private void deleteFile(FileRecord file) throws DigitalLibraryException, IdNotFoundException {
		dl.deleteFile(versionURI, w, r, v, file.path);
	}

	private void createVersionAsCopy() throws DigitalLibraryException, IdNotFoundException {
		dl.createVersion(w, r, v2, versionURI2);
	}

	private void getZippedFolder(FileRecord file) throws DigitalLibraryException, IdNotFoundException, IOException {
		InputStream zip = dl.getZippedFolder(w, r, v, file.dir);
		assertNotNull(zip);
		zip.close();
	}

	private void getFileContent(FileRecord file) throws DigitalLibraryException, IdNotFoundException, IOException {
		InputStream f = dl.getFileContents(w, r, v, file.path);
		assertNotNull(f);
		f.close();
		assertEquals(files[0].mimeType, dl.getFileMimeType(w, r, v, file.path));
	}

	private void getZippedVersion() throws DigitalLibraryException, IdNotFoundException {
		InputStream zip1 = dl.getZippedVersion(w, r, v);
		assertNotNull(zip1);
	}

	private void createOrUpdateFile(FileRecord file) throws DigitalLibraryException, IdNotFoundException, IOException {
		InputStream f = files[0].open();
		ResourceInfo r1 = dl.createOrUpdateFile(versionURI, w, r, v, file.path, f, file.mimeType);
		f.close();
		assertNotNull(r1);
	}

	private class FileRecord {
		public String name;
		public String dir;
		public String path;
		public String mimeType;

		/**
		 * @param name
		 * @param dir
		 * @param path
		 */
		public FileRecord(String name, String dir, String path, String mimeType) {
			this.name = name;
			this.dir = dir;
			this.path = path;
			this.mimeType = mimeType;
		}

		public InputStream open() {
			return this.getClass().getClassLoader().getResourceAsStream(name);
		}
	}

}

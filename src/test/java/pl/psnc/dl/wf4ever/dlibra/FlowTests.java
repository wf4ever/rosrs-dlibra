/**
 * 
 */
package pl.psnc.dl.wf4ever.dlibra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pl.psnc.dl.wf4ever.common.ResearchObject;
import pl.psnc.dl.wf4ever.common.ResourceInfo;
import pl.psnc.dl.wf4ever.dlibra.helpers.DLibraDataSource;
import pl.psnc.dlibra.service.AccessDeniedException;
import pl.psnc.dlibra.service.DLibraException;

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

    private static final String READER_LOGIN = "wf4ever_reader";

    private static final String READER_PASSWORD = "wf4ever_reader!!!";

    private String userId;

    private static final String USER_PASSWORD = "password";

    private static final String USERNAME = "John Doe";

    private DLibraDataSource dl;

    private static final FileRecord[] files = new FileRecord[3];

    private static final String[] directories = { "", "dir/", "testdir" };

    private static final String MAIN_FILE_MIME_TYPE = "text/plain";

    private static final String MAIN_FILE_CONTENT = "test";

    private static final String MAIN_FILE_PATH = "mainFile.txt";

    private static final ResearchObject RO = new ResearchObject(URI.create("http://example.org/ROs/foobar/"));


    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass()
            throws Exception {
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
    public static void tearDownAfterClass()
            throws Exception {
    }


    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp()
            throws Exception {
        userId = "test-" + new Date().getTime();
        dl = new DLibraDataSource(host, port, workspacesDirectory, collectionId, ADMIN_ID, ADMIN_PASSWORD);
        dl.createUser(userId, USER_PASSWORD, USERNAME);
        dl = new DLibraDataSource(host, port, workspacesDirectory, collectionId, userId, USER_PASSWORD);
        dl.createResearchObject(RO, new ByteArrayInputStream(MAIN_FILE_CONTENT.getBytes()), MAIN_FILE_PATH,
            MAIN_FILE_MIME_TYPE);

        files[0] = new FileRecord("file1.txt", "file1.txt", "text/plain");
        files[1] = new FileRecord("file2.txt", "dir/file2.txt", "text/plain");
        files[2] = new FileRecord("file3.jpg", "testdir/file3.jpg", "image/jpg");
    }


    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown()
            throws Exception {
        dl = new DLibraDataSource(host, port, workspacesDirectory, collectionId, userId, USER_PASSWORD);
        dl.deleteResearchObject(RO);
        dl = new DLibraDataSource(host, port, workspacesDirectory, collectionId, ADMIN_ID, ADMIN_PASSWORD);
        dl.deleteUser(userId);
    }


    @Test
    public final void testAddingResources()
            throws DigitalLibraryException, IOException, NotFoundException, ConflictException, AccessDeniedException {
        createOrUpdateFile(files[0]);
        createOrUpdateFile(files[1]);
        getZippedVersion();
        getFileContent(files[0]);
        getFileContent(files[1]);
        checkFileExists(files[0].path);
        getZippedFolder(directories[1]);
        createOrUpdateFile(files[0]);
        createOrUpdateFile(files[1]);
        deleteFile(files[0].path);
        deleteFile(files[1].path);
        checkNoFile(files[0].path);
        checkNoFile(files[1].path);
    }


    @Test
    public final void testEmptyDirectory()
            throws DigitalLibraryException, IOException, NotFoundException, AccessDeniedException {
        createOrUpdateDirectory(directories[1]);
        getZippedFolder(directories[1]);
        createOrUpdateFile(files[1]);
        deleteFile(files[1].path);
        getZippedFolder(directories[1]);
        deleteFile(directories[1]);
        checkNoFile(directories[1]);
    }


    @Test
    public final void testPermissions()
            throws DigitalLibraryException, IOException, NotFoundException, ConflictException, DLibraException {
        createOrUpdateFile(files[0]);
        createOrUpdateFile(files[1]);
        dl = new DLibraDataSource(host, port, workspacesDirectory, collectionId, READER_LOGIN, READER_PASSWORD);
        getFileContent(files[0]);
        getFileContent(files[1]);
        checkCantCreateOrUpdateFile(files[0]);
        checkCantCreateOrUpdateFile(files[1]);
    }


    private void checkNoFile(String path)
            throws DigitalLibraryException, IOException {
        try {
            dl.getFileContents(RO, path).close();
            fail("Deleted file doesn't throw IdNotFoundException");
        } catch (NotFoundException e) {
            // good
        }
    }


    private void checkFileExists(String path)
            throws DigitalLibraryException, NotFoundException {
        Assert.assertTrue(dl.fileExists(RO, path));
    }


    private void deleteFile(String path)
            throws DigitalLibraryException, NotFoundException {
        dl.deleteFile(RO, path);
    }


    private void getZippedFolder(String path)
            throws DigitalLibraryException, IOException, NotFoundException {
        InputStream zip = dl.getZippedFolder(RO, path);
        assertNotNull(zip);
        zip.close();
    }


    private void getFileContent(FileRecord file)
            throws DigitalLibraryException, IOException, NotFoundException {
        InputStream f = dl.getFileContents(RO, file.path);
        assertNotNull(f);
        f.close();
        assertEquals(file.mimeType, dl.getFileMimeType(RO, file.path));
    }


    private void getZippedVersion()
            throws DigitalLibraryException, NotFoundException {
        InputStream zip1 = dl.getZippedVersion(RO);
        assertNotNull(zip1);
    }


    private void createOrUpdateFile(FileRecord file)
            throws DigitalLibraryException, IOException, NotFoundException, AccessDeniedException {
        InputStream f = file.open();
        ResourceInfo r1 = dl.createOrUpdateFile(RO, file.path, f, file.mimeType);
        f.close();
        assertNotNull(r1);
    }


    private void checkCantCreateOrUpdateFile(FileRecord file)
            throws DigitalLibraryException, IOException, NotFoundException {
        InputStream f = file.open();
        try {
            dl.createOrUpdateFile(RO, file.path, f, file.mimeType);
            fail("Should throw an exception when creating file");
        } catch (AccessDeniedException e) {
            // good
        } finally {
            f.close();
        }

    }


    private void createOrUpdateDirectory(String path)
            throws DigitalLibraryException, IOException, NotFoundException, AccessDeniedException {
        ResourceInfo r1 = dl.createOrUpdateFile(RO, path, new ByteArrayInputStream(new byte[0]), "text/plain");
        assertNotNull(r1);
    }


    private class FileRecord {

        public String name;

        public String path;

        public String mimeType;


        /**
         * @param name
         * @param dir
         * @param path
         */
        public FileRecord(String name, String path, String mimeType) {
            this.name = name;
            this.path = path;
            this.mimeType = mimeType;
        }


        public InputStream open() {
            return this.getClass().getClassLoader().getResourceAsStream(name);
        }
    }

}

/**
 * 
 */
package pl.psnc.dl.wf4ever.dlibra;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Set;

import pl.psnc.dlibra.service.AccessDeniedException;

import com.google.common.collect.Multimap;

/**
 * @author piotrhol
 * 
 */
public interface DigitalLibrary {

    public UserProfile getUserProfile()
            throws DigitalLibraryException, NotFoundException;


    public List<String> getResourcePaths(String workspaceId, String researchObjectId, String versionId, String folder)
            throws DigitalLibraryException, NotFoundException;


    public List<String> getResourcePaths(String workspaceId, String researchObjectId, String versionId, String folder,
            long editionId)
            throws DigitalLibraryException, NotFoundException;


    public InputStream getZippedFolder(String workspaceId, String researchObjectId, String versionId, String folder)
            throws DigitalLibraryException, NotFoundException;


    public InputStream getZippedFolder(String workspaceId, String researchObjectId, String versionId, String folder,
            long editionId)
            throws DigitalLibraryException, NotFoundException;


    public InputStream getFileContents(String workspaceId, String researchObjectId, String versionId, String filePath)
            throws DigitalLibraryException, NotFoundException;


    public InputStream getFileContents(String workspaceId, String researchObjectId, String versionId, String filePath,
            long editionId)
            throws DigitalLibraryException, NotFoundException;


    public String getFileMimeType(String workspaceId, String researchObjectId, String versionId, String filePath)
            throws DigitalLibraryException, NotFoundException;


    public String getFileMimeType(String workspaceId, String researchObjectId, String versionId, String filePath,
            long editionId)
            throws DigitalLibraryException, NotFoundException;


    public ResourceInfo createOrUpdateFile(String workspaceId, String researchObjectId, String versionId,
            String filePath, InputStream inputStream, String type)
            throws DigitalLibraryException, NotFoundException, AccessDeniedException;


    public void deleteFile(String workspaceId, String researchObjectId, String versionId, String filePath)
            throws DigitalLibraryException, NotFoundException;


    /**
     * Check if the file exists in the research object.
     * 
     * @param workspaceId
     *            workspace id
     * @param researchObjectId
     *            research object id
     * @param versionId
     *            version id
     * @param filePath
     *            file path
     * @return true if the file exists, false otherwise
     * @throws DigitalLibraryException
     */
    public boolean fileExists(String workspaceId, String researchObjectId, String versionId, String filePath)
            throws DigitalLibraryException;


    public List<String> getResearchObjectIds(String workspaceId)
            throws DigitalLibraryException, NotFoundException;


    public void createResearchObject(String workspaceId, String researchObjectId)
            throws DigitalLibraryException, NotFoundException, ConflictException;


    public List<String> getVersionIds(String workspaceId, String researchObjectId)
            throws DigitalLibraryException, NotFoundException;


    public void createVersion(String workspaceId, String researchObjectId, String version, InputStream mainFileContent,
            String mainFilePath, String mainFileMimeType)
            throws DigitalLibraryException, NotFoundException, ConflictException;


    public void createVersion(String workspaceId, String researchObjectId, String version, String baseVersion)
            throws DigitalLibraryException, NotFoundException;


    public void deleteResearchObject(String workspaceId, String researchObjectId)
            throws DigitalLibraryException, NotFoundException;


    public boolean createUser(String userId, String password, String username)
            throws DigitalLibraryException, NotFoundException, ConflictException;


    public boolean userExists(String userId)
            throws DigitalLibraryException;


    public void deleteUser(String userId)
            throws DigitalLibraryException, NotFoundException;


    public Set<Snapshot> getEditionList(String workspaceId, String researchObjectId, String versionId)
            throws DigitalLibraryException, NotFoundException;


    public InputStream getZippedVersion(String workspaceId, String researchObjectId, String versionId)
            throws DigitalLibraryException, NotFoundException;


    public InputStream getZippedVersion(String workspaceId, String researchObjectId, String versionId, long editionId)
            throws DigitalLibraryException, NotFoundException;


    public void publishVersion(String workspaceId, String researchObjectId, String versionId)
            throws DigitalLibraryException, NotFoundException;


    public void unpublishVersion(String workspaceId, String researchObjectId, String versionId)
            throws DigitalLibraryException, NotFoundException;


    public Snapshot createEdition(String workspaceId, String versionName, String researchObjectId, String versionId)
            throws DigitalLibraryException, NotFoundException;


    public void deleteVersion(String workspaceId, String researchObjectId, String versionId)
            throws DigitalLibraryException, NotFoundException;


    public List<String> getWorkspaceIds()
            throws DigitalLibraryException, NotFoundException;


    public void createWorkspace(String workspaceId)
            throws DigitalLibraryException, NotFoundException, ConflictException;


    public void deleteWorkspace(String workspaceId)
            throws DigitalLibraryException, NotFoundException;


    /**
     * Stores attributes in dLibra for a given RO
     * 
     * @param workspaceId
     * @param researchObjectId
     * @param versionId
     * @param roAttributes
     *            pairs of property URIs and values (String, Calendar, other...)
     * @throws NotFoundException
     * @throws DigitalLibraryException
     */
    public void storeAttributes(String workspaceId, String researchObjectId, String versionId,
            Multimap<URI, Object> roAttributes)
            throws NotFoundException, DigitalLibraryException;

}

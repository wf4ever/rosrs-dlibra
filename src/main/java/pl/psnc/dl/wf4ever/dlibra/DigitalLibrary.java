/**
 * 
 */
package pl.psnc.dl.wf4ever.dlibra;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * @author piotrhol
 * 
 */
public interface DigitalLibrary {

	public UserProfile getUserProfile() throws DigitalLibraryException, NotFoundException;

	public List<String> getResourcePaths(String workspaceId, String researchObjectId, String versionId, String folder)
			throws DigitalLibraryException, NotFoundException;

	public List<String> getResourcePaths(String workspaceId, String researchObjectId, String versionId, String folder,
			long editionId) throws DigitalLibraryException, NotFoundException;

	public InputStream getZippedFolder(String workspaceId, String researchObjectId, String versionId, String folder)
			throws DigitalLibraryException, NotFoundException;

	public InputStream getZippedFolder(String workspaceId, String researchObjectId, String versionId, String folder,
			long editionId) throws DigitalLibraryException, NotFoundException;

	public InputStream getFileContents(String workspaceId, String researchObjectId, String versionId, String filePath)
			throws DigitalLibraryException, NotFoundException;

	public InputStream getFileContents(String workspaceId, String researchObjectId, String versionId, String filePath,
			long editionId) throws DigitalLibraryException, NotFoundException;

	public String getFileMimeType(String workspaceId, String researchObjectId, String versionId, String filePath)
			throws DigitalLibraryException, NotFoundException;

	public String getFileMimeType(String workspaceId, String researchObjectId, String versionId, String filePath,
			long editionId) throws DigitalLibraryException, NotFoundException;

	public ResourceInfo createOrUpdateFile(String workspaceId, String researchObjectId, String versionId,
			String filePath, InputStream inputStream, String type) throws DigitalLibraryException, NotFoundException;

	public void deleteFile(String workspaceId, String researchObjectId, String versionId, String filePath)
			throws DigitalLibraryException, NotFoundException;

	public List<String> getResearchObjectIds(String workspaceId) throws DigitalLibraryException, NotFoundException;

	public void createResearchObject(String workspaceId, String researchObjectId) throws DigitalLibraryException,
			NotFoundException;

	public List<String> getVersionIds(String workspaceId, String researchObjectId) throws DigitalLibraryException,
			NotFoundException;

	public void createVersion(String workspaceId, String researchObjectId, String version)
			throws DigitalLibraryException, NotFoundException;

	public void createVersion(String workspaceId, String researchObjectId, String version, String baseVersion)
			throws DigitalLibraryException, NotFoundException;

	public void deleteResearchObject(String workspaceId, String researchObjectId) throws DigitalLibraryException,
			NotFoundException;

	public void createUser(String userId, String password) throws DigitalLibraryException, NotFoundException,
			ConflictException;

	public boolean userExists(String userId) throws DigitalLibraryException, NotFoundException;

	public void deleteUser(String userId) throws DigitalLibraryException, NotFoundException;

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

	public List<String> getWorkspaceIds() throws DigitalLibraryException, NotFoundException;

	public void createWorkspace(String workspaceId) throws DigitalLibraryException, NotFoundException;

	public void deleteWorkspace(String workspaceId) throws DigitalLibraryException, NotFoundException;
}

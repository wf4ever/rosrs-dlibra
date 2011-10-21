/**
 * 
 */
package pl.psnc.dl.wf4ever.dlibra;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pl.psnc.dl.wf4ever.dlibra.helpers.IncorrectManifestException;
import pl.psnc.dlibra.metadata.Edition;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.service.DuplicatedValueException;
import pl.psnc.dlibra.service.IdNotFoundException;

/**
 * @author piotrhol
 *
 */
public interface DigitalLibrary
{

	public UserProfile getUserProfile()
		throws DigitalLibraryException, IdNotFoundException;


	public List<String> getResourcePaths(String workspaceId,
			String researchObjectId, String versionId, String folder)
		throws DigitalLibraryException, IdNotFoundException;


	public List<String> getResourcePaths(String workspaceId,
			String researchObjectId, String versionId, String folder,
			long editionId)
		throws DigitalLibraryException, IdNotFoundException;


	@Deprecated
	public String getFileMetadata(String workspaceId, String researchObjectId,
			String versionId, String filePath, URI baseURI)
		throws DigitalLibraryException, IdNotFoundException;


	@Deprecated
	public String getFileMetadata(String workspaceId, String researchObjectId,
			String versionId, String filePath, long editionId, URI baseURI)
		throws DigitalLibraryException, IdNotFoundException;


	public InputStream getZippedFolder(String workspaceId,
			String researchObjectId, String versionId, String folder)
		throws DigitalLibraryException, IdNotFoundException;


	public InputStream getZippedFolder(String workspaceId,
			String researchObjectId, String versionId, String folder,
			long editionId)
		throws DigitalLibraryException, IdNotFoundException;


	public InputStream getFileContents(String workspaceId,
			String researchObjectId, String versionId, String filePath)
		throws DigitalLibraryException, IdNotFoundException;


	public InputStream getFileContents(String workspaceId,
			String researchObjectId, String versionId, String filePath,
			long editionId)
		throws DigitalLibraryException, IdNotFoundException;


	public String getFileMimeType(String workspaceId, String researchObjectId,
			String versionId, String filePath)
		throws DigitalLibraryException, IdNotFoundException;


	public String getFileMimeType(String workspaceId, String researchObjectId,
			String versionId, String filePath, long editionId)
		throws DigitalLibraryException, IdNotFoundException;


	public ResourceInfo createOrUpdateFile(URI versionUri, String workspaceId,
			String researchObjectId, String versionId, String filePath,
			InputStream inputStream, String type)
		throws DigitalLibraryException, IdNotFoundException;


	public void deleteFile(URI versionUri, String workspaceId,
			String researchObjectId, String versionId, String filePath)
		throws DigitalLibraryException, IdNotFoundException;


	public List<String> getResearchObjectIds(String workspaceId)
		throws DigitalLibraryException, IdNotFoundException;


	public List<String> getVersionIds(String workspaceId,
			Map<String, List<String>> queryParameters)
		throws DigitalLibraryException, IdNotFoundException;


	public void createResearchObject(String workspaceId, String researchObjectId)
		throws DigitalLibraryException, IdNotFoundException;


	public List<String> getVersionIds(String workspaceId,
			String researchObjectId)
		throws DigitalLibraryException, IdNotFoundException;


	public void createVersion(String workspaceId, String researchObjectId,
			String version, URI resourceUri)
		throws DigitalLibraryException, IdNotFoundException;


	public void createVersion(String workspaceId, String researchObjectId,
			String version, String baseVersion, URI resourceUri)
		throws DigitalLibraryException, IdNotFoundException;


	public void deleteResearchObject(String workspaceId, String researchObjectId)
		throws DigitalLibraryException, IdNotFoundException;


	public void createUser(String userId, String password)
		throws DigitalLibraryException, IdNotFoundException,
		DuplicatedValueException;


	public boolean userExists(String userId)
		throws DigitalLibraryException, IdNotFoundException;


	public void deleteUser(String userId)
		throws DigitalLibraryException, IdNotFoundException;


	public Set<Edition> getEditionList(String workspaceId,
			String researchObjectId, String versionId)
		throws DigitalLibraryException, IdNotFoundException;


	@Deprecated
	public InputStream getManifest(String workspaceId, String researchObjectId,
			String versionId)
		throws DigitalLibraryException, IdNotFoundException;


	@Deprecated
	public InputStream getManifest(String workspaceId, String researchObjectId,
			String versionId, long editionId)
		throws DigitalLibraryException, IdNotFoundException;


	public InputStream getZippedVersion(String workspaceId,
			String researchObjectId, String versionId)
		throws DigitalLibraryException, IdNotFoundException;


	public InputStream getZippedVersion(String workspaceId,
			String researchObjectId, String versionId, long editionId)
		throws DigitalLibraryException, IdNotFoundException;


	public void publishVersion(String workspaceId, String researchObjectId,
			String versionId)
		throws DigitalLibraryException, IdNotFoundException;


	public void unpublishVersion(String workspaceId, String researchObjectId,
			String versionId)
		throws DigitalLibraryException, IdNotFoundException;


	@Deprecated
	public void updateManifest(URI versionUri, String researchObjectId,
			String versionId, ByteArrayInputStream body)
		throws DigitalLibraryException, IncorrectManifestException,
		IdNotFoundException;


	public EditionId createEdition(String workspaceId, String versionName,
			String researchObjectId, String versionId)
		throws DigitalLibraryException, IdNotFoundException;


	public void deleteVersion(String workspaceId, String researchObjectId,
			String versionId, URI versionURI)
		throws DigitalLibraryException, IdNotFoundException;


	public List<String> getWorkspaceIds()
		throws DigitalLibraryException, IdNotFoundException;


	public void createWorkspace(String workspaceId)
		throws DigitalLibraryException, IdNotFoundException;


	public void deleteWorkspace(String workspaceId)
		throws DigitalLibraryException, IdNotFoundException;
}

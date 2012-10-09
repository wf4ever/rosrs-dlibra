/**
 * 
 */
package pl.psnc.dl.wf4ever.dlibra;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.List;

import javax.xml.transform.TransformerException;

import pl.psnc.dl.wf4ever.common.ResearchObject;
import pl.psnc.dl.wf4ever.common.ResourceInfo;
import pl.psnc.dl.wf4ever.common.UserProfile;
import pl.psnc.dlibra.service.AccessDeniedException;
import pl.psnc.dlibra.service.DLibraException;

import com.google.common.collect.Multimap;

/**
 * Digital library abstraction.
 * 
 * @author piotrhol
 * 
 */
public interface DigitalLibrary {

    /**
     * Get the profile of the user that is logged in to dLibra.
     * 
     * @return user profile
     * @throws NotFoundException
     *             file not found
     * @throws DigitalLibraryException
     *             dLibra exception
     */
    UserProfile getUserProfile()
            throws DigitalLibraryException, NotFoundException;


    /**
     * Get the profile of a user.
     * 
     * @param login
     *            user login
     * @return user profile
     * @throws NotFoundException
     *             file not found
     * @throws DigitalLibraryException
     *             dLibra exception
     */
    UserProfile getUserProfile(String login)
            throws DigitalLibraryException, NotFoundException;


    public List<String> getResourcePaths(ResearchObject ro, String folder)
            throws DigitalLibraryException, NotFoundException;


    public InputStream getZippedFolder(ResearchObject ro, String folder)
            throws DigitalLibraryException, NotFoundException;


    public InputStream getFileContents(ResearchObject ro, String filePath)
            throws DigitalLibraryException, NotFoundException;


    public String getFileMimeType(ResearchObject ro, String filePath)
            throws DigitalLibraryException, NotFoundException;


    public ResourceInfo createOrUpdateFile(ResearchObject ro, String filePath, InputStream inputStream, String type)
            throws DigitalLibraryException, NotFoundException, AccessDeniedException;


    /**
     * Return the file metadata.
     * 
     * @param workspaceId
     *            workspace id
     * @param researchObjectId
     *            RO id
     * @param versionId
     *            version id
     * @param filePath
     *            file path
     * @return file metadata
     * @throws NotFoundException
     *             file not found
     * @throws AccessDeniedException
     *             no rights to get resource metadata
     * @throws DigitalLibraryException
     *             dLibra exception
     */
    ResourceInfo getFileInfo(ResearchObject ro, String filePath)
            throws NotFoundException, DigitalLibraryException, AccessDeniedException;


    public void deleteFile(ResearchObject ro, String filePath)
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
    public boolean fileExists(ResearchObject ro, String filePath)
            throws DigitalLibraryException;


    public void createResearchObject(ResearchObject ro, InputStream mainFileContent, String mainFilePath,
            String mainFileMimeType)
            throws DigitalLibraryException, NotFoundException, ConflictException;


    public void deleteResearchObject(ResearchObject ro)
            throws DigitalLibraryException, NotFoundException, RemoteException, DLibraException, IOException,
            TransformerException;


    public boolean createUser(String userId, String password, String username)
            throws DigitalLibraryException, NotFoundException, ConflictException;


    public boolean userExists(String userId)
            throws DigitalLibraryException;


    public void deleteUser(String userId)
            throws DigitalLibraryException, NotFoundException;


    public InputStream getZippedVersion(ResearchObject ro)
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
    public void storeAttributes(ResearchObject ro, Multimap<URI, Object> roAttributes)
            throws NotFoundException, DigitalLibraryException;

}

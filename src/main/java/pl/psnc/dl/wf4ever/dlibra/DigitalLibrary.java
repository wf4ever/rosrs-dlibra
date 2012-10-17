/**
 * 
 */
package pl.psnc.dl.wf4ever.dlibra;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

import pl.psnc.dl.wf4ever.common.ResearchObject;
import pl.psnc.dl.wf4ever.common.ResourceInfo;
import pl.psnc.dl.wf4ever.common.UserProfile;
import pl.psnc.dlibra.service.AccessDeniedException;

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
     *             user profile not found
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
     *             user profile not found
     * @throws DigitalLibraryException
     *             dLibra exception
     */
    UserProfile getUserProfile(String login)
            throws DigitalLibraryException, NotFoundException;


    /**
     * Get paths relative to RO of all stored content inside a specified folder.
     * 
     * @param ro
     *            research object
     * @param folder
     *            folder path or null
     * @return a list of paths
     * @throws DigitalLibraryException
     *             dLibra error
     * @throws NotFoundException
     *             research object not found
     */
    List<String> getResourcePaths(ResearchObject ro, String folder)
            throws DigitalLibraryException, NotFoundException;


    /**
     * Get a ZIP archive with all content stored inside a specified folder.
     * 
     * @param ro
     *            research object
     * @param folder
     *            folder path or null
     * @return ZIP input stream
     * @throws DigitalLibraryException
     *             dLibra error
     * @throws NotFoundException
     *             research object not found
     */
    InputStream getZippedFolder(ResearchObject ro, String folder)
            throws DigitalLibraryException, NotFoundException;


    /**
     * Get file content.
     * 
     * @param ro
     *            research object
     * @param filePath
     *            file path
     * @return ZIP input stream
     * @throws DigitalLibraryException
     *             dLibra error
     * @throws NotFoundException
     *             research object not found
     */
    InputStream getFileContents(ResearchObject ro, String filePath)
            throws DigitalLibraryException, NotFoundException;


    /**
     * Check if the file exists in the research object.
     * 
     * @param ro
     *            research object
     * @param filePath
     *            file path
     * @return true if the file exists, false otherwise
     * @throws DigitalLibraryException
     *             dLibra error
     */
    boolean fileExists(ResearchObject ro, String filePath)
            throws DigitalLibraryException;


    /**
     * Create a new file or update an existing one.
     * 
     * @param ro
     *            research object
     * @param filePath
     *            file path
     * @param inputStream
     *            content input stream
     * @param type
     *            MIME type
     * @return file metadata
     * @throws DigitalLibraryException
     *             dLibra error
     * @throws NotFoundException
     *             research object not found
     * @throws AccessDeniedException
     *             no access rights
     */
    ResourceInfo createOrUpdateFile(ResearchObject ro, String filePath, InputStream inputStream, String type)
            throws DigitalLibraryException, NotFoundException, AccessDeniedException;


    /**
     * Return the file metadata.
     * 
     * @param ro
     *            research object
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


    /**
     * Delete a file.
     * 
     * @param ro
     *            research object
     * @param filePath
     *            file path
     * @throws DigitalLibraryException
     *             dLibra error
     * @throws NotFoundException
     *             research object or file not found
     */
    void deleteFile(ResearchObject ro, String filePath)
            throws DigitalLibraryException, NotFoundException;


    /**
     * Create a new research object.
     * 
     * @param ro
     *            research object, can't have the dLibra ids set
     * @param mainFileContent
     *            main file input stream
     * @param mainFilePath
     *            main file path
     * @param mainFileMimeType
     *            main file MIME type
     * @throws DigitalLibraryException
     *             dLibra error
     * @throws ConflictException
     *             a research object with the given id already exists
     */
    void createResearchObject(ResearchObject ro, InputStream mainFileContent, String mainFilePath,
            String mainFileMimeType)
            throws DigitalLibraryException, ConflictException;


    /**
     * Delete a research object.
     * 
     * @param ro
     *            research object
     * @throws DigitalLibraryException
     *             dLibra error
     * @throws NotFoundException
     *             research object was not found
     */
    void deleteResearchObject(ResearchObject ro)
            throws DigitalLibraryException, NotFoundException;


    /**
     * Create a user.
     * 
     * @param userId
     *            user login
     * @param password
     *            user password
     * @param username
     *            human-friendly name
     * @return true if the user was created, false if it already existed
     * @throws DigitalLibraryException
     *             dLibra error
     */
    boolean createUser(String userId, String password, String username)
            throws DigitalLibraryException;


    /**
     * Check if a user with a given login exists.
     * 
     * @param userId
     *            user login
     * @return true if the user exists, false otherwise
     * @throws DigitalLibraryException
     *             dLibra error
     */
    boolean userExists(String userId)
            throws DigitalLibraryException;


    /**
     * Delete a user.
     * 
     * @param userId
     *            user login
     * @throws DigitalLibraryException
     *             dLibra error
     * @throws NotFoundException
     *             user not found
     */
    void deleteUser(String userId)
            throws DigitalLibraryException, NotFoundException;


    /**
     * Get a ZIP archive with the research object contents.
     * 
     * @param ro
     *            research object
     * @return ZIP input stream
     * @throws DigitalLibraryException
     *             dLibra error
     * @throws NotFoundException
     *             research object not found
     */
    InputStream getZippedResearchObject(ResearchObject ro)
            throws DigitalLibraryException, NotFoundException;


    /**
     * Stores attributes in dLibra for a given RO.
     * 
     * @param ro
     *            research object
     * @param roAttributes
     *            pairs of property URIs and values (String, Calendar, other...)
     * @throws DigitalLibraryException
     *             dLibra error
     * @throws NotFoundException
     *             research object not found
     */
    void storeAttributes(ResearchObject ro, Multimap<URI, Object> roAttributes)
            throws NotFoundException, DigitalLibraryException;

}

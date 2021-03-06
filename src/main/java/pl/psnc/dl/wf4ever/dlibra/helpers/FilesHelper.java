package pl.psnc.dl.wf4ever.dlibra.helpers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.TransformerException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import pl.psnc.dl.wf4ever.dl.ResourceMetadata;
import pl.psnc.dl.wf4ever.dlibra.hibernate.ResearchObject;
import pl.psnc.dlibra.common.Id;
import pl.psnc.dlibra.common.InputFilter;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.content.ContentServer;
import pl.psnc.dlibra.metadata.EditionFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.File;
import pl.psnc.dlibra.metadata.FileFilter;
import pl.psnc.dlibra.metadata.FileInfo;
import pl.psnc.dlibra.metadata.FileManager;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.PublicationManager;
import pl.psnc.dlibra.metadata.VersionId;
import pl.psnc.dlibra.metadata.VersionInfo;
import pl.psnc.dlibra.service.AccessDeniedException;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;

public class FilesHelper {

    private final static Logger logger = Logger.getLogger(FilesHelper.class);

    private final DLibraDataSource dLibra;

    private final PublicationManager publicationManager;

    private final FileManager fileManager;

    private final ContentServer contentServer;


    public FilesHelper(DLibraDataSource dLibraDataSource)
            throws RemoteException {
        this.dLibra = dLibraDataSource;

        this.publicationManager = dLibraDataSource.getMetadataServer().getPublicationManager();
        this.fileManager = dLibraDataSource.getMetadataServer().getFileManager();
        this.contentServer = dLibraDataSource.getContentServer();
    }


    /**
     * Returns filepaths of all files in a given folder, except for manifest.rdf.
     * 
     * @param groupPublicationName
     * @param publicationName
     * @param folder
     *            If null, all files in the publication will be returned
     * @return List of filepaths, starting with "/"
     * @throws RemoteException
     * @throws DLibraException
     */
    public List<String> getFilePathsInFolder(ResearchObject ro, String folder)
            throws RemoteException, DLibraException {
        ArrayList<String> result = new ArrayList<String>();
        for (FileInfo fileInfo : getFilesInFolder(ro, folder).values()) {
            if (EmptyFoldersUtility.isDlibraPath(fileInfo.getFullPath())) {
                result.add(EmptyFoldersUtility.convertDlibra2Real(fileInfo.getFullPath()));
            } else {
                result.add(fileInfo.getFullPath());
            }
        }
        return result;
    }


    /**
     * 
     * @param groupPublicationName
     * @param publicationName
     * @param folder
     * @return FileInfo will have paths starting with "/"
     * @throws RemoteException
     * @throws DLibraException
     */
    private Map<VersionId, FileInfo> getFilesInFolder(ResearchObject ro, String folder)
            throws RemoteException, DLibraException {
        Map<VersionId, FileInfo> result = new HashMap<VersionId, FileInfo>();
        if (folder != null && !folder.endsWith("/"))
            folder = folder.concat("/");

        List<Id> versionIds = (List<Id>) publicationManager.getObjects(
            new EditionFilter(new EditionId(dLibra.getDlEditionId(ro))), new OutputFilter(VersionId.class))
                .getResultIds();
        for (Id id : versionIds) {
            VersionId versionId = (VersionId) id;

            FileInfo fileInfo = (FileInfo) fileManager.getObjects(new InputFilter(versionId),
                new OutputFilter(FileInfo.class)).getResultInfo();

            String filePath = fileInfo.getFullPath();
            if (EmptyFoldersUtility.isDlibraPath(filePath)
                    && EmptyFoldersUtility.convertDlibra2Real(filePath).equals("/" + folder)) {
                // empty folder
                result.clear();
                return result;
            }
            if (folder == null || filePath.startsWith("/" + folder)) {
                result.put(versionId, fileInfo);
            }
        }

        if (folder != null && result.isEmpty()) {
            throw new IdNotFoundException(folder);
        }

        return result;
    }


    /**
     * Returns input stream for a zipped content of file in a publication that are inside a given folder. Includes
     * manifest.rdf.
     * 
     * @param publicationName
     * @param folderNotStandardized
     * @return
     * @throws RemoteException
     * @throws DLibraException
     */
    public InputStream getZippedFolder(final ResearchObject ro, String folderNotStandardized)
            throws RemoteException, DLibraException {
        final String folder = (folderNotStandardized == null ? null
                : (folderNotStandardized.endsWith("/") ? folderNotStandardized : folderNotStandardized.concat("/")));
        final Map<VersionId, FileInfo> fileVersionsAndInfos = getFilesInFolder(ro, folder);

        PipedInputStream in = new PipedInputStream();
        final PipedOutputStream out;
        try {
            out = new PipedOutputStream(in);
        } catch (IOException e) {
            throw new RuntimeException("This should never happen", e);
        }

        final ZipOutputStream zipOut = new ZipOutputStream(out);

        new Thread("edition zip downloader (" + ro.getDlEditionId() + ")") {

            @Override
            public void run() {
                try {
                    for (Map.Entry<VersionId, FileInfo> mapEntry : fileVersionsAndInfos.entrySet()) {
                        VersionId versionId = mapEntry.getKey();
                        String filePath = mapEntry.getValue().getFullPath().substring(1);
                        ZipEntry entry = new ZipEntry(filePath);
                        zipOut.putNextEntry(entry);
                        logger.debug("Creating a version input stream for " + versionId.toString() + " edition "
                                + ro.getDlEditionId());
                        InputStream versionInputStream = new UnlockingInputStream(
                                contentServer.getVersionInputStream(versionId), contentServer, versionId);
                        logger.debug("Created a version input stream for " + versionId.toString());
                        try {
                            logger.debug("Start copying stream for " + versionId.toString());
                            IOUtils.copy(versionInputStream, zipOut);
                            logger.debug("Finished copying stream for " + versionId.toString());
                        } finally {
                            logger.debug("Closing stream for " + versionId.toString());
                            versionInputStream.close();
                        }
                    }
                } catch (IOException e) {
                    logger.error("Zip transmission failed", e);
                } catch (DLibraException e) {
                    logger.error("Zip transmission failed", e);
                } finally {
                    try {
                        zipOut.close();
                    } catch (Exception e) {
                        logger.warn("Could not close the ZIP file: " + e.getMessage());
                        try {
                            out.close();
                        } catch (IOException e1) {
                            logger.error("Could not close the ZIP output stream", e1);
                        }
                    }
                }
            };
        }.start();
        return in;
    }


    public InputStream getFileContents(ResearchObject ro, String filePath)
            throws IdNotFoundException, RemoteException, DLibraException {
        VersionId versionId = getVersionId(ro, filePath);

        InputStream versionInputStream = new UnlockingInputStream(contentServer.getVersionInputStream(versionId),
                contentServer, versionId);
        logger.debug("Returning a version stream for version Id " + versionId.toString() + " edition Id "
                + ro.getDlEditionId());
        return versionInputStream;
    }


    public boolean fileExists(ResearchObject ro, String filePath)
            throws IdNotFoundException, RemoteException, DLibraException {
        return getVersionId(ro, filePath) != null;
    }


    /*
     * from http://rgagnon.com/javadetails/java-0596.html
     */
    private String getHex(byte[] raw) {
        final String HEXES = "0123456789ABCDEF";
        if (raw == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }


    public ResourceMetadata createOrUpdateFile(ResearchObject ro, String filePath, InputStream inputStream,
            String mimeType)
            throws IOException, DLibraException, TransformerException {
        PublicationId roVersionId = new PublicationId(dLibra.getDlROVersionId(ro));
        EditionId editionId = new EditionId(dLibra.getDlEditionId(ro));

        if (filePath.endsWith("/")) {
            // slash at the end means empty folder
            logger.debug("Slash at the end, file " + filePath + " will be an empty folder");
            filePath = EmptyFoldersUtility.convertReal2Dlibra(filePath);
        }

        VersionId versionId = getVersionIdSafe(ro, filePath);
        VersionId createdVersionId = createNewVersion(versionId, mimeType, roVersionId, filePath);

        saveFileContents(inputStream, createdVersionId);

        if (versionId != null) {
            publicationManager.removeEditionVersion(editionId, versionId);
        }
        publicationManager.addEditionVersion(editionId, createdVersionId);
        versionId = getVersionIdSafe(ro, filePath);

        deleteUnnecessaryEmptyFolders(ro, filePath);

        VersionInfo versionInfo = (VersionInfo) fileManager.getObjects(new InputFilter(versionId),
            new OutputFilter(VersionInfo.class)).getResultInfo();
        return createResourceInfo(versionInfo, filePath, mimeType);
    }


    public ResourceMetadata getFileInfo(ResearchObject ro, String filePath)
            throws RemoteException, IdNotFoundException, AccessDeniedException, DLibraException {
        VersionId versionId = getVersionId(ro, filePath);
        VersionInfo versionInfo = (VersionInfo) fileManager.getObjects(new InputFilter(versionId),
            new OutputFilter(VersionInfo.class)).getResultInfo();
        FileInfo fileInfo = (FileInfo) fileManager.getObjects(new FileFilter(versionInfo.getFileId()),
            new OutputFilter(FileInfo.class)).getResultInfo();
        return createResourceInfo(versionInfo, filePath, fileInfo.getMimeType());
    }


    private ResourceMetadata createResourceInfo(VersionInfo versionInfo, String filePath, String mimeType)
            throws RemoteException, IdNotFoundException, AccessDeniedException, DLibraException {
        String name = filePath.substring(filePath.lastIndexOf('/') + 1);
        byte[] fileDigest = contentServer.getFileDigest(versionInfo.getId());
        String digest = getHex(fileDigest);
        long size = versionInfo.getSize();
        DateTime lastModified = new DateTime(versionInfo.getLastModificationDate());
        return new ResourceMetadata(filePath, name, digest, size, "MD5", lastModified, mimeType);
    }


    /**
     * @param groupPublicationName
     * @param publicationName
     * @param filePath
     * @throws DLibraException
     * @throws IOException
     * @throws TransformerException
     */
    private void deleteUnnecessaryEmptyFolders(ResearchObject ro, String filePath)
            throws DLibraException, IOException, TransformerException {
        String intermediateFilePath = filePath;
        while (intermediateFilePath.lastIndexOf("/") > 0) {
            intermediateFilePath = intermediateFilePath.substring(0, intermediateFilePath.lastIndexOf("/"));
            try {
                deleteFile(ro, EmptyFoldersUtility.convertReal2Dlibra(intermediateFilePath));
            } catch (IdNotFoundException ex) {
                // ok, this folder was not empty
            }
        }
    }


    /**
     * @param filePath
     * @param editionId
     * @return
     * @throws RemoteException
     * @throws DLibraException
     */
    public VersionId getVersionIdSafe(ResearchObject ro, String filePath)
            throws RemoteException, DLibraException {
        try {
            return getVersionId(ro, filePath);
        } catch (IdNotFoundException e) {
            logger.debug(String.format("Failed to find version of %s for edition %d", filePath, ro.getDlEditionId()));
        }
        return null;
    }


    private VersionId createNewVersion(VersionId oldVersionId, String mimeType, PublicationId publicationId,
            String filePath)
            throws IdNotFoundException, RemoteException, DLibraException {
        File file;
        if (oldVersionId != null) {
            VersionInfo versionInfo = (VersionInfo) fileManager.getObjects(new InputFilter(oldVersionId),
                new OutputFilter(VersionInfo.class)).getResultInfo();
            file = (File) fileManager.getObjects(new FileFilter(versionInfo.getFileId()), new OutputFilter(File.class))
                    .getResult();
        } else {
            file = new File(mimeType, publicationId, "/" + filePath);
        }
        return fileManager.createVersion(file, 0, new Date(), "").getId();

    }


    /**
     * @param inputStream
     * @param versionId
     * @throws RemoteException
     * @throws IdNotFoundException
     * @throws AccessDeniedException
     * @throws DLibraException
     * @throws IOException
     */
    private void saveFileContents(InputStream inputStream, VersionId versionId)
            throws RemoteException, IdNotFoundException, AccessDeniedException, DLibraException, IOException {
        OutputStream output = contentServer.getVersionOutputStream(versionId);
        try {
            byte[] buffer = new byte[DLibraDataSource.BUFFER_SIZE];
            int bytesRead = 0;

            while ((bytesRead = inputStream.read(buffer)) > 0) {
                output.write(buffer, 0, bytesRead);
            }
        } finally {
            inputStream.close();
            output.close();
        }
    }


    public void deleteFile(ResearchObject ro, String filePath)
            throws DLibraException, IOException, TransformerException {
        EditionId editionId = new EditionId(dLibra.getDlEditionId(ro));

        boolean recreateEmptyFolder = false;
        String emptyFolder = "";
        try {
            VersionId versionId = getVersionId(ro, filePath);

            emptyFolder = filePath.substring(0, filePath.lastIndexOf("/") + 1);
            if (!emptyFolder.isEmpty() && getFilePathsInFolder(ro, emptyFolder).size() == 1) {
                recreateEmptyFolder = true;
            }

            publicationManager.removeEditionVersion(editionId, versionId);
        } catch (IdNotFoundException ex) {
            // maybe it is a folder
            List<String> files = getFilePathsInFolder(ro, filePath);
            if (!files.isEmpty()) {
                for (String file : files) {
                    if (file.startsWith("/")) {
                        file = file.substring(1);
                    }
                    VersionId versionId = getVersionId(ro, file);
                    publicationManager.removeEditionVersion(editionId, versionId);
                }
            } else {
                // it must be an empty folder
                try {
                    filePath = EmptyFoldersUtility.convertReal2Dlibra(filePath);
                    VersionId versionId = getVersionId(ro, filePath);
                    logger.debug(String.format("Removing empty folder, file version %s from edition %s", versionId,
                        editionId));
                    publicationManager.removeEditionVersion(editionId, versionId);
                } catch (IdNotFoundException ex2) {
                    // if not, throw the original exception
                    logger.debug("Nothing to delete, error");
                    throw ex;
                }
            }
        }

        if (recreateEmptyFolder) {
            createOrUpdateFile(ro, emptyFolder, new ByteArrayInputStream(new byte[] {}), "text/plain");
        }

        // TODO check if there are any references to the file and delete with
        // filemanager if no
    }


    public VersionId getVersionId(ResearchObject ro, String filePath)
            throws IdNotFoundException, RemoteException, DLibraException {
        VersionId versionId = (VersionId) fileManager.getObjects(
            new FileFilter().setEditionId(new EditionId(dLibra.getDlEditionId(ro))).setFileName("/" + filePath),
            new OutputFilter(VersionId.class)).getResultId();
        return versionId;
    }

}

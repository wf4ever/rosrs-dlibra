/**
 * 
 */
package pl.psnc.dl.wf4ever.dlibra.helpers;

import java.rmi.RemoteException;
import java.util.Collection;

import pl.psnc.dlibra.common.DLObject;
import pl.psnc.dlibra.common.InputFilter;
import pl.psnc.dlibra.common.OutputFilter;
import pl.psnc.dlibra.metadata.Edition;
import pl.psnc.dlibra.metadata.EditionFilter;
import pl.psnc.dlibra.metadata.EditionId;
import pl.psnc.dlibra.metadata.PublicationFilter;
import pl.psnc.dlibra.metadata.PublicationId;
import pl.psnc.dlibra.metadata.PublicationManager;
import pl.psnc.dlibra.metadata.VersionId;
import pl.psnc.dlibra.service.AccessDeniedException;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.IdNotFoundException;

/**
 * @author piotrhol
 * 
 */
public class EditionHelper {

    private final PublicationManager publicationManager;


    public EditionHelper(DLibraDataSource dLibraDataSource)
            throws RemoteException {
        publicationManager = dLibraDataSource.getMetadataServer().getPublicationManager();
    }


    /**
     * Returns the most recently created edition of the RO version (publication).
     * 
     * @param publicationId
     *            Id of the publication (RO version).
     * @return
     * @throws RemoteException
     * @throws DLibraException
     *             in case no edition is found
     */
    public Edition getLastEdition(PublicationId publicationId)
            throws RemoteException, DLibraException {
        InputFilter in = new PublicationFilter(null, publicationId).setEditionState(Edition.ALL_STATES
                - Edition.PERMANENT_DELETED);
        OutputFilter out = new OutputFilter(Edition.class);
        Collection<DLObject> results = publicationManager.getObjects(in, out).getResults();
        if (results.isEmpty()) {
            return null;
        }
        Edition result = null;
        for (DLObject object : results) {
            Edition edition = (Edition) object;
            if (result == null
                    || edition.getCreationDate().after(result.getCreationDate())
                    || (edition.getCreationDate().equals(result.getCreationDate()) && edition.getId().getId() > result
                            .getId().getId())) {
                result = edition;
            }
        }
        return result;
    }


    public Edition getEdition(EditionId editionId)
            throws RemoteException, DLibraException {
        InputFilter in = new EditionFilter(editionId);
        OutputFilter out = new OutputFilter(Edition.class);
        return (Edition) publicationManager.getObjects(in, out).getResult();
    }


    /**
     * @param publicationName
     * @param publicationId
     * @param createdVersion
     * @return
     * @throws DLibraException
     * @throws AccessDeniedException
     * @throws IdNotFoundException
     * @throws RemoteException
     * @throws IllegalArgumentException
     */
    EditionId createEdition(String editionName, PublicationId publicationId, VersionId[] versionIds)
            throws DLibraException, AccessDeniedException, IdNotFoundException, RemoteException,
            IllegalArgumentException {
        Edition edition = new Edition(null, publicationId, false);
        edition.setName(editionName);
        return publicationManager.createEdition(edition, versionIds);
    }

}

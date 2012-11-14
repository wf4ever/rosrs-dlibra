package pl.psnc.dl.wf4ever.dlibra;

import java.net.URI;

import pl.psnc.dl.wf4ever.dl.DigitalPublication;

public class ResearchObject implements DigitalPublication {

    private long workspaceId;

    private long roId;

    private long versionId;

    private long editionId;

    private URI uri;


    public ResearchObject(URI roUri) {
        setUri(roUri);
    }


    @Override
    public long getDlWorkspaceId() {
        return workspaceId;
    }


    @Override
    public void setDlWorkspaceId(long dlWorkspaceId) {
        this.workspaceId = dlWorkspaceId;
    }


    @Override
    public long getDlROId() {
        return roId;
    }


    @Override
    public void setDlROId(long dlROId) {
        this.roId = dlROId;
    }


    @Override
    public long getDlROVersionId() {
        return versionId;
    }


    @Override
    public void setDlROVersionId(long dlROVersionId) {
        this.versionId = dlROVersionId;
    }


    @Override
    public long getDlEditionId() {
        return editionId;
    }


    @Override
    public void setDlEditionId(long dlEditionId) {
        this.editionId = dlEditionId;
    }


    @Override
    public URI getUri() {
        return uri;
    }


    @Override
    public void setUri(URI uri) {
        this.uri = uri;
    }
}

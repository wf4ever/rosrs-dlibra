package pl.psnc.dl.wf4ever.dlibra.helpers;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import pl.psnc.dlibra.content.ContentServer;
import pl.psnc.dlibra.metadata.VersionId;
import pl.psnc.dlibra.service.DLibraException;

/**
 * Encapsulates the {@link InputStream} used by dLibra, so that the underlying file is unlocked.
 * 
 * @author piotrekhol
 * 
 */
public class UnlockingInputStream extends InputStream {

    /** Logger. */
    private static final Logger LOG = Logger.getLogger(UnlockingInputStream.class);

    /** Underlying stream. */
    private InputStream inputStream;

    /** dLibra content server. */
    private ContentServer contentServer;

    /** dLibra file version id. */
    private VersionId versionId;


    /**
     * Constructor.
     * 
     * @param inputStream
     *            underlying input stream
     * @param contentServer
     *            dLibra content server
     * @param versionId
     *            file version id
     */
    public UnlockingInputStream(InputStream inputStream, ContentServer contentServer, VersionId versionId) {
        this.inputStream = inputStream;
        this.contentServer = contentServer;
        this.versionId = versionId;
    }


    @Override
    public int available()
            throws IOException {
        return inputStream.available();
    }


    @Override
    public void close()
            throws IOException {
        super.close();
        if (contentServer != null && versionId != null) {
            try {
                contentServer.releaseElement(versionId);
            } catch (DLibraException e) {
                LOG.error("Could not release element " + versionId, e);
            }
        }
    }


    @Override
    public synchronized void mark(int readlimit) {
        inputStream.mark(readlimit);
    }


    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }


    @Override
    public int read()
            throws IOException {
        return inputStream.read();
    }


    @Override
    public int read(byte[] b)
            throws IOException {
        return inputStream.read(b);
    }


    @Override
    public int read(byte[] b, int off, int len)
            throws IOException {
        return inputStream.read(b, off, len);
    }


    @Override
    public synchronized void reset()
            throws IOException {
        inputStream.reset();
    }


    @Override
    public long skip(long n)
            throws IOException {
        return inputStream.skip(n);
    }

}

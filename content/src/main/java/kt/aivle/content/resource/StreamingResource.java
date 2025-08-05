// StreamingResource.java - 스트리밍용 리소스 wrapper
package kt.aivle.content.resource;

import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

public class StreamingResource implements Resource {
    private final Resource delegate;
    private final String contentType;
    private final long contentLength;

    public StreamingResource(Resource delegate, String contentType, long contentLength) {
        this.delegate = delegate;
        this.contentType = contentType;
        this.contentLength = contentLength;
    }

    @Override
    public boolean exists() {
        return delegate.exists();
    }

    @Override
    public boolean isReadable() {
        return delegate.isReadable();
    }

    @Override
    public boolean isOpen() {
        return delegate.isOpen();
    }

    @Override
    public boolean isFile() {
        return delegate.isFile();
    }

    @Override
    public URL getURL() throws IOException {
        return delegate.getURL();
    }

    @Override
    public URI getURI() throws IOException {
        return delegate.getURI();
    }

    @Override
    public File getFile() throws IOException {
        return delegate.getFile();
    }

    @Override
    public long contentLength() throws IOException {
        return contentLength > 0 ? contentLength : delegate.contentLength();
    }

    @Override
    public long lastModified() throws IOException {
        return delegate.lastModified();
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        return delegate.createRelative(relativePath);
    }

    @Override
    public String getFilename() {
        return delegate.getFilename();
    }

    @Override
    public String getDescription() {
        return "Streaming resource [" + delegate.getDescription() + "]";
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return delegate.getInputStream();
    }

    public String getContentType() {
        return contentType;
    }
}
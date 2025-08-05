// PartialFileSystemResource.java - Range Request용 리소스
package kt.aivle.content.resource;

import org.springframework.core.io.AbstractResource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class PartialFileSystemResource extends AbstractResource {
    private final Path path;
    private final long start;
    private final long end;
    private final long contentLength;

    public PartialFileSystemResource(Path path, long start, long end) {
        this.path = path;
        this.start = start;
        this.end = end;
        this.contentLength = end - start + 1;
    }

    @Override
    public String getDescription() {
        return "Partial file [" + this.path.toAbsolutePath() + "] range [" + start + "-" + end + "]";
    }

    @Override
    public InputStream getInputStream() throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(path.toFile(), "r");
        randomAccessFile.seek(start);

        return new BoundedInputStream(
                new FileInputStream(randomAccessFile.getFD()),
                contentLength
        );
    }

    @Override
    public long contentLength() {
        return contentLength;
    }

    @Override
    public boolean exists() {
        return Files.exists(path);
    }

    @Override
    public boolean isReadable() {
        return Files.isReadable(path);
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }
}
// BoundedInputStream.java - 제한된 바이트만 읽는 InputStream
package kt.aivle.content.resource;

import java.io.IOException;
import java.io.InputStream;

public class BoundedInputStream extends InputStream {
    private final InputStream inputStream;
    private long remaining;

    public BoundedInputStream(InputStream inputStream, long maxBytes) {
        this.inputStream = inputStream;
        this.remaining = maxBytes;
    }

    @Override
    public int read() throws IOException {
        if (remaining <= 0) {
            return -1;
        }
        int result = inputStream.read();
        if (result != -1) {
            remaining--;
        }
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (remaining <= 0) {
            return -1;
        }
        len = (int) Math.min(len, remaining);
        int result = inputStream.read(b, off, len);
        if (result != -1) {
            remaining -= result;
        }
        return result;
    }

    @Override
    public long skip(long n) throws IOException {
        n = Math.min(n, remaining);
        long skipped = inputStream.skip(n);
        remaining -= skipped;
        return skipped;
    }

    @Override
    public int available() throws IOException {
        return (int) Math.min(inputStream.available(), remaining);
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    public long getRemaining() {
        return remaining;
    }
}
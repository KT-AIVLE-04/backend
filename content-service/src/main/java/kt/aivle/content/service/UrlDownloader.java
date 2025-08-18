package kt.aivle.content.service;

import kt.aivle.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static kt.aivle.content.exception.ContentErrorCode.URI_DOWNLOAD_ERROR;

@Component
@RequiredArgsConstructor
public class UrlDownloader {

    private static final List<String> ALLOWED_VIDEO = List.of(
            "video/mp4", "video/quicktime", "video/x-matroska", "video/webm"
    );

    public Downloaded fetch(String url) throws Exception {
        var uri = URI.create(url);
        var conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setConnectTimeout(15_000);
        conn.setReadTimeout(120_000);
        conn.setInstanceFollowRedirects(true);

        int code = conn.getResponseCode();
        if (code / 100 != 2) throw new BusinessException(URI_DOWNLOAD_ERROR, "원본 응답 오류: " + code);

        String ct = conn.getHeaderField("Content-Type");
        if (ct == null) ct = "application/octet-stream";
        if (ALLOWED_VIDEO.stream().noneMatch(ct::startsWith)) {
            throw new BusinessException(URI_DOWNLOAD_ERROR, "허용되지 않은 타입: " + ct);
        }

        String filename = guessName(conn, uri);
        Path tmp = Files.createTempFile("vid-", getExtSafe(filename));
        try (InputStream in = conn.getInputStream()) {
            Files.copy(in, tmp, REPLACE_EXISTING);
        }

        long size = Files.size(tmp);
        return new Downloaded(filename, ct, tmp, size);
    }

    private String guessName(HttpURLConnection conn, URI uri) {
        String cd = conn.getHeaderField("Content-Disposition");
        if (cd != null) {
            for (String part : cd.split(";")) {
                String p = part.trim();
                if (p.startsWith("filename=")) return p.substring(9).replace("\"", "");
            }
        }
        String path = uri.getPath();
        String last = path.substring(path.lastIndexOf('/') + 1);
        return last.isBlank() ? "video.mp4" : last;
    }

    private String getExtSafe(String name) {
        if (name == null) return ".mp4";
        int i = name.lastIndexOf('.');
        return i >= 0 ? name.substring(i) : ".mp4";
    }

    public record Downloaded(String originalName, String contentType, Path file, long size) {
    }
}


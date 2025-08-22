package kt.aivle.content.infra;

import kt.aivle.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.cookie.CookiesForCustomPolicy;
import software.amazon.awssdk.services.cloudfront.model.CannedSignerRequest;
import software.amazon.awssdk.services.cloudfront.model.CustomSignerRequest;
import software.amazon.awssdk.services.cloudfront.url.SignedUrl;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;
import static kt.aivle.content.exception.ContentErrorCode.CLOUD_FRONT_SIGN_ERROR;

@Component
@RequiredArgsConstructor
public class CloudFrontSigner {

    @Value("${cdn.domain}")
    private String cdnDomain;

    @Value("${cdn.keyPairId}")
    private String keyPairId;

    @Value("${cdn.ttl}")
    private int defaultTtl;

    private static final String KEY_FILE = "CLOUDFRONT_PRIVATE_KEY";

    public String signOriginalUrl(String objectKey) {
        try {
            PrivateKey pk = loadPkFromFile();

            String encodedKey = UriUtils.encodePath(objectKey, UTF_8);

            SignedUrl signed = CloudFrontUtilities.create().getSignedUrlWithCannedPolicy(
                    CannedSignerRequest.builder()
                            .resourceUrl("https://" + cdnDomain + "/" + encodedKey)
                            .keyPairId(keyPairId)
                            .privateKey(pk)
                            .expirationDate(Instant.now().plusSeconds(defaultTtl))
                            .build()
            );
            return signed.url();
        } catch (Exception e) {
            throw new BusinessException(CLOUD_FRONT_SIGN_ERROR, e.getMessage());
        }
    }

    public String getThumbUrl(String objectKey) {
        String encodedKey = UriUtils.encodePath(objectKey, UTF_8);
        return "https://" + cdnDomain + "/" + encodedKey;
    }

    public CookiesForCustomPolicy issueThumbCookiesFor(String thumbPrefix) {
        try {
            String encodedPrefix = UriUtils.encodePath(thumbPrefix, UTF_8);
            String resourcePattern = "https://" + cdnDomain + "/" + encodedPrefix + "*";
            PrivateKey pk = loadPkFromFile();
            return CloudFrontUtilities.create().getCookiesForCustomPolicy(
                    CustomSignerRequest.builder()
                            .resourceUrl(resourcePattern)
                            .keyPairId(keyPairId)
                            .privateKey(pk)
                            .expirationDate(Instant.now().plusSeconds(defaultTtl))
                            .build()
            );
        } catch (Exception e) {
            throw new BusinessException(CLOUD_FRONT_SIGN_ERROR, e.getMessage());
        }
    }

    private PrivateKey loadPkFromFile() throws Exception {
        String pem = System.getenv(KEY_FILE);
        byte[] der = Base64.getDecoder().decode(pem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }
}
package kt.aivle.sns.infra;

import kt.aivle.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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

import static kt.aivle.sns.exception.SnsErrorCode.CLOUD_FRONT_SIGN_ERROR;

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
            SignedUrl signed = CloudFrontUtilities.create().getSignedUrlWithCannedPolicy(
                    CannedSignerRequest.builder()
                            .resourceUrl("https://" + cdnDomain + "/" + objectKey)
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
        return "https://" + cdnDomain + "/" + objectKey;
    }

    public CookiesForCustomPolicy issueThumbCookiesFor(String thumbPrefix) {
        try {
            String resourcePattern = "https://" + cdnDomain + "/" + thumbPrefix + "*";

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
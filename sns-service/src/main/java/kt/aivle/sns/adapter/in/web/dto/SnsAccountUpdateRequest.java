package kt.aivle.sns.adapter.in.web.dto;

public class SnsAccountUpdateRequest {

    private Long storeId;
    private String snsAccountId; // 유튜브 채널 id

    private String snsAccountDescription; // 채널 설명
    private String[] keywords; // 채널 키워드

    public SnsAccountUpdateRequest() {};

    public String getSnsAccountId() {
        return snsAccountId;
    }

    public void setSnsAccountId(String snsAccountId) {
        this.snsAccountId = snsAccountId;
    }

    public String getSnsAccountDescription() {
        return snsAccountDescription;
    }

    public void setSnsAccountDescription(String snsAccountDescription) {
        this.snsAccountDescription = snsAccountDescription;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    public Long getStoreId() {
        return storeId;
    }
}

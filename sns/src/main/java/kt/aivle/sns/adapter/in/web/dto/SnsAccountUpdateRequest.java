package kt.aivle.sns.adapter.in.web.dto;

import kt.aivle.sns.domain.model.SnsType;

public class SnsAccountUpdateRequest {

    private SnsType snsType; // sns 타입
    private String snsAccountId; // 유튜브 채널 id

    private String snsAccountDescription; // 채널 설명
    private String[] keywords; // 채널 키워드

    public SnsAccountUpdateRequest() {};

    public SnsType getSnsType() {
        return snsType;
    }

    public void setSnsType(SnsType snsType) {
        this.snsType = snsType;
    }

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
}

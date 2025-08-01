package kt.aivle.store.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Industry {
    AGRICULTURE("A", "농업, 임업 및 어업"),
    MINING("B", "광업"),
    MANUFACTURING("C", "제조업"),
    ELECTRICITY("D", "전기, 가스, 증기 및 공기조절"),
    WATER("E", "수도, 하수, 폐기물 관리"),
    CONSTRUCTION("F", "건설업"),
    RETAIL("G", "도매 및 소매업"),
    TRANSPORT("H", "운수 및 창고업"),
    FOOD("I", "숙박 및 음식점업"),
    ICT("J", "정보통신업"),
    FINANCE("K", "금융 및 보험업"),
    REAL_ESTATE("L", "부동산업"),
    PROFESSIONAL("M", "전문, 과학 및 기술 서비스업"),
    BUSINESS("N", "사업시설관리 및 지원 서비스업"),
    PUBLIC("O", "공공행정, 국방"),
    EDUCATION("P", "교육서비스업"),
    HEALTH("Q", "보건 및 사회복지 서비스업"),
    CULTURE("R", "예술, 스포츠 및 여가"),
    PERSONAL("S", "수리 및 기타 개인 서비스업"),
    HOUSEHOLD("T", "가구 내 고용활동 등"),
    FOREIGN("U", "국제 및 외국기관"),
    ETC("ETC", "기타");

    private final String code;
    private final String label;
}



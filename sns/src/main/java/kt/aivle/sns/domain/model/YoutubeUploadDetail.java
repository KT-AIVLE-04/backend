package kt.aivle.sns.domain.model;

import java.time.OffsetDateTime;

public class YoutubeUploadDetail {
    private String categoryId; // 카테고리 번호로 입력 (String)
//    1 	영화/애니메이션    Film & Animation
//    2	    자동차 및 차량    Autos & Vehicles
//    10	음악	            Music
//    15	동물	            Pets & Animals
//    17	스포츠           Sports
//    18	단편 영화        Short Movies
//    19	여행 및 이벤트	Travel & Events
//    20	게임	            Gaming
//    21	블로그	        Videoblogging
//    22	사람 & 블로그	People & Blogs
//    23	코미디	        Comedy
//    24	엔터테인먼트	    Entertainment
//    25	뉴스 및 정치	    News & Politics
//    26	강의	            Howto & Style
//    27	교육	            Education
//    28	과학기술	        Science & Technology
//    30	영화	            Movies
//    31	애니메이션	    Anime/Animation
//    32	액션/어드벤처	    Action/Adventure
//    34	드라마	        Drama
//    35	가족	            Family
//    36	외국	            Foreign
//    37	공포	            Horror
//    38	SF	            Sci-Fi/Fantasy
//    39	스릴러	        Thriller
//    40	단편 영화	    Shorts
//    41	쇼	            Shows
//    42	예고편	        Trailers
    private boolean notifySubscribers;
    private OffsetDateTime publishAt;


    public YoutubeUploadDetail() {}

    public OffsetDateTime getPublishAt() {
        return publishAt;
    }

    public void setPublishAt(OffsetDateTime publishAt) {
        this.publishAt = publishAt;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public boolean isNotifySubscribers() {
        return notifySubscribers;
    }

    public void setNotifySubscribers(boolean notifySubscribers) {
        this.notifySubscribers = notifySubscribers;
    }
}

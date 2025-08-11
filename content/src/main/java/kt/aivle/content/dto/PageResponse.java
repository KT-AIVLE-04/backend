package kt.aivle.content.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 페이징 응답 DTO
 *
 * Spring Data JPA의 Page 객체를 클라이언트 친화적인 형태로 변환
 */
public class PageResponse<T> {

    private List<T> content;           // 현재 페이지의 데이터
    private int page;                  // 현재 페이지 번호 (0부터 시작)
    private int size;                  // 페이지 크기
    private long totalElements;        // 전체 요소 수
    private int totalPages;            // 전체 페이지 수
    private boolean first;             // 첫 번째 페이지 여부
    private boolean last;              // 마지막 페이지 여부
    private boolean hasNext;           // 다음 페이지 존재 여부
    private boolean hasPrevious;       // 이전 페이지 존재 여부
    private int numberOfElements;      // 현재 페이지의 요소 수

    // 기본 생성자
    public PageResponse() {}

    // 생성자
    public PageResponse(List<T> content, int page, int size, long totalElements, int totalPages,
                        boolean first, boolean last, boolean hasNext, boolean hasPrevious, int numberOfElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = first;
        this.last = last;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
        this.numberOfElements = numberOfElements;
    }

    // Spring Data Page에서 PageResponse로 변환하는 정적 팩토리 메소드
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious(),
                page.getNumberOfElements()
        );
    }

    // 페이지 정보 요약 문자열
    public String getPageSummary() {
        int start = page * size + 1;
        int end = Math.min(start + numberOfElements - 1, (int) totalElements);
        return String.format("%d-%d of %d", start, end, totalElements);
    }

    // 현재 페이지가 비어있는지 확인
    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }

    // Getters and Setters
    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    public int getNumberOfElements() {
        return numberOfElements;
    }

    public void setNumberOfElements(int numberOfElements) {
        this.numberOfElements = numberOfElements;
    }
}
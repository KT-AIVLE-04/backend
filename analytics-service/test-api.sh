#!/bin/bash

# Analytics Service API 테스트 스크립트
# 사용법: ./test-api.sh [base_url] [user_id]
# 예시: ./test-api.sh http://localhost:8084 123

# 기본값 설정
BASE_URL=${1:-"http://localhost:8080"}
USER_ID=${2:-"123"}
API_BASE="${BASE_URL}/api/analytics"

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 로그 함수
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# API 테스트 함수
test_api() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    
    log_info "테스트: $description"
    echo "  $method $endpoint"
    
    if [ -n "$data" ]; then
        echo "  Body: $data"
    fi
    
    # API 호출
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" \
            -H "Content-Type: application/json" \
            -H "X-USER-ID: $USER_ID" \
            "$endpoint")
    else
        response=$(curl -s -w "\n%{http_code}" \
            -H "Content-Type: application/json" \
            -H "X-USER-ID: $USER_ID" \
            -X "$method" \
            -d "$data" \
            "$endpoint")
    fi
    
    # 응답 분리
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n -1)
    
    # 결과 출력
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        log_success "HTTP $http_code"
        echo "  Response: $body" | head -c 200
        if [ ${#body} -gt 200 ]; then
            echo "..."
        fi
    else
        log_error "HTTP $http_code"
        echo "  Response: $body"
    fi
    
    echo ""
}

# 헬스체크
check_health() {
    log_info "=== 서비스 헬스체크 ==="
    health_response=$(curl -s "$BASE_URL/actuator/health" 2>/dev/null)
    if [ $? -eq 0 ] && [ -n "$health_response" ]; then
        log_success "서비스가 정상 동작 중입니다."
        echo "  Health: $health_response"
    else
        log_error "서비스에 연결할 수 없습니다. 서비스가 실행 중인지 확인해주세요."
        exit 1
    fi
    echo ""
}

# 메인 테스트 실행
main() {
    echo "=========================================="
    echo "    Analytics Service API 테스트"
    echo "=========================================="
    echo "Base URL: $BASE_URL"
    echo "User ID: $USER_ID"
    echo "=========================================="
    echo ""
    
    # 헬스체크
    check_health
    
    # 1. 메트릭 조회 API 테스트
    log_info "=== 메트릭 조회 API 테스트 ==="
    
    # 게시물 메트릭 조회 (전체)
    test_api "GET" "$API_BASE/post-metrics" \
        '{"dateRange": "1week"}' \
        "게시물 메트릭 조회 (전체)"
    
    # 게시물 메트릭 조회 (특정 계정)
    test_api "GET" "$API_BASE/post-metrics" \
        '{"dateRange": "1week", "accountId": "1"}' \
        "게시물 메트릭 조회 (특정 계정)"
    
    # 게시물 메트릭 조회 (특정 게시물)
    test_api "GET" "$API_BASE/post-metrics" \
        '{"dateRange": "1week", "postId": "1"}' \
        "게시물 메트릭 조회 (특정 게시물)"
    
    # 계정 메트릭 조회 (전체)
    test_api "GET" "$API_BASE/account-metrics" \
        '{"dateRange": "1week"}' \
        "계정 메트릭 조회 (전체)"
    
    # 계정 메트릭 조회 (특정 계정)
    test_api "GET" "$API_BASE/account-metrics" \
        '{"dateRange": "1week", "accountId": "1"}' \
        "계정 메트릭 조회 (특정 계정)"
    
    # 댓글 조회 (전체)
    test_api "GET" "$API_BASE/post-comments" \
        '{"dateRange": "1week"}' \
        "댓글 조회 (전체)"
    
    # 댓글 조회 (특정 게시물)
    test_api "GET" "$API_BASE/post-comments" \
        '{"dateRange": "1week", "postId": "1"}' \
        "댓글 조회 (특정 게시물)"
    
    # 2. 배치 작업 API 테스트
    log_info "=== 배치 작업 API 테스트 ==="
    
    # 배치 작업 상태 조회
    test_api "GET" "$API_BASE/batch/status" \
        "" \
        "배치 작업 상태 조회 (전체)"
    
    # 특정 배치 작업 상태 조회
    test_api "GET" "$API_BASE/batch/status/account-metrics-collection" \
        "" \
        "계정 메트릭 수집 작업 상태 조회"
    
    # 3. 배치 작업 실행 API 테스트 (주의: 실제 API 호출됨)
    log_warning "=== 배치 작업 실행 API 테스트 (실제 실행됨) ==="
    
    read -p "배치 작업을 실행하시겠습니까? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        # 계정 메트릭 수집
        test_api "POST" "$API_BASE/batch/collect-account-metrics" \
            "" \
            "계정 메트릭 수집 실행"
        
        # 게시물 메트릭 수집
        test_api "POST" "$API_BASE/batch/collect-post-metrics" \
            "" \
            "게시물 메트릭 수집 실행"
        
        # 댓글 수집
        test_api "POST" "$API_BASE/batch/collect-post-comments" \
            "" \
            "댓글 수집 실행"
        
        # 특정 계정 메트릭 수집
        test_api "POST" "$API_BASE/batch/collect-account-metrics/1" \
            "" \
            "특정 계정(1) 메트릭 수집 실행"
        
        # 특정 게시물 메트릭 수집
        test_api "POST" "$API_BASE/batch/collect-post-metrics/1" \
            "" \
            "특정 게시물(1) 메트릭 수집 실행"
        
        # 모든 메트릭 수집
        test_api "POST" "$API_BASE/batch/collect-all-metrics" \
            "" \
            "모든 메트릭 수집 실행"
        
        # 수집 후 상태 확인
        log_info "=== 수집 후 배치 작업 상태 확인 ==="
        test_api "GET" "$API_BASE/batch/status" \
            "" \
            "배치 작업 상태 조회 (수집 후)"
    else
        log_info "배치 작업 실행을 건너뜁니다."
    fi
    
    # 4. 에러 케이스 테스트
    log_info "=== 에러 케이스 테스트 ==="
    
    # 잘못된 User ID
    test_api "GET" "$API_BASE/post-metrics" \
        '{"dateRange": "1week"}' \
        "잘못된 User ID로 요청 (X-USER-ID 헤더 없음)"
    
    # 잘못된 날짜 범위
    test_api "GET" "$API_BASE/post-metrics" \
        '{"dateRange": "invalid"}' \
        "잘못된 날짜 범위로 요청"
    
    # 존재하지 않는 엔드포인트
    test_api "GET" "$API_BASE/nonexistent" \
        "" \
        "존재하지 않는 엔드포인트 요청"
    
    echo "=========================================="
    log_success "API 테스트 완료!"
    echo "=========================================="
}

# 스크립트 실행
main "$@"

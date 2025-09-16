import http from 'k6/http';
import {check, sleep} from 'k6';

// 성능 비교 테스트 (낮은 부하)
export let options = {
    stages: [
        {duration: '30s', target: 10},    // Ramp up to 5 users
        {duration: '60s', target: 30},   // Ramp up to 10 users
        {duration: '60s', target: 50},   // Stay at 10 users
        {duration: '30s', target: 0},    // Ramp down
    ],
    thresholds: {
        http_req_duration: ['p(95)<15000'], // 95% of requests should be below 15s
        http_req_failed: ['rate<0.2'],     // Error rate should be below 20%
    },
};

const BASE_URL = 'http://localhost:8080';

export default function () {
    const testWebFlux = __ENV.TEST_TYPE === 'webflux' || !__ENV.TEST_TYPE;
    const apiPath = testWebFlux ? '/api/shorts' : '/api/shorts-mvc';

    // 테스트용 이미지 데이터 (적당한 크기 - 50KB)
    const imageData = Array.from({length: 1024 * 50}, () => Math.floor(Math.random() * 256));

    const sessionId = `test-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;

    const requestData = {
        sessionId: sessionId,
        title: `Test Shorts ${testWebFlux ? 'WebFlux' : 'MVC'}`,
        content: `Performance test for ${testWebFlux ? 'WebFlux' : 'MVC'} version`,
        adDuration: 15
    };

    // k6에서 multipart/form-data 방식 (단일 이미지로 간소화)
    const imageBuffer = new Uint8Array(imageData).buffer;
    const formData = {
        request: http.file(JSON.stringify(requestData), 'request.json', 'application/json'),
        images: http.file(imageBuffer, 'test-image.jpg', 'image/jpeg')
    };

    const startTime = Date.now();

    // Gateway를 통한 Shorts 생성 요청
    const response = http.post(`${BASE_URL}${apiPath}`, formData);

    const requestTime = Date.now() - startTime;

    check(response, {
        'status is 202': (r) => r.status === 202,
        'has jobId': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.result && body.result.jobId;
            } catch (e) {
                return false;
            }
        },
    });

    // Silent execution

    sleep(2);
}
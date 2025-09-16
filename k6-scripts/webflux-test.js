import http from 'k6/http';
import {check, sleep} from 'k6';

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
    // 테스트용 이미지 데이터 (적당한 크기 - 100KB)
    const imageData = Array.from({length: 1024 * 100}, () => Math.floor(Math.random() * 256));

    const sessionId = `webflux-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;

    // JSON 요청 데이터
    const requestData = {
        sessionId: sessionId,
        title: 'Performance Test Shorts WebFlux',
        content: 'This is a performance test for WebFlux version',
        adDuration: 16
    };

    // k6에서 multipart/form-data 방식 (단일 이미지로 간소화)
    const imageBuffer = new Uint8Array(imageData).buffer;
    const formData = {
        request: http.file(JSON.stringify(requestData), 'request.json', 'application/json'),
        images: http.file(imageBuffer, 'test-image.jpg', 'image/jpeg')
    };

    // 1. Shorts 생성 요청 (타임아웃 늘리기)
    const response = http.post(`${BASE_URL}/api/shorts`, formData, {
        timeout: '30s',  // 30초 타임아웃
    });

    check(response, {
        'shorts creation status is 202': (r) => r.status === 202,
        'response time < 2000ms': (r) => r.timings.duration < 2000,
        'has jobId': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.result && body.result.jobId;
            } catch (e) {
                return false;
            }
        },
    });

    if (response.status === 202) {
        try {
            const result = JSON.parse(response.body);
            const jobId = result.result.jobId;

            // 2. Job 상태 확인 (AI 처리 시간 고려)
            let jobCompleted = false;
            let attempts = 0;
            const maxAttempts = 15;  // 최대 15번 시도
            const totalWaitTime = 30; // 최대 30초 대기

            while (!jobCompleted && attempts < maxAttempts) {
                sleep(2);  // 2초마다 체크 (AI 처리 시간 고려)

                let jobResponse = http.get(`${BASE_URL}/api/shorts/jobs/${jobId}`, {
                    timeout: '10s',  // Job 상태 확인도 타임아웃 설정
                });

                check(jobResponse, {
                    'job status check is 200': (r) => r.status === 200,
                });

                if (jobResponse.status === 200) {
                    try {
                        const jobResult = JSON.parse(jobResponse.body);
                        const status = jobResult.result.status;

                        if (status === 'SUCCEEDED' || status === 'FAILED') {
                            jobCompleted = true;

                            check(jobResponse, {
                                'job completed successfully': (r) => {
                                    const result = JSON.parse(r.body);
                                    return result.result.status === 'SUCCEEDED';
                                },
                            });
                            break;
                        }
                    } catch (e) {
                        break;
                    }
                } else {
                    break;
                }

                attempts++;
            }
        } catch (e) {
            // Silent error handling
        }
    }

    sleep(1);  // 짧게 조정 (Job 상태 확인에서 이미 충분히 대기)
}
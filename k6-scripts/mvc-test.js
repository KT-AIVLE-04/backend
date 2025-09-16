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

    const sessionId = `mvc-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;

    // JSON 요청 데이터
    const requestData = {
        sessionId: sessionId,
        title: 'Performance Test Shorts MVC',
        content: 'This is a performance test for MVC version',
        adDuration: 30
    };

    // k6에서 multipart/form-data 방식 (단일 이미지로 간소화)
    const imageBuffer = new Uint8Array(imageData).buffer;
    const formData = {
        request: http.file(JSON.stringify(requestData), 'request.json', 'application/json'),
        images: http.file(imageBuffer, 'test-image.jpg', 'image/jpeg')
    };

    // 1. Shorts 생성 요청 (타임아웃 늘리기)
    const response = http.post(`${BASE_URL}/api/shorts-mvc`, formData, {
        timeout: '30s',
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

            // 2. Job 상태 확인 (몇 번 체크)
            let jobCompleted = false;
            let attempts = 0;
            const maxAttempts = 10;

            while (!jobCompleted && attempts < maxAttempts) {
                sleep(0.5);

                let jobResponse = http.get(`${BASE_URL}/api/shorts-mvc/jobs/${jobId}`);

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
                        }
                    } catch (e) {
                        console.error('Failed to parse job status response:', e);
                    }
                }

                attempts++;
            }
        } catch (e) {
            console.error('Failed to parse shorts creation response:', e);
        }
    }

    sleep(1);
}
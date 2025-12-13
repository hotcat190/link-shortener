import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';
import { getRandomId, getRandomIp } from './helpers.js';
import { BASE_URL } from './constants.js'

// 1. CONFIGURATION
export const options = {
  scenarios: {
    viral_load: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 50 }, 
        { duration: '30s', target: 100 },  
        { duration: '30s', target: 200 }, 
        { duration: '30s', target: 100 },
        { duration: '60s', target: 0 },
      ],
    },
  },
  thresholds: {
    // 95% of requests must finish within 500ms
    http_req_duration: ['p(95)<500'],
    // Error rate must be less than 1%
    http_req_failed: ['rate<0.01'],
  },
};

// 2. SETUP (Run once before the test starts)
export function setup() {
  // Create ONE link that will become "viral"
  const payload = JSON.stringify({
    url: 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', // The viral content
    customShortenedUrl: `viral-${Math.random().toString(36).substring(7)}`,
    ttlMinute: null
  });
  
  const params = { headers: { 'Content-Type': 'application/json' } };
  const res = http.post(`${BASE_URL}/api`, payload, params);
  const body = res.body;
  return { viralShortCode: body }; 
}

// 3. VIRTUAL USER LOGIC
export default function (data) {
  const ip = getRandomIp()
  // 'data' contains whatever we returned from setup()
  const viralCode = data.viralShortCode;

  // We want 95% Reads, 5% Writes
  const roll = Math.random();

  if (roll < 0.95) {
    // --- READ OPERATION (The Viral Click) ---
    // This hits the cache or DB hard for the same key
    const res = http.get(`${BASE_URL}/${viralCode}`, {
      headers: {
        "X-Forwarded-For": ip,
      },
    });
    
    check(res, {
      'read status is 200 or 302': (r) => r.status === 200 || r.status === 302,
    });

  } else {
    // --- WRITE OPERATION (Random New Links) ---
    // Simulates regular users creating unrelated links in the background
    const payload = JSON.stringify({
      url: `https://example.com/page/${randomIntBetween(1, 10000)}`,
    });
    const params = { headers: { 'Content-Type': 'application/json', "X-Forwarded-For": ip } };
    
    const res = http.post(`${BASE_URL}/api`, payload, params);

    check(res, {
      'write status is 200 or 201': (r) => r.status === 200 || r.status === 201,
    });
  }

  // Short pause to simulate human "think time" (0.1s to 1s)
  sleep(0.1 + Math.random() * 0.9); 
}
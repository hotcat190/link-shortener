import http from "k6/http";
import { fail, sleep } from "k6";
import { vu } from "k6/execution"; // Import the vu execution context

// --- Helper Functions (Copied from fe_both_eval.js) ---

// Sample shortened url ids for testing
import sampleValidIds from "./constants.js";

const sampleUrls = [
  "https://youtube.com",
  "https://google.com",
  "https://facebook.com",
  "https://twitter.com",
  "https://instagram.com",
  "https://linkedin.com",
  "https://github.com",
  "https://reddit.com",
  "https://pinterest.com",
];

function getRandomId() {
  // We still use random here to hit different links
  const randomIndex = Math.floor(Math.random() * sampleValidIds.length);
  return sampleValidIds[randomIndex];
}

function getRandomUrl() {
  const randomIndex = Math.floor(Math.random() * sampleUrls.length);
  return sampleUrls[randomIndex];
}

function getRandomTtlMinuteOrNull() {
  if (Math.random() < 0.5) {
    return null; 
  } else {
    return Math.floor(Math.random() * 60) + 1;
  }
}

function getRandomIp() {
  return `192.168.1.${Math.floor(Math.random() * 200)}`;
}

// --- k6 Options ---

export let options = {
  vus: parseInt(__ENV.VUS || "50"),
  duration: __ENV.DURATION || "60s",
};

http.setResponseCallback(http.expectedStatuses(200, 404, 409, 429));

// --- Main Test Function (Modified) ---

export default function () {
  const ip = getRandomIp();
  let res;

  // Get the unique virtual user ID (starts at 1)
  const vuId = vu.idInTest;

  // Use modulo (%) to assign a fixed role to each VU
  // This creates a predictable 80% / 10% / 10% workload split
  
  if (vuId % 10 < 8) {
    // --- ROLE 1: 80% of VUs (IDs 1-8, 11-18, etc.) ---
    // These VUs will ONLY send GET requests
    res = http.get(`http://localhost:80/api/${getRandomId()}`, {
      headers: {
        "X-Forwarded-For": ip,
      },
    });

  } else if (vuId % 10 == 8) {
    // --- ROLE 2: 10% of VUs (IDs 8, 18, 28, etc.) ---
    // These VUs will ONLY send ASYNC POST requests (random URL)
    const body = {
      url: getRandomUrl(),
      ttlMinute: getRandomTtlMinuteOrNull(),
      customShortenedUrl: null, // This triggers the fast async path
    };
    res = http.post("http://localhost:80/api", JSON.stringify(body), {
      headers: {
        "Content-Type": "application/json",
        "X-Forwarded-For": ip,
      },
    });

  } else {
    // --- ROLE 3: 10% of VUs (IDs 9, 19, 29, etc.) ---
    // These VUs will ONLY send SYNC POST requests (custom URL)
    const body = {
      url: getRandomUrl(),
      ttlMinute: getRandomTtlMinuteOrNull(),
      // We use the VU ID to make the custom URL unique to this user
      // This avoids 409 conflicts between VUs
      customShortenedUrl: `custom-vu-${vuId}-${getRandomId()}`, 
    };
    res = http.post("http://localhost:80/api", JSON.stringify(body), {
      headers: {
        "Content-Type": "application/json",
        "X-Forwarded-For": ip,
      },
    });
  }

  // --- Validation (Unchanged) ---
  if (!(res.status === 200 || res.status === 404 || res.status === 409 || res.status === 429)) {
    fail(`Unexpected status code: ${res.status}`);
  }

  sleep(0.1);
}
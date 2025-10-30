import http from "k6/http";
import { fail, sleep } from "k6";

// Sample shortened url ids for testing
// Please replace with your own ids if needed
const sampleValidIds = [
  "custom-bx16aellkbo",
  "2epE39Theaurt",
  "custom-tsg5ngiiwj",
  "nXwYdslernE",
  "custom-nmkdwbe2zn",
  "custom-jidmld2eish",
  "custom-w0y5u8tr35",
  "custom-gdbtuo02p57",
  "custom-fp8ww4rlab",
  "5TXMTIWr5zbsOsQ",
  "SbhEWaIp3Wyh9",
  "custom-jsovfy5dbjr",
  "sRAVm",
  "SQfkTUgMWI1e",
  "custom-fymm23vr7ra",
  "custom-u82nd8d3p4",
  "L9a6fziuyW",
  "YFzX",
  "custom-7yb3x7oqyn7",
  "Qim87NG",
];

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
  "https://chatgpt.com/c/67f600c5-4a0c-8001-a849-ead89fab4685",
  "https://chat.openai.com/c/67f600c5-4a0c-8001-a849-ead89fab4685",
  "https://chat.openai.com/c/67f600c5-4a0c-8001-a849-ead89fab4686",
  "https://chat.openai.com/c/67f600c5-4a0c-8001-a849-ead89fab4687",
  "https://chat.openai.com/c/67f600c5-4a0c-8001-a849-ead89fab4688",
  "https://chat.openai.com/c/67f600c5-4a0c-8001-a849-ead89fab4689",
  "https://chat.openai.com/c/67f600c5-4a0c-8001-a849-ead89fab4690",
];

function getRandomId() {
  if (Math.random() < 0.5) {
    const randomIndex = Math.floor(Math.random() * sampleValidIds.length);
    return sampleValidIds[randomIndex];
  } else {
    return Math.random().toString(36).substring(24);
  }
}

function getRandomUrl() {
  const randomIndex = Math.floor(Math.random() * sampleUrls.length);
  return sampleUrls[randomIndex];
}

function getRandomTtlMinuteOrNull() {
  if (Math.random() < 0.5) {
    return null; // 50% chance to return null
  } else {
    return Math.floor(Math.random() * 60) + 1; // Random number between 1 and 60
  }
}

function getRandomCustomShortenedUrlOrNull() {
  if (Math.random() < 0.5) {
    return null; // 50% chance to return null
  } else {
    const randomString = Math.random().toString(36).substring(2, 33); // Random string of length 32
    return `custom-${randomString}`;
  }
}

// 200 random ip address
function getRandomIp() {
  return `192.168.1.${Math.floor(Math.random() * 200)}`;
}

function getBody() {
  const url = getRandomUrl();

  const ttlMinute = getRandomTtlMinuteOrNull();

  const customShortenedUrl = getRandomCustomShortenedUrlOrNull();

  return {
    url,
    ttlMinute,
    customShortenedUrl,
  };
}

export let options = {
  vus: parseInt(__ENV.VUS || "50"),
  duration: __ENV.DURATION || "60s",
};

http.setResponseCallback(http.expectedStatuses(200, 404, 409, 429));

export default function () {
  const ip = getRandomIp();

  let res;

  if (Math.random() < 0.5) {
    res = http.get(`http://localhost:8888/api/${getRandomId()}`, {
      headers: {
        "X-Forwarded-For": ip,
      },
    });
  } else {
    const body = getBody();

    res = http.post("http://localhost:8888/api", JSON.stringify(body), {
      headers: {
        "Content-Type": "application/json",
        "X-Forwarded-For": ip,
      },
    });
  }

  if (!(res.status === 200 || res.status === 404 || res.status === 429)) {
    fail(`Unexpected status code: ${res.status}`);
  }

  sleep(0.1);
}

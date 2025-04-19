import http from "k6/http";
import { fail, sleep } from "k6";

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
    customShortenedUrl
  };
}

export let options = {
  vus: 50,
  duration: "1s",
};

http.setResponseCallback(http.expectedStatuses(200, 404, 409, 429));

export default function () {
  const ip = getRandomIp();

  const url = "http://localhost:8888/api";

  const body = getBody();

  let res = http.post(url, JSON.stringify(body), {
    headers: {
      "Content-Type": "application/json",
      "X-Forwarded-For": ip,
    },
  });

  if (!(res.status === 200 || res.status === 404 || res.status === 429)) {
    fail(`Unexpected status code: ${res.status}`);
  }

  sleep(0.1);
}

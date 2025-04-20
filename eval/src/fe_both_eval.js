import http from "k6/http";
import { fail, sleep } from "k6";

// Sample shortened url ids for testing
// Please replace with your own ids if needed
const sampleValidIds = [
  "vPGncM3gb4P",
  "XusHL",
  "custom-8zg5nz68yr",
  "custom-78fj6f4ez7b",
  "aBe6DsxhCTVc5",
  "WXGfN9t",
  "custom-3owyea5ful6",
  "custom-8pmp3zb5skp",
  "custom-kitxmnpb9c",
  "ziksY4jwz9d",
  "custom-atcipn01dcd",
  "custom-53bcxr4lwu8",
  "custom-sw9f382spf",
  "kykOB0",
  "custom-wfrb3ruyceb",
  "custom-lqt859jaw3q",
  "custom-kfun1kl2s5n",
  "ISlxfFU",
  "custom-d74k1d96g0u",
  "custom-93y2bq183ws",
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
  vus: 50,
  duration: "60s",
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

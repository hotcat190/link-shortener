import http from "k6/http";
import { fail, sleep } from "k6";

// Sample shortened url ids for testing
// Please replace with your own ids if needed
const sampleValidIds = [
  "custom-vokvq9ijks",
  "6a491c03-4ae5-49c2-b6ca-93c5731eda40",
  "ab7d795d-bfbe-4d27-9e4b-f3a691ad5007",
  "81fd9ab3-7b1b-4b9a-96be-5fd3589b1c1e",
  "88f911d8-8e54-4c6b-8cc3-d70aff56fa8e",
  "cdc1ebaa-e521-4e5f-adab-a2b7b7f6bb4d",
  "2b49729c-918f-4346-96c9-82ee716e794e",
  "custom-5m2wk1pnwze",
  "custom-xczqvwatap",
  "4a1a25f0-4c4e-47be-b020-626afe5878ee",
  "d73d8bc7-0a0d-4dfd-93c5-5d09f6ac4695",
  "custom-lgmzfszbgu",
  "custom-tbnlhb19iyq",
  "2a9e9d71-6129-47df-87b7-2f6024f411d4",
  "2f765798-e0ca-41f3-9203-86a9b1040d2c",
  "5de81686-ed58-48a9-92dc-a1ee1faa4c91",
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

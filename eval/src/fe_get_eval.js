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

// 200 random ip address
function getRandomIp() {
  return `192.168.1.${Math.floor(Math.random() * 200)}`;
}

function getRandomId() {
  if (Math.random() < 0.5) {
    const randomIndex = Math.floor(Math.random() * sampleValidIds.length);
    return sampleValidIds[randomIndex];
  } else {
    return Math.random().toString(36).substring(24);
  }
}

export let options = {
  vus: parseInt(__ENV.VUS || "50"),
  duration: __ENV.DURATION || "60s",
};

http.setResponseCallback(http.expectedStatuses(200, 404, 409, 429));

export default function () {
  const ip = getRandomIp();

  let res = http.get(`http://localhost:8888/api/${getRandomId()}`, {
    headers: {
      "X-Forwarded-For": ip,
    },
  });

  if (!(res.status === 200 || res.status === 404 || res.status === 429)) {
    fail(`Unexpected status code: ${res.status}`);
  }

  sleep(0.1);
}

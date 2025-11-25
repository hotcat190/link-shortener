import http from "k6/http";
import { fail, sleep } from "k6";

// Sample shortened url ids for testing
// Please replace with your own ids if needed
import sampleValidIds from "./constants";

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

  let res = http.get(`http://localhost:80/api/${getRandomId()}`, {
    headers: {
      "X-Forwarded-For": ip,
    },
  });

  if (!(res.status === 200 || res.status === 404 || res.status === 409 || res.status === 429)) {
    fail(`Unexpected status code: ${res.status}`);
  }

  sleep(0.1);
}

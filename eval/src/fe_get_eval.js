import http from "k6/http";
import { fail } from "k6";

// Sample shortened url ids for testing
// Please replace with your own ids if needed
const sampleValidIds = [
  "custom-ill1yimx9f",
  "6d993398-5cc3-4c6f-bbb6-5d51ceaaeaab",
  "409da585-ef17-494c-94d6-108eaff31076",
  "8266dd82-fba2-4f5c-9874-ebfd92d49f62",
  "custom-cqdn0utc9bs",
  "custom-sx0cc29z6wk",
  "ec42cc4c-944d-464d-a3f0-705c8dccb3d6",
  "custom-vausyonb6ad",
  "custom-ggkcud1z0rk",
  "ef5fa22c-0b73-45c0-8e8e-14b36f59fd27",
  "custom-5rsduqcfs67",
  "custom-7j36kpbgi2g",
  "1a472e53-d00c-4314-a333-820a889d3965",
  "9ee23efa-a510-44d5-b41a-9c3dbcca88fb",
];

// 500 random ip address
function getRandomIp() {
  if (Math.random() < 0.5) {
    return `192.168.1.${Math.floor(Math.random() * 255)}`;
  } else {
    return `10.10.1.${Math.floor(Math.random() * 255)}`;
  }
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
  vus: 50,
  duration: "60s",
};

http.setResponseCallback(http.expectedStatuses(200, 404, 409, 429));

export default function () {
  const ip = getRandomIp();

  let res = http.get(`http://localhost:8080/short/${getRandomId()}`, {
    headers: {
      "X-Forwarded-For": ip,
    },
  });

  if (!(res.status === 200 || res.status === 404 || res.status === 429)) {
    fail(`Unexpected status code: ${res.status}`);
  }
}

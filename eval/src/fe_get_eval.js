import http from "k6/http";
import { fail } from "k6";

// Sample shortened url ids for testing
// Please replace with your own ids if needed
const sampleValidIds = [
  "0fc7a50e-711c-4b1a-99c9-6c4371fe5a76",
  "023b10e4-3438-4f81-9b21-609edac8f94b",
  "99798d8b-d8f9-4f33-8d3b-60c632d8dfb0",
  "my-short-url-278fnq",
  "8f668174-9c88-49da-88f4-ea4a6827b199",
  "5a010e06-c5bb-486f-88e2-1d58079004f6",
  "8b0a4956-ab0c-43e7-a132-8f63af796007",
  "my-short-url-qwxett",
  "2210af47-e418-491b-b11d-e5df02b7a392",
  "d2120f3d-ecd3-4c4e-a385-3eb097aa63a9",
  "my-short-url-4xr8s0",
  "dd09a1bb-d033-49dd-b220-05940a571034",
  "my-short-url-m4jk8b",
  "65a1b20f-092c-4fe5-9375-9492ef3717ef",
  "7093f72f-e78c-49a3-8112-be27a27e24e4",
  "my-short-url-a4tx9j",
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
  duration: "5s",
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

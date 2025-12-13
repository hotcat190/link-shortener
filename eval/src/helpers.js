export function getRandomId() {
  if (Math.random() < 0.5) {
    const randomIndex = Math.floor(Math.random() * sampleValidIds.length);
    return sampleValidIds[randomIndex];
  } else {
    return Math.random().toString(36).substring(24);
  }
}


export function getRandomIp() {
  return `192.168.1.${Math.floor(Math.random() * 200)}`;
}

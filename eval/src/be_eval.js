const Docker = require("dockerode");

// Replace these with your actual values
const DOCKER_HOST = "localhost";
const DOCKER_PORT = 2375; // Default TCP port for Docker API (no TLS)

const APP_CONTAINER_NAME = "backend-with-rate-limiting-app-1";
const DB_CONTAINER_NAME = "backend-with-rate-limiting-db-1";

const docker = new Docker({
  host: DOCKER_HOST,
  port: DOCKER_PORT,
});

const appContainer = docker.getContainer(APP_CONTAINER_NAME);
const dbContainer = docker.getContainer(DB_CONTAINER_NAME);

function getCpuUsageInPercent(stats) {
  const cpuDelta =
    stats.cpu_stats.cpu_usage.total_usage -
    stats.precpu_stats.cpu_usage.total_usage;
  const systemDelta =
    stats.cpu_stats.system_cpu_usage - stats.precpu_stats.system_cpu_usage;
  const cpuCount = stats.cpu_stats.online_cpus;

  let cpuPercent = 0;
  if (systemDelta > 0 && cpuDelta > 0) {
    cpuPercent = (cpuDelta / systemDelta) * cpuCount * 100;
  }
  return cpuPercent;
}

function getMemoryUsageInMB(stats) {
  return stats.memory_stats.usage / (1024 * 1024);
}

async function getContainerStats(container) {
  try {
    const stats = await container.stats({ stream: false });

    return {
      cpu: getCpuUsageInPercent(stats),
      memory: getMemoryUsageInMB(stats),
    };
  } catch (err) {
    return undefined;
  }
}

async function eval(totalRunTime) {
  const startTime = Date.now();

  let totalCpuUsage = 0;
  let totalMemoryUsage = 0;
  let totalRequests = 0;

  while (Date.now() - startTime < totalRunTime) {
    const appStats = await getContainerStats(appContainer);
    const dbStats = await getContainerStats(dbContainer);

    totalCpuUsage += appStats.cpu + dbStats.cpu;
    totalMemoryUsage += appStats.memory + dbStats.memory;
    totalRequests += 1;
  }

  const averageCpuUsage = totalCpuUsage / totalRequests;
  const averageMemoryUsage = totalMemoryUsage / totalRequests;

  return {
    averageCpuUsage,
    averageMemoryUsage,
  };
}

eval(60000).then((result) => {
  console.log("----------------" + "Backend Evaluation" + "-----------------");
  console.log("Average CPU Usage   :", result.averageCpuUsage);
  console.log("Average Memory Usage:", result.averageMemoryUsage);
});

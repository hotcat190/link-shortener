const Docker = require("dockerode");

const DOCKER_HOST = "localhost";
const DOCKER_PORT = 2375;

const CONTAINER_NAMES = ["app", "mysql", "redis", "nginx"];

const docker = new Docker({
  // socketPath: "/var/run/docker.sock",
  socketPath: "/home/lml/.docker/desktop/docker.sock",
});

const containers = CONTAINER_NAMES.map((name) => docker.getContainer(name));

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
    const containerStats = await Promise.all(
      containers.map((container) => getContainerStats(container))
    );

    const validStats = containerStats.filter((stats) => stats !== undefined);

    totalCpuUsage += validStats.reduce((sum, stats) => sum + stats.cpu, 0);
    totalMemoryUsage += validStats.reduce(
      (sum, stats) => sum + stats.memory,
      0
    );
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

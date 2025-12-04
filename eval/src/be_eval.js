const Docker = require("dockerode");
const fs = require("fs");
const path = require("path");

const script_label = process.argv[2] || "N/A";
const vus = process.argv[3] || "N/A";

// Ver 3: Microservices + DB Sharding
// Core services (included in Total calculation)
const CORE_CONTAINER_NAMES = [
  "creation-service",   // API tạo link
  "redirect-service",   // API redirect
  "link-consumer",      // Worker async
  "mysql_shard_1",      // Database Shard 1
  "mysql_shard_2",      // Database Shard 2
];

// Infrastructure services (tracked separately)
const INFRA_CONTAINER_NAMES = [
  "redis",              // Cache
  "nginx",              // Gateway
  "rabbitmq",           // Message Queue
];

// All containers to monitor
const CONTAINER_NAMES = [...CORE_CONTAINER_NAMES, ...INFRA_CONTAINER_NAMES];

// SỬA: Tự động phát hiện Windows hay Linux để chọn socket đúng
const isWindows = process.platform === 'win32';
const dockerOptions = isWindows 
    ? { socketPath: '//./pipe/docker_engine' } 
    : { socketPath: '/var/run/docker.sock' };

// --- KHỞI TẠO BIẾN DOCKER Ở ĐÂY (Đã sửa) ---
const docker = new Docker(dockerOptions);

const containers = CONTAINER_NAMES.map((name) => docker.getContainer(name));

function getCpuUsageInPercent(stats) {
  // Check nếu stats bị lỗi hoặc chưa có dữ liệu
  if (!stats || !stats.cpu_stats || !stats.precpu_stats) return 0;

  const cpuDelta =
    stats.cpu_stats.cpu_usage.total_usage -
    stats.precpu_stats.cpu_usage.total_usage;
  const systemDelta =
    stats.cpu_stats.system_cpu_usage - stats.precpu_stats.system_cpu_usage;
  const cpuCount = stats.cpu_stats.online_cpus || 1; // Fallback to 1 if undefined

  let cpuPercent = 0;
  if (systemDelta > 0 && cpuDelta > 0) {
    cpuPercent = (cpuDelta / systemDelta) * cpuCount * 100;
  }
  return cpuPercent;
}

function getMemoryUsageInMB(stats) {
  if (!stats || !stats.memory_stats || stats.memory_stats.usage === undefined) {
    return 0;
  }
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
    // console.log(`Error getting stats for container: ${err.message}`);
    // Return 0 để không làm crash chương trình khi 1 container chưa kịp bật
    return { cpu: 0, memory: 0 };
  }
}

async function eval(totalRunTime) {
  const startTime = Date.now();

  // Tracking for all containers
  let totalCpuUsage = 0;
  let totalMemoryUsage = 0;
  
  // Tracking for CORE containers only (for comparison with Monolith)
  let coreCpuUsage = 0;
  let coreMemoryUsage = 0;
  
  // Per-container tracking
  const perContainerStats = {};
  CONTAINER_NAMES.forEach(name => {
    perContainerStats[name] = { cpu: 0, memory: 0 };
  });
  
  let totalRequests = 0;

  while (Date.now() - startTime < totalRunTime) {
    const containerStats = await Promise.all(
      containers.map((container, index) => 
        getContainerStats(container).then(stats => ({
          name: CONTAINER_NAMES[index],
          stats
        }))
      )
    );

    containerStats.forEach(({ name, stats }) => {
      if (stats) {
        totalCpuUsage += stats.cpu;
        totalMemoryUsage += stats.memory;
        
        // Track per-container
        if (perContainerStats[name]) {
             perContainerStats[name].cpu += stats.cpu;
             perContainerStats[name].memory += stats.memory;
        }
        
        // Track CORE containers separately
        if (CORE_CONTAINER_NAMES.includes(name)) {
          coreCpuUsage += stats.cpu;
          coreMemoryUsage += stats.memory;
        }
      }
    });

    totalRequests += 1;
    // Nghỉ 1s trước khi đo lần tiếp theo để tránh spam CPU
    await new Promise(resolve => setTimeout(resolve, 1000));
  }

  // Calculate averages
  // Tránh chia cho 0
  const divisor = totalRequests > 0 ? totalRequests : 1;

  const averageCpuUsage = totalCpuUsage / divisor;
  const averageMemoryUsage = totalMemoryUsage / divisor;
  const averageCoreCpuUsage = coreCpuUsage / divisor;
  const averageCoreMemoryUsage = coreMemoryUsage / divisor;
  
  // Per-container averages
  const perContainerAvg = {};
  CONTAINER_NAMES.forEach(name => {
    perContainerAvg[name] = {
      cpu: perContainerStats[name].cpu / divisor,
      memory: perContainerStats[name].memory / divisor
    };
  });

  return {
    averageCpuUsage,
    averageMemoryUsage,
    averageCoreCpuUsage,
    averageCoreMemoryUsage,
    perContainerAvg,
  };
}

eval(60000).then((result) => {
  const logFilePath = path.join(
    __dirname,
    "..",
    "results",
    "logs",
    "be.log"
  );
  
  // Đảm bảo thư mục tồn tại trước khi ghi file
  const dir = path.dirname(logFilePath);
  if (!fs.existsSync(dir)){
      fs.mkdirSync(dir, { recursive: true });
  }

  const header = `================ Backend Evaluation (Ver 3 - Microservices + Sharding) ================`;
  const subHeader = `Script: ${script_label}, VUs: ${vus}`;
  const separator = "--------------------------------------------------------------------------------";

  const logLines = [
    header,
    subHeader,
    separator,
    "",
    "=== TOTAL (All Containers) ===",
    `  CPU Usage   : ${result.averageCpuUsage.toFixed(2)} %`,
    `  Memory Usage: ${result.averageMemoryUsage.toFixed(2)} MB`,
    "",
    "=== CORE ONLY (For Monolith Comparison) ===",
    `  Containers  : ${CORE_CONTAINER_NAMES.join(", ")}`,
    `  CPU Usage   : ${result.averageCoreCpuUsage.toFixed(2)} %`,
    `  Memory Usage: ${result.averageCoreMemoryUsage.toFixed(2)} MB`,
    "",
    "=== PER-CONTAINER BREAKDOWN ===",
  ];

  // Add per-container stats
  CONTAINER_NAMES.forEach(name => {
    const stats = result.perContainerAvg[name];
    const isCore = CORE_CONTAINER_NAMES.includes(name) ? "[CORE]" : "[INFRA]";
    logLines.push(`  ${name.padEnd(20)} ${isCore} CPU: ${stats.cpu.toFixed(2).padStart(8)} %  |  RAM: ${stats.memory.toFixed(2).padStart(10)} MB`);
  });

  logLines.push("");
  logLines.push(separator);
  logLines.push("");

  const logData = logLines.join("\n");

  // Also print to console for immediate feedback
  console.log(logData);

  fs.appendFile(logFilePath, logData + "\n", (err) => {
    if (err) {
      console.error("Error writing to log file:", err);
    }
  });
});
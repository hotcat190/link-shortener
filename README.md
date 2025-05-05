## Giới thiệu - Nhóm 4

Dự án được thực hiện bởi hai thành viên:

- Triệu Minh Nhật - 22021214
- Lương Mạnh Linh - 22021215

## Chủ đề

**CASE STUDY 1** - Tối ưu hóa dự án rút gọn link.  
Mục tiêu của dự án là rút gọn một đường link dài thành một ID ngắn gọn, dễ sử dụng.

## Ngôn ngữ và Framework sử dụng

- **Backend**: Java Spring Boot
- **Frontend**: React TypeScript
- **Đánh giá hiệu năng**: K6, Dockerorde (JavaScript)

## Các điểm nổi bật

- Tối ưu hóa việc sinh UUID cho các link được rút gọn.
- Sử dụng Hibernate (JPA) để thực hiện ORM (Object-Relational Mapping).
- Tích hợp Redis để cache dữ liệu cho các thao tác đọc, giúp tăng tốc độ truy xuất.
- Sử dụng Nginx làm gateway và triển khai cơ chế rate limiting để kiểm soát lưu lượng truy cập.
- **Tính năng bổ sung nổi bật**:
  - Người dùng có thể tùy chỉnh link rút gọn thay vì để hệ thống tự sinh ngẫu nhiên.
  - Hỗ trợ thiết lập thời gian tồn tại (time-to-live) cho mỗi link.
  - Theo dõi số lần nhấp chuột (click count) cho từng link rút gọn.

## Cách cài đặt và bản demo

### Yêu cầu

Để chạy dự án này, cần chuẩn bị sẵn các công cụ sau:

- **Git**
- **Node.js**
- **Docker và Docker Compose**

### Chạy local

Làm theo các bước sau để chạy dự án `với đầy đủ chức năng` trên máy local:

1. Clone repository:

   ```bash
   git clone https://github.com/TrieuMinhNhat/link-shortener.git
   ```

2. Chạy Docker Compose để khởi động backend và database:

   ```bash
   cd backend
   docker compose up --build
   ```

3. Chạy frontend:

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

4. Truy cập trình duyệt tại địa chỉ:
   `http://localhost:5173`
   để sử dụng ứng dụng.

### Bản demo

Nhóm đã triển khai bản demo online, hãy truy cập tại địa chỉ:

👉 [http://52.77.235.14/](http://52.77.235.14/)

để trải nghiệm trực tiếp.

`Lưu ý:` Bản demo đã lược bỏ Rate Limiting và Cache nhằm dễ dàng triển khai trên cloud. Bản demo sử dụng mã nguồn tại branch `cloud-separated-deploy`


# Đo hiệu năng
- Công cụ: K6, Dockerode
- Thời gian đo: 60 s
- Delay giữa các request với mỗi người dùng: 100 ms
- Rate Limiting Config:
    - Capacity: 10
    - Refill rate: 1/s 
## 50 người dùng
### CREATE-ONLY
| Metric            | No Pattern | Rate Limiting Only | Rate Limiting + Cache |
|-------------------|------------|---------------------|------------------------|
| LATENCY (ms)      | 16.53      | 6.13                | 6.97                   |
| RPS               | 426.38     | 467.85              | 464.19                 |
| CPU USAGE (%)     | 176.44     | 134.58              | 156.65                 |
| RAM USAGE (MB)    | 1199.06    | 1022.85             | 1010.9                 |

### GET-ONLY
| Metric         | No Pattern | Rate Limiting Only | Rate Limiting + Cache |
| -------------- | ---------- | ------------------ | --------------------- |
| LATENCY (ms)   | 7.13       | 3.28               | 1.93                  |
| RPS            | 463.34     | 480.89             | 487.45                |
| CPU USAGE (%)  | 193.03     | 96.72              | 89.58                 |
| RAM USAGE (MB) | 1387.68    | 1385.08            | 1175.96               |


## MIX 50% CREATE - 50% GET
| Metric         | No Pattern | Rate Limiting Only | Rate Limiting + Cache |
| -------------- | ---------- | ------------------ | --------------------- |
| LATENCY (ms)   | 7.47       | 3.43               | 3.54                  |
| RPS            | 463.18     | 480.15             | 479.54                |
| CPU USAGE (%)  | 160.42     | 69.91              | 109.56                |
| RAM USAGE (MB) | 1562.45    | 1428.7             | 1254.56               |


## 500 người dùng
### CREATE-ONLY
| Metric         | No Pattern | Rate Limiting Only | Rate Limiting + Cache |
| -------------- | ---------- | ------------------ | --------------------- |
| LATENCY (ms)   | 562.05     | 189.04             | 172.61                |
| RPS            | 751.96     | 1724.57            | 1822.44               |
| CPU USAGE (%)  | 336.5      | 169.06             | 195.85                |
| RAM USAGE (MB) | 1329.87    | 1145.9             | 1273.32               |

### GET-ONLY
| Metric         | No Pattern | Rate Limiting Only | Rate Limiting + Cache |
| -------------- | ---------- | ------------------ | --------------------- |
| LATENCY (ms)   | 392.05     | 200.13             | 154.25                |
| RPS            | 1011.93    | 1657.65            | 1961.7                |
| CPU USAGE (%)  | 370.89     | 181.04             | 69.34                 |
| RAM USAGE (MB) | 1565.59    | 1450.55            | 1579.77               |

## MIX 50% CREATE - 50% GET
| Metric         | No Pattern | Rate Limiting Only | Rate Limiting + Cache |
| -------------- | ---------- | ------------------ | --------------------- |
| LATENCY (ms)   | 445.6      | 184.96             | 144.36                |
| RPS            | 912.23     | 1746.55            | 1977.12               |
| CPU USAGE (%)  | 295.65     | 87.09              | 75.2                  |
| RAM USAGE (MB) | 1637.92    | 1585.69            | 1640.57               |

## Kết luận
Việc áp dụng Rate Limiting và Cache giúp cải thiện rõ rệt hiệu năng hệ thống. Chỉ riêng Rate Limiting đã mang lại sự cải thiện đáng kể về tốc độ xử lý và độ ổn định, đồng thời giảm tải tài nguyên. Khi kết hợp thêm Cache, hệ thống tiếp tục được tối ưu, đặc biệt trong các tình huống tải cao. Dù Cache khiến CPU tiêu thụ nhiều hơn và đôi lúc tăng RAM, nhưng đổi lại mang lại khả năng phản hồi nhanh và phục vụ người dùng hiệu quả hơn. Tùy nhu cầu, chỉ dùng Rate Limiting cũng đã đủ hiệu quả.


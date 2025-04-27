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

`Lưu ý:` Bản demo đã lược bỏ Rate Limiting và Cache nhằm dễ dàng triển khai trên cloud. Bản demo sử dụng mã nguồn tại branche `cloud-separated-deploy`

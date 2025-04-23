# README.md

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

# Shop Management (TX1)

Ứng dụng Spring Boot 4 / Java 17 quản lý danh mục và sản phẩm bằng Thymeleaf, MySQL và Spring Data JPA. Sản phẩm có quan hệ `@ManyToOne` tới danh mục. Có CRUD, validate, tìm theo tên, lọc danh mục và phân trang.

## Chạy cục bộ

1. Cài Java 17, Maven và MySQL 8. Tạo database `shop_management` hoặc để JDBC tự tạo khi tài khoản có quyền.
2. Nếu MySQL khác cấu hình mặc định, đặt `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`.
3. Trong thư mục `demo`, chạy `mvn clean package` và `mvn spring-boot:run`.
4. Mở `http://localhost:8080` (sản phẩm) hoặc `/categories` (danh mục).

## Docker Compose

Trong `demo`, chạy:

```bash
mvn clean package
docker compose up --build -d
docker compose ps
docker compose down
```

Compose cung cấp MySQL tại cổng 3306 và ứng dụng tại cổng 8080. Dữ liệu MySQL được giữ trong volume `mysql_data`.
Nếu các cổng này đã được dùng, đặt `MYSQL_HOST_PORT` và `SHOP_HOST_PORT` trước khi chạy Compose (ví dụ 3307 và 8081).

## API để kiểm thử bằng Postman

| Phương thức | Đường dẫn | Chức năng |
| --- | --- | --- |
| GET | `/api/categories` | Danh sách danh mục |
| GET | `/api/categories/{id}` | Chi tiết danh mục |
| POST | `/api/categories` | Tạo danh mục |
| PUT | `/api/categories/{id}` | Sửa danh mục |
| DELETE | `/api/categories/{id}` | Xóa danh mục |
| GET | `/api/products?name=laptop&categoryId=1&page=0&size=10` | Tìm, lọc, phân trang |
| GET | `/api/products/{id}` | Chi tiết sản phẩm |
| POST | `/api/products` | Tạo sản phẩm |
| PUT | `/api/products/{id}` | Sửa sản phẩm |
| DELETE | `/api/products/{id}` | Xóa sản phẩm |

Gửi JSON với `Content-Type: application/json`. Ví dụ tạo danh mục: `{"name":"Điện tử","description":"Thiết bị điện tử"}`. Ví dụ tạo sản phẩm: `{"name":"Laptop","price":1200,"stock":5,"categoryId":1}`. Xóa danh mục còn sản phẩm trả `409 Conflict`.
Import `postman/ShopManagement.postman_collection.json` vào Postman, chỉnh biến `baseUrl` rồi chạy Collection Runner theo thứ tự để kiểm tra CRUD, validate và metrics.

Nếu `POST /api/products` trả 400, chọn **Body → raw → JSON** và gửi đủ `name`, `price` (> 0), `stock` (>= 0), `categoryId` (ID danh mục đã tồn tại). Trong collection, chạy request **01 Create category** trước **02 Create product** để biến `categoryId` được gán. `GET /api/products` không cần body.

## Minikube

Máy chạy cần có Docker, Minikube và kubectl. Từ thư mục `demo`:

```bash
minikube start
mvn clean package
docker build -t shop-management-app:latest .
minikube image load shop-management-app:latest
kubectl apply -f k8s/mysql-deployment.yml
kubectl rollout status deployment/mysql
kubectl apply -f k8s/app-deployment.yml
kubectl rollout status deployment/shop-app
minikube service shop-app-service
```

Manifest dùng 2 replica ứng dụng, MySQL có persistent volume, NodePort 30080 và probe sức khỏe. Mật khẩu trong manifest là giá trị demo; hãy thay bằng Secret khi triển khai ngoài môi trường học tập.

## GitLab CI/CD

`.gitlab-ci.yml` chạy build → test → package → Docker image → triển khai khi push nhánh `main`. Cần GitLab **shell runner** gắn tag `minikube`, có Maven, Java 17, Docker, Minikube, kubectl và quyền truy cập cluster. Job deploy tự build lại image từ JAR artifact để hoạt động kể cả khi job trước chạy trên runner khác.

## Prometheus và Grafana

Ứng dụng xuất metrics tại `/actuator/prometheus`. Cài stack giám sát:

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
kubectl create namespace monitoring
helm install monitoring prometheus-community/kube-prometheus-stack -n monitoring
kubectl apply -f k8s/service-monitor.yml
kubectl port-forward svc/monitoring-grafana 3000:80 -n monitoring
```

Truy cập Grafana tại `http://localhost:3000`, lấy mật khẩu admin bằng `kubectl get secret monitoring-grafana -n monitoring -o jsonpath="{.data.admin-password}"` rồi giải mã base64. Có thể import dashboard Spring Boot Micrometer ID `11378` hoặc `12900`. `ServiceMonitor` thu thập JVM, CPU, memory, HTTP và HikariCP metrics từ cổng ứng dụng.

## Kiểm thử tự động

`mvn test` dùng H2 ở chế độ MySQL, kiểm tra CRUD, tìm kiếm/lọc, validate, trang Thymeleaf và endpoint Prometheus; không cần MySQL cục bộ.

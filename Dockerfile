# Sử dụng một base image có chứa Java 21
FROM openjdk:21-jdk-slim

# Thiết lập thư mục làm việc bên trong container
WORKDIR /app

# Copy file pom.xml và tải các dependency để tận dụng caching
COPY pom.xml .
COPY .mvn/ .mvn
COPY mvnw .
RUN ./mvnw dependency:go-offline

# Copy toàn bộ mã nguồn còn lại
COPY src ./src

# Build ứng dụng, bỏ qua các bài test để build nhanh hơn
RUN ./mvnw package -DskipTests

# Mở cổng 8080 mà Spring Boot thường chạy
EXPOSE 8080

# Lệnh để chạy ứng dụng khi container khởi động
ENTRYPOINT ["java", "-jar", "target/trungtambaohanh-0.0.1-SNAPSHOT.jar"]

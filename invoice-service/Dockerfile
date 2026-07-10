FROM eclipse-temurin:17.0.17_10-jre-ubi9-minimal
ENV TZ=Asia/Ho_Chi_Minh
WORKDIR /app
COPY target/invoice-service-1.0.0.jar /app/service.jar
ENTRYPOINT ["java", "-jar", "/app/service.jar"]

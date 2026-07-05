FROM eclipse-temurin:17.0.17_10-jre-ubi9-minimal
ENV TZ='Asia/Ho_Chi_Minh'
WORKDIR /app
COPY target/authn-service-1.0.0.jar ./authentication.jar
CMD ["java", "-jar", "-Dspring.profiles.active=dev", "/app/authentication.jar"]
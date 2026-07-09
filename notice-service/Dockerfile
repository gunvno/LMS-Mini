FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY base-service/pom.xml base-service/pom.xml
COPY notice-service/pom.xml notice-service/pom.xml

RUN cd base-service && mvn -q -DskipTests dependency:go-offline

COPY base-service/src base-service/src
RUN cd base-service && mvn -q -DskipTests install

RUN cd notice-service && mvn -q -DskipTests dependency:go-offline

COPY notice-service/src notice-service/src

RUN cd notice-service && mvn -q -DskipTests package

FROM eclipse-temurin:17.0.17_10-jre-ubi9-minimal
ENV TZ=Asia/Ho_Chi_Minh
WORKDIR /app
COPY --from=build /workspace/notice-service/target/notice-service-1.0.0.jar /app/service.jar
ENTRYPOINT ["java", "-jar", "/app/service.jar"]

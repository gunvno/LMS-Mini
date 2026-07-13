FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY base-service/pom.xml base-service/pom.xml
COPY chat-service/pom.xml chat-service/pom.xml

RUN --mount=type=cache,target=/root/.m2 \
    cd base-service && mvn -q -DskipTests dependency:go-offline

COPY base-service/src base-service/src
RUN --mount=type=cache,target=/root/.m2 \
    cd base-service && mvn -q -DskipTests install

RUN --mount=type=cache,target=/root/.m2 \
    cd chat-service && mvn -q -DskipTests dependency:go-offline

COPY chat-service/src chat-service/src
RUN --mount=type=cache,target=/root/.m2 \
    cd chat-service && mvn -q -DskipTests package

FROM eclipse-temurin:17.0.17_10-jre-ubi9-minimal
ENV TZ=Asia/Ho_Chi_Minh
WORKDIR /app
COPY --from=build /workspace/chat-service/target/chat-service-1.0.0.jar /app/service.jar
ENTRYPOINT ["java", "-jar", "/app/service.jar"]

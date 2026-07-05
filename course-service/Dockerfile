FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY base-service/pom.xml base-service/pom.xml
COPY course-service/pom.xml course-service/pom.xml

RUN --mount=type=cache,target=/root/.m2 \
    cd base-service && mvn -q -DskipTests dependency:go-offline
RUN --mount=type=cache,target=/root/.m2 \
    cd course-service && mvn -q -DskipTests dependency:go-offline

COPY base-service/src base-service/src
COPY course-service/src course-service/src

RUN --mount=type=cache,target=/root/.m2 \
    cd base-service && mvn -q -DskipTests install
RUN --mount=type=cache,target=/root/.m2 \
    cd course-service && mvn -q -DskipTests package

FROM eclipse-temurin:17.0.17_10-jre-ubi9-minimal
ENV TZ=Asia/Ho_Chi_Minh
WORKDIR /app
COPY --from=build /workspace/course-service/target/course-service-1.0.0.jar /app/course-service.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/course-service.jar"]

FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY base-service/pom.xml base-service/pom.xml
COPY course-service/pom.xml course-service/pom.xml

RUN cd base-service && mvn -q -DskipTests dependency:go-offline

COPY base-service/src base-service/src
RUN cd base-service && mvn -q -DskipTests install

RUN cd course-service && mvn -q -DskipTests dependency:go-offline

COPY course-service/src course-service/src

RUN cd course-service && mvn -q -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /workspace/course-service/target/course-service-1.0.0.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]
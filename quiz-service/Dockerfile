FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY base-service/pom.xml base-service/pom.xml
COPY quiz-service/pom.xml quiz-service/pom.xml

RUN cd base-service && mvn -q -DskipTests dependency:go-offline

COPY base-service/src base-service/src
RUN cd base-service && mvn -q -DskipTests install

RUN cd quiz-service && mvn -q -DskipTests dependency:go-offline

COPY quiz-service/src quiz-service/src

RUN cd quiz-service && mvn -q -DskipTests package

FROM eclipse-temurin:17.0.17_10-jre-ubi9-minimal
ENV TZ=Asia/Ho_Chi_Minh
WORKDIR /app
COPY --from=build /workspace/quiz-service/target/quiz-service-1.0.0.jar /app/service.jar
ENTRYPOINT ["java", "-jar", "/app/service.jar"]

FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY base-service/pom.xml base-service/pom.xml
COPY author-service/pom.xml author-service/pom.xml

RUN cd base-service && mvn -q -DskipTests dependency:go-offline

COPY base-service/src base-service/src

RUN cd base-service && mvn -q -DskipTests install

RUN cd author-service && mvn -q -DskipTests dependency:go-offline

COPY author-service/src author-service/src

RUN cd author-service && mvn -q -DskipTests package

FROM eclipse-temurin:17-jre

ENV TZ=Asia/Ho_Chi_Minh
WORKDIR /app

COPY --from=build /workspace/author-service/target/author-service-1.0.0.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]
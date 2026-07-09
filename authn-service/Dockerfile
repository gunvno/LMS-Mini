FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY base-service/pom.xml base-service/pom.xml
COPY authn-service/pom.xml authn-service/pom.xml

RUN cd base-service && mvn -q -DskipTests dependency:go-offline

COPY base-service/src base-service/src
RUN cd base-service && mvn -q -DskipTests install

RUN cd authn-service && mvn -q -DskipTests dependency:go-offline

COPY authn-service/src authn-service/src

RUN cd authn-service && mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
ENV TZ=Asia/Ho_Chi_Minh
WORKDIR /app

COPY --from=build /workspace/authn-service/target/authn-service-1.0.0.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
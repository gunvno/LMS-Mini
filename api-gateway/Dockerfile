FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY api-gateway/pom.xml api-gateway/pom.xml

RUN cd api-gateway && mvn -q -DskipTests dependency:go-offline

COPY api-gateway/src api-gateway/src

RUN cd api-gateway && mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
ENV TZ=Asia/Ho_Chi_Minh
WORKDIR /app

COPY --from=build /workspace/api-gateway/target/api-gateway-1.0.0.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
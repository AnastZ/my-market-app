
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY payment_api/pom.xml ./payment_api/
COPY market_service/pom.xml ./market_service/
COPY payment_service/pom.xml ./payment_service/

RUN apk add --no-cache maven curl

RUN mvn dependency:go-offline -B

COPY payment_api/src ./payment_api/src
COPY market_service/src ./market_service/src
COPY payment_service/src ./payment_service/src

RUN mvn generate-sources -pl payment_api
RUN mvn clean package -pl market_service -am -DskipTests

FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl

WORKDIR /app

COPY --from=builder /app/market_service/target/market_service-*.jar market.jar

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

EXPOSE ${MARKET_PORT:-8084}

ENTRYPOINT ["java", "-jar", "market.jar"]
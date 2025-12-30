FROM eclipse-temurin:17.0.17_10-jdk-alpine-3.23 AS builder

WORKDIR /build

RUN apk add --no-cache maven

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17.0.17_10-jre-alpine-3.23

WORKDIR /app

ENV TZ=Asia/Shanghai
RUN apk add --no-cache tzdata curl \
    && ln -snf /usr/share/zoneinfo/$TZ /etc/localtime \
    && echo $TZ > /etc/timezone

COPY --from=builder /build/target/metrics_mall-*.jar app.jar

RUN addgroup -S www && adduser -S www -G www \
    && mkdir -p /var/log/metrics_mall \
    && chown -R www:www /var/log/metrics_mall

USER www:www

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENV JAVA_OPTS="-Xms128m -Xmx1024m -XX:MaxMetaspaceSize=128m -XX:MetaspaceSize=64m -XX:MaxDirectMemorySize=16m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Xshare:off"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

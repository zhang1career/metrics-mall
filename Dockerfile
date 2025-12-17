FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

ENV TZ=Asia/Shanghai
RUN apk add --no-cache tzdata curl \
    && ln -snf /usr/share/zoneinfo/$TZ /etc/localtime \
    && echo $TZ > /etc/timezone \

COPY target/metrics_mall-*.jar app.jar

RUN addgroup -S appgroup && adduser -S appuser -G appgroup \
    && mkdir -p /var/log/metrics_mall \
    && chown -R appuser:appgroup /var/log/metrics_mall

USER appuser:appgroup

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget --spider -q http://localhost:8080/actuator/health || exit 1

ENV JAVA_OPTS="-Xms128m -Xmx1024m -XX:MaxMetaspaceSize=128m -XX:MetaspaceSize=64m -XX:MaxDirectMemorySize=16m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Xshare:off"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

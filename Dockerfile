# ----- run stage only (단일 스테이지 예시) -----
FROM eclipse-temurin:17-jre
WORKDIR /app

# (중요) cgroup 리소스 감지 비활성화
ENV JAVA_TOOL_OPTIONS="-XX:-UseContainerSupport"

# (중요) Actuator의 시스템/메트릭 오토컨피그 전부 제외
ENV SPRING_AUTOCONFIGURE_EXCLUDE="org.springframework.boot.actuate.autoconfigure.metrics.SystemMetricsAutoConfiguration,org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration,org.springframework.boot.actuate.autoconfigure.observation.ObservationAutoConfiguration"
# (중요) ProcessorMetrics 바인더 비활성화
ENV MANAGEMENT_METRICS_BINDERS_PROCESSOR_ENABLED="false"

# JAR 복사 (필요에 맞게 수정)
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080
ENV TZ=Asia/Seoul
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java","-jar","/app/app.jar"]

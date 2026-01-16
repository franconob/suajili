# ---- build ----
FROM gradle:8-jdk21 AS build
WORKDIR /repo
COPY . .
# Reduce memory + concurrency (huge impact)
ENV GRADLE_OPTS="-Dorg.gradle.jvmargs=-Xmx1024m -XX:MaxMetaspaceSize=256m -Dfile.encoding=UTF-8 -Dkotlin.daemon.jvm.options=-Xmx512m"
ENV ORG_GRADLE_PROJECT_org.gradle.workers.max=1
ENV ORG_GRADLE_PROJECT_org.gradle.parallel=false
ENV ORG_GRADLE_PROJECT_kotlin.incremental=false

RUN gradle --no-daemon --stacktrace :server:build -x test

# ---- run ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /repo/server/build/libs/*.jar app.jar

ENV PORT=8080
EXPOSE 8080
CMD ["sh", "-c", "java -jar app.jar"]
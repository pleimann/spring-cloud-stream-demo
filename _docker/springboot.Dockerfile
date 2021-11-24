FROM gradle:7-jdk17 AS build
WORKDIR /app

COPY . /app
RUN --mount=type=cache,target=/root/.gradle gradle clean build --exclude-task test --no-daemon --no-watch-fs
RUN java -Djarmode=layertools -jar /app/build/libs/app.jar extract --destination /app/build/extracted


FROM openjdk:17-jdk-bullseye
WORKDIR /app

VOLUME /tmp

EXPOSE 8080

## Add the wait script to the image
ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.9.0/wait /wait
RUN chmod +x /wait

ARG BUILD_LOC=/app/build/extracted
COPY --from=build ${BUILD_LOC}/dependencies/ ./
COPY --from=build ${BUILD_LOC}/spring-boot-loader/ ./
COPY --from=build ${BUILD_LOC}/snapshot-dependencies/ ./
COPY --from=build ${BUILD_LOC}/application/ ./

ENV CLASSPATH='/app:/app/lib/*'

RUN echo "exec java org.springframework.boot.loader.JarLauncher" >> /app.sh && chmod +x /app.sh

ENTRYPOINT /wait && /app.sh

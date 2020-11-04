FROM maven:3.6.3-openjdk-8 as build

RUN mkdir build

WORKDIR /build

COPY src /build/src
COPY pom.xml /build/pom.xml

RUN mvn clean package

FROM openjdk:8
COPY --from=build /build/target/gist-backup-jar-with-dependencies.jar /app/gist-backup-jar-with-dependencies.jar

WORKDIR /app

ENTRYPOINT java $ARGS -jar gist-backup-jar-with-dependencies.jar
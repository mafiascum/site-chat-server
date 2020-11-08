FROM gradle:6.7-jdk11 as build
MAINTAINER cciccia@gmail.com

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN ./gradlew build

FROM adoptopenjdk/openjdk11:alpine-jre

RUN mkdir /app
RUN mkdir /app/config

COPY --from=build /home/gradle/src/build/libs/site-chat-server.jar /app/site-chat-server.jar

COPY ./ServerConfig.txt /app/config

EXPOSE 4241
CMD java -Xms1024m -Xmx2048m -jar /app/site-chat-server.jar --verbose -p 4241 -d file:/app/config/


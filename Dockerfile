FROM openjdk:8u111-jdk-alpine
MAINTAINER ccatlett2000@mctherealm.net

RUN apk add --no-cache git apache-ant

WORKDIR /usr/src/chat/
ADD . .
RUN ant -f SiteChatServer.xml compile

EXPOSE 4241
CMD java -Xms1024m -Xmx2048m -cp '/usr/src/chat/lib/*' net.mafiascum.web.sitechat.server.SiteChatServer --verbose -p 4241 -d file:/usr/src/chat/secret/

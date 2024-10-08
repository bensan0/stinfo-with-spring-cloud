FROM amazoncorretto:21.0.4-al2023-headless
MAINTAINER phillip
ADD ./chat-service.jar /app/chat-service.jar 
EXPOSE 8087
ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS /app/chat-service.jar $PARAMS"]
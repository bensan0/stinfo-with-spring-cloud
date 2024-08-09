FROM amazoncorretto:21.0.4-al2023-headless
MAINTAINER phillip
ADD ./gateway.jar /app/gateway.jar 
EXPOSE 8999
CMD ["sh","-c","java -jar $JAVA_OPTS /app/gateway.jar $PARAMS"]

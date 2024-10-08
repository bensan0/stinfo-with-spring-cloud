FROM amazoncorretto:21.0.4-al2023-headless
MAINTAINER phillip
ADD ./stock-service.jar /app/stock-service.jar 
EXPOSE 8082
ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS /app/stock-service.jar $PARAMS"]
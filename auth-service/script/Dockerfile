FROM amazoncorretto:21.0.4-al2023-headless
MAINTAINER phillip
ADD ./auth-service.jar /app/auth-service.jar 
EXPOSE 8086
ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS /app/auth-service.jar $PARAMS"]
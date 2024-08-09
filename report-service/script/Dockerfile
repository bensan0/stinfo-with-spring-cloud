FROM amazoncorretto:21.0.4-al2023-headless
MAINTAINER phillip
ADD ./report-service.jar /app/report-service.jar 
EXPOSE 8084
ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS /app/report-service.jar $PARAMS"]
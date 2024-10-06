FROM amazoncorretto:21.0.4-al2023-headless
MAINTAINER phillip
ADD ./scraper-service.jar /app/scraper-service.jar
EXPOSE 8083
ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS /app/scraper-service.jar $PARAMS"]
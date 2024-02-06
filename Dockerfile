FROM amazoncorretto:17.0.7-alpine

WORKDIR /app

COPY target/CsvS3AwsLambdaDynamoDb-0.0.1-SNAPSHOT.jar app.jar

CMD ["java", "-jar", "app.jar"]

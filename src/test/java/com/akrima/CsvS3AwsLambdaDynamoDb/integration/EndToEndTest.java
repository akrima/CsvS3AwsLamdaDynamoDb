package com.akrima.CsvS3AwsLambdaDynamoDb.integration;

import ch.qos.logback.core.util.FileUtil;
import com.akrima.CsvS3AwsLambdaDynamoDb.handler.S3EventHandler;
import com.akrima.CsvS3AwsLambdaDynamoDb.model.UserData;
import com.akrima.CsvS3AwsLambdaDynamoDb.utils.MockContextUtil;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.tests.EventLoader;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Region;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class EndToEndTest {
    public static final String BUCKET_NAME = "akrimas3bucket";
    public static final String KEY = "UserData.csv";

    @Container
    private static final LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
            .withServices(LocalStackContainer.Service.S3, LocalStackContainer.Service.DYNAMODB)
            .withEnv("DEFAULT_REGION", "ca-central-1")
            .waitingFor(Wait.forLogMessage(".*Ready.*", 1))
            .withStartupTimeout(Duration.ofSeconds(60));
    public static final String TABLE_NAME = "UserData";


    @Test
    void handleRequestTest() throws IOException, InterruptedException {
         // Set up your Lambda function with the configured S3 and DynamoDB clients
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://" + localstack.getHost() + ":" + localstack.getMappedPort(4566), localstack.getRegion()))
                .withPathStyleAccessEnabled(true)
                //.withForceGlobalBucketAccessEnabled(true)
                .build();

        // Create S3 bucket
        s3Client.createBucket(BUCKET_NAME, Region.fromValue( localstack.getRegion()));

        // Put the CSV file in the S3 bucket
        s3Client.putObject(BUCKET_NAME, KEY, getFileFromResources("files/UserData.csv"));

        // Create DynamoDb client
        AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://" + localstack.getHost() + ":" + localstack.getMappedPort(4566),  localstack.getRegion()))
                .build();

        // Create dynamoDb Table
        DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(dynamoDBClient);
        CreateTableRequest createTableRequest = dynamoDBMapper.generateCreateTableRequest(UserData.class);
        createTableRequest.setProvisionedThroughput(new ProvisionedThroughput(5L, 5L));
        dynamoDBClient.createTable(createTableRequest);


        S3EventHandler s3EventHandler = new S3EventHandler(s3Client, dynamoDBClient);


        //Thread.sleep(5000); FIXME uncomment if needed

        // Extract the s3Event from the json file (resources)
        S3Event s3Event  = EventLoader.loadS3Event("files/s3Event.json");

        // Trigger the Lambda function with the S3Event
        String result = s3EventHandler.handleRequest(s3Event, new MockContextUtil());

        // Assertions
        assertEquals("successfully save data to dynamoDB", result);

        // Verify data inserted into DynamoDb Database
        GetItemResult item = dynamoDBClient.getItem(TABLE_NAME, Map.of("ID", new AttributeValue("2")));

        // Process the result and perform assertions
        assertNotNull(item);
        assertEquals("2", item.getItem().get("ID").getS());
        assertEquals("John", item.getItem().get("Name").getS());
        assertEquals("30", item.getItem().get("Age").getN());

        // Email assertions
        List<AttributeValue> emails = item.getItem().get("Emails").getL();
        assertEquals(2, emails.size());
        assertTrue(emails.stream().anyMatch(attr -> "john@example.com".equals(attr.getS())));
        assertTrue(emails.stream().anyMatch(attr -> "jane@example.com".equals(attr.getS())));

        // Address assertions
        Map<String, AttributeValue> addressMap = item.getItem().get("Address").getM();
        assertEquals("123 Main St", addressMap.get("street").getS());
        assertEquals("Cityville", addressMap.get("City").getS());
        assertEquals("12345", addressMap.get("ZipCode").getS());

        // Assert that our dynamoDb table contains 2 records
        ScanResult scanResult = dynamoDBClient.scan(new ScanRequest().withTableName(TABLE_NAME));
        assertEquals(2, scanResult.getScannedCount());
    }

    private File getFileFromResources(String fileName) throws IOException {
        ClassLoader classLoader = FileUtil.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        if (inputStream  == null) {
            throw new IllegalArgumentException("File not found : " + fileName);
        } else {
            File tempFile = File.createTempFile("temp", ".csv");
            tempFile.deleteOnExit();

            Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return tempFile;
        }
    }
}

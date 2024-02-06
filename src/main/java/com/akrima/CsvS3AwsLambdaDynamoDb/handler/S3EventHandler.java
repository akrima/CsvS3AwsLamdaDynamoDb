package com.akrima.CsvS3AwsLambdaDynamoDb.handler;


import com.akrima.CsvS3AwsLambdaDynamoDb.model.UserData;
import com.akrima.CsvS3AwsLambdaDynamoDb.utils.CsvUtil;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperTableModel;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class S3EventHandler implements RequestHandler<S3Event, String> {

    // init s3Client
    private AmazonS3 s3Client;
    private AmazonDynamoDB dynamoDBClient;

    public S3EventHandler() {
        // Default constructor
    }

    public S3EventHandler(AmazonS3 s3Client, AmazonDynamoDB dynamoDBClient) {
        this.s3Client = s3Client;
        this.dynamoDBClient = dynamoDBClient;
    }
    @Override
    public String handleRequest(S3Event s3Event, Context context) {
        context.getLogger().log("S3 event received :::" + s3Event.toString());
        String bucketName = s3Event.getRecords().get(0).getS3().getBucket().getName();
        String fileName = s3Event.getRecords().get(0).getS3().getObject().getKey();

        try (S3ObjectInputStream s3ObjectInputStream = s3Client.getObject(bucketName, fileName).getObjectContent()){

            List<UserData> userDataList = CsvUtil.readAndProcessCSV(s3ObjectInputStream, context);
            context.getLogger().log("CSV users count ::: " + userDataList.size());

            // init dynamoDb table

            DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(dynamoDBClient);
            List<TransactWriteItem> transactWriteItemList = userDataList.stream()
                    .map(userData -> {
                        DynamoDBMapperTableModel<UserData> model = dynamoDBMapper.getTableModel(UserData.class);
                        Map<String, AttributeValue> attributeValues = model.convert(userData);
                        return new TransactWriteItem().withPut(new Put().withItem(attributeValues).withTableName("UserData"));
                    })
                    .collect(Collectors.toList());

            TransactWriteItemsRequest transactWriteItemsRequest = new TransactWriteItemsRequest()
                    .withTransactItems(transactWriteItemList);

            TransactWriteItemsResult transactWriteItemsResponse = dynamoDBClient.transactWriteItems(transactWriteItemsRequest);
            context.getLogger().log("successfully save data to dynamoDB with transactWriteItemsResponse ::: " + transactWriteItemsResponse.toString());
        } catch (Exception e) {
            context.getLogger().log("Error while reading file from S3 and saving to dynamoDb :::" + e.getMessage());
            return "Error while reading file from S3 and saving to dynamoDb :::" + e.getMessage();
        }
        return "successfully save data to dynamoDB";
    }
}

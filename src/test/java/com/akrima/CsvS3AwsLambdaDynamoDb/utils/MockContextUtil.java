package com.akrima.CsvS3AwsLambdaDynamoDb.utils;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

import java.nio.charset.StandardCharsets;

/**
 * Well we can add more unit tests ...
 */

// Mock Context class for testing purposes
public class MockContextUtil implements Context {
    @Override
    public String getAwsRequestId() {
        return "mockRequestId";
    }

    @Override
    public String getLogGroupName() {
        return "mockLogGroupName";
    }

    @Override
    public String getLogStreamName() {
        return "mockLogStreamName";
    }

    @Override
    public String getFunctionName() {
        return "mockFunctionName";
    }

    @Override
    public String getFunctionVersion() {
        return "mockFunctionVersion";
    }

    @Override
    public String getInvokedFunctionArn() {
        return "mockInvokedFunctionArn";
    }

    @Override
    public CognitoIdentity getIdentity() {
        return null;
    }

    @Override
    public ClientContext getClientContext() {
        return null;
    }

    @Override
    public int getRemainingTimeInMillis() {
        return 0;
    }

    @Override
    public int getMemoryLimitInMB() {
        return 0;
    }

    @Override
    public LambdaLogger getLogger() {
        return new LambdaLogger() {
            @Override
            public void log(String message) {
                System.out.println("Mock Logger: " + message);
            }

            @Override
            public void log(byte[] message) {
                System.out.println("Mock Logger: " + new String(message, StandardCharsets.UTF_8));
            }
        };
    }
}
package com.akrima.CsvS3AwsLambdaDynamoDb.utils;

import com.akrima.CsvS3AwsLambdaDynamoDb.model.UserData;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CsvUtilTest {

    @Test
    void readAndProcessCSV_ValidInput_ReturnsListOfUserData() {
        // Prepare test CSV data
        String csvData = """
                ID,Nom,Age,Address,Emails
                2,John,30,"{""street"":""123 Main St"",""City"":""Cityville"",""ZipCode"":""12345""}","[""john@example.com"",""jane@example.com""]"
                3,Jane,25,"{""street"":""456 Oak St"",""City"":""Townsville"",""ZipCode"":""54321""}","[""jane@example.com""]"                    
                """;



        // Convert CSV data to S3ObjectInputStream
        S3ObjectInputStream inputStream = new S3ObjectInputStream(new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8)), null);

        // Mock Context
        Context mockContext = new MockContextUtil();

        // Perform the test
        List<UserData> result = CsvUtil.readAndProcessCSV(inputStream, mockContext);

        // Verify the result
        assertEquals(2, result.size());

        // Verify the content of the first UserData object
        UserData userData1 = result.get(0);
        assertEquals("2", userData1.getId());
        assertEquals("John", userData1.getName());
        assertEquals(30, userData1.getAge());
        assertEquals("123 Main St", userData1.getAddress().getStreet());
        assertEquals("Cityville", userData1.getAddress().getCity());
        assertEquals("12345", userData1.getAddress().getZipCode());
        assertEquals(List.of("john@example.com", "jane@example.com"), userData1.getEmails());

        // Verify the content of the second UserData object
        UserData userData2 = result.get(1);
        assertEquals("3", userData2.getId());
        assertEquals("Jane", userData2.getName());
        assertEquals(25, userData2.getAge());
        assertEquals("456 Oak St", userData2.getAddress().getStreet());
        assertEquals("Townsville", userData2.getAddress().getCity());
        assertEquals("54321", userData2.getAddress().getZipCode());
        assertEquals(List.of("jane@example.com"), userData2.getEmails());
    }

    @Test
    void readAndProcessCSV_InvalidRecord_ThrowsRuntimeException() {
        String invalidCsv = """
                io,Nom,Age,Address,Emails
                2,John,30,"{""street"":""123 Main St"",""City"":""Cityville"",""ZipCode"":""12345""}","[""john@example.com"",""jane@example.com""]"
                3,Jane,25,"{""street"":""456 Oak St"",""City"":""Townsville"",""ZipCode"":""54321""}","[""jane@example.com""]"                    
                """;
        // Convert CSV data to S3ObjectInputStream
        S3ObjectInputStream inputStream = new S3ObjectInputStream(new ByteArrayInputStream(invalidCsv.getBytes(StandardCharsets.UTF_8)), null);

        // Mock Context
        Context mockContext = new MockContextUtil();

        // Perform the test
        assertThrows(RuntimeException.class, () -> CsvUtil.readAndProcessCSV(inputStream, mockContext));

    }

    @Test
    void readAndProcessCSV_InvalidAge_ThrowsRuntimeException() {
        String invalidCsv = """
                io,Nom,Age,Address,Emails
                2,John,invalidAge,"{""street"":""123 Main St"",""City"":""Cityville"",""ZipCode"":""12345""}","[""john@example.com"",""jane@example.com""]"
                3,Jane,25,"{""street"":""456 Oak St"",""City"":""Townsville"",""ZipCode"":""54321""}","[""jane@example.com""]"                    
                """;
        // Convert CSV data to S3ObjectInputStream
        S3ObjectInputStream inputStream = new S3ObjectInputStream(new ByteArrayInputStream(invalidCsv.getBytes(StandardCharsets.UTF_8)), null);

        // Mock Context
        Context mockContext = new MockContextUtil();

        // Perform the test
        assertThrows(RuntimeException.class, () -> CsvUtil.readAndProcessCSV(inputStream, mockContext));
    }

    @Test
    void readAndProcessCSV_File_From_resources() throws IOException {
        try (InputStream resourceStream = getClass().getClassLoader().getResourceAsStream("files/UserData.csv")) {
            S3ObjectInputStream inputStream = new S3ObjectInputStream(resourceStream, null);

            // Mock Context
            Context mockContext = new MockContextUtil();

            // Perform the test
            List<UserData> result = CsvUtil.readAndProcessCSV(inputStream, mockContext);

            // Verify the result
            assertEquals(2, result.size());

            // Verify the content of the first UserData object
            UserData userData1 = result.get(0);
            assertEquals("2", userData1.getId());
            assertEquals("John", userData1.getName());
            assertEquals(30, userData1.getAge());
            assertEquals("123 Main St", userData1.getAddress().getStreet());
            assertEquals("Cityville", userData1.getAddress().getCity());
            assertEquals("12345", userData1.getAddress().getZipCode());
            assertEquals(List.of("john@example.com", "jane@example.com"), userData1.getEmails());

        }
    }

}


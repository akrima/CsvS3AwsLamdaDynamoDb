package com.akrima.CsvS3AwsLambdaDynamoDb.utils;

import com.akrima.CsvS3AwsLambdaDynamoDb.model.UserData;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CsvUtil {

    public static List<UserData> readAndProcessCSV(S3ObjectInputStream inputStream, Context context) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            CSVParser csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);

            return StreamSupport.stream(csvParser.spliterator(), false)
                    .map(csvRecord -> {
                        try {
                            String id = csvRecord.get("ID");
                            String name = csvRecord.get("Nom");
                            int age = Integer.parseInt(csvRecord.get("Age"));

                            // Extracting Address from CSV
                            String addressString = csvRecord.get("Address");
                            ObjectMapper objectMapper = new ObjectMapper();
                            Map<String, String> addressMap = objectMapper.readValue(addressString, new TypeReference<>() {
                            });

                            String street = addressMap.get("street");
                            String city = addressMap.get("City");
                            String zipCode = addressMap.get("ZipCode");

                            // Extracting Emails from CSV
                            String emailsString = csvRecord.get("Emails");
                            List<String> emails = objectMapper.readValue(emailsString, new TypeReference<List<String>>() {});

                            UserData.Address address = new UserData.Address(street, city, zipCode);
                            return new UserData(id, name, age, address, emails);
                        } catch (NumberFormatException | IOException e) {
                            context.getLogger().log("Error parsing CSV record: " + e.getMessage());
                            throw new RuntimeException("Error parsing CSV record:", e);
                        }
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            context.getLogger().log("Error CSV reading exception: " + e.getMessage());
            throw new RuntimeException("CSV reading exception", e);
        }
    }


}

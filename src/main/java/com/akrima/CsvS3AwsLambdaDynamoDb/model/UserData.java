package com.akrima.CsvS3AwsLambdaDynamoDb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import java.util.List;

/**
 * I had to use class instead of java record!!! cause with record  dynamodb is not able
 * to read data from the object sent by lambda (required getters)
 * I could go with record but i had to write explicit getters
 */
@DynamoDBTable(tableName = "UserData")
public class UserData {

    private String id;
    private String name;
    private int age;
    private Address address;
    private List<String> emails;

    public UserData() {
        // Needed for DynamoDB mapper to instantiate the object
    }

    public UserData(String id, String name, int age, Address address, List<String> emails) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.address = address;
        this.emails = emails;
    }

    @DynamoDBHashKey(attributeName = "ID")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDBAttribute(attributeName = "Name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @DynamoDBAttribute(attributeName = "Age")
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @DynamoDBAttribute(attributeName = "Address")
    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @DynamoDBAttribute(attributeName = "Emails")
    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    @DynamoDBDocument
    public static class Address {

        private String street;
        private String city;
        private String zipCode;

        public Address() {
            // Needed for DynamoDB mapper to instantiate the object
        }

        public Address(String street, String city, String zipCode) {
            this.street = street;
            this.city = city;
            this.zipCode = zipCode;
        }

        @DynamoDBAttribute(attributeName = "street")
        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        @DynamoDBAttribute(attributeName = "City")
        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        @DynamoDBAttribute(attributeName = "ZipCode")
        public String getZipCode() {
            return zipCode;
        }

        public void setZipCode(String zipCode) {
            this.zipCode = zipCode;
        }
    }
}


```markdown
# CsvS3AwsLambdaDynamoDb

This project is a demo illustrating the processing of a CSV file stored in an AWS S3 bucket, with the data saved to a DynamoDB table. The code is written using Spring Boot and the AWS SDK.

## Project Structure

- **src/main/java/com/akrima/CsvS3AwsLambdaDynamoDb/handler/S3EventHandler.java:** A Lambda event handler that processes events triggered by the addition of files to the S3 bucket.

- **src/main/java/com/akrima/CsvS3AwsLambdaDynamoDb/model/UserData.java:** A class representing user data to be saved in DynamoDB.

## Dependencies

- **Spring Boot Starter:** The starter dependency for Spring Boot projects.

- **AWS SDK:** Dependencies for interacting with AWS services such as S3, Lambda, and DynamoDB.

- **Apache Commons IO:** Used to read file content from S3.

- **Jackson (ObjectMapper):** For JSON data deserialization.

- **Testcontainers:** For integration tests with local containers, including DynamoDB and LocalStack.

## Configuration

The `pom.xml` file contains the Maven configuration of the project, including dependencies and plugins used to create an executable JAR.

The `S3EventHandler.java` file contains the Lambda event handler code, and `UserData.java` defines the structure of user data.

## Usage

1. Ensure that your AWS environment is correctly configured.

2. Build the project with Maven:
   ```bash
   mvn clean install
   ```

3. Deploy the generated JAR to AWS Lambda via aws console or the following sam command (don't forget to update template file):
```bash
    sam deploy --template-file template.yml --stack-name CsvS3AwsLambdaDynamoDb --capabilities CAPABILITY_IAM CAPABILITY_NAMED_IAM
   ```
4. Configure an S3 trigger to call the Lambda function when a file is added to the bucket.

## Tests

Integration tests are provided in the `src/test/java` folder using Testcontainers for local AWS services.

---

This project was created Abderrahim KRIMA.

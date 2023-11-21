# App README

This application is a Java application that uses the MongoDB Java Driver and AWS SDK for Java to interact with MongoDB Atlas. It assumes an AWS IAM role and fetches documents from a MongoDB collection in a loop, sleeping for 60 seconds between each iteration.

## Getting Started

### Prerequisites

- Java 8 or later
- MongoDB Atlas account
- AWS account with IAM role setup

### Installation

1. Clone this repository to your local machine.
2. Open the project in your preferred IDE.
3. Update the MongoDB Atlas connection details and AWS IAM role details in the `main` method of the `App` class.

## Usage

Run the application with the following command:

```sh
mvn exec:java -Dexec.mainClass=com.example.App -Dexec.args="<region> <roleArn> <roleSessionName>"
```

Where:

- `<region>` is the Amazon region (for example, `us-east-1`).
- `<roleArn>` is the Amazon Resource Name (ARN) of the role to assume (for example, `arn:aws:iam::000008047983:role/s3role`).
- `<roleSessionName>` is an identifier for the assumed role session (for example, `mysession`).

The application will connect to the specified MongoDB Atlas cluster, assume the specified AWS IAM role, and fetch documents from the `movies` collection in the `sample_mflix` database. It will continue to fetch documents every 60 seconds until the application is stopped.

## Code Overview

The `App` class is the entry point of the application. It takes command-line arguments for the AWS region, role ARN, and role session name. It then creates an AWS STS client, assumes the specified role, and uses the returned credentials to create a MongoDB client. The application then enters a loop where it fetches a document from the `movies` collection and prints it to the console. The application sleeps for 60 seconds between each iteration.

The `MongoAwsCredentialSupplier` and `CredentialsSupplier` classes are helper classes used to refresh AWS credentials when they expire.

## Contributing

Contributions are welcome! Please ensure any PRs include documentation, tests, and pass checks.

## License

This project is licensed under the MIT License - see the LICENSE.md file for details.

## Resources

- [MongoDB Java Driver](https://mongodb.github.io/mongo-java-driver/)
- [AWS SDK for Java](https://aws.amazon.com/sdk-for-java/)
- [MongoDB Atlas](https://www.mongodb.com/cloud/atlas)
- [AWS IAM](https://aws.amazon.com/iam/)

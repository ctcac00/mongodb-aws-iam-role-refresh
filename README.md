# mongodb-aws-iam-role-refresh

Simple example program showcasing how to refresh AWS IAM credentials while connected to a MongoDB Atlas instance

## How to run

```sh
mvn exec:java -Dexec.mainClass=com.example.App -Dexec.args="eu-west-2 arn:aws:iam::123456789012:role/aws-iam-role mysession"
```

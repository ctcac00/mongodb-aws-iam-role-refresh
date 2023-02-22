package com.example;

import com.mongodb.AwsCredential;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;
import software.amazon.awssdk.services.sts.model.Credentials;

import java.time.Instant;
import java.util.Arrays;
import java.util.function.Supplier;

import org.bson.Document;

public class App {

    public static void main(String[] args) {
        boolean running = true;
        final String USAGE = "\n" +
                "Usage:\n" +
                "    AssumeRoleExample <region> <roleArn> <roleSessionName> \n\n" +
                "Where:\n" +
                "    region - the Amazon region (for example, us-east-1) \n" +
                "    roleArn - the Amazon Resource Name (ARN) of the role to assume (for example, rn:aws:iam::000008047983:role/s3role). \n"
                +
                "    roleSessionName - an identifier for the assumed role session (for example, mysession). \n";

        if (args.length != 3) {
            System.out.println(USAGE);
            System.exit(1);
        }

        String regionString = args[0];
        String roleArn = args[1];
        String roleSessionName = args[2];

        Region region = Region.of(regionString);
        StsClient stsClient = StsClient.builder()
                .region(region)
                .build();

        MongoCredential credential = MongoCredential.createAwsCredential(null, null)
                .withMechanismProperty(MongoCredential.AWS_CREDENTIAL_PROVIDER_KEY,
                        new MongoAwsCredentialSupplier(
                                new CredentialsSupplier(stsClient, roleArn, roleSessionName)));

        MongoClient mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .applyToClusterSettings(
                                builder -> builder.hosts(Arrays.asList(
                                        new ServerAddress("cluster0-shard-00-00.a1bcd.mongodb.net", 27017),
                                        new ServerAddress("cluster0-shard-00-01.a1bcd.mongodb.net", 27017),
                                        new ServerAddress("cluster0-shard-00-02.a1bcd.mongodb.net", 27017))))
                        .applyToSslSettings(builder -> builder.enabled(true))
                        .credential(credential)
                        .build());

        // use client
        MongoDatabase database = mongoClient.getDatabase("sample_mflix");
        MongoCollection<Document> collection = database.getCollection("movies");

        while (running) {

            System.out.println("fetching next doc");
            Document doc = collection.find()
                    .first();
            if (doc == null) {
                System.out.println("No results found.");
            } else {
                System.out.println(doc.toJson());
                System.out.println();
            }

            System.out.println("sleeping for 1 min");
            // Wait for 60 seconds before running again
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                System.out.println("Thread was interrupted: " + e.getMessage());
            }
        }

        mongoClient.close();
        System.out.println("mongoClient closed");
        stsClient.close();
    }

    private static class MongoAwsCredentialSupplier implements Supplier<AwsCredential> {
        private final Supplier<Credentials> wrappedSupplier;
        private Credentials credentials;

        public MongoAwsCredentialSupplier(Supplier<Credentials> wrappedSupplier) {
            this.wrappedSupplier = wrappedSupplier;
            credentials = wrappedSupplier.get();
        }

        @Override
        public AwsCredential get() {
            synchronized (this) {
                // alternatively, could start a thread that keeps the credentials up to date, in
                // order to avoid blocking
                if (credentials.expiration().isAfter(Instant.now().minusSeconds(120))) {
                    System.out.println("Refreshing AWS credentials");
                    credentials = wrappedSupplier.get();
                }
            }
            return new AwsCredential(
                    credentials.accessKeyId(),
                    credentials.secretAccessKey(),
                    credentials.sessionToken());
        }
    }

    private static class CredentialsSupplier implements Supplier<Credentials> {
        private final StsClient stsClient;
        private final String roleArn;
        private final String roleSessionName;

        public CredentialsSupplier(StsClient stsClient, String roleArn, String roleSessionName) {
            this.stsClient = stsClient;
            this.roleArn = roleArn;
            this.roleSessionName = roleSessionName;
        }

        @Override
        public Credentials get() {
            AssumeRoleRequest roleRequest = AssumeRoleRequest.builder()
                    .roleArn(roleArn)
                    .roleSessionName(roleSessionName)
                    .build();

            AssumeRoleResponse roleResponse = stsClient.assumeRole(roleRequest);
            return roleResponse.credentials();
        }
    }
}
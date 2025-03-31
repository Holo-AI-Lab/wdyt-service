package ai.holo.wdyt.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@Slf4j
public class S3Service {
    private final String bucketName;
    private final String s3Endpoint;
    private final S3Client client;
    private final EnvironmentUtil environmentUtil;

    public S3Service(@Value("${aws.region}") String region,
                     @Value("${aws.profile}") String awsProfile,
                     @Value("${aws.s3.bucket}") String bucketName,
                     @Value("${aws.s3.endpoint}") String s3Endpoint, EnvironmentUtil environmentUtil) {
        this.bucketName = bucketName;
        this.s3Endpoint = s3Endpoint;
        this.environmentUtil = environmentUtil;
        this.client = S3Client.builder().region(Region.of(region)).
                credentialsProvider(ProfileCredentialsProvider.create(awsProfile)).build();
    }

    public InputStream getImageFromS3(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return client.getObject(getObjectRequest);
    }

    public String saveImage(InputStream image, String path) {
        path = updatePathBasedOnEnvironment(path);
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(path)
                    .build();
            client.putObject(putObjectRequest, RequestBody.fromInputStream(image, image.available()));
            return path;
        } catch (IOException e) {
            log.error("Failed to save image to S3", e);
            throw new RuntimeException(e);
        }
    }

    private String updatePathBasedOnEnvironment(String path) {
        if (environmentUtil.isDevelopment()) {
            path = "dev/" + path;
        }
        return path;
    }


    public void deleteDirectory(String directoryPrefix) {
        directoryPrefix = updatePathBasedOnEnvironment(directoryPrefix);
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(directoryPrefix)
                .build();

        ListObjectsV2Response listResponse;
        do {
            listResponse = client.listObjectsV2(listRequest);

            List<ObjectIdentifier> objectsToDelete = listResponse.contents().stream()
                    .map(s3Object -> ObjectIdentifier.builder().key(s3Object.key()).build())
                    .toList();

            if (!objectsToDelete.isEmpty()) {
                DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                        .bucket(bucketName)
                        .delete(Delete.builder().objects(objectsToDelete).build())
                        .build();

                client.deleteObjects(deleteRequest);
            }

            listRequest = listRequest.toBuilder().continuationToken(listResponse.nextContinuationToken()).build();
        } while (listResponse.isTruncated());
    }

    public String getFileS3Url(String path) {
        return String.format("%s/%s", s3Endpoint, path);
    }
}

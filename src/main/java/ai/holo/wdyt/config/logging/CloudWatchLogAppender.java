package ai.holo.wdyt.config.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;

import java.util.Collections;

@Setter
@Getter
public class CloudWatchLogAppender extends AppenderBase<ILoggingEvent> {

    private String logGroupName;
    private String logStreamName;
    private String region;
    private String awsProfile;
    private CloudWatchLogsClient cloudWatchLogsClient;
    private Layout<ILoggingEvent> layout;

    @Override
    public void start() {
        super.start();
        cloudWatchLogsClient = CloudWatchLogsClient.builder()
                .region(Region.of(region)).
                credentialsProvider(ProfileCredentialsProvider.create(awsProfile))
                .build();
        ensureLogGroupAndStreamExist();
    }

    @Override
    protected void append(ILoggingEvent event) {
        String message = layout.doLayout(event);

        InputLogEvent logEvent = InputLogEvent.builder()
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();

        PutLogEventsRequest putLogEventsRequest = PutLogEventsRequest.builder()
                .logGroupName(logGroupName)
                .logStreamName(logStreamName)
                .logEvents(Collections.singletonList(logEvent))
                .build();

        cloudWatchLogsClient.putLogEvents(putLogEventsRequest);
    }

    private void ensureLogGroupAndStreamExist() {
        try {
            cloudWatchLogsClient.createLogGroup(CreateLogGroupRequest.builder().logGroupName(logGroupName).build());
        } catch (ResourceAlreadyExistsException ignored) {}

        try {
            cloudWatchLogsClient.createLogStream(CreateLogStreamRequest.builder().logGroupName(logGroupName).logStreamName(logStreamName).build());
        } catch (ResourceAlreadyExistsException ignored) {}
    }
}

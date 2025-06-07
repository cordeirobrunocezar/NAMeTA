package com.example.lib;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.rpc.ApiException;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.AudioEncoding;
import com.google.cloud.dialogflow.v2.DetectIntentRequest;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.InputAudioConfig;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;
import com.google.protobuf.ByteString;
import com.google.protobuf.Value;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class Dialogflow {
    private ServiceAccountCredentials credentials;
    private String display_name;
    private Map<String, Value> parameters;

    public Dialogflow(InputStream credentialsStream) throws IOException {
        this.credentials = ServiceAccountCredentials.fromStream(credentialsStream);
    }

    public Map<String, Value> get_parameters() {
        return parameters;
    }

    public String get_display_name() {
        return display_name;
    }

    public String detectIntentTexts(
            String text,
            String sessionId,
            String languageCode
    ) throws IOException {
        SessionsSettings sessionsSettings = SessionsSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();
        SessionsClient sessionsClient = SessionsClient.create(sessionsSettings);
        SessionName session = SessionName.of(credentials.getProjectId(), sessionId);
        TextInput textInput = TextInput.newBuilder()
                .setText(text).setLanguageCode(languageCode).build();
        QueryInput queryInput = QueryInput
                .newBuilder().setText(textInput).build();
        DetectIntentResponse response = sessionsClient.detectIntent(session, queryInput);
        display_name = response.getQueryResult().getIntent().getDisplayName();
        parameters = response.getQueryResult().getParameters().getFieldsMap();
        return response.getQueryResult().getFulfillmentText();
    }

    // DialogFlow API Detect Intent sample with audio files.
    public String detectIntentAudio(
            String projectId,
            String audioFilePath,
            String sessionId,
            String languageCode
    ) throws IOException, ApiException {
        SessionsSettings sessionsSettings = SessionsSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();
        // Instantiates a client
        try (SessionsClient sessionsClient = SessionsClient.create(sessionsSettings)) {
            // Set the session name using the sessionId (UUID) and projectID (my-project-id)
            SessionName session = SessionName.of(credentials.getProjectId(), sessionId);

            // Note: hard coding audioEncoding and sampleRateHertz for simplicity.
            // Audio encoding of the audio content sent in the query request.
            AudioEncoding audioEncoding = AudioEncoding.AUDIO_ENCODING_LINEAR_16;
            int sampleRateHertz = 16000;

            // Instructs the speech recognizer how to process the audio content.
            InputAudioConfig inputAudioConfig =
                    InputAudioConfig.newBuilder()
                            .setAudioEncoding(
                                    audioEncoding) // audioEncoding = AudioEncoding.AUDIO_ENCODING_LINEAR_16
                            .setLanguageCode(languageCode) // languageCode = "en-US"
                            .setSampleRateHertz(sampleRateHertz) // sampleRateHertz = 16000
                            .build();

            // Build the query with the InputAudioConfig
            QueryInput queryInput = QueryInput.newBuilder().setAudioConfig(inputAudioConfig).build();

            // Read the bytes from the audio file
            byte[] inputAudio = Files.readAllBytes(Paths.get(audioFilePath));

            // Build the DetectIntentRequest
            DetectIntentRequest request =
                    DetectIntentRequest.newBuilder()
                            .setSession(session.toString())
                            .setQueryInput(queryInput)
                            .setInputAudio(ByteString.copyFrom(inputAudio))
                            .build();

            // Performs the detect intent request
            DetectIntentResponse response = sessionsClient.detectIntent(request);

            // Display the query result
            QueryResult queryResult = response.getQueryResult();
            System.out.println("====================");
            System.out.format("Query Text: '%s'\n", queryResult.getQueryText());
            System.out.format(
                "Detected Intent: %s (confidence: %f)\n",
                queryResult.getIntent().getDisplayName(),
                queryResult.getIntentDetectionConfidence()
                );
            System.out.format(
                "Fulfillment Text: '%s'\n",
                queryResult.getFulfillmentMessagesCount() > 0
                        ? queryResult.getFulfillmentMessages(0).getText()
                        : "Triggered Default Fallback Intent"
                        );
         
            return queryResult.getFulfillmentText();
        }
    }
}
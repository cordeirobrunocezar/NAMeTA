package com.example.lib;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class GoogleSheets {
    private static final String APPLICATION_NAME = "google-sheets";
    private GoogleCredential credentials;

    public GoogleSheets(InputStream credentialsStream) throws IOException, GeneralSecurityException {
        this.credentials = GoogleCredential.fromStream(credentialsStream);
    }

    public Sheets createSheetsService() throws GeneralSecurityException, IOException {
        HttpTransport httpTransport = new NetHttpTransport();
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        return new Sheets.Builder(httpTransport, jsonFactory, credentials.createScoped(
                Collections.singleton(SheetsScopes.SPREADSHEETS)))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public ValueRange readSingleRange(
            Sheets service,
            String spreadsheetId,
            String range
    ) throws IOException {
        ValueRange result = service.spreadsheets().values().get(spreadsheetId, range).execute();
        int numRows = result.getValues() != null ? result.getValues().size() : 0;
        System.out.printf("%d rows retrieved.", numRows);
        return result;
    }

    public BatchGetValuesResponse readMultipleRanges(
        Sheets service,
        String spreadsheetId,
        List<String> ranges
    ) throws IOException {
        BatchGetValuesResponse result = service.spreadsheets().values().batchGet(spreadsheetId)
                .setRanges(ranges).execute();
        System.out.printf("%d ranges retrieved.", result.getValueRanges().size());
        return result;
    }

    public void writeToSingleRange(
            Sheets service,
            String spreadsheetId,
            String range,
            List<List<Object>> values,
            String valueInputOption
    ) throws IOException {
        ValueRange body = new ValueRange()
                .setValues(values);
        UpdateValuesResponse result =
                service.spreadsheets().values().update(spreadsheetId, range, body)
                        .setValueInputOption(valueInputOption)
                        .execute();
        System.out.printf("%d cells updated.", result.getUpdatedCells());
    }

    public void writeToMultipleRanges(
            Sheets service,
            String spreadSheetId,
            List<ValueRange> data,
            String range,
            List<List<Object>> values,
            String valueInputOption
    ) throws IOException {
        data.add(new ValueRange()
                .setRange(range)
                .setValues(values));
        BatchUpdateValuesRequest body = new BatchUpdateValuesRequest()
                .setValueInputOption(valueInputOption)
                .setData(data);
        BatchUpdateValuesResponse result =
                service.spreadsheets().values().batchUpdate(spreadSheetId, body).execute();
        System.out.printf("%d cells updated.", result.getTotalUpdatedCells());
    }

    public void appendValue(
            Sheets service,
            String spreadsheetId,
            String range,
            List<List<Object>> values,
            String valueInputOption
    ) throws IOException {
        ValueRange body = new ValueRange()
                .setValues(values);
        AppendValuesResponse result =
                service.spreadsheets().values().append(spreadsheetId, range, body)
                        .setValueInputOption(valueInputOption)
                        .execute();
        System.out.printf("%d cells appended.", result.getUpdates().getUpdatedCells());
    }
}

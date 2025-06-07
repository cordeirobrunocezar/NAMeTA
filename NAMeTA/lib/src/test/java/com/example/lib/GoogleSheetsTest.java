package com.example.lib;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class GoogleSheetsTest {
    private File file = new File("NAMeTA\\app\\src\\main\\res\\raw\\credentials_sheets.json");
    private InputStream fileInputStream = new FileInputStream(file);
    private GoogleSheets googleSheets = new GoogleSheets(fileInputStream);
    private Sheets sheets = googleSheets.createSheetsService();
    private String spreadSheetId = "";
    private String range = "Medication Plan!A1";
    private List<String> ranges = List.of("Medication Plan!A1","Medication Plan!B1");

    public GoogleSheetsTest() throws GeneralSecurityException, IOException {
    }

    @Test
    public void test_readSingleRange() throws IOException {
        ValueRange result = googleSheets.readSingleRange(
                sheets,
                spreadSheetId,
                range
        );
        System.out.println(result);
    }

    @Test
    public void test_readMultipleRanges() throws IOException {
        BatchGetValuesResponse result = googleSheets.readMultipleRanges(
                sheets,
                spreadSheetId,
                ranges
        );
        System.out.println(result);
    }

    @Test
    public void test_writeToSingleRange() throws IOException {
        googleSheets.writeToSingleRange(
                sheets,
                spreadSheetId,
                "Medication Plan!A2",
                List.of(List.of("lemon")),
                "USER_ENTERED"
        );
        ValueRange result = googleSheets.readSingleRange(
                sheets,
                spreadSheetId,
                "Medication Plan!A2"
        );
        System.out.println(result);
    }

    List<ValueRange> data = new ArrayList<>();;
    @Test
    public void test_writeToMultipleRanges() throws IOException {
        googleSheets.writeToMultipleRanges(
                sheets,
                spreadSheetId,
                data,
                "Medication Plan!A3:A4",
                List.of(List.of("orange, grape")),
                "USER_ENTERED"
        );
        BatchGetValuesResponse result = googleSheets.readMultipleRanges(
                sheets,
                spreadSheetId,
                List.of("Medication Plan!A3", "Medication Plan!A4")
        );
        System.out.println(result);
    }

    @Test
    public void test_appendValue() throws IOException {
        googleSheets.appendValue(
                sheets,
                spreadSheetId,
                "Medication Plan!A5",
                List.of(List.of("apple, banana")),
                "USER_ENTERED"
        );
        BatchGetValuesResponse result = googleSheets.readMultipleRanges(
                sheets,
                spreadSheetId,
                List.of("Medication Plan!A3", "Medication Plan!A4")
        );
        System.out.println(result);
    }
}

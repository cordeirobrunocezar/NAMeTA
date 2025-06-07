package com.example.nameta;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.lib.Dialogflow;
import com.example.lib.GoogleSheets;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.nameta", appContext.getPackageName());
    }
    @Test
    public void dialogflowOnAndroidTest() throws IOException {
        Context appContext = ApplicationProvider.getApplicationContext();
        InputStream credentials = appContext.getResources().openRawResource(R.raw.credentials_dialogflow);
        Dialogflow dataSource = new Dialogflow(credentials);
        String sessionId = "android-unittest-session";
        String languageCode = "pt-BR";
        String result = dataSource.detectIntentTexts("Olá!", sessionId, languageCode);
        assertEquals("Olá! Como posso ajudá-lo?", result);
    }

    @Test
    public void googleSheetsOnAndroidTest() throws IOException, GeneralSecurityException {
        Context appContext = ApplicationProvider.getApplicationContext();
        InputStream credentials = appContext.getResources().openRawResource(R.raw.credentials_dialogflow);
        GoogleSheets googleSheets = new GoogleSheets(credentials);
        Sheets sheets = googleSheets.createSheetsService();
        String spreadSheetId = "1vFxAt97DXEJwIEZC3yleHeWR7VCHdOze14B9IbTu-vk";

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
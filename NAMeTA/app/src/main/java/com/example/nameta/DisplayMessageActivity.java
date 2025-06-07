package com.example.nameta;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lib.Dialogflow;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.example.lib.GoogleSheets;
import com.google.api.services.sheets.v4.Sheets;
import com.google.protobuf.Value;

public class DisplayMessageActivity extends AppCompatActivity {

    TextToSpeech tts;
    String result = null;
    private static final String TAG = "HERE";

    static String type = null;
    static String name = null;
    static String amount = null;
    static String unit_of_measure = null;
    static String display_name = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        // Dialogflow
        InputStream credentials = getResources().openRawResource(R.raw.credentials_dialogflow);
        Dialogflow data = null;
        try {
            data = new Dialogflow(credentials);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String sessionId = "myapplication-session";
        String languageCode = "pt-BR";
        try {
            assert data != null;
            result = data.detectIntentTexts(message, sessionId, languageCode);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // GoogleSheets
        InputStream credentialSheets = getResources().openRawResource(R.raw.credentials_sheets);
        GoogleSheets googleSheets = null;
        try {
            googleSheets = new GoogleSheets(credentialSheets);
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }

        Sheets sheets = null;
        try {
            assert googleSheets != null;
            sheets = googleSheets.createSheetsService();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }

        String spreadSheetId = "";

        display_name = data.get_display_name();

        Log.i(TAG, display_name);
        Log.i(TAG, String.valueOf(display_name.equals("medicineSet")));

        if (display_name.equals("medicineSet")) {
            Map<String, Value> parameters = data.get_parameters();

            Log.i(TAG, String.valueOf(parameters));

            amount = Objects.requireNonNull(parameters.get("amount")).getStringValue();
            type = Objects.requireNonNull(parameters.get("type")).getStringValue();
            unit_of_measure = Objects.requireNonNull(parameters.get("unit_of_measure")).getStringValue();
            name = Objects.requireNonNull(parameters.get("name")).getStringValue();

            //Log.i(TAG, Objects.requireNonNull(parameters.get("brand_name")).getStringValue());
            Log.i(TAG, type);
            Log.i(TAG, name);
            Log.i(TAG, amount);
            Log.i(TAG, unit_of_measure);
        }

        Log.i("HERE",display_name);
        if (display_name.equals("finalConfirmation")) {
            Log.i(TAG, String.valueOf(sheets));
            Sheets finalSheets = sheets;
            GoogleSheets finalGoogleSheets = googleSheets;
            new Thread(() -> {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
                String date = sdf.format(new Date());

                try {
                    Log.i("HERE", "try");
                    assert finalSheets != null;
                    finalGoogleSheets.appendValue(
                            finalSheets,
                            spreadSheetId,
                            "NAMeTA Data!A1:E1",
                            List.of(List.of(date, amount, type, unit_of_measure, name)),
                            "USER_ENTERED"
                    );
                    finalGoogleSheets.appendValue(
                            finalSheets,
                            spreadSheetId,
                            "NAMeTA Data!B1",
                            List.of(List.of(amount)),
                            "USER_ENTERED"
                    );
                    finalGoogleSheets.appendValue(
                            finalSheets,
                            spreadSheetId,
                            "NAMeTA Data!C1",
                            List.of(List.of(type)),
                            "USER_ENTERED"
                    );
                    finalGoogleSheets.appendValue(
                            finalSheets,
                            spreadSheetId,
                            "NAMeTA Data!D1",
                            List.of(List.of(unit_of_measure)),
                            "USER_ENTERED"
                    );
                    finalGoogleSheets.appendValue(
                            finalSheets,
                            spreadSheetId,
                            "NAMeTA Data!E1",
                            List.of(List.of(name)),
                            "USER_ENTERED"
                    );
                } catch (IOException e) {
                    Log.e("Sheets failed", e.getLocalizedMessage());
                }
            }).start();
        }

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.textView);
        textView.setText(result);

        tts = new TextToSpeech(getApplicationContext(), status -> {
            tts.setLanguage(Locale.forLanguageTag("pt-br"));
            if (status == TextToSpeech.SUCCESS) {
                if (result != null) {
                    tts.speak(result, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
        });
    }

    // Text to speech stop and shutdown when user leaves the view
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            Log.d(TAG, "tts destroyer");
        }
    }

}
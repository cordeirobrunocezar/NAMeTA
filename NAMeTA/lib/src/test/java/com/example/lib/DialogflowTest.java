package com.example.lib;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DialogflowTest {
    @Test
    public void simpleIntent_hasAnswer() throws IOException {
        File file = new File("NAMeTA\\app\\src\\main\\res\\raw\\credentials_dialogflow.json");
        InputStream fileInputStream = new FileInputStream(file);
        Dialogflow dialogflow = new Dialogflow(fileInputStream);
        String sessionId = "unittest-session";
        String languageCode = "pt_BR";
        String result = dialogflow.detectIntentTexts("Olá!", sessionId, languageCode);
        assert result.equals("Olá! Como posso ajudá-lo?");
    }
}

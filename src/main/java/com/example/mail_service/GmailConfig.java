package com.example.mail_service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class GmailConfig {
    private static final String APPLICATION_NAME = "Gmail API";
    private  String accessToken = "";
    private static final File filePath = new File(System.getProperty("user.dir") + "/credentials.json");
    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    public  Gmail getGmailService() throws IOException, GeneralSecurityException {

        InputStream in = new FileInputStream(filePath);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        Credential authorize = new GoogleCredential.Builder().setTransport(GoogleNetHttpTransport.newTrustedTransport())
                .setJsonFactory(JSON_FACTORY)
                .setClientSecrets(clientSecrets.getDetails().getClientId(),
                        clientSecrets.getDetails().getClientSecret())
                .build().setAccessToken(getAccessToken(clientSecrets.getDetails().getClientId(), clientSecrets.getDetails().getClientSecret()))
                .setRefreshToken("1//0eCI8N97Y4f3gCgYIARAAGA4SNwF-L9IrPGRtYfflV3ukmL1Tmej36v68MMv90QbfXj9PDt53LIsrObuya-DinhH1HzLFlOxsg1U");

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, authorize)
                .setApplicationName(APPLICATION_NAME).build();
    }

    private static String getAccessToken(String clientId, String clientSecret) {

        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("grant_type", "refresh_token");
            params.put("client_id", clientId);
            params.put("client_secret", clientSecret);
            params.put("refresh_token",
                    "1//0eCI8N97Y4f3gCgYIARAAGA4SNwF-L9IrPGRtYfflV3ukmL1Tmej36v68MMv90QbfXj9PDt53LIsrObuya-DinhH1HzLFlOxsg1U");

            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (postData.length() != 0) {
                    postData.append('&');
                }
                postData.append(URLEncoder.encode(param.getKey(), StandardCharsets.UTF_8));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), StandardCharsets.UTF_8));
            }
            byte[] postDataBytes = postData.toString().getBytes(StandardCharsets.UTF_8);

            URL url = new URL("https://accounts.google.com/o/oauth2/token");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setUseCaches(false);
            con.setRequestMethod("POST");
            con.getOutputStream().write(postDataBytes);

            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder buffer = new StringBuilder();
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                buffer.append(line);
            }

            JSONObject json = new JSONObject(buffer.toString());
            return json.getString("access_token");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}

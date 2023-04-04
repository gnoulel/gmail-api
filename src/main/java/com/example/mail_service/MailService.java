package com.example.mail_service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {
    private static final String user = "me";
    private final GmailConfig gmailConfig;
    private static final File filePath = new File(System.getProperty("user.dir") + "/vs2023031319.xlsx");

    public List<Map<String, String>> getMails() throws Exception {
        String query = "in:inbox " + " subject:(TEST)" + " OR has:attachment AND -codechef AND -edureka)";

        Gmail gmail = gmailConfig.getGmailService();
        Gmail.Users.Messages.List request = gmail.users().messages().list(user).setQ(query).setMaxResults(5L);
        ListMessagesResponse messagesResponse = request.execute();

        request.setPageToken(messagesResponse.getNextPageToken());
        List<Message> messages = messagesResponse.getMessages();

        if (messages == null) {
            return new ArrayList<>();
        }

        List<Map<String, String>> messageList = new ArrayList<>();

        for (Message value : messages) {
            String messageId = value.getId();
            Message message = gmail.users().messages().get(user, messageId).execute();

            String attachmentId = "";
            String fileName = "";

            String mimeType = message.getPayload().getMimeType();
            List<MessagePart> messageParts = message.getPayload().getParts();

            if (messageParts != null && (mimeType.contains("multipart/mixed") || mimeType.contains("multipart/related"))) {
                for (MessagePart messagePart : messageParts) {
                    String content = messagePart.getMimeType();
                    if (content.contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") || content.contains("octet-stream")) {
                        attachmentId = messagePart.getBody().getAttachmentId();
                        fileName = messagePart.getFilename();
                    }

                    if (!"".equals(attachmentId) && !"".equals(fileName) && fileName.contains("vs")) {
                        break;
                    }
                }

                String fileData = gmail.users().messages().attachments().get(user, messageId, attachmentId).execute().getData();
                createFile(fileData, fileName);
            }
            Map<String, String> result = new HashMap<>();
            List<MessagePartHeader> headers = message.getPayload().getHeaders();
            String subject = "";
            String date = "";
            boolean isSubjectFound = false;
            boolean isDateFound = false;
            for (MessagePartHeader header : headers) {
                if (header.getName().equalsIgnoreCase("subject")) {
                    subject = header.getValue();
                    isSubjectFound = true;
                } else if (header.getName().equalsIgnoreCase("date")) {
                    date = header.getValue();
                    isDateFound = true;
                }
                if (isSubjectFound && isDateFound) {
                    break;
                }
            }
            result.put("id", messageId);
            result.put("subject", subject);
            result.put("date", date);
            result.put("msg", message.getSnippet());
            messageList.add(result);
        }

        return messageList;
    }

    public void createFile(String fileData, String fileName) throws IOException {
        String SRC = System.getProperty("user.dir") + "/" + fileName;
        File file = new File(SRC);

        file.getParentFile().mkdirs();
        file.createNewFile();

        try (FileOutputStream fos = new FileOutputStream(file, false)) {
            byte[] decoder = Base64.getDecoder().decode(fileData);
            fos.write(decoder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readFile() {
        try {
            FileInputStream fis = new FileInputStream(filePath);
            XSSFWorkbook wb = new XSSFWorkbook(fis);
            XSSFSheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                Iterator<Cell> cellIterator = row.cellIterator();   //iterating over each column
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_STRING ->    //field that represents string cell type
                                System.out.print(cell.getStringCellValue() + "\t\t\t");
                        case Cell.CELL_TYPE_NUMERIC ->    //field that represents number cell type
                                System.out.print(cell.getNumericCellValue() + "\t\t\t");
                        default -> {
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

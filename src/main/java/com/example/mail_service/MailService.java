package com.example.mail_service;

import com.google.api.client.util.DateTime;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {
    private static final String user = "me";

    public List<MessageReader> getMails(@Nullable String sender, @Nullable String subject) {
        List<MessageReader> messageReaders = new ArrayList<>();

        try {

            Gmail gmailService = GmailConfig.getGmailService();
            ListMessagesResponse listResponse = gmailService.users().messages().list(user).execute();
            if (listResponse != null && listResponse.getMessages() != null) {
                for (Message message : listResponse.getMessages()) {
                    Message msgDetail = gmailService.users().messages().get(user, message.getId()).execute();
                    List<MessagePart> parts = msgDetail.getPayload().getParts();

                    if (parts != null) {
                        List<MessagePartHeader> headers = msgDetail.getPayload().getHeaders();
                        MessagePartBody body = msgDetail.getPayload().getBody();

                        MessageReader msgReader = readParts(parts);
                        msgReader.setMsgId(msgDetail.getId());
                        msgReader.setDate(msgDetail.getInternalDate());
                        for (MessagePartHeader header : headers) {
                            String name = header.getName();
                            if (name.equals("From") || name.equals("from")) {
                                msgReader.setSender(header.getValue());
                                break;
                            }
                        }
                        long startDateMills = DateUtils.atStartOfDay(new Date()).getTime();
                        long endDateMills = DateUtils.atEndOfDay(new Date()).getTime();

                        if (body != null && body.getAttachmentId() != null) {
                            MessagePartBody attachments = gmailService.users().messages().attachments().get(user, message.getId(), body.getAttachmentId()).execute();
                            String temp = "1";
                        }

                        if (msgReader.getSender() != null
                                && msgReader.getSender().contains(sender)
                                && startDateMills < msgDetail.getInternalDate()
                                && msgDetail.getInternalDate() < endDateMills)
                            messageReaders.add(msgReader);
                    }
                }
            }
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
        return messageReaders;
    }


    private MessageReader readParts(List<MessagePart> parts) {
        MessageReader msgReader = new MessageReader();
        int cnt = 0;
        for (MessagePart part : parts) {
            try {
                String mime = part.getMimeType();
                if (mime.contentEquals("text/plain")) {
                    String s = new String(Base64.decodeBase64(part.getBody().getData().getBytes()));
                    msgReader.setText(s);
                } else if (mime.contentEquals("multipart/alternative")) {
                    List<MessagePart> subparts = part.getParts();
                    MessageReader subreader = readParts(subparts);
                    msgReader.setText(subreader.getText());
                } else if (mime.contentEquals("application/octet-stream")) {
                    cnt++;
                    msgReader.setNo_of_files(cnt);
                }

            } catch (Exception e) {
                // get file here
            }
        }
        return msgReader;
    }
}

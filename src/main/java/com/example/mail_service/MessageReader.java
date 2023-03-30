package com.example.mail_service;

import lombok.Data;

@Data
public class MessageReader {
        private String msgId;
        private String subject;
        private String text;
        int no_of_files;
        private String sender;
        private long date;

}
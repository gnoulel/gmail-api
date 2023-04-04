package com.example.mail_service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mail")
@RequiredArgsConstructor
public class MailController {

   @Autowired MailService mailService;

    @GetMapping("")
    public ResponseEntity<?> getMails(String sender, String subject){
        try {
            Map<String, String> response = new HashMap<>();

            List<Map<String, String>> messages = this.mailService.getMails();

            if (messages.isEmpty()) {
                response.put("statusCode", "200");
                response.put("message", "No Emails");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }

            Map<String, List<Map<String, String>>> messageResponse = new HashMap<>();
            messageResponse.put("messages", messages);
            return ResponseEntity.status(HttpStatus.OK).body(messageResponse);
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}

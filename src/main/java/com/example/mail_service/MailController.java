package com.example.mail_service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mail")
@RequiredArgsConstructor
public class MailController {

   @Autowired MailService mailService;

    @GetMapping("")
    public ResponseEntity<?> getMails(String sender, String subject){
        Object result = mailService.getMails(sender, subject);
        return ResponseEntity.ok(AppResponse.success(result));
    }
}

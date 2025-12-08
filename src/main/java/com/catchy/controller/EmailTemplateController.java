package com.catchy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EmailTemplateController {

    @GetMapping("/admin/email-templates")
    public String emailTemplates() {
        return "email-templates";
    }
}

package ai.holo.wdyt.contactus.controller;

import ai.holo.wdyt.contactus.model.dto.ContactUsDto;
import ai.holo.wdyt.contactus.service.ContactUsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contact-us")
public class ContractUsController {

    private final ContactUsService contactUsService;

    public ContractUsController(ContactUsService contactUsService) {
        this.contactUsService = contactUsService;
    }

    @CrossOrigin(origins = "https://thewdyt.com")
    @PostMapping("/post")
    public void contactUs(ContactUsDto contactUsDto) {
        contactUsService.saveContactUs(contactUsDto);
    }
}

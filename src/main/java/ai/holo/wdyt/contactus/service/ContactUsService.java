package ai.holo.wdyt.contactus.service;

import ai.holo.wdyt.contactus.model.dto.ContactUsDto;
import ai.holo.wdyt.contactus.model.entity.ContactUs;
import ai.holo.wdyt.contactus.repository.ContactUsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactUsService {

    private final ContactUsRepository contactUsRepository;

    public ContactUsService(ContactUsRepository contactUsRepository) {
        this.contactUsRepository = contactUsRepository;
    }

    @Transactional
    public void saveContactUs(ContactUsDto contactUsDto) {
        ContactUs contactUs = new ContactUs(contactUsDto.email(), contactUsDto.name(), contactUsDto.subject(), contactUsDto.message());
        contactUsRepository.save(contactUs);
    }
}

package ai.holo.wdyt.contactus.repository;

import ai.holo.wdyt.contactus.model.entity.ContactUs;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactUsRepository extends JpaRepository<ContactUs, Long> {
}

package ai.holo.wdyt.askai.repository;

import ai.holo.wdyt.askai.model.entity.ChatGptPrompt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromptRepository extends JpaRepository<ChatGptPrompt, Long> {

    List<ChatGptPrompt> findAllByActiveTrue();
}

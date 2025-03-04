package ai.holo.wdyt.askai.model.dto;

import ai.holo.wdyt.askai.model.entity.ChatGptPrompt;

public record AiSubmissionPrompt(ChatGptPrompt prompt, String promptText) {
}
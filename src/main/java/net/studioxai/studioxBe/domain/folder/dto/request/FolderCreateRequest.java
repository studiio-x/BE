package net.studioxai.studioxBe.domain.folder.dto;

import jakarta.validation.constraints.NotBlank;

public record FolderCreateRequest(
        @NotBlank
        String name
) {
}

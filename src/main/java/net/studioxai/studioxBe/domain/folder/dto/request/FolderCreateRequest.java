package net.studioxai.studioxBe.domain.folder.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FolderCreateRequest(
        @NotBlank
        String name
) {
}

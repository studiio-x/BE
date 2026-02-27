package net.studioxai.studioxBe.domain.chat.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ConceptSelectRequest(
        @NotNull @Min(0) @Max(3) Integer selectedIndex
) {
}

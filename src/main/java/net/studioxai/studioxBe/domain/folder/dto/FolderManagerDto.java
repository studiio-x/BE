package net.studioxai.studioxBe.domain.folder.dto;

import net.studioxai.studioxBe.domain.folder.entity.enums.Permission;
import net.studioxai.studioxBe.global.annotation.ImageUrl;

public record FolderManagerDto(
    Long userId,
    @ImageUrl String profileUrl,
    String username,
    String email,
    Permission permission
) {
   public static FolderManagerDto create(
           Long userId,
           String profileUrl,
           String username,
           String email,
           Permission permission) {
       return new FolderManagerDto(
               userId,
               profileUrl,
               username,
               email,
               permission
       );
   }
}

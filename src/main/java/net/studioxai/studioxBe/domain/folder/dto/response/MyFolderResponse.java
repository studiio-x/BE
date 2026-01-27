package net.studioxai.studioxBe.domain.folder.dto.response;

import net.studioxai.studioxBe.domain.folder.dto.RootFolderDto;

import java.util.List;

public record MyFolderResponse(
        List<RootFolderDto> myProject,
        List<RootFolderDto> sharedProject
) {
    public static MyFolderResponse create(List<RootFolderDto> myProject, List<RootFolderDto> sharedProject){
        return new MyFolderResponse(myProject, sharedProject);
    }
}

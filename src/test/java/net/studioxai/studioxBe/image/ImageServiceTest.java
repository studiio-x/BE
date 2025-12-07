package net.studioxai.studioxBe.image;

import net.studioxai.studioxBe.domain.folder.entity.Folder;
import net.studioxai.studioxBe.domain.image.entity.Image;
import net.studioxai.studioxBe.domain.image.repository.ImageRepository;
import net.studioxai.studioxBe.domain.image.service.ImageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private ImageService imageService;

    @Test
    @DisplayName("[getImagesByFolder] 폴더와 개수를 받아 이미지 URL 리스트를 반환한다")
    void getImagesByFolder_success() {
        // given
        Folder folder = mock(Folder.class);

        Image img1 = mock(Image.class);
        Image img2 = mock(Image.class);

        given(img1.getImageUrl()).willReturn("url1");
        given(img2.getImageUrl()).willReturn("url2");

        given(imageRepository.findByFolderOrderByCreatedAt(eq(folder), any(Pageable.class)))
                .willReturn(List.of(img1, img2));

        int count = 2;

        // when
        List<String> result = imageService.getImagesByFolder(folder, count);

        // then
        assertThat(result).containsExactly("url1", "url2");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(imageRepository).findByFolderOrderByCreatedAt(eq(folder), pageableCaptor.capture());

        Pageable usedPageable = pageableCaptor.getValue();
        assertThat(usedPageable.getPageNumber()).isEqualTo(0);
        assertThat(usedPageable.getPageSize()).isEqualTo(count);
    }

    @Test
    @DisplayName("[getImagesByFolders] 여러 폴더에 대한 이미지를 폴더별로 그룹핑하고 각 폴더당 최대 개수만큼만 반환한다")
    void getImagesByFolders_groupAndLimit() {
        // given
        Folder folder1 = mock(Folder.class);
        Folder folder2 = mock(Folder.class);

        given(folder1.getId()).willReturn(1L);
        given(folder2.getId()).willReturn(2L);

        List<Folder> folders = List.of(folder1, folder2);

        Image img1 = mock(Image.class);
        Image img2 = mock(Image.class);
        Image img3 = mock(Image.class);
        Image img4 = mock(Image.class);

        given(img1.getFolder()).willReturn(folder1);
        given(img2.getFolder()).willReturn(folder1);
        given(img3.getFolder()).willReturn(folder1);
        given(img4.getFolder()).willReturn(folder2);

        given(img1.getImageUrl()).willReturn("f1_img1");
        given(img2.getImageUrl()).willReturn("f1_img2");
        given(img4.getImageUrl()).willReturn("f2_img1");

        given(imageRepository.findByFolderInOrderByFolderIdAndCreatedAtDesc(folders))
                .willReturn(List.of(img1, img2, img3, img4));

        int count = 2;
        // when
        Map<Long, List<String>> result = imageService.getImagesByFolders(folders, count);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(1L)).containsExactly("f1_img1", "f1_img2"); // 3개 중 앞 2개만
        assertThat(result.get(2L)).containsExactly("f2_img1");            // 1개 뿐이므로 그대로

        verify(imageRepository).findByFolderInOrderByFolderIdAndCreatedAtDesc(folders);
    }

    @Test
    @DisplayName("[getImagesByFolders] 이미지가 하나도 없으면 빈 Map을 반환한다")
    void getImagesByFolders_empty() {
        // given
        List<Folder> folders = List.of();
        given(imageRepository.findByFolderInOrderByFolderIdAndCreatedAtDesc(folders))
                .willReturn(List.of());

        // when
        Map<Long, List<String>> result = imageService.getImagesByFolders(folders, 3);

        // then
        assertThat(result).isEmpty();
    }
}
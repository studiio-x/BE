package net.studioxai.studioxBe.domain.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.studioxai.studioxBe.domain.chat.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageCleanupService {

    private final ChatMessageRepository chatMessageRepository;

    @Value("${chat.retention-days:30}")
    private int retentionDays;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldMessages() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        int deleted = chatMessageRepository.deleteOlderThan(cutoff);
        if (deleted > 0) {
            log.info("Cleaned up {} chat messages older than {} days", deleted, retentionDays);
        }
    }
}

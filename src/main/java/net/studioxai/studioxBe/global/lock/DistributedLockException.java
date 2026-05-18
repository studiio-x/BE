package net.studioxai.studioxBe.global.lock;

import lombok.Getter;

@Getter
public class DistributedLockException extends RuntimeException {

    public DistributedLockException(String message) {
        super(message);
    }
}

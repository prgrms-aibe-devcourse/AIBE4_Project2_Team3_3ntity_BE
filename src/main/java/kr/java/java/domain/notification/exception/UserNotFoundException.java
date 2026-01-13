package kr.java.java.domain.notification.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long userID) {
        super("[Error] 유저를 찾을 수 없습니다. 유저 ID: " + userID);
    }

    public UserNotFoundException(UUID uuid) {
        super("[Error] 유저를 찾을 수 없습니다. 유저 UUID: " + uuid);
    }
}

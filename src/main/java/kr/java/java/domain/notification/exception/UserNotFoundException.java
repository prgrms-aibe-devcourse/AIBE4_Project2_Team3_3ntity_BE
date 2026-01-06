package kr.java.java.domain.notification.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long userID) {
        super("[Error] 유저를 찾을 수 없습니다. 유저 ID: " + userID);
    }
}

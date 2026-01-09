package kr.java.java.domain.comment.exception;

public class CommentAccessDeniedException extends RuntimeException {
    public CommentAccessDeniedException(String message) {
        super(message);
    }
}

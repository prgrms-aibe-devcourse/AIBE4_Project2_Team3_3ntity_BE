package kr.java.java.domain.comment.exception;

public class CommentTargetMissingException extends RuntimeException {
    public CommentTargetMissingException(String message) {
        super(message);
    }
}

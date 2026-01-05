package kr.java.java.domain.review.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewCreateRequest {

    private Long matchingId; // 어떤 매칭(이용내역)에 대한 리뷰인지
    private Long userId;     // 작성자 ID
    private Integer rating;  // 별점 (1~5)
    private String content;  // 리뷰 내용
}

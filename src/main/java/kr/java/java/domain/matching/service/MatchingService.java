package kr.java.java.domain.matching.service;

import kr.java.java.domain.matching.dto.MatchingRequest;
import kr.java.java.domain.matching.entity.Matching;
import kr.java.java.domain.matching.enums.MatchStatus;
import kr.java.java.domain.matching.repository.MatchingRepository;
import kr.java.java.domain.space.entity.Space;
import kr.java.java.domain.space.repository.SpaceRepository;
import kr.java.java.domain.user.entity.User;
import kr.java.java.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchingService {
    private final UserRepository userRepository;
    private final SpaceRepository spaceRepository;
    private final MatchingRepository matchingRepository;

    public void createMatching(MatchingRequest request, Long loginUserId){
        Space targetSpace = spaceRepository.findById(request.spaceId())
                .orElseThrow(() -> new IllegalArgumentException("Space를 찾을 수 없습니다."));
        User targetUser = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("User를 찾을 수 없습니다."));
        User loginUser = userRepository.findById(loginUserId)
                .orElseThrow(() -> new IllegalArgumentException("User를 찾을 수 없습니다."));

        User sender = loginUser;
        User receiver;

        if(loginUser.getId().equals(targetUser.getId())){
            receiver = targetSpace.getUser();
        } else{
            receiver = loginUser;
        }

        validateMatching(targetSpace, targetUser, sender, receiver);

        Matching matching = Matching.builder()
                .sender(sender)
                .receiver(receiver)
                .space(targetSpace)
                .user(targetUser)
                .message(request.message())
                .build();

        matchingRepository.save(matching);
    }

    private void validateMatching(Space space, User targetUser, User sender, User receiver){
        if(sender.getId().equals(receiver.getId())){
            throw new IllegalArgumentException("본인과의 매칭은 진행할 수 없습니다.");
        }

        List<MatchStatus> activeStatuses = List.of(MatchStatus.WAITING, MatchStatus.ONGOING);

        boolean alreadyActive = matchingRepository.existsBySpaceAndUserAndStatusIn(space, targetUser, activeStatuses);

        if(alreadyActive){
            throw new IllegalStateException("이미 대기 중기거나 진행 중인 매칭이 존재합니다.");
        }
    }
}

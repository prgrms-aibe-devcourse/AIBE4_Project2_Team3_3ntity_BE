package kr.java.java.domain.space.service;

import kr.java.java.domain.space.dto.SpaceRequest;
import kr.java.java.domain.space.entity.Space;
import kr.java.java.domain.space.exception.DuplicateSpaceException;
import kr.java.java.domain.space.repository.SpaceRepository;
import kr.java.java.domain.user.entity.User;
import kr.java.java.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SpaceService {
    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository;
    public void createSpace(SpaceRequest spaceRequest, Long loginUserId) {
        //TODO 로그인 유저 권한 체크하는 부분 추가 예정
        if (spaceRepository.existsByAddressAndDetailAddress(spaceRequest.address(), spaceRequest.detailAddress())) {
            log.error("동일한 공간이 존재합니다");
            throw new DuplicateSpaceException("동일한 공간이 존해합니다.");
        }
        User user = userRepository.getReferenceById(loginUserId);
        Space space = spaceRequest.toEntity(user);
        spaceRepository.save(space);
    }

}

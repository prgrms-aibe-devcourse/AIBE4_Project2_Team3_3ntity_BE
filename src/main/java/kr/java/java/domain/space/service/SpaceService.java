package kr.java.java.domain.space.service;

import kr.java.java.domain.space.dto.SpaceListResponse;
import kr.java.java.domain.space.dto.SpaceRequest;
import kr.java.java.domain.space.dto.SpaceResponse;
import kr.java.java.domain.space.dto.SpaceUpdateRequest;
import kr.java.java.domain.space.entity.Space;
import kr.java.java.domain.space.exception.DuplicateSpaceException;
import kr.java.java.domain.space.exception.NotFoundSpaceException;
import kr.java.java.domain.space.exception.NotFoundUserException;
import kr.java.java.domain.space.exception.UnAuthorizedException;
import kr.java.java.domain.space.repository.SpaceRepository;
import kr.java.java.domain.user.entity.User;
import kr.java.java.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SpaceService {
    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createSpace(SpaceRequest spaceRequest, Long loginUserId) {
        //TODO 로그인 유저 권한 체크하는 부분 추가 예정
        if (spaceRepository.existsByAddressAndDetailAddress(spaceRequest.address(), spaceRequest.detailAddress())) {
            log.error("동일한 공간이 존재합니다");
            throw new DuplicateSpaceException("동일한 공간이 존해합니다.");
        }
        User user = userRepository.getReferenceById(loginUserId);
        Space space = spaceRequest.toEntity(user);
        spaceRepository.save(space);

        // TODO 권한 업그레이드 하는 부분 추가 예정
    }

    @Transactional(readOnly = true)
    public SpaceResponse getSpace(Long id) {
        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new NotFoundSpaceException("해당 공간이 없습니다. id=" + id));
        return new SpaceResponse(space);
    }

    @Transactional(readOnly = true)
    public List<SpaceListResponse> getAllSpaces(){
        return spaceRepository.findAllByOrderByIdDesc().stream()
                .map(SpaceListResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SpaceListResponse> getSpacesByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            //TODO 나중에 유저에서 커스텀예외가 생기면 예외를 변경할 예정
            throw new NotFoundUserException("존재하지 않는 유저입니다. ID: " + userId);
        }
        return spaceRepository.findByUserIdOrderByIdDesc(userId).stream()
                .map(SpaceListResponse::new)
                .toList();
    }

    @Transactional
    public void deleteSpace(Long id, Long userId) {

        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new NotFoundSpaceException("해당 공간이 없습니다. id=" + id));

        if (!space.getUser().getId().equals(userId)) {
            throw new UnAuthorizedException("삭제 권한이 없습니다.");
        }

        try {
            spaceRepository.delete(space);

        } catch (DataIntegrityViolationException e) {
            log.error("공간 삭제 실패 (참조 데이터 존재) - ID: {}", id);
            throw new RuntimeException("현재 예약 내역이 있어 삭제할 수 없습니다.");
        }
    }

    @Transactional
    public SpaceResponse updateSpace(Long id, SpaceUpdateRequest request, Long userId) {
        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new NotFoundSpaceException("해당 공간이 없습니다. id=" + id));

        if (!space.getUser().getId().equals(userId)) {
            throw new UnAuthorizedException("수정 권한이 없습니다.");
        }

        space.update(request);
        return new SpaceResponse(space);
    }
}

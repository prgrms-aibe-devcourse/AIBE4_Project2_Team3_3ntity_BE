package kr.java.java.domain.space.repository;

import kr.java.java.domain.space.dto.SpaceSearchCondition;
import kr.java.java.domain.space.entity.Space;

import java.util.List;

public interface SpaceRepositoryCustom {
    List<Space> search(SpaceSearchCondition condition);
}
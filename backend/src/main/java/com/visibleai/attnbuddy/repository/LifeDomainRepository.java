package com.visibleai.attnbuddy.repository;

import com.visibleai.attnbuddy.model.LifeDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LifeDomainRepository extends JpaRepository<LifeDomain, Long> {

    List<LifeDomain> findByUserIdOrderByPositionAsc(Long userId);
}

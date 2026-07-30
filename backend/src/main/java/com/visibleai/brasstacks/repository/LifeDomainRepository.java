package com.visibleai.brasstacks.repository;

import com.visibleai.brasstacks.model.LifeDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LifeDomainRepository extends JpaRepository<LifeDomain, Long> {

    List<LifeDomain> findByUserIdOrderByPositionAsc(Long userId);
}

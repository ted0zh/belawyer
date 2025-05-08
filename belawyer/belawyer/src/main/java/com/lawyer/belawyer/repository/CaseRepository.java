package com.lawyer.belawyer.repository;

import com.lawyer.belawyer.data.entity.Case;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaseRepository extends JpaRepository<Case,Long> {
    Optional<Case> findById(Long id);
    List<Case> findByUserIsNull();
}

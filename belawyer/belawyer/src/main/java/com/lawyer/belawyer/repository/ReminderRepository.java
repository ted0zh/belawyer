package com.lawyer.belawyer.repository;

import com.lawyer.belawyer.data.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder,Long> {
    //List<Reminder> findByUserId(Long id);
}

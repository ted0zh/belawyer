//package com.lawyer.belawyer.repository;
//
//import com.lawyer.belawyer.data.entity.Reminder;
//import com.lawyer.belawyer.data.entity.User;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//public interface ReminderRepository extends JpaRepository<Reminder, Long> {
//
//    // 1) Стандартен метод за всички напомняния на даден User
//    List<Reminder> findByUser(User user);
//
//    // 2) Сложна заявка, която връща напомняния с "reminderDate + reminderTime" между два момента
//    @Query("SELECT r FROM Reminder r WHERE " +
//            "FUNCTION('concat', r.reminderDate, 'T', r.reminderTime) >= :fromDateTime " +
//            "AND FUNCTION('concat', r.reminderDate, 'T', r.reminderTime) < :toDateTime " +
//            "AND r.sent = false")
//    List<Reminder> findPendingBetween(
//            @Param("fromDateTime") LocalDateTime fromDateTime,
//            @Param("toDateTime") LocalDateTime toDateTime
//    );
//}
package com.lawyer.belawyer.repository;

import com.lawyer.belawyer.data.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    @Query("""
        SELECT r
        FROM Reminder r
        WHERE r.sent = false
          AND r.reminderDate = :date
          AND r.reminderTime BETWEEN :timeStart AND :timeEnd
    """)
    List<Reminder> findPendingReminders(
            @Param("date") LocalDate date,
            @Param("timeStart") LocalTime timeStart,
            @Param("timeEnd") LocalTime timeEnd
    );
}


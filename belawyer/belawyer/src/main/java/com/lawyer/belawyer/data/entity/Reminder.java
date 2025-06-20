//package com.lawyer.belawyer.data.entity;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import jakarta.persistence.*;
//import lombok.Data;
//
//import java.time.LocalDate;
//import java.time.LocalTime;
//
//@Entity
//@Table(name = "reminders")
//@Data
//public class Reminder {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String title;
//    private LocalDate reminderDate;
//    private LocalTime reminderTime;
//
//    private boolean sent = false; // дали вече е изпратено имейл-напомнянето
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "case_id")
//    @JsonIgnore
//    private Case caseEntity;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id")
//    @JsonIgnore
//    private User user;
//}
package com.lawyer.belawyer.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "reminders")
@Data
public class Reminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private LocalDate reminderDate;

    private LocalTime reminderTime;

    private boolean sent = false;

    @ManyToOne
    @JoinColumn(name = "case_id")
    @JsonIgnore
    private Case caseEntity;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
}

//package com.lawyer.belawyer.data.dto;
//
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//import java.time.LocalDate;
//import java.time.LocalTime;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//public class ReminderDto {
//    private Long id;               // ще се връща при GET и при create (response)
//    private String title;
//    private LocalDate reminderDate;
//    private LocalTime reminderTime;
//    private Long caseId;           // към кое дело е това напомняне
//}
package com.lawyer.belawyer.data.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReminderDto {
    private String title;
    private LocalDate reminderDate;
    private LocalTime reminderTime;
    private String targetUsername;
   }


//
//package com.lawyer.belawyer.config;
//
//import com.lawyer.belawyer.data.entity.Reminder;
//import com.lawyer.belawyer.repository.ReminderRepository;
//import com.lawyer.belawyer.service.serviceImpl.EmailServiceImpl;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.util.List;
//
//@Configuration
//@EnableScheduling
//public class ReminderSchedulerConfig {
//
//    private final ReminderRepository reminderRepository;
//    private final EmailServiceImpl emailService;
//
//    @Autowired
//    public ReminderSchedulerConfig(
//            ReminderRepository reminderRepository,
//            EmailServiceImpl emailService
//    ) {
//        this.reminderRepository = reminderRepository;
//        this.emailService = emailService;
//    }
//
//
//    //@Scheduled(fixedRateString = "${reminder.check.interval:300000}")
//    public void checkAndSendRemindersOneHourBefore() {
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime oneHourLater = now.plusHours(1);
//
//        LocalDate datePart  = oneHourLater.toLocalDate();
//        LocalTime startTime = oneHourLater.toLocalTime();
//        LocalTime  endTime   = oneHourLater.plusMinutes(5).toLocalTime();
//
//
//        List<Reminder> toSend = reminderRepository.findPendingReminders(datePart, startTime, endTime);
//        System.out.println("Заявка findPendingReminders върна " + toSend.size() + " ред(а).");
//        for (Reminder r : toSend) {
//            try {
//                String to = r.getUser().getEmail();
//                String subject = "Напомняне: " + r.getTitle();
//                String body = String.format(
//                        "Здравейте, %s!\n\n" +
//                                "Това е вашето напомняне 1 час преди:\n\n" +
//                                "  • Заглавие: %s\n" +
//                                "  • Дата: %s\n" +
//                                "  • Час: %s\n" +
//                                "  • Дело №: %s\n\n" +
//                                "Успех!\nЕкипът на belawyer",
//                        r.getUser().getUsername(),
//                        r.getTitle(),
//                        r.getReminderDate(),
//                        r.getReminderTime(),
//                        (r.getCaseEntity() != null ? r.getCaseEntity().getId() : "–")
//                );
//
//                emailService.sendSimpleMessage(to, subject, body);
//
//                r.setSent(true);
//                reminderRepository.save(r);
//
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//    }
//}
//

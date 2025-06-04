//package com.lawyer.belawyer.service.serviceImpl;
//
//import com.lawyer.belawyer.data.entity.Reminder;
//import com.lawyer.belawyer.repository.ReminderRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.TaskScheduler;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//
//@Service
//public class ReminderSchedulerService {
//
//    private final TaskScheduler scheduler;
//    private final EmailServiceImpl emailService;
//    private final ReminderRepository reminderRepository;
//
//    @Autowired
//    public ReminderSchedulerService(TaskScheduler scheduler,
//                                    EmailServiceImpl emailService,
//                                    ReminderRepository reminderRepository) {
//        this.scheduler = scheduler;
//        this.emailService = emailService;
//        this.reminderRepository = reminderRepository;
//    }
//
//
//    public void scheduleEmail(Reminder reminder) {
//        LocalDateTime dateTime = reminder.getReminderDate().atTime(reminder.getReminderTime());
//        long delay = java.time.Duration.between(LocalDateTime.now(), dateTime).toMillis();
//
//        if (delay <= 0) {
//            return;
//        }
//
//        scheduler.schedule(() -> {
//            try {
//                String to = reminder.getUser().getEmail();
//                String subj = "Напомняне: " + reminder.getTitle();
//                String body = String.format("Наближава вашето напомняне \"%s\" за дело #%d на %s %s.",
//                        reminder.getTitle(),
//                        reminder.getCaseEntity().getId(),
//                        reminder.getReminderDate(),
//                        reminder.getReminderTime());
//
//                emailService.sendSimpleMessage(to, subj, body);
//
//                reminder.setSent(true);
//                reminderRepository.save(reminder);
//
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }, java.util.Date.from(dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()));
//    }
//}
//

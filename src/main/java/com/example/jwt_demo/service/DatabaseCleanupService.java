package com.example.jwt_demo.service;

import com.example.jwt_demo.repository.UserRepository;

import com.example.jwt_demo.model.User;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Service
public class DatabaseCleanupService {
	
	private final EmailService emailService;
    private final UserRepository userRepository;
    private final ExecutorService emailExecutor;
    
    @Autowired
    public DatabaseCleanupService(
        EmailService emailService,
        UserRepository userRepository,
        @Qualifier("emailExecutor") ExecutorService emailExecutor
    ) {
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.emailExecutor = emailExecutor;
    }

    @Scheduled(cron = "0 0 0 */3 * *")
    @Transactional
    @Async
    public void deleteUnverifiedAccounts() {
		System.out.println("[Scheduled Task][DELETE] Checking for expired unverified users...");
		LocalDateTime now = LocalDateTime.now();
	    LocalDateTime deletionThreshold = now.minusMinutes(3);
	    List<User> usersToDelete = userRepository.findAllByEmailVerifiedFalseAndDateCreatedBefore(deletionThreshold);
	    for (User user : usersToDelete) {
	        System.out.println("Deleting unverified account: " + user.getEmail());
	        userRepository.delete(user);
	    }
    }
	
    @Scheduled(cron = "0 0 0 * * *")
    @Async
    public void warnUnverifiedAccounts() {
		System.out.println("[Scheduled Task][WARN] Checking for unverified users...");
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime warningThreshold = now.minusMinutes(1);
		LocalDateTime deletionThreshold = now.minusMinutes(3);
		List<User> usersToWarn = userRepository.findAllByEmailVerifiedFalseAndDateCreatedBetween(deletionThreshold, warningThreshold);
		
		int chunkSize = 100;
        for (int i = 0; i < usersToWarn.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, usersToWarn.size());
            List<User> chunk = usersToWarn.subList(i, end);

            List<Callable<Void>> tasks = new ArrayList<>();
            for (User user : chunk) {
                tasks.add(() -> {
                	try {
                		System.out.println("Sending verification reminder to: " + user.getEmail());
            	        long daysLeft = Math.max(0, 3 - ChronoUnit.MINUTES.between(user.getDateCreated(), now));
            	        emailService.sendVerificationReminderEmail(user.getEmail(), daysLeft);
					} catch (Exception e) {
						System.err.println("Failed to send email to " + user.getEmail() + ": " + e.getMessage());
					}
                	return null;
                });
            }
            try {
            	List<Future<Void>> futures = emailExecutor.invokeAll(tasks, 1, TimeUnit.MINUTES);
                for (int j = 0; j < futures.size(); j++) {
                    Future<Void> future = futures.get(j);
                    if (!future.isDone()) {
                        User user = chunk.get(j);
                        System.err.println("Email to " + user.getEmail() + " did not complete in time.");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Email sending interrupted: " + e.getMessage());
            }
        }
	}
}

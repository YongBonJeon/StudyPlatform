package com.studyolle.event;

import com.studyolle.domain.Account;
import com.studyolle.domain.Enrollment;
import com.studyolle.domain.Event;
import com.studyolle.domain.Study;
import com.studyolle.event.form.EventForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EnrollmentRepository enrollmentRentRepository;

    public Event createEvent(Study study, Event event, Account account) {
        event.setStudy(study);
        event.setCreatedBy(account);
        event.setCreateDateTime(LocalDateTime.now());

        return eventRepository.save(event);
    }

    public void updateEvent(Event event, EventForm eventForm) {
        modelMapper.map(eventForm, event);
        event.acceptWaitingList();
    }

    public void deleteEvent(Event event) {
        eventRepository.delete(event);
    }

    public void newEnrollment(Event event, Account account) {
        if(!enrollmentRentRepository.existsByEventAndAccount(event, account)) {
            Enrollment enrollment = new Enrollment();
            enrollment.setEnrolledAt(LocalDateTime.now());
            enrollment.setAccepted(event.isAbleToAcceptWaitingEnrollment());
            enrollment.setAccount(account);
            enrollment.setEvent(event);
            enrollmentRentRepository.save(enrollment);
        }
    }

    public void cancelEnrollment(Event event, Account account) {
        Enrollment enrollment = enrollmentRentRepository.findByEventAndAccount(event, account);
        event.removeEnrollment(enrollment);
        enrollmentRentRepository.delete(enrollment);

        if(event.isAbleToAcceptWaitingEnrollment()) {
            Enrollment enrollmentToAccept = event.getTheFirstWaitingEnrollment();
            if(enrollmentToAccept != null) {
                enrollmentToAccept.setAccepted(true);
            }
        }
    }
}

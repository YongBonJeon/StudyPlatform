package com.studyolle.event.validator;

import com.studyolle.domain.Event;
import com.studyolle.event.form.EventForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;

@Component
public class EventValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return EventForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, org.springframework.validation.Errors errors) {
        EventForm eventForm = (EventForm) target;
        if (isNotValidEndEnrollmentDateTime(eventForm)) {
            errors.rejectValue("endEnrollmentDateTime", "wrong.datetime", "모임 접수 종료 일시를 모임 시작 일시보다 빠르게 설정할 수 없습니다.");
        }
        if (isNotValidEndDateTime(eventForm)) {
            errors.rejectValue("endDateTime", "wrong.datetime", "모임 종료 일시를 정확히 입력하세요.");
        }
        if(isNotValidStartDateTime(eventForm)) {
            errors.rejectValue("startDateTime", "wrong.datetime", "모임 시작 일시를 정확히 입력하세요.");
        }
    }

    private boolean isNotValidStartDateTime(EventForm eventForm) {
        return eventForm.getStartDateTime().isBefore(eventForm.getEndEnrollmentDateTime());
    }

    private static boolean isNotValidEndEnrollmentDateTime(EventForm eventForm) {
        return eventForm.getEndEnrollmentDateTime().isBefore(LocalDateTime.now());
    }

    private static boolean isNotValidEndDateTime(EventForm eventForm) {
        return eventForm.getEndDateTime().isBefore(eventForm.getStartDateTime()) || eventForm.getEndDateTime().isBefore(eventForm.getEndEnrollmentDateTime());
    }

    public void validateUpdateForm(EventForm eventForm, Event event, Errors errors) {
        if(eventForm.getLimitOfEnrollments() < event.getNumberOfAcceptedEnrollments()) {
            errors.rejectValue("limitOfEnrollments", "wrong.value", "확인된 참가 신청보다 모집 인원 수를 더 낮게 설정할 수 없습니다.");
        }
    }
}

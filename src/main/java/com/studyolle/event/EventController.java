package com.studyolle.event;

import com.studyolle.account.CurrentUser;
import com.studyolle.domain.Account;
import com.studyolle.domain.Event;
import com.studyolle.domain.Study;
import com.studyolle.event.form.EventForm;
import com.studyolle.event.validator.EventValidator;
import com.studyolle.study.StudyRepository;
import com.studyolle.study.StudyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/study/{path}")
@RequiredArgsConstructor
public class EventController {

    private final StudyService studyService;
    private final EventService eventService;
    private final StudyRepository studyRepository;
    private final EventRepository eventRepository;

    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;

    @InitBinder("eventForm")
    public void eventFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(eventValidator);
    }


    @GetMapping("/new-event")
    public String newEventForm(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(new EventForm());
        return "event/form";
    }

    @PostMapping("/new-event")
    public String newEventSubmit(@CurrentUser Account account, @PathVariable String path, @Valid EventForm eventForm, Errors errors, Model model) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            return "event/form";
        }
        Event event = eventService.createEvent(study, modelMapper.map(eventForm, Event.class), account);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{id}")
    public String getEvent(@CurrentUser Account account, @PathVariable String path, @PathVariable Long id, Model model) {
        model.addAttribute(account);
        model.addAttribute(studyRepository.findStudyWithManagersByPath(path));
        model.addAttribute(eventRepository.findById(id).orElseThrow());
        return "event/view";
    }

    @GetMapping("/events")
    public String viewStudyEvents(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudy(path);
        model.addAttribute(account);
        model.addAttribute(study);

        List<Event> events = eventRepository.findByStudyOrderByStartDateTime(study);
        List<Event> newEvents = new ArrayList<>();
        List<Event> oldEvents = new ArrayList<>();
        events.forEach(e -> {
            if (e.getEndDateTime().isBefore(LocalDateTime.now())) {
                oldEvents.add(e);
            } else {
                newEvents.add(e);
            }
        });

        model.addAttribute("newEvents", newEvents);
        model.addAttribute("oldEvents", oldEvents);

        return "study/events";
    }

    @GetMapping("/events/{id}/edit")
    public String updateEventForm(@CurrentUser Account account, @PathVariable String path, @PathVariable("id") Event event, Model model) {
        model.addAttribute(account);
        model.addAttribute(event);
        model.addAttribute(studyService.getStudyToUpdate(account, path));
        model.addAttribute(modelMapper.map(event, EventForm.class));
        return "event/update-form";
    }

    @PostMapping("/events/{id}/edit")
    public String updateEventSubmit(@CurrentUser Account account, @PathVariable String path,
                                    @PathVariable("id") Event event, @Valid EventForm eventForm, Errors errors,
                                    Model model) {
        Study study = studyService.getStudyToUpdate(account, path);
        eventForm.setEventType(event.getEventType());
        eventValidator.validateUpdateForm(eventForm, event, errors);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute(event);
            return "event/update-form";
        }

        eventService.updateEvent(event, eventForm);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }

    @PostMapping("/events/{id}/delete")
    public String cancelEvent(@CurrentUser Account account, @PathVariable String path, @PathVariable("id") Event event) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        eventService.deleteEvent(event);
        return "redirect:/study/" + study.getEncodedPath() + "/events";
    }

    @PostMapping("/events/{id}/enroll")
    public String newEnrollment(@CurrentUser Account account, @PathVariable String path, @PathVariable("id") Event event) {
        Study study = studyService.getStudyToEnroll(path);
        eventService.newEnrollment(event, account);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }

    @PostMapping("/events/{id}/disenroll")
    public String cancelEnrollment(@CurrentUser Account account, @PathVariable String path, @PathVariable("id") Event event) {
        Study study = studyService.getStudyToEnroll(path);
        eventService.cancelEnrollment(event, account);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }
}


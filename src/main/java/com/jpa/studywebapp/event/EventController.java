package com.jpa.studywebapp.event;

import com.jpa.studywebapp.account.CurrentUser;
import com.jpa.studywebapp.domain.Account;
import com.jpa.studywebapp.domain.Event;
import com.jpa.studywebapp.domain.Study;
import com.jpa.studywebapp.event.form.EventForm;
import com.jpa.studywebapp.event.validator.EventValidator;
import com.jpa.studywebapp.study.StudyService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.internal.Errors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/study/{path}")
@RequiredArgsConstructor
public class EventController {

    private final StudyService studyService;
    private final EventService eventService;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;
    private final EventRepository eventRepository;

    @InitBinder("eventForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(eventValidator);
    }

    //모임 뷰 조회
    @GetMapping("/new-event")
    public String newEventForm(@CurrentUser Account account, Model model, @PathVariable String path){
        Study study = studyService.getStudyToUpdateStatus(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(new EventForm());
        return "event/form";
    }

    //모임 만들기 submit
    @PostMapping("/new-event")
    public String submitEventForm(@CurrentUser Account account, Model model, @PathVariable String path,
                                  @Valid EventForm eventForm, Errors errors) throws UnsupportedEncodingException {

        Study study = studyService.getStudyToUpdateStatus(account, path);

        if(errors.hasErrors()){
            model.addAttribute(study);
            model.addAttribute(account);
            return "event/form";
        }

        Event event = eventService.updateEvent(account, study, modelMapper.map(eventForm, Event.class));
        return "redirect:/study/" + study.getURLEncoder(path) + "/events/" + event.getId();
    }

    //만들어진 모임 조회
    @GetMapping("/events/{id}")
    public String getEventById(@CurrentUser Account account,@PathVariable String path, Model model, @PathVariable Long id){
        model.addAttribute(account);
        model.addAttribute(eventRepository.findById(id).orElseThrow(NoSuchElementException::new));
        model.addAttribute(studyService.getStudy(path));

        return "event/view";
    }

    @GetMapping("/events")
    public String viewEventList(@CurrentUser Account account, Model model, @PathVariable String path){
        //해당 경로가 맞는지 체크, 매니저가 아님
        Study study = studyService.getStudy(path);
        model.addAttribute(account);
        model.addAttribute(study);

        //이벤트를 종료시간으로 구분
        // 1. 현재시간보다 일찍 종료되었으면 옛날
        // 2. 그렇지 않으면 최신
        List<Event> eventList = eventRepository.findByStudyOrderByStartDateTime(study);
        List<Event> newEvents = new ArrayList<>();
        List<Event> oldEvents = new ArrayList<>();

        for(Event event : eventList){
            if(event.getEndDateTime().isBefore(LocalDateTime.now())){
                oldEvents.add(event);
            }else{
                newEvents.add(event);
            }
        }

        model.addAttribute("newEvents", newEvents);
        model.addAttribute("oldEvents", oldEvents);
        return "study/events";
    }
}

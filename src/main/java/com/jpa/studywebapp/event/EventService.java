package com.jpa.studywebapp.event;

import com.jpa.studywebapp.domain.Account;
import com.jpa.studywebapp.domain.Event;
import com.jpa.studywebapp.domain.Study;
import com.jpa.studywebapp.event.form.EventForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;

    public Event updateEvent(Account account, Study study, Event event) {
        event.setCreatedBy(account);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setStudy(study);
        eventRepository.save(event);
        return event;
    }

    public void updateEvent(Event event, EventForm eventForm) {
        modelMapper.map(eventForm, event);
    }
}

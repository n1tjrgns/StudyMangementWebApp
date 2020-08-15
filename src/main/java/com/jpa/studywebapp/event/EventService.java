package com.jpa.studywebapp.event;

import com.jpa.studywebapp.domain.Account;
import com.jpa.studywebapp.domain.Event;
import com.jpa.studywebapp.domain.Study;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public Event updateEvent(Account account, Study study, Event event) {
        event.setCreatedBy(account);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setStudy(study);
        eventRepository.save(event);
        return event;
    }
}

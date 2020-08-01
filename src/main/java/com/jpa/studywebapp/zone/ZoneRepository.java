package com.jpa.studywebapp.zone;


import com.jpa.studywebapp.domain.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface ZoneRepository extends JpaRepository<Zone, Long> {

    Zone findByLocalNameCity(String localNameCity);
}

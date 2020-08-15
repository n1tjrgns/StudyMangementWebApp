package com.jpa.studywebapp.study;

import com.jpa.studywebapp.domain.Account;
import com.jpa.studywebapp.domain.Study;
import com.jpa.studywebapp.domain.Tag;
import com.jpa.studywebapp.domain.Zone;
import com.jpa.studywebapp.study.event.StudyUpdateEvent;
import com.jpa.studywebapp.study.form.StudyDescriptionForm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.jpa.studywebapp.study.form.StudyForm.PATH_REGEXP;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyService {

    public final StudyRepository studyRepository;
    public final ModelMapper modelMapper;
    public final ApplicationEventPublisher applicationEventPublisher;

    public Study createStudy(Study study, Account account) {
        Study newStudy = studyRepository.save(study);
        newStudy.addManager(account);
        return newStudy;
    }

    public Study getStudyToUpdate(Account account, String path) {
        Study study = this.getStudy(path); //스터디가 존재하는지 확인
        checkIfManager(account, study); // 매니저 권한인지 확인

        return study;
    }

    public void checkIfManager(Account account, Study study) {
        if(!study.isManagedBy(account)){
            throw new AccessDeniedException("권한이 없습니다.");
        }
    }

    public Study getStudy(String path) {
        Study study = studyRepository.findByPath(path);
        checkIfExistingStudy(path, study);

        return study;
    }

    public void checkIfExistingStudy(String path, Study study) {
        if(study == null){
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
    }

    public void updateStudyDescription(Study study, StudyDescriptionForm studyDescriptionForm) {
        modelMapper.map(studyDescriptionForm, study);
        applicationEventPublisher.publishEvent(new StudyUpdateEvent(study,"스터디 소개를 수정했습니다."));
    }

    public void updateStudyImage(Study study, String image) {
        study.setImage(image);
    }

    public void enableStudyBanner(Study study){
        study.setUseBanner(true);
    }

    public void disableStudyBanner(Study study){
        study.setUseBanner(false);
    }

    public void removeTag(Study study, Tag tag) {
        study.getTags().remove(tag);
    }

    public void addTag(Study study, Tag tag) {
        study.getTags().add(tag);
    }

    public void addZone(Study study, Zone zone) {
        study.getZones().add(zone);
    }

    public void removeZone(Study study, Zone zone) {
        study.getZones().remove(zone);
    }

    public Study getStudyToUpdateTag(Account account, String path) {
        Study study = studyRepository.findStudyWithTagsByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account, study);
        return study;
    }

    public Study getStudyToUpdateZone(Account account, String path) {
        Study study = studyRepository.findStudyWithZonesByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account, study);
        return study;
    }

    public Study getStudyToUpdateStatus(Account account, String path) {
        Study study = studyRepository.findStudyWithManagersByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account, study);
        return study;
    }

    public void publish(Study study) {
        study.publish();
    }

    public void close(Study study) {
        study.close();
    }

    public void recruitStart(Study study) {
        study.recruitStart();
    }

    public void recruitStop(Study study) {
        study.recruitStop();
    }

    //새로운 경로가 사용 할 수 있는 경로인지 체크
    public boolean isValidPath(String newPath) {
        if(!newPath.matches(PATH_REGEXP)){
            return false;
        }
        return !studyRepository.existsByPath(newPath);
    }

    public void updatePath(Study study, String newPath) {
        study.updateNewPath(newPath);
    }

    public boolean isValidTitle(String newTitle) {
        return newTitle.length() <= 50;
    }

    public void updateTitle(Study study, String newTitle) {
        study.setTitle(newTitle);
    }

    public void deleteStudy(Study study) {
        study.isRemovable();
    }

    public void addMember(Account account, Study study) {
        study.addMember(account);
    }

    public void removeMember(Account account, Study study) {
        study.removeMember(account);
    }
}

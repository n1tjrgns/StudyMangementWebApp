package com.jpa.studywebapp.study;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpa.studywebapp.account.CurrentUser;
import com.jpa.studywebapp.domain.Account;
import com.jpa.studywebapp.domain.Study;
import com.jpa.studywebapp.domain.Tag;
import com.jpa.studywebapp.domain.Zone;
import com.jpa.studywebapp.settings.form.TagForm;
import com.jpa.studywebapp.settings.form.ZoneForm;
import com.jpa.studywebapp.study.form.StudyDescriptionForm;
import com.jpa.studywebapp.tag.TagRepository;
import com.jpa.studywebapp.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/study/{path}/settings")
@RequiredArgsConstructor
public class StudySettingsController {

    private final StudyService studyService;
    private final ModelMapper modelMapper;
    private final TagRepository tagRepository;
    private final ObjectMapper objectMapper;
    private final TagService tagService;
    private final ZoneRepository zoneRepository;

    //스터디 수정 페이지
    @GetMapping("/description")
    public String viewStudySetting(@CurrentUser Account account, Model model, @PathVariable String path) {
        //해당 스터디가 존재하고, 매니저 권한인지 확인.
        Study study = studyService.getStudyToUpdate(account, path);

        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(modelMapper.map(study, StudyDescriptionForm.class));

        return "study/settings/description";
    }

    @PostMapping("/description")
    public String updateStudyInfo(@CurrentUser Account account, Model model, @PathVariable String path, RedirectAttributes attributes,
                                  @Valid StudyDescriptionForm studyDescriptionForm, Errors errors) throws UnsupportedEncodingException {

        Study study = studyService.getStudyToUpdate(account, path);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            return "study/settings/description";
        }

        studyService.updateStudyDescription(study, studyDescriptionForm);
        attributes.addFlashAttribute("message", "스터디 소개가 수정되었습니다.");

        return "redirect:/study/" + URLEncoder.encode(study.getPath(), String.valueOf(StandardCharsets.UTF_8));
    }

    @GetMapping("/banner")
    public String studyImageForm(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        return "study/settings/banner";
    }

    @PostMapping("/banner")
    public String studyImageForm(@CurrentUser Account account, @PathVariable String path, String image,
                                 RedirectAttributes attributes) throws UnsupportedEncodingException {
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.updateStudyImage(study, image);
        attributes.addFlashAttribute("message", "이미지를 수정했습니다.");
        return "redirect:/study/" + getPath(path) + "/settings/banner";
    }

    private String getPath(String path) throws UnsupportedEncodingException {
        return URLEncoder.encode(path, String.valueOf(StandardCharsets.UTF_8));
    }

    @PostMapping("/banner/enable")
    public String enableStudyBanner(@CurrentUser Account account, @PathVariable String path) throws UnsupportedEncodingException {
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.enableStudyBanner(study);
        return "redirect:/study/" + getPath(path) + "/settings/banner";
    }

    @PostMapping("/banner/disable")
    public String disableStudyBanner(@CurrentUser Account account, @PathVariable String path) throws UnsupportedEncodingException {
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.disableStudyBanner(study);
        return "redirect:/study/" + getPath(path) + "/settings/banner";
    }

    @GetMapping("/tags")
    public String studyTagsForm(@CurrentUser Account account, @PathVariable String path, Model model)
            throws JsonProcessingException {
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);

        model.addAttribute("tags", study.getTags().stream()
                .map(Tag::getTitle).collect(Collectors.toList()));
        List<String> allTagTitles = tagRepository.findAll().stream()
                .map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTagTitles));
        return "study/settings/tags";
    }

    @PostMapping("/tags/add")
    public ResponseEntity addTag(@CurrentUser Account account, @PathVariable String path, @RequestBody TagForm tagForm) {
        Study study = studyService.getStudyToUpdateTag(account, path);
        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
        studyService.addTag(study, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tags/remove")
    public ResponseEntity removeTag(@CurrentUser Account account, @PathVariable String path, @RequestBody TagForm tagForm){
        Study study = studyService.getStudyToUpdateTag(account, path);
        Tag tag = tagRepository.findByTitle(tagForm.getTagTitle());

        if(tag == null){
            return ResponseEntity.badRequest().build();
        }

        studyService.removeTag(study, tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/zones")
    public String getZoneForm(@CurrentUser Account account, Model model, @PathVariable String path) throws JsonProcessingException {

        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);

        model.addAttribute("zones", study.getZones().stream().map(Zone::toString).collect(Collectors.toList()));

        List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));

        return "study/settings/zones";
    }

    @PostMapping("/zones/add")
    public ResponseEntity addZone(@CurrentUser Account account, @RequestBody  ZoneForm zoneForm, @PathVariable String path){
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        Study study = studyService.getStudyToUpdateZone(account, path);
        if(zone == null){
            return ResponseEntity.badRequest().build();
        }
        studyService.addZone(study, zone);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/zones/remove")
    public ResponseEntity removeZone(@CurrentUser Account account, @RequestBody  ZoneForm zoneForm, @PathVariable String path){
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        Study study = studyService.getStudyToUpdateZone(account, path);
        if(zone == null){
            return ResponseEntity.badRequest().build();
        }
        studyService.removeZone(study, zone);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/study")
    public String getStudyForm(@CurrentUser Account account, Model model, @PathVariable String path){
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);

        return "study/settings/study";
    }

    @PostMapping("/study/publish")
    public String publishStudy(@CurrentUser Account account, @PathVariable String path, RedirectAttributes attributes) throws UnsupportedEncodingException {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.publish(study);
        attributes.addFlashAttribute("message", "스터디가 공개되었습니다.");

        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    @PostMapping("/study/close")
    public String closeStudy(@CurrentUser Account account, @PathVariable String path, RedirectAttributes attributes) throws UnsupportedEncodingException {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.close(study);
        attributes.addFlashAttribute("message", "스터디가 종료되었습니다.");

        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    @PostMapping("/recruit/start")
    public String startRecruit(@CurrentUser Account account, @PathVariable String path, RedirectAttributes attributes) throws UnsupportedEncodingException {
        Study study = studyService.getStudyToUpdateStatus(account, path);

        //1시간이 지났는지 체크, 시간이 안됐다면 메세지 전달, 리턴
        if(!study.canUpdateRecruit()){
            attributes.addFlashAttribute("message", "1시간안에 여러번 상태를 변경 할 수 없습니다.");
            return "redirect:/study/" + getPath(path) + "/settings/study";
        }
        //시간이 지났다면 모집 시작
        studyService.recruitStart(study);
        attributes.addFlashAttribute("message", "인원 모집을 시작합니다.");

        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    @PostMapping("/recruit/stop")
    public String stopRecruit(@CurrentUser Account account, @PathVariable String path, RedirectAttributes attributes) throws UnsupportedEncodingException {
        Study study = studyService.getStudyToUpdateStatus(account, path);

        //1시간이 지났는지 체크, 시간이 안됐다면 메세지 전달, 리턴
        if(!study.canUpdateRecruit()){
            attributes.addFlashAttribute("message", "1시간안에 여러번 상태를 변경 할 수 없습니다.");
            return "redirect:/study/" + getPath(path) + "/settings/study";
        }
        //시간이 지났다면 모집 시작
        studyService.recruitStop(study);
        attributes.addFlashAttribute("message", "인원 모집을 시작합니다.");

        return "redirect:/study/" + getPath(path) + "/settings/study";
    }
}
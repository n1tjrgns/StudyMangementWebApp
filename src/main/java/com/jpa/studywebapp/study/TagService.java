package com.jpa.studywebapp.study;

import com.jpa.studywebapp.domain.Tag;
import com.jpa.studywebapp.tag.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TagService {

    private final TagRepository tagRepository;

    public Tag findOrCreateNew(String tagTitle) {
        Tag tag = tagRepository.findByTitle(tagTitle);
        if(tag == null){
            tag = tagRepository.save(Tag.builder().title(tagTitle).build());
        }

        return tag;
    }
}

package com.jpa.studywebapp.settings.form;

import com.jpa.studywebapp.domain.Zone;
import lombok.Data;

@Data
public class ZoneForm {

    private String zoneName;

    public String getCityName() {
        return zoneName.substring(0, zoneName.indexOf("("));
    }

    public String getProvinceName() {
        return zoneName.substring(zoneName.indexOf("/") + 1);
    }

    public String getLocalNameCity() {
        return zoneName.substring(zoneName.indexOf("(") + 1, zoneName.indexOf(")"));
    }

    public Zone getZone() {
        return Zone.builder().city(this.getCityName())
                .localNameCity(this.getLocalNameCity())
                .province(this.getProvinceName()).build();
    }
}

package com.wornux.utils.logs;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class RevisionDto {

    private long docCode;
    private int id;
    private Date date;
    private String modifierUser;
    private String ipAddress;
    private String type;
}

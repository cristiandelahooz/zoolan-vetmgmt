package com.wornux.utils.logs;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author me@fredpena.dev
 * @created 05/12/2024 - 12:45
 */
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

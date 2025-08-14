package com.wornux.utils.logs;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

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

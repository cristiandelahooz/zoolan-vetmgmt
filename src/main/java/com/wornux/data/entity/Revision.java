package com.wornux.data.entity;

import com.wornux.security.auditable.RevisionListenerImpl;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

@Getter
@Setter
@ToString
@Entity
@RevisionEntity(RevisionListenerImpl.class)
public class Revision extends DefaultRevisionEntity {

  protected String modifierUser;
  protected String ipAddress;
}

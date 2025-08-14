package com.wornux.views.waitingroom;

import com.wornux.data.enums.Priority;
import com.wornux.dto.request.WaitingRoomCreateRequestDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WaitingRoomFormModel {

  private Long clientId;
  private Long petId;
  private String reasonForVisit;
  private Priority priority;
  private String notes;

  public WaitingRoomCreateRequestDto toDto() {
    return new WaitingRoomCreateRequestDto(clientId, petId, reasonForVisit, priority, notes);
  }
}

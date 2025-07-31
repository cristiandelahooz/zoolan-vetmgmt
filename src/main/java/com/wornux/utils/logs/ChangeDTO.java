package com.wornux.utils.logs;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChangeDTO {

    private String property;
    private String previousValue;
    private String currentValue;
}

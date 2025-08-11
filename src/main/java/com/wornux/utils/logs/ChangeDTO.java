package com.wornux.utils.logs;

import lombok.Getter;
import lombok.Setter;

/**
 * @author me@fredpena.dev
 * @created 05/12/2024 - 12:47
 */
@Setter
@Getter
public class ChangeDTO {

    private String property;
    private String previousValue;
    private String currentValue;
}

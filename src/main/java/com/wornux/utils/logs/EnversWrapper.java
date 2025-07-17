package com.wornux.utils.logs;

import org.hibernate.envers.RevisionType;

import java.util.Map;

/**
 * @author me@fredpena.dev
 * @created 08/12/2024 - 00:58
 */
public final class EnversWrapper {

    private EnversWrapper() {
    }

    public static String revisionType(RevisionType type) {
        if (type == RevisionType.ADD) {
            return "Add";
        } else if (type == RevisionType.MOD) {
            return "Mod";
        } else {
            return "Del";
        }
    }

    private static String wrapperValue(Object value) {
        //        if (value != null) {
        //            System.out.println("value: " + value.getClass().getSimpleName());
        //        }
        return switch (value) {
        case null -> "";
        case String object -> object;
        //            case Therapist object -> object.getName();
        //            case CancellationCategory object -> object.getDescription();
        //            case AppointmentRecurrent object -> object.getRepeatEvery().getKey();
        //            case PatientTherapist object -> String.valueOf(object.getPrice());
        default -> "";
        };

    }

    public static ChangeDTO createChange(Map.Entry<String, Object[]> entry) {
        ChangeDTO dto = new ChangeDTO();
        dto.setProperty(entry.getKey());
        dto.setPreviousValue(wrapperValue(entry.getValue()[0]));
        dto.setCurrentValue(wrapperValue(entry.getValue()[1]));

        return dto;
    }
}

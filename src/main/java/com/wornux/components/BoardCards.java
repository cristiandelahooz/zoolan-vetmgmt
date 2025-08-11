package com.wornux.components;

import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * @author me@fredpena.dev
 * @created 14/02/2025 - 18:42
 */
public class BoardCards extends Div implements HasTheme {

    public BoardCards() {
        setWidthFull();
        addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.ROW, LumoUtility.FlexWrap.WRAP,
                LumoUtility.Overflow.HIDDEN);
    }

    public void add(BoardCard... boardCards) {
        for (BoardCard boardCard : boardCards) {
            boardCard.getStyle().setFlexBasis("0");
            boardCard.addClassNames(LumoUtility.Flex.GROW);
        }
        super.add(boardCards);
    }
}

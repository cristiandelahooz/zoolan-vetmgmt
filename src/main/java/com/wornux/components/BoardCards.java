package com.wornux.components;

import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.theme.lumo.LumoUtility;

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

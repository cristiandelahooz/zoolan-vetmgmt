package com.wornux.components;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.theme.lumo.LumoIcon;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class SearchComponent extends TextField {

  public SearchComponent() {
    addClassNames(
        LumoUtility.Flex.GROW, LumoUtility.MinWidth.NONE, LumoUtility.Padding.Vertical.NONE);
    setAriaLabel("Search");
    setClearButtonVisible(true);
    setWidthFull();
    setPlaceholder("Search");
    setPrefixComponent(LumoIcon.SEARCH.create());
    setValueChangeMode(ValueChangeMode.EAGER);
  }
}

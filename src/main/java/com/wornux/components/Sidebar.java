package com.wornux.components;

import static com.wornux.utils.CSSUtility.SLIDER_RESPONSIVE_CONTENT;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.lumo.LumoUtility;
import lombok.Getter;
import org.vaadin.lineawesome.LineAwesomeIcon;

public class Sidebar extends Section implements HasEnabled, HasTheme {

  private final H2 title = new H2();
  private final Span subTitle = new Span();
  @Getter private final Div content = new Div();
  private final Div headerContent = new Div();

  @Getter private final Button save = new Button("Guardar");
  @Getter private final Button cancel = new Button("Descartar cambios");
  @Getter private final Button delete = new Button("Eliminar");

  public Sidebar() {
    addClassNames(
        LumoUtility.Background.BASE,
        LumoUtility.BoxShadow.MEDIUM,
        LumoUtility.Display.FLEX,
        LumoUtility.FlexDirection.COLUMN,
        LumoUtility.Overflow.HIDDEN,
        LumoUtility.Position.FIXED,
        "bottom-0",
        "top-0",
        "transition-all",
        "z-10");
    setWidth(480, Unit.PIXELS);

    save.addClassNames(LumoUtility.Width.AUTO);
    cancel.addClassNames(LumoUtility.Width.AUTO);
    delete.addClassNames(LumoUtility.Width.AUTO);

    content.addClassNames(
        LumoUtility.Display.FLEX,
        LumoUtility.FlexDirection.COLUMN,
        LumoUtility.Flex.GROW,
        LumoUtility.Padding.Bottom.MEDIUM,
        SLIDER_RESPONSIVE_CONTENT);

    close();

    addClassNames(LumoUtility.BorderRadius.NONE, LumoUtility.Margin.NONE);

    Footer footer = createFooter();
    Header header = createHeader();

    Scroller formScroller = scrollerVertical();
    formScroller.setContent(content);

    add(header, headerContent, formScroller, footer);
  }

  public Scroller scrollerVertical() {
    Scroller scroller = new Scroller();
    scroller.setSizeFull();
    scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
    scroller.addClassNames(LumoUtility.AlignContent.START, LumoUtility.Padding.NONE);

    return scroller;
  }

  private Header createHeader() {
    this.title.addClassNames(LumoUtility.FontSize.XLARGE);

    Div layout = new Div(this.title);
    layout.addClassNames(
        LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN, LumoUtility.Gap.SMALL);

    subTitle.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
    layout.add(subTitle);

    Button close = new Button(LineAwesomeIcon.TIMES_SOLID.create(), e -> close());
    close.addClassNames(LumoUtility.Margin.Vertical.NONE, LumoUtility.Width.AUTO);
    close.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
    close.setAriaLabel("Close sidebar");
    close.setTooltipText("Close sidebar");

    Header header = new Header(layout, close);
    header.getElement().getThemeList().add(Lumo.DARK);
    header.addClassNames(
        LumoUtility.Display.FLEX,
        LumoUtility.Border.BOTTOM,
        LumoUtility.JustifyContent.BETWEEN,
        LumoUtility.Padding.End.MEDIUM,
        LumoUtility.Padding.Start.LARGE,
        LumoUtility.Padding.Vertical.MEDIUM);

    return header;
  }

  public void createHeaderContent(Component... components) {
    headerContent.add(components);
  }
  
  public void clearHeaderContent() {
    headerContent.removeAll();
  }

  public void createContent(Component... components) {
    content.add(components);
  }

  private Footer createFooter() {
    this.delete.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
    this.save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    Footer footer = new Footer(this.delete, this.cancel, this.save);
    footer.addClassNames(
        LumoUtility.Background.CONTRAST_5,
        LumoUtility.Display.FLEX,
        LumoUtility.Gap.SMALL,
        LumoUtility.JustifyContent.END,
        LumoUtility.Padding.Horizontal.MEDIUM,
        LumoUtility.Padding.Vertical.SMALL);
    return footer;
  }

  public void close() {
    addClassNames("-end-full");
    removeClassName("end-0");
    setEnabled(false);
  }

  private void open() {
    addClassNames("end-0");
    removeClassNames("-end-full");
    setEnabled(true);
  }

  public void setText(String title) {
    this.title.setText(title);
  }

  public void newObject(String title) {
    this.delete.setVisible(false);
    this.cancel.setVisible(true);
    this.title.setText(title);
    open();
  }

  public void editObject(String title) {
    this.delete.setVisible(true);
    this.cancel.setVisible(false);
    this.title.setText(title);
    open();
  }

  public void addSubTitle(String subTitle) {
    this.subTitle.setText(subTitle);
  }

  public void setOnSaveClickListener(ComponentEventListener<ClickEvent<Button>> listener) {
    save.addClickListener(listener);
  }

  public void setOnCancelClickListener(ComponentEventListener<ClickEvent<Button>> listener) {
    cancel.addClickListener(listener);
  }

  public void setOnDeleteClickListener(ComponentEventListener<ClickEvent<Button>> listener) {
    delete.addClickListener(listener);
  }
}

package com.wornux.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.shared.HasClearButton;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import java.text.DecimalFormat;

@Tag("decimal-field")
public class DecimalField extends CustomField<Double>
    implements HasValidation, HasClearButton, HasEnabled {

  private final TextField field = new TextField();

  public DecimalField() {
    this("");
  }

  public DecimalField(String label) {
    setLabel(label);
    add(field);

    String regex = "[0-9.]";
    field.setAllowedCharPattern(regex);

    field.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT);

    field.setPrefixComponent(VaadinIcon.MONEY.create());

    field.addBlurListener(
        event -> {
          String value =
              field.getValue() != null && !field.getValue().isEmpty() ? field.getValue() : "0";
          setPresentationValue(Double.parseDouble(replaceAll(value)));
        });

    field.setWidth("100%");
    setWidth("100%");

    setPresentationValue(0.0);
  }

  public void setPrefixComponent(Component component) {
    field.setPrefixComponent(component);
  }

  public void setSuffixComponent(Component component) {
    setPrefixComponent(null);
    field.setSuffixComponent(component);
  }

  @Override
  protected Double generateModelValue() {
    String value = field.getValue();
    if (value != null && !value.isEmpty()) {
      return Double.parseDouble(replaceAll(value));
    }

    return null;
  }

  @Override
  protected void setPresentationValue(Double aDouble) {
    field.setValue(aDouble != null ? getDecimalFormat(aDouble) : "0.0");
  }

  @Override
  public void setReadOnly(boolean readOnly) {
    field.setReadOnly(readOnly);
  }

  public String getDecimalFormat(final Object obj) {
    return getDecimalFormat("#,##0.00", obj);
  }

  public String getDecimalFormat(String pattern, final Object obj) {
    return new DecimalFormat(pattern).format(obj);
  }

  private String replaceAll(String input) {
    String result = input.replaceAll("[^0-9.]", "");

    int lastIndex = result.lastIndexOf(".");
    if (lastIndex != -1) {
      String beforeLastPoint = result.substring(0, lastIndex);
      String afterLastPoint = result.substring(lastIndex + 1);

      beforeLastPoint = beforeLastPoint.replace(".", "");

      return beforeLastPoint + "." + afterLastPoint;

    } else {
      return result;
    }
  }

  @Override
  public void setLabel(String label) {
    field.setLabel(label);
  }

  @Override
  public void setErrorMessage(String errorMessage) {
    field.setErrorMessage(errorMessage);
  }

  @Override
  public void setInvalid(boolean invalid) {
    field.setInvalid(invalid);
  }

  @Override
  public void setClearButtonVisible(boolean clearButtonVisible) {
    field.setClearButtonVisible(clearButtonVisible);
  }

  @Override
  public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
    field.setRequiredIndicatorVisible(requiredIndicatorVisible);
  }

  @Override
  public void setEnabled(boolean enabled) {
    field.setEnabled(enabled);
  }
}

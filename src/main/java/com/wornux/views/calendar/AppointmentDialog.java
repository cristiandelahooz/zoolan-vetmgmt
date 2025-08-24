package com.wornux.views.calendar;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.wornux.data.entity.Pet;
import com.wornux.data.enums.OfferingType;
import com.wornux.dto.request.AppointmentCreateRequestDto;
import com.wornux.dto.request.AppointmentUpdateRequestDto;
import com.wornux.dto.response.AppointmentResponseDto;
import com.wornux.services.interfaces.AppointmentService;
import com.wornux.services.interfaces.PetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public class AppointmentDialog extends Dialog {

  private final AppointmentService appointmentService;
  private final PetService petService;
  private final Consumer<Void> onSaveCallback;

  private AppointmentResponseDto currentAppointment;

  private TextField titleField;
  private Select<Pet> petSelect;
  private Select<OfferingType> offeringTypeSelect;
  private CustomDateTimePicker startDateTimePicker;
  private CustomDateTimePicker endDateTimePicker;
  private TextField notesField;

  public AppointmentDialog(
      AppointmentService appointmentService, PetService petService, Consumer<Void> onSaveCallback) {
    this.appointmentService = appointmentService;
    this.petService = petService;
    this.onSaveCallback = onSaveCallback;

    initializeDialog();
    createFormFields();
    createButtons();
  }

  private void initializeDialog() {
    setWidth("600px");
    setCloseOnEsc(true);
    setCloseOnOutsideClick(false);
  }

  private void createFormFields() {
    FormLayout formLayout = new FormLayout();

    titleField = new TextField("Título de la Cita");
    titleField.setRequired(true);
    titleField.setWidthFull();

    petSelect = new Select<>();
    petSelect.setLabel("Mascota");
    petSelect.setRequiredIndicatorVisible(true);
    petSelect.setWidthFull();
    petSelect.setItemLabelGenerator(
        pet ->
            pet.getName()
                + " - "
                + (pet.getOwners().isEmpty()
                    ? "Sin dueño"
                    : pet.getOwners().get(0).getFirstName()
                        + " "
                        + pet.getOwners().get(0).getLastName()));

    loadPets();

    offeringTypeSelect = new Select<>();
    offeringTypeSelect.setLabel("Tipo de Servicio");
    offeringTypeSelect.setItems(OfferingType.values());
    offeringTypeSelect.setItemLabelGenerator(OfferingType::name);
    offeringTypeSelect.setRequiredIndicatorVisible(true);
    offeringTypeSelect.setWidthFull();

    startDateTimePicker = new CustomDateTimePicker("Fecha y Hora de Inicio");
    startDateTimePicker.setWidthFull();

    endDateTimePicker = new CustomDateTimePicker("Fecha y Hora de Fin");
    endDateTimePicker.setWidthFull();

    notesField = new TextField("Notas");
    notesField.setWidthFull();

    formLayout.add(
        titleField,
        petSelect,
        offeringTypeSelect,
        startDateTimePicker,
        endDateTimePicker,
        notesField);
    formLayout.setColspan(notesField, 2);

    add(formLayout);
  }

  private void loadPets() {
    try {
      List<Pet> pets =
          petService.getAllPets(PageRequest.of(0, 1000)).stream()
              .map(
                  dto -> {
                    Pet pet = new Pet();
                    pet.setId(dto.id());
                    pet.setName(dto.name());
                    return pet;
                  })
              .collect(Collectors.toList());
      petSelect.setItems(pets);
    } catch (Exception e) {
      log.error("Error loading pets", e);
      Notification.show("Error cargando mascotas", 3000, Notification.Position.MIDDLE)
          .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
  }

  private void createButtons() {
    Button saveButton = new Button("Guardar", e -> saveAppointment());
    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    Button deleteButton = new Button("Eliminar", e -> deleteAppointment());
    deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

    Button cancelButton = new Button("Cancelar", e -> close());

    getFooter().add(cancelButton);

    if (currentAppointment != null) {
      getFooter().add(deleteButton);
    }

    getFooter().add(saveButton);
  }

  public void openForNew(LocalDateTime startTime, LocalDateTime endTime) {
    currentAppointment = null;
    setHeaderTitle("Nueva Cita");

    clearForm();
    startDateTimePicker.setValue(startTime);
    endDateTimePicker.setValue(endTime != null ? endTime : startTime.plusHours(1));

    refreshButtons();
    open();
  }

  public void openForEdit(AppointmentResponseDto appointment) {
    currentAppointment = appointment;
    setHeaderTitle("Editar Cita");

    populateForm(appointment);
    refreshButtons();
    open();
  }

  private void clearForm() {
    titleField.clear();
    petSelect.clear();
    offeringTypeSelect.clear();
    startDateTimePicker.clear();
    endDateTimePicker.clear();
    notesField.clear();
  }

  private void populateForm(AppointmentResponseDto appointment) {
    titleField.setValue(
        appointment.getAppointmentTitle() != null ? appointment.getAppointmentTitle() : "");
    offeringTypeSelect.setValue(appointment.getOfferingType());
    startDateTimePicker.setValue(appointment.getStartAppointmentDate());
    endDateTimePicker.setValue(appointment.getEndAppointmentDate());

    // Note: Pet selection might need additional logic to match the Pet entity
  }

  private void refreshButtons() {
    getFooter().removeAll();
    createButtons();
  }

  private void saveAppointment() {
    if (!validateForm()) {
      return;
    }

    try {
      if (currentAppointment == null) {
        createNewAppointment();
      } else {
        updateExistingAppointment();
      }

      onSaveCallback.accept(null);
      close();

    } catch (Exception e) {
      log.error("Error saving appointment", e);
      Notification.show("Error guardando la cita", 3000, Notification.Position.MIDDLE)
          .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
  }

  private boolean validateForm() {
    if (titleField.isEmpty()
        || petSelect.isEmpty()
        || offeringTypeSelect.isEmpty()
        || startDateTimePicker.isEmpty()
        || endDateTimePicker.isEmpty()) {
      Notification.show("Por favor complete todos los campos requeridos");
      return false;
    }

    if (startDateTimePicker.getValue().isAfter(endDateTimePicker.getValue())) {
      Notification.show("La fecha de inicio debe ser anterior a la fecha de fin");
      return false;
    }

    return true;
  }

  private void createNewAppointment() {
    AppointmentCreateRequestDto createDto = new AppointmentCreateRequestDto();
    createDto.setReason(titleField.getValue());
    createDto.setPetId(petSelect.getValue().getId());
    createDto.setOfferingType(offeringTypeSelect.getValue());
    createDto.setStartAppointmentDate(startDateTimePicker.getValue());
    createDto.setEndAppointmentDate(endDateTimePicker.getValue());
    createDto.setNotes(notesField.getValue());

    appointmentService.createAppointment(createDto);

    Notification.show("Cita creada exitosamente", 3000, Notification.Position.BOTTOM_END)
        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
  }

  private void updateExistingAppointment() {
    AppointmentUpdateRequestDto updateDto = new AppointmentUpdateRequestDto();
    updateDto.setStartAppointmentDate(startDateTimePicker.getValue());
    updateDto.setEndAppointmentDate(endDateTimePicker.getValue());

    appointmentService.updateAppointment(currentAppointment.getEventId(), updateDto);

    Notification.show("Cita actualizada exitosamente", 3000, Notification.Position.BOTTOM_END)
        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
  }

  private void deleteAppointment() {
    if (currentAppointment == null) {
      return;
    }

    try {
      appointmentService.deleteAppointment(currentAppointment.getEventId());

      Notification.show("Cita eliminada", 3000, Notification.Position.BOTTOM_END)
          .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

      onSaveCallback.accept(null);
      close();

    } catch (Exception e) {
      log.error("Error deleting appointment", e);
      Notification.show("Error eliminando la cita", 3000, Notification.Position.MIDDLE)
          .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
  }
}

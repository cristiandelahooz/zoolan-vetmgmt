package com.wornux.views.waitingroom;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.wornux.data.entity.Client;
import com.wornux.data.entity.Pet;
import com.wornux.data.entity.WaitingRoom;
import com.wornux.data.enums.Priority;
import com.wornux.data.enums.VisitType;
import com.wornux.dto.request.WaitingRoomCreateRequestDto;
import com.wornux.services.interfaces.ClientService;
import com.wornux.services.interfaces.PetService;
import com.wornux.services.interfaces.WaitingRoomService;
import com.wornux.utils.NotificationUtils;
import com.wornux.views.clients.SelectOwnerDialog;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import lombok.Setter;

public class WaitingRoomForm extends Dialog {

  private final TextField clientField = new TextField("Cliente");
  private final Button selectClientBtn = new Button("Seleccionar");
  private final ComboBox<Pet> petField = new ComboBox<>("Mascota");
  private final TextField reasonField = new TextField("Razón de Visita");
  private final ComboBox<Priority> priorityField = new ComboBox<>("Prioridad");
  private final ComboBox<VisitType> typeField = new ComboBox<>("Tipo");
  private final TextArea notesField = new TextArea("Notas");
  private final DateTimePicker arrivalTimeField = new DateTimePicker("Hora de Llegada");
  private final Button saveButton = new Button("Guardar");
  private final Button cancelButton = new Button("Cancelar");
  private final transient WaitingRoomService waitingRoomService;
  private final transient ClientService clientService;
  private final transient PetService petService;
  private final SelectOwnerDialog selectOwnerDialog;
  private Client selectedClient;
  @Setter private transient Consumer<WaitingRoomCreateRequestDto> onSave;

  private transient WaitingRoom editingWaitingRoom;

  public WaitingRoomForm(
      WaitingRoomService waitingRoomService, ClientService clientService, PetService petService) {
    this.waitingRoomService = waitingRoomService;
    this.clientService = clientService;
    this.petService = petService;

    this.selectOwnerDialog = new SelectOwnerDialog(clientService);

    setHeaderTitle("Entrada a Sala de Espera");
    setModal(true);
    setWidth("500px");
    setMaxHeight("98vh");

    setResizable(true);
    setDraggable(true);

    // ===== Cliente (solo lectura + botón que abre el diálogo)
    clientField.setReadOnly(true);
    clientField.setPlaceholder("Selecciona un cliente…");
    selectClientBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
    selectClientBtn.addClickListener(e -> selectOwnerDialog.open());
    clientField.setSuffixComponent(selectClientBtn);

    // Al elegir cliente en el diálogo, cascada Cliente → Mascota
    selectOwnerDialog.addClienteSeleccionadoListener(
        cliente -> {
          selectedClient = cliente;
          clientField.setInvalid(false);
          clientField.setValue(cliente.getFirstName() + " " + cliente.getLastName());

          List<Pet> pets = petService.getPetsByOwnerId2(cliente.getId());
          petField.setItems(pets);
          petField.setItemLabelGenerator(Pet::getName);
          petField.setEnabled(true);
          petField.setHelperText(null);
          petField.clear();
        });

    // ===== Mascota (se habilita tras elegir cliente)
    petField.setItemLabelGenerator(Pet::getName);
    petField.setEnabled(false);
    petField.setHelperText("Seleccione un cliente primero");

    // ===== Campos simples
    priorityField.setItems(Priority.values());

    // tipo
    // ===== Tipo (Médica / Grooming)
    typeField.setItems(VisitType.values());
    typeField.setItemLabelGenerator(t -> t == VisitType.MEDICA ? "Médica" : "Grooming");
    typeField.setRequiredIndicatorVisible(true);
    typeField.setPlaceholder("Seleccione el tipo de atención");

    // Hora de llegada: solo lectura y automática
    arrivalTimeField.setReadOnly(true);
    arrivalTimeField.setHelperText("Se registrará automáticamente al guardar");
    arrivalTimeField.setStep(java.time.Duration.ofMinutes(1));

    // ===== Layout
    FormLayout formLayout =
        new FormLayout(
            clientField, // Cliente (readonly) + botón suffix
            petField, // Mascota (se habilita con el cliente)
            reasonField,
            typeField,
            priorityField,
            notesField,
            arrivalTimeField);

    saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

    saveButton.addClickListener(e -> save());
    cancelButton.addClickListener(e -> close());

    add(formLayout, saveButton, cancelButton);
  }

  private static String nullSafe(String s) {
    return s == null ? "" : s;
  }

  private void save() {
    if (!validateForm()) {
      NotificationUtils.error("Por favor, complete/corrija los campos marcados.");
      return;
    }

    // Hora automática: en creación se toma ahora; en edición se conserva la original
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime arrival =
        (editingWaitingRoom == null) ? now : editingWaitingRoom.getArrivalTime();

    WaitingRoomCreateRequestDto dto =
        WaitingRoomCreateRequestDto.builder()
            .clientId(selectedClient.getId())
            .petId(petField.getValue().getId())
            .reasonForVisit(reasonField.getValue())
            .type(typeField.getValue())
            .priority(priorityField.getValue())
            .notes(notesField.getValue())
            .arrivalTime(arrival)
            .build();

    try {
      if (editingWaitingRoom == null) {
        waitingRoomService.save(dto);
        NotificationUtils.success("Entrada creada exitosamente.");
      } else {
        editingWaitingRoom.setClient(selectedClient);
        editingWaitingRoom.setPet(petField.getValue());
        editingWaitingRoom.setReasonForVisit(reasonField.getValue());
        editingWaitingRoom.setType(typeField.getValue());
        editingWaitingRoom.setPriority(priorityField.getValue());
        editingWaitingRoom.setNotes(notesField.getValue());
        // arrivalTime NO se modifica en edición (se mantiene el original)
        waitingRoomService.update(editingWaitingRoom);
        NotificationUtils.success("Entrada actualizada exitosamente.");
      }
      if (onSave != null) onSave.accept(dto);
      close();
      clearForm();
    } catch (Exception e) {
      NotificationUtils.error("Error al guardar: " + e.getMessage());
    }
  }

  private boolean validateForm() {
    boolean valid = true;

    if (selectedClient == null) {
      clientField.setInvalid(true);
      clientField.setErrorMessage("Debe seleccionar un cliente");
      valid = false;
    } else {
      clientField.setInvalid(false);
    }

    if (typeField.isEmpty()) {
      typeField.setInvalid(true);
      typeField.setErrorMessage("Debe seleccionar el tipo");
      valid = false;
    } else typeField.setInvalid(false);

    if (petField.isEmpty()) {
      petField.setInvalid(true);
      valid = false;
    } else petField.setInvalid(false);

    if (reasonField.isEmpty()) {
      reasonField.setInvalid(true);
      valid = false;
    } else reasonField.setInvalid(false);

    if (priorityField.isEmpty()) {
      priorityField.setInvalid(true);
      valid = false;
    } else priorityField.setInvalid(false);

    // arrivalTimeField no se valida: es automático
    return valid;
  }

  public void openForNew() {
    clearForm();
    editingWaitingRoom = null;
    saveButton.setText("Guardar");
    // Mostrar valor estimado (informativo); el definitivo se toma al guardar
    arrivalTimeField.setValue(LocalDateTime.now());
    open();
  }

  public void openForEdit(WaitingRoom waitingRoom) {
    clearForm();
    editingWaitingRoom = waitingRoom;

    // Cliente + cascada
    if (waitingRoom.getClient() != null) {
      selectedClient = waitingRoom.getClient();
      clientField.setValue(selectedClient.getFirstName() + " " + selectedClient.getLastName());
      var pets = petService.getPetsByOwnerId2(selectedClient.getId());
      petField.setItems(pets);
      petField.setItemLabelGenerator(Pet::getName);
      petField.setEnabled(true);
      petField.setHelperText(null);
    }

    petField.setValue(waitingRoom.getPet());
    reasonField.setValue(nullSafe(waitingRoom.getReasonForVisit()));
    priorityField.setValue(waitingRoom.getPriority());
    notesField.setValue(nullSafe(waitingRoom.getNotes()));
    arrivalTimeField.setValue(waitingRoom.getArrivalTime());

    saveButton.setText("Actualizar");
    open();
  }

  private void clearForm() {
    selectedClient = null;
    clientField.clear();
    clientField.setInvalid(false);

    petField.clear();
    petField.setItems();
    petField.setEnabled(false);
    petField.setHelperText("Seleccione un cliente primero");
    petField.setInvalid(false);

    reasonField.clear();
    reasonField.setInvalid(false);

    typeField.clear();
    typeField.setInvalid(false);

    priorityField.clear();
    priorityField.setInvalid(false);

    notesField.clear();

    arrivalTimeField.clear();
    arrivalTimeField.setInvalid(false);
  }
}

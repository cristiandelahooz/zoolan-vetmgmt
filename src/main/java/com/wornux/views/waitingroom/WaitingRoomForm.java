// src/main/java/com/wornux/views/waitingroom/WaitingRoomForm.java
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
import com.wornux.dto.request.WaitingRoomCreateRequestDto;
import com.wornux.services.interfaces.ClientService;
import com.wornux.services.interfaces.PetService;
import com.wornux.services.interfaces.WaitingRoomService;
import com.wornux.utils.NotificationUtils;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import lombok.Setter;

public class WaitingRoomForm extends Dialog {
    private final ComboBox<Client> clientField = new ComboBox<>("Cliente");
    private final ComboBox<Pet> petField = new ComboBox<>("Mascota");
    private final TextField reasonField = new TextField("Razón de Visita");
    private final ComboBox<Priority> priorityField = new ComboBox<>("Prioridad");
    private final TextArea notesField = new TextArea("Notas");
    private final com.vaadin.flow.component.datetimepicker.DateTimePicker arrivalTimeField = new DateTimePicker(
            "Hora de Llegada");

    private final Button saveButton = new Button("Guardar");
    private final Button cancelButton = new Button("Cancelar");

    private final transient WaitingRoomService waitingRoomService;
    private final transient ClientService clientService;
    private final transient PetService petService;

    @Setter
    private transient Consumer<WaitingRoomCreateRequestDto> onSave;

    private transient WaitingRoom editingWaitingRoom;

    public WaitingRoomForm(WaitingRoomService waitingRoomService, ClientService clientService, PetService petService) {
        this.waitingRoomService = waitingRoomService;
        this.clientService = clientService;
        this.petService = petService;

        setHeaderTitle("Entrada a Sala de Espera");
        setModal(true);
        setWidth("500px");

        clientField.setItems(clientService.getAllActiveClients());
        clientField.setItemLabelGenerator(c -> c.getFirstName() + " " + c.getLastName());
        petField.setItems(petService.getAllPets());
        petField.setItemLabelGenerator(Pet::getName);
        priorityField.setItems(Priority.values());

        FormLayout formLayout = new FormLayout(clientField, petField, reasonField, priorityField, notesField,
                arrivalTimeField);

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        saveButton.addClickListener(e -> save());
        cancelButton.addClickListener(e -> close());

        add(formLayout, saveButton, cancelButton);
    }

    private void save() {
        if (!validateForm()) {
            NotificationUtils.error("Por favor, complete/corrija los campos marcados.");
            return;
        }

        WaitingRoomCreateRequestDto dto = WaitingRoomCreateRequestDto.builder().clientId(clientField.getValue().getId())
                .petId(petField.getValue().getId()).reasonForVisit(reasonField.getValue()).priority(priorityField
                        .getValue()).notes(notesField.getValue()).arrivalTime(arrivalTimeField.getValue()).build();

        if (editingWaitingRoom == null) {
            waitingRoomService.save(dto);
            NotificationUtils.success("Entrada creada exitosamente.");
        } else {
            editingWaitingRoom.setClient(clientField.getValue());
            editingWaitingRoom.setPet(petField.getValue());
            editingWaitingRoom.setReasonForVisit(reasonField.getValue());
            editingWaitingRoom.setPriority(priorityField.getValue());
            editingWaitingRoom.setNotes(notesField.getValue());
            editingWaitingRoom.setArrivalTime(arrivalTimeField.getValue());
            waitingRoomService.update(editingWaitingRoom);
            NotificationUtils.success("Entrada actualizada exitosamente.");
        }
        if (onSave != null)
            onSave.accept(dto);
        close();
    }

    private boolean validateForm() {
        boolean valid = true;
        if (clientField.isEmpty()) {
            clientField.setInvalid(true);
            valid = false;
        } else
            clientField.setInvalid(false);

        if (petField.isEmpty()) {
            petField.setInvalid(true);
            valid = false;
        } else
            petField.setInvalid(false);

        if (reasonField.isEmpty()) {
            reasonField.setInvalid(true);
            valid = false;
        } else
            reasonField.setInvalid(false);

        if (priorityField.isEmpty()) {
            priorityField.setInvalid(true);
            valid = false;
        } else
            priorityField.setInvalid(false);

        if (arrivalTimeField.isEmpty() || arrivalTimeField.getValue() == null) {
            arrivalTimeField.setInvalid(true);
            arrivalTimeField.setErrorMessage("La hora de llegada es requerida");
            valid = false;
        } else if (arrivalTimeField.getValue().isBefore(LocalDateTime.now())) {
            arrivalTimeField.setInvalid(true);
            arrivalTimeField.setErrorMessage("La hora de llegada debe ser en el futuro");
            valid = false;
        } else {
            arrivalTimeField.setInvalid(false);
        }

        return valid;
    }

    public void openForNew() {
        clearForm();
        editingWaitingRoom = null;
        saveButton.setText("Guardar");
        open();
    }

    public void openForEdit(WaitingRoom waitingRoom) {
        editingWaitingRoom = waitingRoom;
        clientField.setValue(waitingRoom.getClient());
        petField.setValue(waitingRoom.getPet());
        reasonField.setValue(waitingRoom.getReasonForVisit());
        priorityField.setValue(waitingRoom.getPriority());
        notesField.setValue(waitingRoom.getNotes());
        arrivalTimeField.setValue(waitingRoom.getArrivalTime());
        saveButton.setText("Actualizar");
        open();
    }

    private void clearForm() {
        clientField.clear();
        petField.clear();
        reasonField.clear();
        priorityField.clear();
        notesField.clear();
        arrivalTimeField.clear();
        clientField.setInvalid(false);
        petField.setInvalid(false);
        reasonField.setInvalid(false);
        priorityField.setInvalid(false);
    }
}

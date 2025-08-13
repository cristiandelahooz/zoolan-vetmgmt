package com.wornux.views.waitingroom;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.data.entity.Client;
import com.wornux.data.entity.Pet;
import com.wornux.data.enums.Priority;
import com.wornux.dto.request.WaitingRoomCreateRequestDto;
import com.wornux.services.interfaces.ClientService;
import com.wornux.services.interfaces.PetService;
import com.wornux.services.interfaces.WaitingRoomService;
import com.wornux.utils.NotificationUtils;
import com.wornux.views.clients.SelectOwnerDialog;

import java.util.List;
import java.util.function.Consumer;

public class WaitingRoomForm extends Dialog {

    private static final int MAX_REASON_LENGTH = 255;
    private static final int MAX_NOTES_LENGTH  = 255;

    private final TextField clientName = new TextField("Dueño");
    private final Button selectClientButton = new Button("Seleccionar");
    private final ComboBox<Pet> petComboBox = new ComboBox<>("Mascota");
    private final TextField reasonForVisit = new TextField("Razón de la Visita");
    private final ComboBox<Priority> priorityComboBox = new ComboBox<>("Prioridad");
    private final TextArea notes = new TextArea("Notas");

    private final Button saveButton = new Button("Guardar");
    private final Button cancelButton = new Button("Cancelar");

    private final SelectOwnerDialog selectOwnerDialog;
    private final transient ClientService clientService;
    private final transient PetService petService;
    private final transient WaitingRoomService waitingRoomService;

    private transient Client selectedClient;
    private Long clientId;

    private final Binder<WaitingRoomFormModel> binder = new BeanValidationBinder<>(WaitingRoomFormModel.class);
    private final transient WaitingRoomFormModel model = new WaitingRoomFormModel();

    private Consumer<WaitingRoomCreateRequestDto> onSave;

    public WaitingRoomForm(WaitingRoomService waitingRoomService, ClientService clientService, PetService petService) {

        this.waitingRoomService = waitingRoomService;
        this.clientService = clientService;
        this.petService = petService;
        this.selectOwnerDialog = new SelectOwnerDialog(clientService);

        setHeaderTitle("Nueva Entrada - Sala de Espera");
        setWidth("600px");
        setModal(true);

        createForm();
        setupEvents();
        setupValidation();
    }

    private void createForm() {
        clientName.setReadOnly(true);

        priorityComboBox.setItems(Priority.values());
        petComboBox.setItemLabelGenerator(Pet::getName);

        FormLayout formLayout = new FormLayout();
        formLayout.add(clientName, selectClientButton, petComboBox, reasonForVisit, priorityComboBox, notes);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        HorizontalLayout buttons = new HorizontalLayout(cancelButton, saveButton);
        buttons.addClassNames(LumoUtility.JustifyContent.END, LumoUtility.Gap.MEDIUM);

        VerticalLayout content = new VerticalLayout(
                new H3("Registro en Sala de Espera"),
                formLayout,
                buttons
        );
        content.setPadding(true);
        add(content);
    }

    private void setupEvents() {
        selectClientButton.addClickListener(e -> selectOwnerDialog.open());

        selectOwnerDialog.addClienteSeleccionadoListener(cliente -> {
            selectedClient = cliente;
            clientId = cliente.getId();
            clientName.setValue(cliente.getFirstName() + " " + cliente.getLastName());

            List<Pet> pets = petService.getPetsByOwnerId2(clientId);
            petComboBox.setItems(pets);
            petComboBox.setItemLabelGenerator(Pet::getName);

            clientName.setInvalid(false);
            petComboBox.setInvalid(false);
        });

        saveButton.addClickListener(this::save);
        cancelButton.addClickListener(e -> close());
    }

    private void setupValidation() {
        petComboBox.setRequired(true);
        petComboBox.setRequiredIndicatorVisible(true);
        petComboBox.setErrorMessage("Debe seleccionar una mascota");

        reasonForVisit.setRequired(true);
        reasonForVisit.setRequiredIndicatorVisible(true);
        reasonForVisit.setMaxLength(MAX_REASON_LENGTH);
        reasonForVisit.setErrorMessage("La razón de la visita es requerida");

        priorityComboBox.setRequired(true);
        priorityComboBox.setRequiredIndicatorVisible(true);
        priorityComboBox.setErrorMessage("La prioridad es requerida");

        notes.setMaxLength(MAX_NOTES_LENGTH);
        notes.setHelperText("Máx. " + MAX_NOTES_LENGTH + " caracteres");

        binder.forField(reasonForVisit)
                .asRequired("La razón de la visita es requerida")
                .bind(WaitingRoomFormModel::getReasonForVisit, WaitingRoomFormModel::setReasonForVisit);

        binder.forField(priorityComboBox)
                .asRequired("La prioridad es requerida")
                .bind(WaitingRoomFormModel::getPriority, WaitingRoomFormModel::setPriority);

        binder.forField(notes)
                .bind(WaitingRoomFormModel::getNotes, WaitingRoomFormModel::setNotes);
    }

    private void save(ClickEvent<Button> event) {

        if (!validateForm()) {
            NotificationUtils.error("Por favor, complete/corrija los campos marcados.");
            return;
        }

        model.setClientId(clientId);
        model.setPetId(petComboBox.getValue().getId());

        WaitingRoomCreateRequestDto dto = model.toDto();

        try {
            waitingRoomService.save(dto);
            NotificationUtils.success("Entrada agregada exitosamente a la sala de espera");

            if (onSave != null) onSave.accept(dto);
            close();
            clearForm();
        } catch (Exception e) {
            NotificationUtils.error("Error al guardar: " + e.getMessage());
        }
    }

    /** Valida campos obligatorios y longitudes, y marca los componentes como inválidos si aplica. */
    private boolean validateForm() {
        boolean ok = true;

        if (clientId == null) {
            clientName.setInvalid(true);
            clientName.setErrorMessage("Debe seleccionar un cliente");
            ok = false;
        } else {
            clientName.setInvalid(false);
        }

        if (petComboBox.isEmpty()) {
            petComboBox.setInvalid(true);
            petComboBox.setErrorMessage("Debe seleccionar una mascota");
            ok = false;
        } else {
            petComboBox.setInvalid(false);
        }

        if (reasonForVisit.isEmpty()) {
            reasonForVisit.setInvalid(true);
            reasonForVisit.setErrorMessage("La razón de la visita es requerida");
            ok = false;
        } else if (reasonForVisit.getValue().length() > MAX_REASON_LENGTH) {
            reasonForVisit.setInvalid(true);
            reasonForVisit.setErrorMessage("Máximo " + MAX_REASON_LENGTH + " caracteres");
            ok = false;
        } else {
            reasonForVisit.setInvalid(false);
        }

        if (priorityComboBox.isEmpty()) {
            priorityComboBox.setInvalid(true);
            priorityComboBox.setErrorMessage("La prioridad es requerida");
            ok = false;
        } else {
            priorityComboBox.setInvalid(false);
        }

        if (notes.getValue() != null && notes.getValue().length() > MAX_NOTES_LENGTH) {
            notes.setInvalid(true);
            notes.setErrorMessage("Notas: máximo " + MAX_NOTES_LENGTH + " caracteres");
            ok = false;
        } else {
            notes.setInvalid(false);
        }

        ok = binder.validate().isOk() && ok;

        return ok;
    }

    public void openForNew() {
        binder.setBean(model);
        clientName.clear();
        clientId = null;
        selectedClient = null;
        petComboBox.clear();
        petComboBox.setItems();
        reasonForVisit.clear();
        priorityComboBox.clear();
        notes.clear();
        
        clientName.setInvalid(false);
        petComboBox.setInvalid(false);
        reasonForVisit.setInvalid(false);
        priorityComboBox.setInvalid(false);
        notes.setInvalid(false);

        open();
    }

    public void setOnSave(Consumer<WaitingRoomCreateRequestDto> listener) {
        this.onSave = listener;
    }

    public void clearForm() {
        binder.setBean(new WaitingRoomFormModel());
        clientName.clear();
        petComboBox.clear();
        reasonForVisit.clear();
        priorityComboBox.clear();
        notes.clear();
        clientId = null;
        selectedClient = null;

        clientName.setInvalid(false);
        petComboBox.setInvalid(false);
        reasonForVisit.setInvalid(false);
        priorityComboBox.setInvalid(false);
        notes.setInvalid(false);
    }
}

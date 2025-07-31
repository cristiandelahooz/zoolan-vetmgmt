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
import com.vaadin.hilla.crud.FormService;
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

    private final TextField clientName = new TextField("Dueño");
    private final Button selectClientButton = new Button("Seleccionar");
    private final ComboBox<Pet> petComboBox = new ComboBox<>("Mascota");
    private final TextField reasonForVisit = new TextField("Razón de la Visita");
    private final ComboBox<Priority> priorityComboBox = new ComboBox<>("Prioridad");
    private final TextArea notes = new TextArea("Notas");

    private final Button saveButton = new Button("Guardar");
    private final Button cancelButton = new Button("Cancelar");

    private final SelectOwnerDialog selectOwnerDialog;
    private final ClientService clientService;
    private final PetService petService;
    private final WaitingRoomService waitingRoomService;


    private Client selectedClient;
    private Long clientId;

    private final Binder<WaitingRoomFormModel> binder = new BeanValidationBinder<>(WaitingRoomFormModel.class);
    private final WaitingRoomFormModel model = new WaitingRoomFormModel();

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

            // Cargar mascotas del cliente seleccionado
            List<Pet> pets = petService.getPetsByOwnerId2(clientId);
            petComboBox.setItems(pets);
            petComboBox.setItemLabelGenerator(Pet::getName);
        });

        saveButton.addClickListener(this::save);
        cancelButton.addClickListener(e -> close());
    }

    private void setupValidation() {
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
        if (clientId == null) {
            NotificationUtils.error("Debe seleccionar un cliente");
            return;
        }

        Pet selectedPet = petComboBox.getValue();
        if (selectedPet == null) {
            NotificationUtils.error("Debe seleccionar una mascota");
            return;
        }

        model.setClientId(clientId);
        model.setPetId(selectedPet.getId());

        WaitingRoomCreateRequestDto dto = model.toDto();


        try {
            //waitingRoomService.save(dto);
            ((FormService<WaitingRoomCreateRequestDto, Long>) waitingRoomService).save(dto);
            NotificationUtils.success("Entrada agregada exitosamente a la sala de espera");

            if (onSave != null) onSave.accept(dto);
            close();
        } catch (Exception e) {
            NotificationUtils.error("Error al guardar: " + e.getMessage());
        }
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
        open();
    }

    public void setOnSave(Consumer<WaitingRoomCreateRequestDto> listener) {
        this.onSave = listener;
    }
}

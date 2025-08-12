package com.wornux.views.pets;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.data.entity.Client;
import com.wornux.data.enums.*;
import com.wornux.dto.request.PetCreateRequestDto;
import com.wornux.services.interfaces.ClientService;
import com.wornux.services.interfaces.PetService;
import com.wornux.utils.NotificationUtils;
import com.wornux.views.clients.SelectOwnerDialog;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class PetForm extends Dialog {

    private final TextField name = new TextField("Nombre");
    private final ComboBox<PetType> type = new ComboBox<>("Tipo");
    private final ComboBox<String> breed = new ComboBox<>("Raza");
    private final ComboBox<Gender> gender = new ComboBox<>("Género");
    private final DatePicker birthDate = new DatePicker("Fecha de Nacimiento");
    private final TextField color = new TextField("Color");
    private final ComboBox<PetSize> size = new ComboBox<>("Tamaño");
    private final ComboBox<FurType> furType = new ComboBox<>("Tipo de Pelo");

    private final ClientService clientService;
    private final Button selectOwnerButton = new Button("Seleccionar");
    private final TextField ownerName = new TextField("Dueño"); // solo display
    private Client selectedOwner;
    private Long ownerId;               
    private final SelectOwnerDialog selectOwnerDialog;

    private final Button saveButton = new Button("Guardar");
    private final Button cancelButton = new Button("Cancelar");

    private final PetService petService;

    @Setter
    private Runnable onSaveCallback;

    private final List<Consumer<PetCreateRequestDto>> petSavedListeners = new ArrayList<>();
    private final List<Runnable> petCancelledListeners = new ArrayList<>();

    public PetForm(PetService petService, ClientService clientService) {
        this.petService = petService;
        this.clientService = clientService;
        this.selectOwnerDialog = new SelectOwnerDialog(clientService);

        setHeaderTitle("Nueva Mascota");
        setModal(true);
        setWidth("700px");
        setHeight("auto");

        createForm();
        setupRequiredIndicators();
        setupEventListeners();
    }

    private void createForm() {
        ownerName.setReadOnly(true);

        type.setItems(PetType.values());
        type.setItemLabelGenerator(PetType::name);
        type.addValueChangeListener(e -> {
            PetType selected = e.getValue();
            if (selected != null) {
                breed.setItems(selected.getBreeds());
                breed.setValue(null);
            } else {
                breed.clear();
                breed.setItems();
            }
        });

        gender.setItems(Gender.values());
        size.setItems(PetSize.values());
        furType.setItems(FurType.values());

        FormLayout layout = new FormLayout();
        layout.add(name, type, breed, birthDate, gender, color, size, furType);
        layout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        HorizontalLayout ownerLayout = new HorizontalLayout(ownerName, selectOwnerButton);
        ownerLayout.setAlignItems(FlexComponent.Alignment.END);

        VerticalLayout content = new VerticalLayout(
                new H3("Información de la Mascota"),
                layout,
                new H3("Dueño"),
                ownerLayout
        );
        content.addClassNames(LumoUtility.Padding.MEDIUM);

        HorizontalLayout buttons = new HorizontalLayout(cancelButton, saveButton);
        buttons.addClassNames(LumoUtility.JustifyContent.END, LumoUtility.Gap.MEDIUM);

        add(content, buttons);
    }

    /**
     * Solo indicadores/mensajes visuales. La validación real ocurre en validateForm().
     */
    private void setupRequiredIndicators() {
        name.setRequired(true);
        name.setRequiredIndicatorVisible(true);
        name.setErrorMessage("El nombre es requerido");

        type.setRequired(true);
        type.setRequiredIndicatorVisible(true);
        type.setErrorMessage("El tipo es requerido");

        breed.setRequired(true);
        breed.setRequiredIndicatorVisible(true);
        breed.setErrorMessage("La raza es requerida");

        size.setRequired(true);
        size.setRequiredIndicatorVisible(true);
        size.setErrorMessage("El tamaño es requerido");

        birthDate.setRequired(true);
        birthDate.setRequiredIndicatorVisible(true);
        birthDate.setErrorMessage("La fecha de nacimiento es requerida");

        gender.setRequired(true);
        gender.setRequiredIndicatorVisible(true);
        gender.setErrorMessage("El género es requerido");

        furType.setRequired(true);
        furType.setRequiredIndicatorVisible(true);
        furType.setErrorMessage("El tipo de pelo es requerido");

        color.setRequired(true);
        color.setRequiredIndicatorVisible(true);
        color.setErrorMessage("El color es requerido");

    }

    private void setupEventListeners() {
        saveButton.addClickListener(this::save);
        cancelButton.addClickListener(e -> {
            firePetCancelledEvent();
            close();
        });

        selectOwnerButton.addClickListener(e -> selectOwnerDialog.open());

        selectOwnerDialog.addClienteSeleccionadoListener(cliente -> {
            ownerId = cliente.getId();
            selectedOwner = cliente;
            ownerName.setInvalid(false);
            ownerName.setValue(cliente.getFirstName() + " " + cliente.getLastName());
        });
    }

    private void save(ClickEvent<Button> event) {
        try {
            if (!validateForm()) {
                NotificationUtils.error("Por favor, complete todos los campos requeridos");
                return;
            }

            PetCreateRequestDto dto = new PetCreateRequestDto(
                    name.getValue(),
                    type.getValue(),
                    breed.getValue(),
                    birthDate.getValue(),
                    ownerId,
                    gender.getValue(),
                    color.getValue(),
                    size.getValue(),
                    furType.getValue()
            );

            petService.createPet(dto);
            NotificationUtils.success("Mascota creada exitosamente");
            firePetSavedEvent(dto);

            if (onSaveCallback != null) {
                onSaveCallback.run();
            }

            close();
        } catch (Exception e) {
            log.error("Error creando mascota", e);
            NotificationUtils.error("Error al crear mascota: " + e.getMessage());
        }
    }

    /**
     * Validación manual y visual. Marca los campos inválidos e indica si falta seleccionar dueño.
     */
    private boolean validateForm() {
        boolean isValid = true;

        if (name.isEmpty()) { name.setInvalid(true); isValid = false; } else { name.setInvalid(false); }
        if (type.isEmpty()) { type.setInvalid(true); isValid = false; } else { type.setInvalid(false); }
        if (breed.isEmpty()) { breed.setInvalid(true); isValid = false; } else { breed.setInvalid(false); }
        if (color.isEmpty()) { color.setInvalid(true); isValid = false; } else { color.setInvalid(false); }
        if (birthDate.isEmpty()) { birthDate.setInvalid(true); isValid = false; } else { birthDate.setInvalid(false); }
        if (gender.isEmpty()) { gender.setInvalid(true); isValid = false; } else { gender.setInvalid(false); }
        if (size.isEmpty()) { size.setInvalid(true); isValid = false; } else { size.setInvalid(false); }
        if (furType.isEmpty()) { furType.setInvalid(true); isValid = false; } else { furType.setInvalid(false); }

        if (ownerId == null) {
            ownerName.setInvalid(true);
            ownerName.setErrorMessage("Debe seleccionar un dueño");
            isValid = false;
        } else {
            ownerName.setInvalid(false);
        }

        if (!type.isEmpty() && !breed.isEmpty()) {
            PetType t = type.getValue();
            String b = breed.getValue();
            if (t != null && (b == null || !t.isValidBreedForType(b))) {
                breed.setInvalid(true);
                breed.setErrorMessage("La raza no es válida para el tipo seleccionado");
                isValid = false;
            }
        }

        return isValid;
    }

    /** Limpia el formulario para crear nuevo registro. */
    public void openForNew() {
        clearForm();
        resetValidationStates();
        name.focus();
        open();
    }

    private void clearForm() {
        name.clear();
        type.clear();
        breed.clear();
        birthDate.clear();
        gender.clear();
        color.clear();
        size.clear();
        furType.clear();

        ownerName.clear();
        ownerId = null;
        selectedOwner = null;
    }

    private void resetValidationStates() {
        name.setInvalid(false);
        type.setInvalid(false);
        breed.setInvalid(false);
        color.setInvalid(false);
        size.setInvalid(false);
        furType.setInvalid(false);
        birthDate.setInvalid(false);
        gender.setInvalid(false);
        ownerName.setInvalid(false);
    }

    public void addPetSavedListener(Consumer<PetCreateRequestDto> listener) {
        petSavedListeners.add(listener);
    }

    public void addPetCancelledListener(Runnable listener) {
        petCancelledListeners.add(listener);
    }

    private void firePetSavedEvent(PetCreateRequestDto dto) {
        petSavedListeners.forEach(l -> l.accept(dto));
    }

    private void firePetCancelledEvent() {
        petCancelledListeners.forEach(Runnable::run);
    }
}

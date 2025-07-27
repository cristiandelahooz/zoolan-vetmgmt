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
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
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
    private final Button selectOwnerButton = new Button("Seleccionar Dueño");
    private final TextField ownerName = new TextField("Dueño");
    private Client selectedOwner;
    private Long ownerId;
    private final SelectOwnerDialog selectOwnerDialog;

    private final Button saveButton = new Button("Guardar");
    private final Button cancelButton = new Button("Cancelar");

    private final Binder<PetCreateRequestDto> binder = new BeanValidationBinder<>(PetCreateRequestDto.class);

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
        setupValidation();
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
            }
        });

        gender.setItems(Gender.values());
        size.setItems(PetSize.values());
        furType.setItems(FurType.values());

        FormLayout layout = new FormLayout();
        layout.add(name, type, breed, birthDate, gender, color, size, furType);
        layout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

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

    private void setupValidation() {
        binder.forField(name)
                .asRequired("El nombre es requerido")
                .bind(PetCreateRequestDto::getName, PetCreateRequestDto::setName);

        binder.forField(type)
                .asRequired("El tipo es requerido")
                .bind(PetCreateRequestDto::getType, PetCreateRequestDto::setType);

        binder.forField(breed)
                .asRequired("La raza es requerida")
                .withValidator(b -> {
                    PetType selected = type.getValue();
                    return selected != null && selected.isValidBreedForType(b);
                }, "La raza no es válida para el tipo seleccionado")
                .bind(PetCreateRequestDto::getBreed, PetCreateRequestDto::setBreed);

        binder.forField(birthDate).bind(PetCreateRequestDto::getBirthDate, PetCreateRequestDto::setBirthDate);
        binder.forField(gender).bind(PetCreateRequestDto::getGender, PetCreateRequestDto::setGender);
        binder.forField(color).bind(PetCreateRequestDto::getColor, PetCreateRequestDto::setColor);
        binder.forField(size).bind(PetCreateRequestDto::getSize, PetCreateRequestDto::setSize);
        binder.forField(furType).bind(PetCreateRequestDto::getFurType, PetCreateRequestDto::setFurType);
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
            ownerName.setValue(cliente.getFirstName() + " " + cliente.getLastName());
        });
    }

    private void save(ClickEvent<Button> event) {
        try {
            if (ownerId == null) {
                NotificationUtils.error("Debe seleccionar un dueño");
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

    public void openForNew() {
        binder.readBean(null);
        name.focus();
        open();
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

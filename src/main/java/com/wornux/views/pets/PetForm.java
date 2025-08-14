package com.wornux.views.pets;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
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
import com.wornux.data.entity.Pet;
import com.wornux.data.enums.FurType;
import com.wornux.data.enums.Gender;
import com.wornux.data.enums.PetSize;
import com.wornux.data.enums.PetType;
import com.wornux.dto.request.PetCreateRequestDto;
import com.wornux.dto.request.PetUpdateRequestDto;
import com.wornux.services.interfaces.ClientService;
import com.wornux.services.interfaces.PetService;
import com.wornux.utils.NotificationUtils;
import com.wornux.views.clients.SelectOwnerDialog;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class PetForm extends Dialog {

    /* ---------- Campos Mascota ---------- */
    private final TextField name = new TextField("Nombre");
    private final ComboBox<PetType> type = new ComboBox<>("Tipo");
    private final ComboBox<String> breed = new ComboBox<>("Raza");
    private final ComboBox<Gender> gender = new ComboBox<>("Género");
    private final DatePicker birthDate = new DatePicker("Fecha de Nacimiento");
    private final TextField color = new TextField("Color");
    private final ComboBox<PetSize> size = new ComboBox<>("Tamaño");
    private final ComboBox<FurType> furType = new ComboBox<>("Tipo de Pelo");

    /* ---------- Dueños (Creación - dueño único con diálogo) ---------- */
    private final Button selectOwnerButton = new Button("Seleccionar");
    private final TextField ownerName = new TextField("Dueño"); // solo display/lectura
    private Client selectedOwner;
    private Long ownerId; // se setea al elegir un dueño en el diálogo
    private final SelectOwnerDialog selectOwnerDialog;
    private HorizontalLayout ownerCreateLayout; // referencia para ocultar/mostrar

    /* ---------- Dueños (Edición - múltiples) ---------- */
    private final MultiSelectComboBox<Client> owners = new MultiSelectComboBox<>("Dueños");

    /* ---------- Botones ---------- */
    private final Button saveButton = new Button("Guardar");
    private final Button cancelButton = new Button("Cancelar");

    /* ---------- Estado ---------- */
    private boolean isEditing = false;
    private Long petIdToEdit;

    /* ---------- Servicios ---------- */
    private final PetService petService;
    private final ClientService clientService;

    /* ---------- Callbacks externos ---------- */
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
        /* Dueño (creación) */
        ownerName.setReadOnly(true);

        /* Dueños (edición - Multi) */
        owners.setItemLabelGenerator(c -> (nvl(c.getFirstName()) + " " + nvl(c.getLastName())).trim());
        owners.setClearButtonVisible(true);
        owners.setPlaceholder("Selecciona uno o más dueños");
        owners.setItems(clientService.getAllActiveClients());
        owners.setWidthFull();
        owners.setVisible(false);

        /* Combos mascota */
        type.setItems(PetType.values());
        type.setItemLabelGenerator(PetType::name);
        type.addValueChangeListener(e -> {
            PetType selected = e.getValue();
            if (selected != null) {
                breed.clear();
                breed.setItems(selected.getBreeds());
            } else {
                breed.clear();
                breed.setItems();
            }
        });

        gender.setItems(Gender.values());
        size.setItems(PetSize.values());
        furType.setItems(FurType.values());

        /* Layout datos mascota */
        FormLayout layout = new FormLayout();
        layout.add(name, type, breed, birthDate, gender, color, size, furType);
        layout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        /* Layout dueño (creación) */
        ownerCreateLayout = new HorizontalLayout(ownerName, selectOwnerButton);
        ownerCreateLayout.setAlignItems(FlexComponent.Alignment.END);

        /* Contenido principal */
        VerticalLayout content = new VerticalLayout(
                new H3("Información de la Mascota"),
                layout,
                ownerCreateLayout,
                owners
        );
        content.addClassNames(LumoUtility.Padding.MEDIUM);

        /* Botonera */
        HorizontalLayout buttons = new HorizontalLayout(cancelButton, saveButton);
        buttons.addClassNames(LumoUtility.JustifyContent.END, LumoUtility.Gap.MEDIUM);

        add(content, buttons);
    }

    /** Solo indicadores/mensajes visuales. La validación real ocurre en validateForm(). */
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

        birthDate.setRequired(true);
        birthDate.setRequiredIndicatorVisible(true);
        birthDate.setErrorMessage("La fecha de nacimiento es requerida");

        gender.setRequired(true);
        gender.setRequiredIndicatorVisible(true);
        gender.setErrorMessage("El género es requerido");

        size.setRequired(true);
        size.setRequiredIndicatorVisible(true);
        size.setErrorMessage("El tamaño es requerido");

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
            selectedOwner = cliente;
            ownerId = cliente.getId();
            ownerName.setInvalid(false);
            ownerName.setValue(nvl(cliente.getFirstName()) + " " + nvl(cliente.getLastName()));
        });
    }

    private void save(ClickEvent<Button> e) {
        try {
            if (!validateForm()) {
                NotificationUtils.error("Por favor, complete/corrija los campos marcados.");
                return;
            }

            if (isEditing && petIdToEdit != null) {
                // UPDATE
                PetUpdateRequestDto dto = new PetUpdateRequestDto();
                dto.setName(trimOrNull(name.getValue()));
                dto.setType(type.getValue());
                dto.setBreed(trimOrNull(breed.getValue()));
                dto.setBirthDate(birthDate.getValue());
                dto.setGender(gender.getValue());
                dto.setColor(trimOrNull(color.getValue()));
                dto.setSize(size.getValue());
                dto.setFurType(furType.getValue());


                var selectedOwnerIds = owners.getSelectedItems().stream()
                        .map(Client::getId)
                        .toList();

                if (!selectedOwnerIds.isEmpty()) {
                    dto.setOwnerIds(selectedOwnerIds);
                } else {
                    dto.setOwnerIds(null);
                }

                petService.updatePet(petIdToEdit, dto);
                NotificationUtils.success("Mascota actualizada exitosamente");
                firePetSavedEvent(null);
                if (onSaveCallback != null) onSaveCallback.run();
                close();
                return;
            }

            // CREATE (requiere un dueño seleccionado)
            if (ownerId == null) {
                ownerName.setInvalid(true);
                ownerName.setErrorMessage("Debe seleccionar un dueño");
                NotificationUtils.error("Debe seleccionar un dueño");
                return;
            }

            PetCreateRequestDto createDto = new PetCreateRequestDto();
            createDto.setName(trimOrNull(name.getValue()));
            createDto.setType(type.getValue());
            createDto.setBreed(trimOrNull(breed.getValue()));
            createDto.setBirthDate(birthDate.getValue());
            createDto.setOwnerId(ownerId);
            createDto.setGender(gender.getValue());
            createDto.setColor(trimOrNull(color.getValue()));
            createDto.setSize(size.getValue());
            createDto.setFurType(furType.getValue());

            petService.createPet(createDto);

            NotificationUtils.success("Mascota creada exitosamente");
            firePetSavedEvent(null);
            if (onSaveCallback != null) onSaveCallback.run();
            close();

        } catch (Exception ex) {
            log.error("Error al guardar mascota", ex);
            NotificationUtils.error("Error al guardar la mascota: " + ex.getMessage());
        }
    }

    /** Valida requeridos. En creación exige selección de dueño. */
    private boolean validateForm() {
        boolean ok = true;

        if (name.isEmpty()) { name.setInvalid(true); ok = false; } else name.setInvalid(false);
        if (type.isEmpty()) { type.setInvalid(true); ok = false; } else type.setInvalid(false);
        if (breed.isEmpty()) { breed.setInvalid(true); ok = false; } else breed.setInvalid(false);
        if (birthDate.isEmpty()) { birthDate.setInvalid(true); ok = false; } else birthDate.setInvalid(false);
        if (gender.isEmpty()) { gender.setInvalid(true); ok = false; } else gender.setInvalid(false);
        if (color.isEmpty()) { color.setInvalid(true); ok = false; } else color.setInvalid(false);
        if (size.isEmpty()) { size.setInvalid(true); ok = false; } else size.setInvalid(false);
        if (furType.isEmpty()) { furType.setInvalid(true); ok = false; } else furType.setInvalid(false);

        // En creación, requerir dueño
        if (!isEditing && ownerId == null) {
            ownerName.setInvalid(true);
            ok = false;
        } else {
            ownerName.setInvalid(false);
        }


        if (!type.isEmpty() && !breed.isEmpty()) {
            PetType t = type.getValue();
            String b = breed.getValue();
            if (t != null && b != null && !t.getBreeds().contains(b)) {
                breed.setInvalid(true);
                breed.setErrorMessage("La raza no pertenece al tipo seleccionado");
                ok = false;
            }
        }

        return ok;
    }

    public void openForEdit(Pet pet) {
        isEditing = true;
        petIdToEdit = pet.getId();
        setHeaderTitle("Editar Mascota");

        type.setValue(pet.getType());
        if (pet.getType() != null) {
            breed.setItems(pet.getType().getBreeds());
        } else {
            breed.clear();
            breed.setItems();
        }

        name.setValue(nvl(pet.getName()));
        breed.setValue(safeBreedValue(pet.getBreed(), pet.getType()));
        birthDate.setValue(pet.getBirthDate());
        gender.setValue(pet.getGender());
        color.setValue(nvl(pet.getColor()));
        size.setValue(pet.getSize());
        furType.setValue(pet.getFurType());

        // Mostrar primer dueño (referencial)
        if (pet.getOwners() != null && !pet.getOwners().isEmpty()) {
            Client first = pet.getOwners().get(0);
            ownerName.setValue(nvl(first.getFirstName()) + " " + nvl(first.getLastName()));
        } else {
            ownerName.clear();
        }

        // EDICIÓN: mostrar Multi de dueños
        owners.setVisible(true);
        ownerCreateLayout.setVisible(false);

        owners.clear();
        if (pet.getOwners() != null) {
            owners.setValue(new HashSet<>(pet.getOwners())); // preselección
        }

        
        selectedOwner = null;
        ownerId = null;

        resetInvalid();
        open();
    }

    public void openForNew() {
        isEditing = false;
        petIdToEdit = null;
        setHeaderTitle("Nueva Mascota");
        clearForm();
        resetInvalid();

        // CREACIÓN: ocultar Multi y mostrar UI de creación
        owners.clear();
        owners.setVisible(false);
        ownerCreateLayout.setVisible(true);

        name.focus();
        open();
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static String trimOrNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    /** Si la raza actual no pertenece al tipo, devolvemos null para evitar invalidaciones forzadas */
    private String safeBreedValue(String currentBreed, PetType currentType) {
        if (currentBreed == null || currentType == null) return null;
        return currentType.getBreeds().contains(currentBreed) ? currentBreed : null;
    }

    private void resetInvalid() {
        name.setInvalid(false);
        type.setInvalid(false);
        breed.setInvalid(false);
        birthDate.setInvalid(false);
        gender.setInvalid(false);
        color.setInvalid(false);
        size.setInvalid(false);
        furType.setInvalid(false);
        ownerName.setInvalid(false);
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

    /* ---------- Listeners externos ---------- */
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

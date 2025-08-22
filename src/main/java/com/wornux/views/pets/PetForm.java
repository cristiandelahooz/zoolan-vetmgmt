package com.wornux.views.pets;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
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
import com.wornux.views.clients.SelectOwnersMultiDialog;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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

  /* ---------- Dueños (Creación) ---------- */
  private final Button selectOwnerButton = new Button("Seleccionar");
  private final TextField ownerName = new TextField("Dueño");
  private Client selectedOwner;
  private Long ownerId;
  private final SelectOwnerDialog selectOwnerDialog;
  private HorizontalLayout ownerCreateLayout;

  /* ---------- Dueños (Edición) ---------- */
  private final HorizontalLayout ownersLayout = new HorizontalLayout();
  private final Button selectOwnersButton = new Button("Seleccionar Dueños");
  private final Div ownersChips = new Div();
  private final List<Client> selectedOwners = new ArrayList<>();
  private final SelectOwnersMultiDialog selectOwnersMultiDialog;

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
  @Setter private Runnable onSaveCallback;
  private final List<Consumer<PetCreateRequestDto>> petSavedListeners = new ArrayList<>();
  private final List<Runnable> petCancelledListeners = new ArrayList<>();

  public PetForm(PetService petService, ClientService clientService) {
    this.petService = petService;
    this.clientService = clientService;
    this.selectOwnerDialog = new SelectOwnerDialog(clientService);
    this.selectOwnersMultiDialog = new SelectOwnersMultiDialog(clientService);

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
    type.addValueChangeListener(
        e -> {
          PetType selected = e.getValue();
          breed.clear();
          breed.setItems(selected != null ? selected.getBreeds() : new ArrayList<>());
        });

    gender.setItems(Gender.values());
    size.setItems(PetSize.values());
    furType.setItems(FurType.values());

    FormLayout layout = new FormLayout();
    layout.add(name, type, breed, birthDate, gender, color, size, furType);
    layout.setResponsiveSteps(
        new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("500px", 2));

    ownerCreateLayout = new HorizontalLayout(ownerName, selectOwnerButton);
    ownerCreateLayout.setAlignItems(FlexComponent.Alignment.END);

    ownersLayout.add(ownersChips, selectOwnersButton);
    ownersLayout.setVisible(false);

    VerticalLayout content =
        new VerticalLayout(
            new H3("Información de la Mascota"), layout, ownerCreateLayout, ownersLayout);
    content.addClassNames(LumoUtility.Padding.MEDIUM);

    HorizontalLayout buttons = new HorizontalLayout(cancelButton, saveButton);
    buttons.addClassNames(LumoUtility.JustifyContent.END, LumoUtility.Gap.MEDIUM);

    add(content, buttons);
  }

  private void setupRequiredIndicators() {
    name.setRequiredIndicatorVisible(true);
    type.setRequiredIndicatorVisible(true);
    breed.setRequiredIndicatorVisible(true);
    birthDate.setRequiredIndicatorVisible(true);
    gender.setRequiredIndicatorVisible(true);
    size.setRequiredIndicatorVisible(true);
    furType.setRequiredIndicatorVisible(true);
    color.setRequiredIndicatorVisible(true);
  }

  private void setupEventListeners() {
    saveButton.addClickListener(this::save);
    cancelButton.addClickListener(
        e -> {
          firePetCancelledEvent();
          close();
        });

    // Creación
    selectOwnerButton.addClickListener(e -> selectOwnerDialog.open());
    selectOwnerDialog.addClienteSeleccionadoListener(
        cliente -> {
          selectedOwner = cliente;
          ownerId = cliente.getId();
          ownerName.setInvalid(false);
          ownerName.setValue(nvl(cliente.getFirstName()) + " " + nvl(cliente.getLastName()));
        });

    // Edición
    selectOwnersButton.addClickListener(
        e -> {
          selectOwnersMultiDialog.open(
              selectedOwners,
              owners -> {
                updateOwnersChips(owners);
                selectedOwners.clear();
                selectedOwners.addAll(owners);
              });
        });
  }

  private void save(ClickEvent<Button> e) {
    if (!validateForm()) {
      NotificationUtils.error("Por favor, complete/corrija los campos marcados.");
      return;
    }

    if (isEditing && petIdToEdit != null) {
      PetUpdateRequestDto dto = new PetUpdateRequestDto();
      dto.setName(trimOrNull(name.getValue()));
      dto.setType(type.getValue());
      dto.setBreed(trimOrNull(breed.getValue()));
      dto.setBirthDate(birthDate.getValue());
      dto.setGender(gender.getValue());
      dto.setColor(trimOrNull(color.getValue()));
      dto.setSize(size.getValue());
      dto.setFurType(furType.getValue());
      dto.setOwnerIds(selectedOwners.stream().map(Client::getId).toList());

      petService.updatePet(petIdToEdit, dto);
      NotificationUtils.success("Mascota actualizada exitosamente");
      firePetSavedEvent(null);
      if (onSaveCallback != null) onSaveCallback.run();
      close();
      return;
    }

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
  }

  private boolean validateForm() {
    boolean ok = true;
    if (name.isEmpty()) {
      name.setInvalid(true);
      ok = false;
    } else name.setInvalid(false);
    if (type.isEmpty()) {
      type.setInvalid(true);
      ok = false;
    } else type.setInvalid(false);
    if (breed.isEmpty()) {
      breed.setInvalid(true);
      ok = false;
    } else breed.setInvalid(false);
    if (birthDate.isEmpty()) {
      birthDate.setInvalid(true);
      ok = false;
    } else birthDate.setInvalid(false);
    if (gender.isEmpty()) {
      gender.setInvalid(true);
      ok = false;
    } else gender.setInvalid(false);
    if (color.isEmpty()) {
      color.setInvalid(true);
      ok = false;
    } else color.setInvalid(false);
    if (size.isEmpty()) {
      size.setInvalid(true);
      ok = false;
    } else size.setInvalid(false);
    if (furType.isEmpty()) {
      furType.setInvalid(true);
      ok = false;
    } else furType.setInvalid(false);

    if (!isEditing && ownerId == null) {
      ownerName.setInvalid(true);
      ok = false;
    } else {
      ownerName.setInvalid(false);
    }
    if (birthDate.isEmpty()) {
      birthDate.setInvalid(true);
      birthDate.setErrorMessage("Debe seleccionar una fecha de nacimiento");
      ok = false;
    } else if (birthDate.getValue().isAfter(java.time.LocalDate.now())) {
      birthDate.setInvalid(true);
      birthDate.setErrorMessage("La fecha de nacimiento no puede ser futura");
      ok = false;
    } else {
      birthDate.setInvalid(false);
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
    breed.setItems(pet.getType() != null ? pet.getType().getBreeds() : new ArrayList<>());
    name.setValue(nvl(pet.getName()));
    breed.setValue(safeBreedValue(pet.getBreed(), pet.getType()));
    birthDate.setValue(pet.getBirthDate());
    gender.setValue(pet.getGender());
    color.setValue(nvl(pet.getColor()));
    size.setValue(pet.getSize());
    furType.setValue(pet.getFurType());

    ownersLayout.setVisible(true);
    ownerCreateLayout.setVisible(false);
    updateOwnersChips(pet.getOwners());
    selectedOwners.clear();
    selectedOwners.addAll(pet.getOwners());

    ownerId = null;
    selectedOwner = null;

    resetInvalid();
    open();
  }

  public void openForNew() {
    isEditing = false;
    petIdToEdit = null;
    setHeaderTitle("Nueva Mascota");
    clearForm();
    resetInvalid();

    ownersLayout.setVisible(false);
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
    selectedOwners.clear();
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

  private void updateOwnersChips(List<Client> owners) {
    ownersChips.removeAll();
    for (Client c : owners) {
      Span chip = new Span(c.getFirstName() + " " + c.getLastName());
      chip.getElement().getThemeList().add("badge pill");
      ownersChips.add(chip);
    }
  }
}

package com.wornux.views.consultations;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.data.entity.Employee;
import com.wornux.data.entity.Pet;
import com.wornux.dto.request.CreateConsultationRequestDto;
import com.wornux.dto.request.UpdateConsultationRequestDto;
import com.wornux.data.entity.Consultation;
import com.wornux.services.interfaces.ConsultationService;
import com.wornux.services.interfaces.EmployeeService;
import com.wornux.services.interfaces.PetService;
import com.wornux.utils.NotificationUtils;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ConsultationsForm extends Dialog {
    private final TextArea notes = new TextArea("Notas");
    private final TextArea diagnosis = new TextArea("Diagnóstico");
    private final TextArea treatment = new TextArea("Tratamiento");
    private final TextArea prescription = new TextArea("Prescripción");
    private final DatePicker consultationDate = new DatePicker("Fecha de Consulta");
    private final ComboBox<String> consultationTime = new ComboBox<>("Hora de Consulta");
    private final TextField petId = new TextField("ID Mascota");
    private final TextField veterinarianId = new TextField("ID Veterinario");
    private final TextField selectedVeterinarianField = new TextField("Veterinario seleccionado");
    private final TextField selectedPetField = new TextField("Mascota seleccionada");

    private final Button saveButton = new Button("Guardar");
    private final Button cancelButton = new Button("Cancelar");
    private Employee selectedVeterinarian;
    private Pet selectedPet;
    private final Button selectVeterinarianButton = new Button("Seleccionar Veterinario");
    private final Button selectPetButton = new Button("Seleccionar Mascota");

    private final transient ConsultationService consultationService;
    private final transient EmployeeService employeeService;
    private final transient PetService petService;

    @Setter
    private transient Consumer<Consultation> onSaveCallback;

    private transient Consultation editingConsultation;

    public ConsultationsForm(ConsultationService consultationService, EmployeeService employeeService,
                             PetService petService) {
        this.consultationService = consultationService;
        this.employeeService = employeeService;
        this.petService = petService;

        setHeaderTitle("Consulta");
        setModal(true);
        setWidth("600px");
        setHeight("auto");

        createForm();
        setupValidation();
        setupEventListeners();
    }

    private void createForm() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(
                notes, diagnosis, treatment, prescription,
                consultationDate, consultationTime,
                selectVeterinarianButton, selectedVeterinarianField,
                selectPetButton, selectedPetField
        );
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        selectVeterinarianButton.addClickListener(e -> openVeterinarianDialog());
        selectPetButton.addClickListener(e -> openPetDialog());

        // Populate time slots (08:00 to 20:00, every 15 minutes)
        List<String> timeSlots = IntStream.rangeClosed(8 * 60, 20 * 60)
                .filter(min -> min % 15 == 0)
                .mapToObj(min -> String.format("%02d:%02d", min / 60, min % 60))
                .collect(Collectors.toList());
        consultationTime.setItems(timeSlots);
        consultationTime.setPlaceholder("Selecciona la hora");

        selectedVeterinarianField.setReadOnly(true);
        selectedPetField.setReadOnly(true);

        consultationDate.setRequired(true);
        consultationDate.setErrorMessage("La fecha de consulta es requerida");

        consultationTime.setRequired(true);
        consultationTime.setErrorMessage("La hora de consulta es requerida");

        petId.setRequired(true);
        petId.setErrorMessage("La mascota es requerida");

        veterinarianId.setRequired(true);
        veterinarianId.setErrorMessage("El veterinario es requerido");

        selectedVeterinarianField.setRequired(true);
        selectedVeterinarianField.setErrorMessage("Debe seleccionar un veterinario");

        selectedPetField.setRequired(true);
        selectedPetField.setErrorMessage("Debe seleccionar una mascota");

        VerticalLayout content = new VerticalLayout(
                new H3("Información de la Consulta"),
                formLayout
        );
        content.addClassNames(LumoUtility.Padding.MEDIUM);
        content.setWidth("auto");
        content.setHeight("auto");

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, saveButton);
        buttonLayout.addClassNames(LumoUtility.JustifyContent.END, LumoUtility.Gap.MEDIUM);

        add(content, buttonLayout);
    }

    private void openVeterinarianDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("900px");
        Grid<Employee> grid = new Grid<>(Employee.class, false);
        grid.setHeight("500px");
        grid.setItems(employeeService.getVeterinarians());

        // Show only vital columns
        grid.addColumn(Employee::getId).setHeader("ID").setKey("id");
        grid.addColumn(Employee::getFirstName).setHeader("Nombre").setKey("firstName");
        grid.addColumn(Employee::getLastName).setHeader("Apellido").setKey("lastName");
        grid.addColumn(e -> e.getEmployeeRole() != null ? e.getEmployeeRole().name() : "").setHeader("Rol").setKey("role");

        grid.addItemClickListener(event -> {
            selectedVeterinarian = event.getItem();
            veterinarianId.setValue(selectedVeterinarian.getId().toString());
            selectedVeterinarianField.setValue(
                    selectedVeterinarian.getId() + " - " +
                            selectedVeterinarian.getFirstName() + " " +
                            selectedVeterinarian.getLastName()
            );
            dialog.close();
        });
        dialog.add(grid);
        dialog.open();
    }

    private void openPetDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");
        Grid<Pet> grid = new Grid<>(Pet.class, false);
        grid.setHeight("500px");
        grid.setItems(petService.getAllPets());

        // Show only vital columns
        grid.addColumn(Pet::getId).setHeader("ID").setKey("id");
        grid.addColumn(Pet::getName).setHeader("Nombre").setKey("name");
        grid.addColumn(Pet::getType).setHeader("Tipo").setKey("type");
        grid.addColumn(Pet::getBreed).setHeader("Raza").setKey("breed");

        grid.addItemClickListener(event -> {
            selectedPet = event.getItem();
            petId.setValue(selectedPet.getId().toString());
            selectedPetField.setValue(
                    selectedPet.getId() + " - " +
                            selectedPet.getName()
            );
            dialog.close();
        });
        dialog.add(grid);
        dialog.open();
    }

    private void setupValidation() {
        notes.setRequired(true);
        consultationDate.setRequired(true);
        consultationTime.setRequired(true);
        petId.setRequired(true);
        veterinarianId.setRequired(true);
    }

    private void setupEventListeners() {
        saveButton.addClickListener(this::save);
        cancelButton.addClickListener(e -> close());
    }

    private void save(ClickEvent<Button> event) {
        if (!validateForm()) {
            NotificationUtils.error("Por favor, complete todos los campos requeridos");
            return;
        }

        LocalDate date = consultationDate.getValue();
        LocalTime time;
        try {
            time = LocalTime.parse(consultationTime.getValue());
        } catch (Exception e) {
            consultationTime.setInvalid(true);
            NotificationUtils.error("Selecciona una hora válida");
            return;
        }
        LocalDateTime dateTime = LocalDateTime.of(date, time);

        if (editingConsultation == null) {
            CreateConsultationRequestDto dto = CreateConsultationRequestDto.builder()
                    .notes(notes.getValue())
                    .diagnosis(diagnosis.getValue())
                    .treatment(treatment.getValue())
                    .prescription(prescription.getValue())
                    .consultationDate(dateTime)
                    .petId(Long.parseLong(petId.getValue()))
                    .veterinarianId(Long.parseLong(veterinarianId.getValue()))
                    .build();

            Consultation saved = consultationService.create(dto);
            NotificationUtils.success("Consulta creada exitosamente");
            if (onSaveCallback != null) onSaveCallback.accept(saved);
        } else {
            UpdateConsultationRequestDto dto = UpdateConsultationRequestDto.builder()
                    .notes(notes.getValue())
                    .diagnosis(diagnosis.getValue())
                    .treatment(treatment.getValue())
                    .prescription(prescription.getValue())
                    .consultationDate(dateTime.toString())
                    .petId(Long.parseLong(petId.getValue()))
                    .veterinarianId(Long.parseLong(veterinarianId.getValue()))
                    .build();

            Consultation updated = consultationService.partialUpdate(editingConsultation.getId(), dto);
            NotificationUtils.success("Consulta actualizada exitosamente");
            if (onSaveCallback != null) onSaveCallback.accept(updated);
        }
        close();
    }

    private boolean validateForm() {
        boolean isValid = true;
        if (consultationDate.isEmpty()) {
            consultationDate.setInvalid(true);
            consultationDate.setErrorMessage("La fecha de consulta es requerida");
            isValid = false;
        } else {
            consultationDate.setInvalid(false);
        }
        if (consultationTime.isEmpty()) {
            consultationTime.setInvalid(true);
            consultationTime.setErrorMessage("La hora de consulta es requerida");
            isValid = false;
        } else {
            consultationTime.setInvalid(false);
        }
        if (petId.isEmpty()) {
            petId.setInvalid(true);
            petId.setErrorMessage("La mascota es requerida");
            isValid = false;
        } else {
            petId.setInvalid(false);
        }
        if (veterinarianId.isEmpty()) {
            veterinarianId.setInvalid(true);
            veterinarianId.setErrorMessage("El veterinario es requerido");
            isValid = false;
        } else {
            veterinarianId.setInvalid(false);
        }
        if (selectedVeterinarianField.isEmpty()) {
            selectedVeterinarianField.setInvalid(true);
            selectedVeterinarianField.setErrorMessage("Debe seleccionar un veterinario");
            isValid = false;
        } else {
            selectedVeterinarianField.setInvalid(false);
        }
        if (selectedPetField.isEmpty()) {
            selectedPetField.setInvalid(true);
            selectedPetField.setErrorMessage("Debe seleccionar una mascota");
            isValid = false;
        } else {
            selectedPetField.setInvalid(false);
        }
        return isValid;
    }

    public void openForNew() {
        clearForm();
        saveButton.setText("Guardar");
        editingConsultation = null;
        open();
    }

    public void openForEdit(Consultation consultation) {
        notes.setValue(consultation.getNotes() != null ? consultation.getNotes() : "");
        diagnosis.setValue(consultation.getDiagnosis() != null ? consultation.getDiagnosis() : "");
        treatment.setValue(consultation.getTreatment() != null ? consultation.getTreatment() : "");
        prescription.setValue(consultation.getPrescription() != null ? consultation.getPrescription() : "");
        saveButton.setText("Actualizar");
        if (consultation.getConsultationDate() != null) {
            consultationDate.setValue(consultation.getConsultationDate().toLocalDate());
            consultationTime.setValue(consultation.getConsultationDate().toLocalTime().toString());
        } else {
            consultationDate.clear();
            consultationTime.clear();
        }
        petId.setValue(consultation.getPet() != null ? consultation.getPet().getId().toString() : "");
        veterinarianId.setValue(consultation.getVeterinarian() != null ? consultation.getVeterinarian().getId().toString() : "");
        editingConsultation = consultation;
        open();
    }

    private void clearForm() {
        notes.clear();
        diagnosis.clear();
        treatment.clear();
        prescription.clear();
        consultationDate.clear();
        consultationTime.clear();
        petId.clear();
        veterinarianId.clear();
        selectedVeterinarianField.clear();
        selectedPetField.clear();
        selectedVeterinarian = null;
        selectedPet = null;
        notes.setInvalid(false);
        consultationDate.setInvalid(false);
        consultationTime.setInvalid(false);
        petId.setInvalid(false);
        veterinarianId.setInvalid(false);
    }
}
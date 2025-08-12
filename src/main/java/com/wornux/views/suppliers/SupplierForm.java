package com.wornux.views.suppliers;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.dto.request.SupplierCreateRequestDto;
import com.wornux.dto.request.UpdateSupplierRequestDto;
import com.wornux.services.interfaces.SupplierService;
import com.wornux.utils.NotificationUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import static com.wornux.constants.ValidationConstants.*;

@Slf4j
public class SupplierForm extends Dialog {
    private final TextField rnc = new TextField("RNC");
    private final TextField companyName = new TextField("Nombre de la Empresa");
    private final TextField contactPerson = new TextField("Nombre del Contacto");
    private final TextField contactPhone = new TextField("Teléfono del Contacto");
    private final EmailField contactEmail = new EmailField("Correo Electrónico del Contacto");
    private final TextField province = new TextField("Provincia");
    private final TextField municipality = new TextField("Municipio");
    private final TextField sector = new TextField("Sector");
    private final TextField streetAddress = new TextField("Dirección");

    private final Button saveButton = new Button("Guardar");
    private final Button cancelButton = new Button("Cancelar");

    private final Binder<SupplierCreateRequestDto> binder = new BeanValidationBinder<>(SupplierCreateRequestDto.class);
    private final SupplierService supplierService;

    @Setter private Runnable onSaveCallback;

    private boolean isEditing = false;
    private Long supplierIdToEdit;

    public SupplierForm(SupplierService supplierService) {
        this.supplierService = supplierService;
        setHeaderTitle("Proveedor");
        setModal(true);
        setWidth("700px");
        setHeight("auto");

        createForm();
        setupValidation();
        setupEventListeners();
    }

    private void createForm() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(rnc, companyName, contactPerson, contactPhone, contactEmail,
                province, municipality, sector, streetAddress);

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        HorizontalLayout buttons = new HorizontalLayout(cancelButton, saveButton);
        buttons.addClassNames(LumoUtility.JustifyContent.END, LumoUtility.Gap.MEDIUM);

        VerticalLayout content = new VerticalLayout(
                new H3("Información del Proveedor"),
                formLayout,
                buttons
        );
        content.addClassNames(LumoUtility.Padding.MEDIUM);

        add(content);
    }

    private void setupValidation() {
        // RNC requerido (11 dígitos)
        rnc.setRequired(true);
        rnc.setRequiredIndicatorVisible(true);
        rnc.setMaxLength(11);
        rnc.setAllowedCharPattern("\\d");
        rnc.setErrorMessage("RNC requerido (11 dígitos)");

        // Empresa requerida
        companyName.setRequired(true);
        companyName.setRequiredIndicatorVisible(true);
        companyName.setErrorMessage("El nombre de la empresa es requerido");

        // Contacto requerido
        contactPerson.setRequired(true);
        contactPerson.setRequiredIndicatorVisible(true);
        contactPerson.setErrorMessage("El nombre del contacto es requerido");

        // Teléfono requerido (formato dominicano, 10 dígitos)
        contactPhone.setRequired(true);
        contactPhone.setRequiredIndicatorVisible(true);
        contactPhone.setMaxLength(10);
        contactPhone.setAllowedCharPattern("\\d");
        contactPhone.setPlaceholder("8098498297");
        contactPhone.setErrorMessage("El teléfono debe ser dominicano (809/849/829 + 7 dígitos)");

        // Email opcional (se valida si se ingresa)
        contactEmail.setErrorMessage("Correo electrónico inválido");

        // Dirección requerida (alineado con @NotNull del backend)
        province.setRequired(true);
        province.setRequiredIndicatorVisible(true);
        province.setErrorMessage("La provincia es requerida");

        municipality.setRequired(true);
        municipality.setRequiredIndicatorVisible(true);
        municipality.setErrorMessage("El municipio es requerido");

        sector.setRequired(true);
        sector.setRequiredIndicatorVisible(true);
        sector.setErrorMessage("El sector es requerido");

        streetAddress.setRequired(true);
        streetAddress.setRequiredIndicatorVisible(true);
        streetAddress.setErrorMessage("La dirección es requerida");

        ((BeanValidationBinder<SupplierCreateRequestDto>) binder).bindInstanceFields(this);
    }

    private void setupEventListeners() {
        saveButton.addClickListener(this::save);
        cancelButton.addClickListener(e -> close());
    }

    private void save(ClickEvent<Button> event) {
        try {
            if (!validateForm()) {
                NotificationUtils.error("Por favor, complete/corrija los campos marcados.");
                return;
            }

            SupplierCreateRequestDto dto = new SupplierCreateRequestDto();
            dto.setRnc(rnc.getValue().trim());
            dto.setCompanyName(companyName.getValue().trim());
            dto.setContactPerson(contactPerson.getValue().trim());
            dto.setContactPhone(contactPhone.getValue().trim());
            dto.setContactEmail(emptyToNull(contactEmail.getValue())); // opcional
            dto.setProvince(province.getValue().trim());
            dto.setMunicipality(municipality.getValue().trim());
            dto.setSector(sector.getValue().trim());
            dto.setStreetAddress(streetAddress.getValue().trim());

            if (isEditing && supplierIdToEdit != null) {
                UpdateSupplierRequestDto updateDto = new UpdateSupplierRequestDto();
                updateDto.setId(supplierIdToEdit);
                updateDto.setRnc(dto.getRnc());
                updateDto.setCompanyName(dto.getCompanyName());
                updateDto.setContactPerson(dto.getContactPerson());
                updateDto.setContactPhone(dto.getContactPhone());
                updateDto.setContactEmail(dto.getContactEmail());
                updateDto.setProvince(dto.getProvince());
                updateDto.setMunicipality(dto.getMunicipality());
                updateDto.setSector(dto.getSector());
                updateDto.setStreetAddress(dto.getStreetAddress());
                updateDto.setActive(true);

                supplierService.update(updateDto);
                NotificationUtils.success("Proveedor actualizado exitosamente");
            } else {
                supplierService.save(dto);
                NotificationUtils.success("Proveedor creado exitosamente");
            }

            if (onSaveCallback != null) onSaveCallback.run();
            close();
        } catch (Exception e) {
            log.error("Error al guardar proveedor", e);
            NotificationUtils.error("Error al guardar proveedor: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        boolean ok = true;

        if (rnc.isEmpty() || !rnc.getValue().matches(RNC_PATTERN)) {
            rnc.setInvalid(true);
            rnc.setErrorMessage("RNC requerido (11 dígitos)");
            ok = false;
        } else rnc.setInvalid(false);

        if (companyName.isEmpty()) {
            companyName.setInvalid(true);
            companyName.setErrorMessage("El nombre de la empresa es requerido");
            ok = false;
        } else companyName.setInvalid(false);

        if (contactPerson.isEmpty()) {
            contactPerson.setInvalid(true);
            contactPerson.setErrorMessage("El nombre del contacto es requerido");
            ok = false;
        } else contactPerson.setInvalid(false);

        if (contactPhone.isEmpty() || !contactPhone.getValue().matches(DOMINICAN_PHONE_PATTERN)) {
            contactPhone.setInvalid(true);
            contactPhone.setErrorMessage("El teléfono debe ser dominicano (809/849/829 + 7 dígitos)");
            ok = false;
        } else contactPhone.setInvalid(false);

        if (!contactEmail.isEmpty() && !contactEmail.getValue().matches(EMAIL_PATTERN)) {
            contactEmail.setInvalid(true);
            contactEmail.setErrorMessage("Correo electrónico inválido");
            ok = false;
        } else contactEmail.setInvalid(false);

        if (province.isEmpty()) { province.setInvalid(true); ok = false; } else province.setInvalid(false);
        if (municipality.isEmpty()) { municipality.setInvalid(true); ok = false; } else municipality.setInvalid(false);
        if (sector.isEmpty()) { sector.setInvalid(true); ok = false; } else sector.setInvalid(false);
        if (streetAddress.isEmpty()) { streetAddress.setInvalid(true); ok = false; } else streetAddress.setInvalid(false);

        return ok;
    }

    public void openForCreate() {
        isEditing = false;
        supplierIdToEdit = null;
        clearForm();
        resetValidationStates();
        rnc.focus();
        open();
    }

    public void openForEdit(SupplierCreateRequestDto dto, Long id) {
        isEditing = true;
        supplierIdToEdit = id;
        binder.readBean(dto);
        resetValidationStates();
        open();
    }

    private void clearForm() {
        rnc.clear();
        companyName.clear();
        contactPerson.clear();
        contactPhone.clear();
        contactEmail.clear();
        province.clear();
        municipality.clear();
        sector.clear();
        streetAddress.clear();
    }

    private void resetValidationStates() {
        rnc.setInvalid(false);
        companyName.setInvalid(false);
        contactPerson.setInvalid(false);
        contactPhone.setInvalid(false);
        contactEmail.setInvalid(false);
        province.setInvalid(false);
        municipality.setInvalid(false);
        sector.setInvalid(false);
        streetAddress.setInvalid(false);
    }

    private static String emptyToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}

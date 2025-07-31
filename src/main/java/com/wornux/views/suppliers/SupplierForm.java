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
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.dto.request.SupplierCreateRequestDto;
import com.wornux.dto.request.UpdateSupplierRequestDto;
import com.wornux.services.interfaces.SupplierService;
import com.wornux.utils.NotificationUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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

    @Setter
    private Runnable onSaveCallback;

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
        binder.forField(rnc).asRequired("RNC requerido").bind(SupplierCreateRequestDto::getRnc, SupplierCreateRequestDto::setRnc);
        binder.forField(companyName).asRequired("Nombre requerido").bind(SupplierCreateRequestDto::getCompanyName, SupplierCreateRequestDto::setCompanyName);
        binder.forField(contactPerson).bind(SupplierCreateRequestDto::getContactPerson, SupplierCreateRequestDto::setContactPerson);
        binder.forField(contactPhone).bind(SupplierCreateRequestDto::getContactPhone, SupplierCreateRequestDto::setContactPhone);
        binder.forField(contactEmail).bind(SupplierCreateRequestDto::getContactEmail, SupplierCreateRequestDto::setContactEmail);
        binder.forField(province).bind(SupplierCreateRequestDto::getProvince, SupplierCreateRequestDto::setProvince);
        binder.forField(municipality).bind(SupplierCreateRequestDto::getMunicipality, SupplierCreateRequestDto::setMunicipality);
        binder.forField(sector).bind(SupplierCreateRequestDto::getSector, SupplierCreateRequestDto::setSector);
        binder.forField(streetAddress).bind(SupplierCreateRequestDto::getStreetAddress, SupplierCreateRequestDto::setStreetAddress);
    }

    private void setupEventListeners() {
        saveButton.addClickListener(this::save);
        cancelButton.addClickListener(e -> close());
    }

    private void save(ClickEvent<Button> event) {
        try {
            SupplierCreateRequestDto dto = new SupplierCreateRequestDto();
            binder.writeBean(dto);

            if (isEditing && supplierIdToEdit != null) {
                UpdateSupplierRequestDto updateDto = new UpdateSupplierRequestDto();
                updateDto.setId(supplierIdToEdit); // importante para identificar al proveedor
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

            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            close();
        } catch (ValidationException e) {
            NotificationUtils.error("Error de validación: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error al guardar proveedor", e);
            NotificationUtils.error("Error al guardar proveedor: " + e.getMessage());
        }
    }

    public void openForCreate() {
        isEditing = false;
        supplierIdToEdit = null;
        binder.readBean(null);
        open();
    }

    public void openForEdit(SupplierCreateRequestDto dto, Long id) {
        isEditing = true;
        supplierIdToEdit = id;
        binder.readBean(dto);
        open();
    }
}

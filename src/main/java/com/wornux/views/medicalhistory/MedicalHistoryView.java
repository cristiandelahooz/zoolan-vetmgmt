package com.wornux.views.medicalhistory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.data.entity.Consultation;
import com.wornux.data.entity.Pet;
import com.wornux.services.interfaces.ConsultationService;
import com.wornux.services.interfaces.PetService;
import com.wornux.views.pets.SelectPetDialog;

import java.util.List;

@Route(value = "historial-medico")
@PageTitle("Historial Médico")
public class MedicalHistoryView extends VerticalLayout implements HasUrlParameter<Long> {

    private final PetService petService;
    private final ConsultationService consultationService;

    private Grid<Consultation> consultationsGrid;
    private Pet selectedPet;

    private TextField selectedPetField;
    private Button selectPetBtn;
    private HorizontalLayout petSelector;

    private boolean lockedByParam = false;

    public MedicalHistoryView(PetService petService, ConsultationService consultationService) {
        this.petService = petService;
        this.consultationService = consultationService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Historial Médico de Mascota");
        title.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.FontWeight.BOLD);

        selectedPetField = new TextField("Mascota");
        selectedPetField.setReadOnly(true);

        selectPetBtn = new Button("Seleccionar", e -> {
            SelectPetDialog dialog = new SelectPetDialog(petService);
            dialog.addPetSelectedListener(pet -> {
                selectedPet = pet;
                selectedPetField.setValue(pet.getName());
                loadMedicalHistory(pet.getId());
                // Si NO está bloqueado por parámetro, el botón debe seguir visible para poder cambiar
                updateSelectorUI();
            });
            dialog.open();
        });

        petSelector = new HorizontalLayout(selectedPetField, selectPetBtn);
        petSelector.setAlignItems(Alignment.END);

        consultationsGrid = new Grid<>(Consultation.class, false);
        consultationsGrid.addColumn(c -> c.getConsultationDate().toLocalDate())
                .setHeader("Fecha").setAutoWidth(true);
        consultationsGrid.addColumn(c -> {
            var v = c.getVeterinarian();
            return v != null ? v.getFirstName() + " " + v.getLastName() : "—";
        }).setHeader("Veterinario").setAutoWidth(true);
        consultationsGrid.addColumn(Consultation::getDiagnosis).setHeader("Diagnóstico").setAutoWidth(true);
        consultationsGrid.addColumn(Consultation::getTreatment).setHeader("Tratamiento").setAutoWidth(true);
        consultationsGrid.addItemClickListener(e -> openConsultationDetail(e.getItem()));

        add(title, petSelector, consultationsGrid);
        setFlexGrow(1, consultationsGrid);

        updateSelectorUI();
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter Long petId) {
        if (petId != null) {
            lockedByParam = true;
            petService.getPetById(petId).ifPresent(p -> {
                selectedPet = p;
                selectedPetField.setValue(p.getName());
                loadMedicalHistory(p.getId());
                updateSelectorUI();
            });
        } else {
            lockedByParam = false;
            selectedPet = null;
            selectedPetField.clear();
            consultationsGrid.setItems(List.of());
            updateSelectorUI();
        }
    }

    private void updateSelectorUI() {
        selectPetBtn.setVisible(!lockedByParam);
        if (!lockedByParam) {
            selectPetBtn.setText(selectedPet == null ? "Seleccionar" : "Cambiar");
        }
    }

    private void loadMedicalHistory(Long petId) {
        consultationsGrid.setItems(consultationService.findByPetId(petId));
    }

    private void openConsultationDetail(Consultation consultation) {
        Dialog detailDialog = new Dialog();
        detailDialog.setWidth("500px");

        VerticalLayout content = new VerticalLayout();
        content.add(new H2("Consulta del " + consultation.getConsultationDate().toString()));
        content.add(new Paragraph("Veterinario: " +
                consultation.getVeterinarian().getFirstName() + " " +
                consultation.getVeterinarian().getLastName()));
        content.add(new Paragraph("Diagnóstico: " + consultation.getDiagnosis()));
        content.add(new Paragraph("Tratamiento: " + consultation.getTreatment()));
        content.add(new Paragraph("Prescripción: " + consultation.getPrescription()));
        content.add(new Paragraph("Notas: " + consultation.getNotes()));

        detailDialog.add(content);
        detailDialog.open();
    }
}

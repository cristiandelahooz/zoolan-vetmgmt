package com.wornux.views.medicalhistory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.data.entity.Consultation;
import com.wornux.data.entity.Pet;
import com.wornux.services.interfaces.ConsultationService;
import com.wornux.services.interfaces.PetService;
import com.wornux.views.MainLayout;
import com.wornux.views.pets.SelectPetDialog;
import jakarta.annotation.security.RolesAllowed;
import java.util.List;

@PageTitle("Historial Médico")
@Route(value = "historial-medico", layout = MainLayout.class)
@RolesAllowed({"ROLE_SYSTEM_ADMIN", "ROLE_MANAGER", "ROLE_USER"})
public class MedicalHistoryView extends VerticalLayout {

  private final PetService petService;
  private final ConsultationService consultationService;

  private final Grid<Consultation> consultationsGrid;
  private Pet selectedPet;

  public MedicalHistoryView(PetService petService, ConsultationService consultationService) {
    this.petService = petService;
    this.consultationService = consultationService;

    setSizeFull();
    setPadding(true);
    setSpacing(true);

    // Título
    H2 title = new H2("Historial Médico de Mascota");
    title.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.FontWeight.BOLD);

    // Campo para mostrar la mascota seleccionada
    TextField selectedPetField = new TextField("Mascota");
    selectedPetField.setReadOnly(true);

    Button selectPetBtn =
        new Button(
            "Seleccionar",
            e -> {
              SelectPetDialog dialog = new SelectPetDialog(petService);
              dialog.addPetSelectedListener(
                  pet -> {
                    selectedPet = pet;
                    selectedPetField.setValue(pet.getName());
                    loadMedicalHistory(pet.getId());
                  });
              dialog.open();
            });

    HorizontalLayout petSelector = new HorizontalLayout(selectedPetField, selectPetBtn);
    petSelector.setAlignItems(Alignment.END);

    // Grid de consultas
    consultationsGrid = new Grid<>(Consultation.class, false);
    consultationsGrid
        .addColumn(c -> c.getConsultationDate().toLocalDate())
        .setHeader("Fecha")
        .setAutoWidth(true);
    consultationsGrid
        .addColumn(
            c -> c.getVeterinarian().getFirstName() + " " + c.getVeterinarian().getLastName())
        .setHeader("Veterinario")
        .setAutoWidth(true);
    consultationsGrid
        .addColumn(Consultation::getDiagnosis)
        .setHeader("Diagnóstico")
        .setAutoWidth(true);
    consultationsGrid
        .addColumn(Consultation::getTreatment)
        .setHeader("Tratamiento")
        .setAutoWidth(true);

    consultationsGrid.addItemClickListener(event -> openConsultationDetail(event.getItem()));

    add(title, petSelector, consultationsGrid);
    setFlexGrow(1, consultationsGrid);
  }

  private void openPetSelectionDialog() {
    SelectPetDialog dialog = new SelectPetDialog(petService);
    dialog.addPetSelectedListener(
        pet -> {
          selectedPet = pet;
          loadMedicalHistory(pet.getId());
        });
    dialog.open();
  }

  private void loadMedicalHistory(Long petId) {
    List<Consultation> consultations = consultationService.findByPetId(petId);
    consultationsGrid.setItems(consultations);
  }

  private void openConsultationDetail(Consultation consultation) {
    Dialog detailDialog = new Dialog();
    detailDialog.setWidth("500px");

    VerticalLayout content = new VerticalLayout();
    content.add(new H2("Consulta del " + consultation.getConsultationDate().toString()));
    content.add(
        new Paragraph(
            "Veterinario: "
                + consultation.getVeterinarian().getFirstName()
                + " "
                + consultation.getVeterinarian().getLastName()));
    content.add(new Paragraph("Diagnóstico: " + consultation.getDiagnosis()));
    content.add(new Paragraph("Tratamiento: " + consultation.getTreatment()));
    content.add(new Paragraph("Prescripción: " + consultation.getPrescription()));
    content.add(new Paragraph("Notas: " + consultation.getNotes()));

    detailDialog.add(content);
    detailDialog.open();
  }
}

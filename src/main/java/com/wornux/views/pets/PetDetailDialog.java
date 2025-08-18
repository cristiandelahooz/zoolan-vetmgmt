package com.wornux.views.pets;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.wornux.data.entity.Consultation;
import com.wornux.data.entity.Pet;
import com.wornux.services.interfaces.ConsultationService;
import com.vaadin.flow.component.UI;
import com.wornux.views.medicalhistory.MedicalHistoryView;
import java.util.List;

public class PetDetailDialog extends Dialog {

    private final ConsultationService consultationService;
    private final Pet pet;

    public PetDetailDialog(Pet pet, ConsultationService consultationService) {
        this.pet = pet;
        this.consultationService = consultationService;

        setHeaderTitle("Detalles de la Mascota");
        setWidth("600px");
        setModal(true);

        VerticalLayout content = new VerticalLayout();

        // 📝 Últimas 3 consultas
        List<Consultation> lastConsultations = consultationService.findByPetId(pet.getId())
                .stream()
                .limit(3)
                .toList();

        VerticalLayout consultationsLayout = new VerticalLayout();
        consultationsLayout.setPadding(false);
        consultationsLayout.setSpacing(false);

        if (lastConsultations.isEmpty()) {
            consultationsLayout.add(new Span("No hay consultas registradas."));
        } else {
            lastConsultations.forEach(c -> {
                Span info = new Span(
                        c.getConsultationDate().toLocalDate() + " • " +
                                c.getDiagnosis() + " • " +
                                (c.getTreatment() != null ? c.getTreatment() : "Sin tratamiento")
                );
                info.getElement().getStyle().set("margin-bottom", "0.5rem");
                consultationsLayout.add(info);
            });
        }



        Button viewHistoryBtn = new Button("Ver historial", e -> {
            close();
            UI.getCurrent().navigate(MedicalHistoryView.class, pet.getId());
        });


        content.add(consultationsLayout, viewHistoryBtn);
        add(content);
    }
}

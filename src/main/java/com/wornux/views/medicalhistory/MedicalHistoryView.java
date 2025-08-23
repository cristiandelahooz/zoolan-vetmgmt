package com.wornux.views.medicalhistory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.data.entity.Consultation;
import com.wornux.data.entity.Pet;
import com.wornux.services.interfaces.ConsultationService;
import com.wornux.services.interfaces.PetService;
import com.wornux.views.MainLayout;
import com.wornux.views.pets.SelectPetDialog;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;
import java.util.Locale;

@Route(value = "historial-medico", layout = MainLayout.class)
@RolesAllowed({ "ROLE_SYSTEM_ADMIN", "ROLE_MANAGER", "ROLE_USER","ROLE_EMP_VETERINARIAN","ROLE_EMP_LAB_TECHNICIAN" })
@PageTitle("Historial Médico")
public class MedicalHistoryView extends VerticalLayout implements HasUrlParameter<Long> {

    private final PetService petService;
    private final ConsultationService consultationService;

    private final Grid<Consultation> consultationsGrid;
    private final TextField selectedPetField;
    private final Button selectPetBtn;
    private final HorizontalLayout petSelector;
    private Pet selectedPet;
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
                // Si NO está bloqueado por parámetro, el botón debe seguir visible para poder
                // cambiar
                updateSelectorUI();
            });
            dialog.open();
        });

        petSelector = new HorizontalLayout(selectedPetField, selectPetBtn);
        petSelector.setAlignItems(Alignment.END);

        consultationsGrid = new Grid<>(Consultation.class, false);
        consultationsGrid.addColumn(c -> c.getConsultationDate().toLocalDate()).setHeader("Fecha").setAutoWidth(true);
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

    private void openConsultationDetail(Consultation c) {
        Dialog detailDialog = new Dialog();
        detailDialog.setHeaderTitle("Consulta del " + formatDateTime(c.getConsultationDate()));
        detailDialog.setWidth("720px");
        detailDialog.setMaxWidth("95vw");

        // ===== Contenido principal
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);

        // === Cabecera: Mascota + tipo (badge) + veterinario (badge)
        HorizontalLayout headerRow = new HorizontalLayout();
        headerRow.setAlignItems(HorizontalLayout.Alignment.CENTER);
        headerRow.setWidthFull();

        H2 petName = new H2(c.getPet() != null ? nullTo("—", c.getPet().getName()) : "—");
        petName.getStyle().set("margin", "0");

        Span petTypeBadge = buildTypeBadge(c.getPet());
        Span vetBadge = badge("Dr(a). " + vetName(c), "contrast");

        headerRow.add(petName, petTypeBadge, vetBadge);
        headerRow.expand(petName);

        // === Meta (responsive): fecha, veterinario, dueño, estado
        FormLayout meta = new FormLayout();
        meta.setWidthFull();
        meta.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("600px", 2));
        meta.addFormItem(new Span(formatDateTime(c.getConsultationDate())), "Fecha y hora");
        meta.addFormItem(new Span(vetName(c)), "Veterinario");
        meta.addFormItem(new Span(primaryOwnerName(c.getPet())), "Dueño principal");
        meta.addFormItem(new Span(Boolean.TRUE.equals(c.getActive()) ? "ACTIVA" : "INACTIVA"), "Estado");

        // === Secciones (Dx / Tx / Rx / Notas) con estilo de tarjeta
        VerticalLayout dxCard = sectionCard("Diagnóstico", textOrNA(c.getDiagnosis()), "primary");
        VerticalLayout txCard = sectionCard("Tratamiento", textOrNA(c.getTreatment()), "success");
        VerticalLayout rxCard = sectionCard("Prescripción", textOrNA(c.getPrescription()), "warning");
        VerticalLayout ntCard = sectionCard("Notas", textOrNA(c.getNotes()), "contrast");

        content.add(headerRow, meta, dxCard, txCard, rxCard, ntCard);
        detailDialog.add(content);

        // ===== Footer con botón cerrar
        Button close = new Button("Cerrar", e -> detailDialog.close());
        close.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
        detailDialog.getFooter().add(close);

        detailDialog.open();
    }

    // --- Badges Vaadin (sin CSS externo)
    private Span badge(String text, String theme) {
        Span b = new Span(text == null || text.isBlank() ? "—" : text);
        b.getElement().getThemeList().add("badge");
        b.getElement().getThemeList().add("pill");
        b.getElement().getThemeList().add("small");
        if (theme != null && !theme.isBlank())
            b.getElement().getThemeList().add(theme);
        return b;
    }

    private Span buildTypeBadge(Pet pet) {
        String label = (pet != null && pet.getType() != null) ? pet.getType().toString() : "—";
        Span chip = badge(label, "contrast");
        if (pet != null && pet.getType() != null) {
            switch (pet.getType()) {
            case PERRO -> chip.getElement().getThemeList().add("primary");
            case GATO -> chip.getElement().getThemeList().add("success");
            case AVE -> chip.getElement().getThemeList().add("warning");
            case HAMSTER -> chip.getElement().getThemeList().add("error");
            default -> chip.getElement().getThemeList().add("contrast");
            }
        }
        return chip;
    }

    private String vetName(Consultation c) {
        if (c == null || c.getVeterinarian() == null)
            return "—";
        var v = c.getVeterinarian();
        return (nullTo("", v.getFirstName()) + " " + nullTo("", v.getLastName())).trim();
    }

    private String primaryOwnerName(Pet pet) {
        try {
            if (pet != null && pet.getOwners() != null && !pet.getOwners().isEmpty()) {
                var o = pet.getOwners().get(0);
                return (nullTo("", o.getFirstName()) + " " + nullTo("", o.getLastName())).trim();
            }
        } catch (Exception ignored) {
        }
        return "—";
    }

    private String textOrNA(String s) {
        return (s == null || s.isBlank()) ? "N/A" : s;
    }

    private String nullTo(String defaultVal, String s) {
        return s == null || s.isBlank() ? defaultVal : s;
    }

    private String formatDateTime(java.time.LocalDateTime dt) {
        if (dt == null)
            return "—";
        return dt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    // --- Pequeña “tarjeta” con título + contenido y un badge de contexto
    private VerticalLayout sectionCard(String title, String body, String badgeTheme) {
        VerticalLayout card = new VerticalLayout();
        card.setSpacing(false);
        card.setPadding(true);
        card.addClassNames(LumoUtility.Border.ALL, LumoUtility.BorderColor.CONTRAST_10, LumoUtility.BorderRadius.LARGE);

        // título + badge
        HorizontalLayout head = new HorizontalLayout();
        head.setAlignItems(HorizontalLayout.Alignment.CENTER);
        head.setWidthFull();

        H3 h = new H3(title);
        h.getStyle().set("margin", "0");
        Span chip = badge(title.substring(0, 1).toUpperCase(Locale.ROOT), badgeTheme);
        head.add(h, chip);
        head.expand(h);

        Paragraph p = new Paragraph(body);
        p.getStyle().set("margin", "0.5rem 0 0 0");

        card.add(head, p);
        return card;
    }
}
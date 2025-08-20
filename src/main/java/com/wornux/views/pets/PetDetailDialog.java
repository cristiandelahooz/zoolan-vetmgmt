package com.wornux.views.pets;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.data.entity.Consultation;
import com.wornux.data.entity.Pet;
import com.wornux.data.entity.Client;
import com.wornux.services.interfaces.ConsultationService;
import com.wornux.views.medicalhistory.MedicalHistoryView;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Locale;

public class PetDetailDialog extends Dialog {

    private final ConsultationService consultationService;
    private final Pet pet;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public PetDetailDialog(Pet pet, ConsultationService consultationService) {
        this.pet = pet;
        this.consultationService = consultationService;

        setHeaderTitle("Detalles de la Mascota");
        setWidth("720px");
        setMaxWidth("95vw");
        setModal(true);

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(true);

        // ====== Consultas ordenadas (recientes primero) ======
        List<Consultation> consultations = Optional.ofNullable(
                        consultationService.findByPetId(pet.getId())
                ).orElseGet(List::of).stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Consultation::getConsultationDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();

        // —— Ficha rápida
        content.add(buildHeaderCard());

        // —— Alertas + snapshot clínico
        content.add(buildClinicalSnapshot(consultations));

        // —— Últimas 3 consultas (presentación compacta)
        content.add(buildLastConsultations(consultations));

        // —— Botón a historial completo
        Button viewHistoryBtn = new Button("Ver historial", e -> {
            close();
            UI.getCurrent().navigate(MedicalHistoryView.class, pet.getId());
        });
        viewHistoryBtn.setIcon(VaadinIcon.OPEN_BOOK.create());
        viewHistoryBtn.getStyle()
                .set("flex", "0 0 auto")          // no shrink
                .set("min-width", "max-content");
        content.add(viewHistoryBtn);

        add(content);
    }

    // ===========================
    // Sección: Ficha rápida
    // ===========================
    private VerticalLayout buildHeaderCard() {
        VerticalLayout box = new VerticalLayout();
        box.setSpacing(false);
        box.setPadding(true);
        box.addClassNames(LumoUtility.Border.ALL, LumoUtility.BorderColor.CONTRAST_10, LumoUtility.BorderRadius.LARGE);

        // Título + badge de tipo
        HorizontalLayout top = new HorizontalLayout();
        top.setAlignItems(HorizontalLayout.Alignment.CENTER);
        top.setWidthFull();

        H2 name = new H2(pet.getName() != null ? pet.getName() : "—");
        name.getStyle().set("margin", "0");

        Span typeBadge = buildTypeBadge(pet);
        top.add(name, typeBadge);
        top.expand(name);

        String type   = pet.getType()   != null ? pet.getType().toString()   : "—";
        String breed  = pet.getBreed()  != null ? pet.getBreed()             : "—";
        String gender = pet.getGender() != null ? pet.getGender().toString() : "—";
        String owner  = primaryOwnerName(pet).orElse("Sin dueño");

        FormLayout kvRow = new FormLayout(
                kvItem("Tipo",   fmtValueUpper(type),   "primary"),
                kvItem("Raza",   fmtValueUpper(breed),  "contrast"),
                kvItem("Género", fmtValueUpper(gender), "success"),
                kvItem("Dueño",  fmtValueUpper(owner),  "contrast")
        );
        kvRow.setWidthFull();
        kvRow.setResponsiveSteps(
                new ResponsiveStep("0",      1),
                new ResponsiveStep("480px",  2),
                new ResponsiveStep("800px",  4)
        );

        box.add(top, kvRow);
        return box;
    }

    private Optional<String> primaryOwnerName(Pet p) {
        try {
            List<Client> owners = p.getOwners();
            if (owners != null && !owners.isEmpty()) {
                Client c = owners.get(0);
                return Optional.of((c.getFirstName() + " " + c.getLastName()).trim());
            }
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    private Span buildTypeBadge(Pet pet) {
        String label = pet != null && pet.getType() != null ? pet.getType().toString() : "—";
        Span badge = new Span(label);
        badge.getElement().getThemeList().add("badge");
        badge.getElement().getThemeList().add("pill");
        badge.getElement().getThemeList().add("small");
        if (pet != null && pet.getType() != null) {
            switch (pet.getType()) {
                case PERRO   -> badge.getElement().getThemeList().add("primary");   // Azul
                case GATO    -> badge.getElement().getThemeList().add("success");   // Verde
                case AVE     -> badge.getElement().getThemeList().add("warning");   // Amarillo
                case CONEJO  -> badge.getElement().getThemeList().add("contrast");  // Gris
                case HAMSTER -> badge.getElement().getThemeList().add("error");     // Rojo
                case REPTIL  -> badge.getElement().getThemeList().add("success");   // Verde
                case OTRO    -> badge.getElement().getThemeList().add("contrast");  // Gris claro
            }
        } else {
            badge.getElement().getThemeList().add("contrast");
        }
        return badge;
    }

    // ===========================
    // Sección: Snapshot clínico
    // ===========================
    private VerticalLayout buildClinicalSnapshot(List<Consultation> consultations) {
        VerticalLayout panel = new VerticalLayout();
        panel.setSpacing(false);
        panel.setPadding(true);
        panel.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)");

        H3 title = new H3("Resumen clínico");
        title.getStyle().set("margin", "0 0 var(--lumo-space-m) 0");

        // 1) Alertas (alergias / precauciones) por heurística simple
        HorizontalLayout alertsRow = new HorizontalLayout();
        alertsRow.setSpacing(true);
        alertsRow.setPadding(false);
        alertsRow.setAlignItems(HorizontalLayout.Alignment.CENTER);

        Optional<String> allergyHit = detectAllergyFrom(consultations);
        if (allergyHit.isPresent()) {
            // alertsRow.add(buildBadge("Alergia reportada", "error"));
        } else {
            // alertsRow.add(buildBadge("Sin alergias registradas", "contrast"));
        }

        // 2) Problemas frecuentes (diagnósticos recientes, únicos)
        List<String> frequentProblems = topRecentDiagnoses(consultations, 5);
        HorizontalLayout problemsRow = new HorizontalLayout();
        problemsRow.setSpacing(true);
        problemsRow.setPadding(false);
        problemsRow.setAlignItems(HorizontalLayout.Alignment.CENTER);

        if (frequentProblems.isEmpty()) {
            problemsRow.add(new Span("Problemas recientes: —"));
        } else {
            problemsRow.add(new Span("Problemas recientes:"));
            frequentProblems.forEach(d -> problemsRow.add(buildBadge(d, "primary")));
        }

        // 3) Tratamiento y medicación recientes (últimos no vacíos)
        String lastTreatment = consultations.stream()
                .map(Consultation::getTreatment)
                .filter(s -> s != null && !s.isBlank())
                .findFirst().orElse("—");

        String lastPrescription = consultations.stream()
                .map(Consultation::getPrescription)
                .filter(s -> s != null && !s.isBlank())
                .findFirst().orElse("—");

        VerticalLayout meds = new VerticalLayout();
        meds.setSpacing(false);
        meds.setPadding(false);
        meds.add(new Paragraph("Tratamiento reciente: " + lastTreatment));
        meds.add(new Paragraph("Prescripción reciente: " + lastPrescription));

        panel.add(title, alertsRow, problemsRow, meds);
        return panel;
    }

    private Optional<String> detectAllergyFrom(List<Consultation> consultations) {
        final String[] keys = {"alerg", "alérg", "allerg"};
        return consultations.stream()
                .flatMap(c -> Arrays.stream(new String[]{
                        nullSafeLower(c.getDiagnosis()),
                        nullSafeLower(c.getNotes()),
                        nullSafeLower(c.getPrescription())
                }))
                .filter(Objects::nonNull)
                .filter(txt -> {
                    for (String k : keys) if (txt.contains(k)) return true;
                    return false;
                })
                .findFirst();
    }

    private List<String> topRecentDiagnoses(List<Consultation> consultations, int limit) {
        return consultations.stream()
                .map(Consultation::getDiagnosis)
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .map(this::normalizeDiagnosis)
                .filter(s -> !s.isBlank())
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }

    private String normalizeDiagnosis(String s) {
        String r = s.toLowerCase(Locale.ROOT).trim();
        r = r.replaceAll("\\s+", " ");
        r = r.replaceAll("[\\.;,:]+$", "");
        return capitalizeFirst(r);
    }

    private String capitalizeFirst(String s) {
        if (s.isBlank()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }

    private Span buildBadge(String text, String theme) {
        Span b = new Span(text);
        b.getElement().getThemeList().add("badge");
        b.getElement().getThemeList().add("pill");
        b.getElement().getThemeList().add("small");
        if (theme != null && !theme.isBlank()) {
            b.getElement().getThemeList().add(theme);
        }
        return b;
    }

    private String nullSafeLower(String s) {
        return s == null ? null : s.toLowerCase(Locale.ROOT);
    }

    // ===========================
    // Sección: Últimas 3 consultas
    // ===========================
    private VerticalLayout buildLastConsultations(List<Consultation> consultations) {
        VerticalLayout box = new VerticalLayout();
        box.setSpacing(false);
        box.setPadding(true);
        box.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)");

        H4 title = new H4("Últimas consultas");
        title.getStyle().set("margin", "0 0 var(--lumo-space-m) 0");
        box.add(title);

        List<Consultation> last3 = consultations.stream().limit(3).toList();
        if (last3.isEmpty()) {
            box.add(new Span("No hay consultas registradas."));
            return box;
        }

        last3.forEach(c -> {
            HorizontalLayout row = new HorizontalLayout();
            row.setAlignItems(HorizontalLayout.Alignment.CENTER);
            row.setWidthFull();

            Icon dot = VaadinIcon.CALENDAR_CLOCK.create();
            dot.getStyle().set("color", "var(--lumo-primary-color)");

            String date = c.getConsultationDate() != null ? DATE_FMT.format(c.getConsultationDate()) : "—";
            String dx = (c.getDiagnosis() != null && !c.getDiagnosis().isBlank()) ? c.getDiagnosis() : "Sin diagnóstico";
            String tr = (c.getTreatment() != null && !c.getTreatment().isBlank()) ? c.getTreatment() : "—";

            Span text = new Span(date + " • Dx: " + dx + " • Tx: " + tr);
            text.getStyle().set("white-space", "normal");

            row.add(dot, text);
            box.add(row);
        });

        return box;
    }

    private com.vaadin.flow.component.Component kvItem(String label, String value, String theme) {
        Span l = kvLabel(label);
        Span v = badgeValue(value, theme);
        HorizontalLayout row = new HorizontalLayout(l, v);
        row.setAlignItems(HorizontalLayout.Alignment.CENTER);
        row.setSpacing(true);
        return row;
    }

    private Span kvLabel(String text) {
        Span s = new Span(text + ":");
        s.addClassName(LumoUtility.FontWeight.MEDIUM);
        s.addClassName(LumoUtility.TextColor.SECONDARY);
        return s;
    }

    private Span badgeValue(String value, String theme) {
        Span b = new Span(value == null || value.isBlank() ? "—" : value);
        b.getElement().getThemeList().add("badge");
        b.getElement().getThemeList().add("pill");
        b.getElement().getThemeList().add("small");
        if (theme != null && !theme.isBlank()) {
            b.getElement().getThemeList().add(theme); 
        }
        return b;
    }

    private String fmtValueUpper(String s) { return s == null ? "—" : s.toUpperCase(Locale.ROOT); }
    private String fmtValueLower(String s) { return s == null ? "—" : s.toLowerCase(Locale.ROOT); }
}

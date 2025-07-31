package com.wornux.views.consultations;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.components.Breadcrumb;
import com.wornux.components.BreadcrumbItem;
import com.wornux.components.InfoIcon;
import com.wornux.data.entity.Consultation;
import com.wornux.services.interfaces.ConsultationService;
import com.wornux.services.interfaces.EmployeeService;
import com.wornux.services.interfaces.PetService;
import com.wornux.utils.GridUtils;
import com.wornux.utils.NotificationUtils;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;


@Route(value = "consultations")
@PageTitle("Consultas")
public class ConsultationsView extends Div {

    private final Grid<Consultation> grid = GridUtils.createBasicGrid(Consultation.class);
    private final TextField searchField = new TextField("Buscar consultas");
    private final Button create = new Button("Nueva Consulta");
    private final Span quantity = new Span();
    private final transient ConsultationService consultationService;
    private final transient ConsultationsForm consultationsForm;

    public ConsultationsView(@Qualifier("consultationServiceImpl") ConsultationService consultationService,
                             @Qualifier("employeeServiceImpl") EmployeeService employeeService,
                             @Qualifier("petServiceImpl") PetService petService) {
        this.consultationService = consultationService;
        this.consultationsForm = new ConsultationsForm(consultationService, employeeService, petService);

        setId("consultations-view");

        consultationsForm.setOnSaveCallback(saved -> {
            refreshAll();
            consultationsForm.close();
        });

        createGrid(consultationService, createFilterSpecification());

        final Div gridLayout = new Div(grid);
        gridLayout.addClassNames(LumoUtility.Margin.Horizontal.MEDIUM, LumoUtility.Padding.SMALL,
                LumoUtility.Height.FULL);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);

        add(createTitle(), createFilter(), gridLayout);
        addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);
        setSizeFull();

        create.addClickListener(event -> consultationsForm.openForNew());
    }

    private void createGrid(ConsultationService service, Specification<Consultation> specification) {
        GridUtils.configureGrid(grid, specification, service.getRepository());

        GridUtils.addColumn(grid, c -> c.getPet() != null ? c.getPet().getName() : "", "Mascota", "pet");
        GridUtils.addColumn(grid, c -> c.getVeterinarian() != null ? c.getVeterinarian().getFirstName() : "", "Veterinario", "veterinarian");
        GridUtils.addColumn(grid, Consultation::getConsultationDate, "Fecha de Consulta", "consultationDate");
        GridUtils.addColumn(grid, Consultation::getNotes, "Notas", "notes");
        GridUtils.addColumn(grid, Consultation::getDiagnosis, "Diagnóstico", "diagnosis");
        GridUtils.addColumn(grid, Consultation::getTreatment, "Tratamiento", "treatment");
        GridUtils.addColumn(grid, Consultation::getPrescription, "Prescripción", "prescription");
        GridUtils.addComponentColumn(grid, this::renderStatus, "Estado", "active");

        grid.addComponentColumn(this::createActionsColumn).setHeader("Acciones").setAutoWidth(true);
    }

    public Specification<Consultation> createFilterSpecification() {
        return (root, query, builder) -> {
            Order order = builder.desc(root.get("consultationDate"));
            if (query != null) {
                query.orderBy(order);
            }

            Predicate searchPredicate = builder.or(
                    builder.like(builder.lower(root.get("notes")), "%" + searchField.getValue().toLowerCase() + "%"),
                    builder.like(builder.lower(root.get("diagnosis")), "%" + searchField.getValue().toLowerCase() + "%"),
                    builder.like(builder.lower(root.get("treatment")), "%" + searchField.getValue().toLowerCase() + "%"),
                    builder.like(builder.lower(root.get("prescription")), "%" + searchField.getValue().toLowerCase() + "%"),
                    builder.like(builder.lower(root.get("pet").get("name")), "%" + searchField.getValue().toLowerCase() + "%"),
                    builder.like(builder.lower(root.get("veterinarian").get("firstName")), "%" + searchField.getValue().toLowerCase() + "%"),
                    builder.like(builder.lower(root.get("veterinarian").get("lastName")), "%" + searchField.getValue().toLowerCase() + "%")
            );

            return searchPredicate;
        };
    }

    private Div createTitle() {
        final Breadcrumb breadcrumb = new Breadcrumb();
        breadcrumb.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        breadcrumb.add(new BreadcrumbItem("Consultas", ConsultationsView.class),
                new BreadcrumbItem("Lista de Consultas", ConsultationsView.class));

        Icon icon = InfoIcon.INFO_CIRCLE.create("Gestionar consultas de la clínica veterinaria.");

        Div headerLayout = new Div(breadcrumb, icon);
        headerLayout.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.ROW,
                LumoUtility.Margin.Top.SMALL);

        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_SMALL);
        create.addClassNames(LumoUtility.Width.AUTO);

        Div layout = new Div(headerLayout, create);
        layout.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.FlexDirection.Breakpoint.Large.ROW, LumoUtility.JustifyContent.BETWEEN,
                LumoUtility.Margin.Horizontal.MEDIUM, LumoUtility.Margin.Top.SMALL, LumoUtility.Gap.XSMALL,
                LumoUtility.AlignItems.STRETCH, LumoUtility.AlignItems.Breakpoint.Large.END);

        return layout;
    }

    private Component createFilter() {
        searchField.setClearButtonVisible(true);
        searchField.setPlaceholder("Buscar por notas, diagnóstico, tratamiento...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.setWidthFull();
        searchField.addValueChangeListener(e -> refreshAll());

        HorizontalLayout toolbar = new HorizontalLayout(searchField);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        toolbar.setAlignItems(FlexComponent.Alignment.END);
        toolbar.addClassNames(LumoUtility.Margin.Horizontal.MEDIUM, LumoUtility.Margin.Top.SMALL,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.MEDIUM);

        return toolbar;
    }

    private void refreshAll() {
        List<Consultation> consultations = consultationService.findAll(Pageable.unpaged()).getContent();
        String filter = searchField.getValue().trim().toLowerCase();

        if (!filter.isEmpty()) {
            consultations = consultations.stream().filter(c ->
                    (c.getNotes() != null && c.getNotes().toLowerCase().contains(filter)) ||
                            (c.getDiagnosis() != null && c.getDiagnosis().toLowerCase().contains(filter)) ||
                            (c.getTreatment() != null && c.getTreatment().toLowerCase().contains(filter)) ||
                            (c.getPrescription() != null && c.getPrescription().toLowerCase().contains(filter)) ||
                            (c.getPet() != null && c.getPet().getId().toString().contains(filter)) ||
                            (c.getVeterinarian() != null && c.getVeterinarian().getId().toString().contains(filter))
            ).toList();
        }
        grid.setItems(consultations);
    }

    private Component createActionsColumn(Consultation consultation) {
        Button edit = new Button(new Icon(VaadinIcon.EDIT));
        edit.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        edit.getElement().setProperty("title", "Editar");
        edit.getStyle().set("min-width", "32px").set("width", "32px").set("padding", "0");

        Button delete = new Button(new Icon(VaadinIcon.TRASH));
        delete.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        delete.getElement().setProperty("title", "Eliminar");
        delete.getStyle().set("min-width", "32px").set("width", "32px").set("padding", "0");

        edit.addClickListener(e -> consultationsForm.openForEdit(consultation));
        delete.addClickListener(e -> {
            consultationService.delete(consultation.getId());
            NotificationUtils.success("Consulta eliminada");
            refreshAll();
        });

        HorizontalLayout actions = new HorizontalLayout(edit, delete);
        actions.setSpacing(true);
        actions.setPadding(false);
        actions.setMargin(false);
        actions.setWidth(null);
        return actions;
    }

    private Component renderStatus(Consultation consultation) {
        boolean isActive = consultation.isActive();
        Span badge = new Span(isActive ? "Activo" : "Inactivo");
        badge.getElement().getThemeList().add("badge pill");
        badge.getElement().getThemeList().add(isActive ? "success" : "error");
        return badge;
    }
}

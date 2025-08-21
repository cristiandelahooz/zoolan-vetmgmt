package com.wornux.views.pets;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
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
import com.vaadin.flow.theme.lumo.LumoIcon;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.components.Breadcrumb;
import com.wornux.components.BreadcrumbItem;
import com.wornux.components.InfoIcon;
import com.wornux.data.entity.Pet;
import com.wornux.data.enums.*;
import com.wornux.dto.response.PetSummaryResponseDto;
import com.wornux.services.interfaces.ClientService;
import com.wornux.services.interfaces.ConsultationService;
import com.wornux.services.interfaces.PetService;
import com.wornux.utils.GridUtils;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.domain.Specification;
import java.util.Optional;
import java.util.Set;

import static com.wornux.utils.PredicateUtils.createPredicateForSelectedItems;
import static com.wornux.utils.PredicateUtils.predicateForTextField;
import org.springframework.data.domain.Pageable;

@Slf4j
@Route("mascotas")
@PageTitle("Mascotas")
public class PetView extends Div {

    private final Grid<Pet> grid = GridUtils.createBasicGrid(Pet.class);

    private final TextField searchField = new TextField("Buscar mascotas");
    private final MultiSelectComboBox<PetType> typeFilter = new MultiSelectComboBox<>("Tipo");
    private final MultiSelectComboBox<Gender> genderFilter = new MultiSelectComboBox<>("Género");
    private final MultiSelectComboBox<PetSize> sizeFilter = new MultiSelectComboBox<>("Tamaño");
    private final Span quantity = new Span();

    private final Button create = new Button();
    private final PetService petService;
    private final PetForm petForm;
    private final ConsultationService consultationService;

    public PetView(@Qualifier("petServiceImpl") PetService petService,
            @Qualifier("clientServiceImpl") ClientService clientService,
                   @Qualifier("consultationServiceImpl") ConsultationService consultationService) {
        this.petService = petService;
        this.petForm = new PetForm(petService, clientService);
        this.consultationService = consultationService;

        setId("pet-view");

        petForm.addPetSavedListener(pet -> {
            refreshAll();
            petForm.close();
        });

        petForm.addPetCancelledListener(petForm::close);

        createGrid(petService, createFilterSpecification());

        final Div gridLayout = new Div(grid);
        gridLayout.addClassNames(LumoUtility.Margin.Horizontal.MEDIUM, LumoUtility.Padding.SMALL,
                LumoUtility.Height.FULL);

        add(createTitle(), createFilter(), gridLayout);
        addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);
        setSizeFull();

        create.addClickListener(event -> petForm.openForNew());

        add(petForm);
    }

    private void createGrid(PetService service, Specification<Pet> specification) {
        GridUtils.configureGrid(grid, specification, service.getRepository());
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        GridUtils.addColumn(grid, Pet::getName, "Nombre", "name");
        //GridUtils.addColumn(grid, pet -> pet.getType().name(), "Tipo", "type");
        // Tipo con colores
        grid.addComponentColumn(pet -> {
            Span badge = new Span(pet.getType() != null ? pet.getType().name() : "");
            badge.getElement().getThemeList().add("badge pill");
            if (pet.getType() != null) {
                switch (pet.getType()) {
                case PERRO -> badge.getElement().getThemeList().add("primary");
                case GATO -> badge.getElement().getThemeList().add("success");
                default -> badge.getElement().getThemeList().add("contrast");
                /*
                 * case PERRO -> badge.getElement().getThemeList().add("primary"); // Azul case GATO ->
                 * badge.getElement().getThemeList().add("success"); // Verde case AVE ->
                 * badge.getElement().getThemeList().add("warning"); // Amarillo case CONEJO ->
                 * badge.getElement().getThemeList().add("contrast"); // Gris case HAMSTER ->
                 * badge.getElement().getThemeList().add("error"); // Rojo case REPTIL ->
                 * badge.getElement().getThemeList().add("success"); // Verde case OTRO ->
                 * badge.getElement().getThemeList().add("contrast"); // Gris claro
                 */
                }
            }
            return badge;
        }).setHeader("Tipo");
        GridUtils.addColumn(grid, Pet::getBreed, "Raza", "breed");
        //GridUtils.addColumn(grid, pet -> pet.getGender() != null ? pet.getGender().name() : "", "Género", "gender");
        // Género con colores
        grid.addComponentColumn(pet -> {
            Span badge = new Span(pet.getGender() != null ? pet.getGender().name() : "");
            badge.getElement().getThemeList().add("badge pill");
            if (pet.getGender() != null) {
                switch (pet.getGender()) {
                case MASCULINO -> badge.getElement().getThemeList().add("primary"); // azul
                case FEMENINO -> badge.getElement().getThemeList().add("error"); // rojo/rosado
                }
            }
            return badge;
        }).setHeader("Género");
        GridUtils.addColumn(grid, Pet::getColor, "Color", "color");
        //GridUtils.addColumn(grid, pet -> pet.getSize() != null ? pet.getSize().name() : "", "Tamaño", "size");

        // Tamaño con colores
        grid.addComponentColumn(pet -> {
            Span badge = new Span(pet.getSize() != null ? pet.getSize().name() : "");
            badge.getElement().getThemeList().add("badge pill");
            if (pet.getSize() != null) {
                switch (pet.getSize()) {
                case PEQUEÑO -> badge.getElement().getThemeList().add("success"); // verde
                case MEDIANO -> badge.getElement().getThemeList().add("warning"); // amarillo
                case GRANDE -> badge.getElement().getThemeList().add("error"); // rojo
                }
            }
            return badge;
        }).setHeader("Tamaño");

        GridUtils.addColumn(grid,
                pet -> pet.getOwners().isEmpty()
                        ? "Sin dueño"
                        : pet.getOwners().get(0).getFirstName() + " " + pet.getOwners().get(0).getLastName(),
                "Dueño", "owners");

       /* grid.asSingleSelect().addValueChangeListener(event -> {
            Pet selected = event.getValue();
            if (selected != null) {
                petForm.openForEdit(selected);
            }
        });*/

        grid.asSingleSelect().addValueChangeListener(event -> {
            Pet selected = event.getValue();
            if (selected != null) {
                new PetDetailDialog(selected, consultationService).open();
            }
        });


        grid.addComponentColumn(this::createActionsColumn).setHeader("Acciones").setAutoWidth(true);

    }

    public Specification<Pet> createFilterSpecification() {
        return (root, query, builder) -> {

            Predicate searchPredicate = predicateForTextField(root, builder, new String[] { "name", "breed", "color" },
                    searchField.getValue());

            Predicate typePredicate = createPredicateForSelectedItems(
                    Optional.ofNullable(typeFilter.getSelectedItems()), items -> root.get("type").in(items), builder);

            Predicate genderPredicate = createPredicateForSelectedItems(
                    Optional.ofNullable(genderFilter.getSelectedItems()), items -> root.get("gender").in(items),
                    builder);

            Predicate sizePredicate = createPredicateForSelectedItems(
                    Optional.ofNullable(sizeFilter.getSelectedItems()), items -> root.get("size").in(items), builder);

            Predicate activePredicate = builder.isTrue(root.get("active"));

            return builder.and(activePredicate, searchPredicate, typePredicate, genderPredicate, sizePredicate);

            //return builder.and(searchPredicate, typePredicate, genderPredicate, sizePredicate);
        };
    }

    private void refreshAll() {
        grid.getDataProvider().refreshAll();
        Pageable pageable = Pageable.unpaged(); // O usa PageRequest.of(...) si quieres paginar
        long count = petService.getAllPets(pageable).stream().filter(PetSummaryResponseDto::active).count();
        quantity.setText("Mascotas activas (" + count + ")");
    }

    private Component createFilter() {
        searchField.setClearButtonVisible(true);
        searchField.setPlaceholder("Buscar por nombre, raza, color");
        searchField.setWidth("350px");
        searchField.setPrefixComponent(LumoIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> refreshAll());

        typeFilter.setWidth("200px");
        genderFilter.setWidth("200px");
        sizeFilter.setWidth("200px");
        typeFilter.setItems(PetType.values());
        genderFilter.setItems(Gender.values());
        sizeFilter.setItems(PetSize.values());

        Set.of(typeFilter, genderFilter, sizeFilter).forEach(combo -> {
            combo.setClearButtonVisible(true);
            combo.setAutoExpand(MultiSelectComboBox.AutoExpandMode.BOTH);
            combo.addValueChangeListener(e -> refreshAll());
        });

        quantity.addClassNames(LumoUtility.BorderRadius.SMALL, LumoUtility.Height.XSMALL, LumoUtility.FontWeight.MEDIUM,
                LumoUtility.JustifyContent.CENTER, LumoUtility.AlignItems.CENTER, LumoUtility.Padding.XSMALL,
                LumoUtility.Padding.Horizontal.SMALL, LumoUtility.Margin.Horizontal.SMALL,
                LumoUtility.Margin.Bottom.XSMALL,
                LumoUtility.TextColor.PRIMARY_CONTRAST, LumoUtility.Background.PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(searchField, typeFilter, genderFilter, sizeFilter, quantity);
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        toolbar.setAlignItems(FlexComponent.Alignment.END);
        toolbar.addClassNames(LumoUtility.Margin.Horizontal.MEDIUM, LumoUtility.Margin.Top.SMALL,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.MEDIUM);

        refreshAll();

        return toolbar;
    }

    private Div createTitle() {
        final Breadcrumb breadcrumb = new Breadcrumb();

        breadcrumb.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        breadcrumb.add(new BreadcrumbItem("Mascotas", PetView.class));

        Icon icon = InfoIcon.INFO_CIRCLE.create("Gestionar mascotas registradas en el sistema.");

        Div headerLayout = new Div(breadcrumb, icon);
        headerLayout.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.ROW,
                LumoUtility.Margin.Top.SMALL);

        create.setText("Nueva Mascota");
        create.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_SMALL);
        create.addClassNames(LumoUtility.Width.AUTO);

        Div layout = new Div(headerLayout, create);
        layout.addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.FlexDirection.Breakpoint.Large.ROW, LumoUtility.JustifyContent.BETWEEN,
                LumoUtility.Margin.Horizontal.MEDIUM, LumoUtility.Margin.Top.SMALL, LumoUtility.Gap.XSMALL,
                LumoUtility.AlignItems.STRETCH, LumoUtility.AlignItems.Breakpoint.Large.END);

        return layout;
    }

    public Component createActionsColumn(Pet pet) {
        Button edit = new Button(new Icon(VaadinIcon.EDIT));
        edit.addThemeVariants(
                ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        edit.getElement().setProperty("title", "Editar");
        edit.getStyle().set("min-width", "32px").set("width", "32px").set("padding", "0");

        Button delete = new Button(new Icon(VaadinIcon.TRASH));
        delete.addThemeVariants(
                ButtonVariant.LUMO_ICON,
                ButtonVariant.LUMO_TERTIARY_INLINE,
                ButtonVariant.LUMO_SMALL,
                ButtonVariant.LUMO_ERROR);
        delete.getElement().setProperty("title", "Eliminar");
        delete.getStyle().set("min-width", "32px").set("width", "32px").set("padding", "0");

        edit.addClickListener(e -> petForm.openForEdit(pet));
        delete.addClickListener(e -> {
            petService.delete(pet.getId());
            refreshAll();
        });

        HorizontalLayout actions = new HorizontalLayout(edit, delete);
        actions.setSpacing(true);
        actions.setPadding(false);
        actions.setMargin(false);
        actions.setWidth(null);
        return actions;
    }



}

package com.wornux.views.clients;

import static com.wornux.utils.PredicateUtils.createPredicateForSelectedItems;
import static com.wornux.utils.PredicateUtils.predicateForTextField;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoIcon;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.components.*;
import com.wornux.data.entity.Client;
import com.wornux.data.enums.ClientRating;
import com.wornux.data.enums.Gender;
import com.wornux.services.interfaces.ClientService;
import com.wornux.utils.GridUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.domain.Specification;

@Slf4j
@Route(value = "individual-clients")
@PageTitle("Clientes Individuales")
public class IndividualClientView extends Div {

  private final Grid<Client> grid = GridUtils.createBasicGrid(Client.class);

  private final TextField searchField = new TextField("Buscar clientes");
  private final MultiSelectComboBox<ClientRating> rating =
      new MultiSelectComboBox<>("Calificación");
  private final MultiSelectComboBox<Gender> gender = new MultiSelectComboBox<>("Género");
  private final Span quantity = new Span();

  private final Button create = new Button();
  private final ClientService clientService;
  private final IndividualClientForm individualClientForm;

  public IndividualClientView(@Qualifier("clientServiceImpl") ClientService clientService) {
    this.clientService = clientService;
    this.individualClientForm = new IndividualClientForm(clientService);

    setId("individual-clients-view");

    // Configure form event listeners
    individualClientForm.addClientSavedListener(
        event -> {
          refreshAll();
          individualClientForm.close();
        });

    individualClientForm.addClientCancelledListener(individualClientForm::close);

    createGrid(clientService, createFilterSpecification());

    final Div gridLayout = new Div(grid);
    gridLayout.addClassNames(
        LumoUtility.Margin.Horizontal.MEDIUM, LumoUtility.Padding.SMALL, LumoUtility.Height.FULL);

    add(createTitle(), createFilter(), gridLayout);
    addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);
    setSizeFull();

    create.addClickListener(
        event -> {
          individualClientForm.open();
        });
  }

  private void createGrid(ClientService service, Specification<Client> specification) {
    GridUtils.configureGrid(grid, specification, service.getRepository());

    GridUtils.addColumn(grid, Client::getCedula, "Cédula", "cedula");

    GridUtils.addColumn(
        grid,
        client -> client.getFirstName() + " " + client.getLastName(),
        "Nombre Completo",
        "firstName",
        "lastName");

    GridUtils.addColumn(grid, Client::getEmail, "Correo Electrónico", "email");

    GridUtils.addColumn(grid, Client::getPhoneNumber, "Teléfono", "phoneNumber");

    GridUtils.addColumn(
        grid,
        client -> client.getGender() != null ? client.getGender().name() : "",
        "Género",
        "gender");

    GridUtils.addComponentColumn(grid, this::renderRating, "Calificación", "rating");

    GridUtils.addColumn(
            grid, client -> client.isActive() ? "Activo" : "Inactivo", "Estado", "active")
        .setTextAlign(ColumnTextAlign.CENTER);

    grid.asSingleSelect()
        .addValueChangeListener(
            event -> {
              // TODO: Implement client editing
            });
  }

  public Specification<Client> createFilterSpecification() {
    return (root, query, builder) -> {
      Order order = builder.desc(root.get("createdAt"));
      if (query != null) {
        query.orderBy(order);
      }

      // Filter only individual clients (those with cedula or passport, not RNC)
      Predicate individualClientPredicate =
          builder.or(
              builder.isNotNull(root.get("cedula")), builder.isNotNull(root.get("passport")));

      Predicate searchPredicate = createSearchPredicate(root, builder);
      Predicate ratingPredicate = createRatingPredicate(root, builder);
      Predicate genderPredicate = createGenderPredicate(root, builder);

      return builder.and(
          individualClientPredicate, searchPredicate, ratingPredicate, genderPredicate);
    };
  }

  private Predicate createSearchPredicate(Root<Client> root, CriteriaBuilder builder) {
    return predicateForTextField(
        root,
        builder,
        new String[] {"firstName", "lastName", "email", "cedula", "passport"},
        searchField.getValue());
  }

  private Predicate createRatingPredicate(Root<Client> root, CriteriaBuilder builder) {
    return createPredicateForSelectedItems(
        Optional.ofNullable(rating.getSelectedItems()),
        items -> root.get("rating").in(items),
        builder);
  }

  private Predicate createGenderPredicate(Root<Client> root, CriteriaBuilder builder) {
    return createPredicateForSelectedItems(
        Optional.ofNullable(gender.getSelectedItems()),
        items -> root.get("gender").in(items),
        builder);
  }

  private void refreshAll() {
    grid.getDataProvider().refreshAll();
    long count =
        clientService.getAllActiveClients().stream()
            .filter(client -> client.getCedula() != null || client.getPassport() != null)
            .count();
    quantity.setText("Clientes Individuales (" + count + ")");
  }

  private Component createFilter() {
    searchField.focus();
    searchField.setClearButtonVisible(true);
    searchField.setPlaceholder("Buscar por nombre, email, cédula...");
    searchField.setPrefixComponent(LumoIcon.SEARCH.create());
    searchField.setValueChangeMode(ValueChangeMode.EAGER);
    searchField.addValueChangeListener(e -> refreshAll());

    quantity.addClassNames(
        LumoUtility.BorderRadius.SMALL,
        LumoUtility.Height.XSMALL,
        LumoUtility.FontWeight.MEDIUM,
        LumoUtility.JustifyContent.CENTER,
        LumoUtility.AlignItems.CENTER,
        LumoUtility.Padding.XSMALL,
        LumoUtility.Padding.Horizontal.SMALL,
        LumoUtility.Margin.Horizontal.SMALL,
        LumoUtility.TextColor.PRIMARY_CONTRAST,
        LumoUtility.Background.PRIMARY);

    rating.setItems(ClientRating.values());
    rating.setItemLabelGenerator(ClientRating::name);

    gender.setItems(Gender.values());
    gender.setItemLabelGenerator(Gender::name);

    Set.of(rating, gender)
        .forEach(
            c -> {
              c.setWidthFull();
              c.setClearButtonVisible(true);
              c.setAutoExpand(MultiSelectComboBox.AutoExpandMode.BOTH);
              c.addValueChangeListener(e -> refreshAll());
            });

    HorizontalLayout toolbar = new HorizontalLayout(searchField, rating, gender, quantity);
    toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    toolbar.setAlignItems(FlexComponent.Alignment.END);
    toolbar.addClassNames(
        LumoUtility.Margin.Horizontal.MEDIUM,
        LumoUtility.Margin.Top.SMALL,
        LumoUtility.Padding.MEDIUM,
        LumoUtility.Gap.MEDIUM);

    refreshAll();

    return toolbar;
  }

  private Div createTitle() {
    final Breadcrumb breadcrumb = new Breadcrumb();

    breadcrumb.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
    breadcrumb.add(
        new BreadcrumbItem("Clientes", IndividualClientView.class),
        new BreadcrumbItem("Clientes Individuales", IndividualClientView.class));

    Icon icon =
        InfoIcon.INFO_CIRCLE.create("Gestionar clientes individuales con cédula o pasaporte.");

    Div headerLayout = new Div(breadcrumb, icon);
    headerLayout.addClassNames(
        LumoUtility.Display.FLEX, LumoUtility.FlexDirection.ROW, LumoUtility.Margin.Top.SMALL);

    create.setText("Nuevo Cliente Individual");
    create.addThemeVariants(
        ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_SMALL);
    create.addClassNames(LumoUtility.Width.AUTO);

    Div layout = new Div(headerLayout, create);
    layout.addClassNames(
        LumoUtility.Display.FLEX,
        LumoUtility.FlexDirection.COLUMN,
        LumoUtility.FlexDirection.Breakpoint.Large.ROW,
        LumoUtility.JustifyContent.BETWEEN,
        LumoUtility.Margin.Horizontal.MEDIUM,
        LumoUtility.Margin.Top.SMALL,
        LumoUtility.Gap.XSMALL,
        LumoUtility.AlignItems.STRETCH,
        LumoUtility.AlignItems.Breakpoint.Large.END);

    return layout;
  }

  private Component renderRating(Client client) {
    ClientRating ratingValue = client.getRating();
    if (ratingValue == null) {
      return new Span("-");
    }

    Span badge = new Span(ratingValue.name());
    badge.getElement().getThemeList().add("badge pill");

    switch (ratingValue) {
      case MUY_BUENO -> badge.getElement().getThemeList().add("success");
      case BUENO -> badge.getElement().getThemeList().add("primary");
      case REGULAR -> badge.getElement().getThemeList().add("warning");
      case PAGO_TARDIO -> badge.getElement().getThemeList().add("warning");
      case CONFLICTIVO -> badge.getElement().getThemeList().add("error");
    }

    return badge;
  }
}

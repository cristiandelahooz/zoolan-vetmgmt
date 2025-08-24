package com.wornux.views.services;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoIcon;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.components.Breadcrumb;
import com.wornux.components.BreadcrumbItem;
import com.wornux.components.InfoIcon;
import com.wornux.data.entity.Offering;
import com.wornux.data.enums.EmployeeRole;
import com.wornux.data.enums.OfferingType;
import com.wornux.security.UserUtils;
import com.wornux.services.interfaces.OfferingService;
import com.wornux.utils.GridUtils;
import com.wornux.utils.NotificationUtils;
import com.wornux.views.MainLayout;
import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.wornux.utils.PredicateUtils.predicateForTextField;

@Slf4j
@Route(value = "offerings", layout = MainLayout.class)
@PageTitle("Servicios")
@RolesAllowed({"ROLE_SYSTEM_ADMIN", "ROLE_MANAGER"})
public class OfferingView extends Div {

  private final Grid<Offering> grid = GridUtils.createBasicGrid(Offering.class);

  private final TextField searchField = new TextField("Buscar servicios");
  private final ComboBox<OfferingType> serviceCategoryFilter = new ComboBox<>("Filtrar por tipo");
  private final Span quantity = new Span();

  private final Button create = new Button();
  private final transient OfferingService offeringService;
  private final OfferingForm offeringForm;

  public OfferingView(OfferingService offeringService) {
    this.offeringService = offeringService;
    this.offeringForm = new OfferingForm(offeringService);

    setId("services-view");

    offeringForm.setOnSaveCallback(this::refreshAll);
    offeringForm.addServiceSavedListener(
        event -> {
          refreshAll();
          offeringForm.close();
        });
    offeringForm.addServiceCancelledListener(offeringForm::close);

    createGrid(offeringService, createFilterSpecification());

    final Div gridLayout = new Div(grid);
    gridLayout.addClassNames(
        LumoUtility.Margin.Horizontal.MEDIUM, LumoUtility.Padding.SMALL, LumoUtility.Height.FULL);

    add(createTitle(), createFilter(), gridLayout);
    addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN);
    setSizeFull();

    create.addClickListener(event -> offeringForm.openForNew());
  }

  private void createGrid(OfferingService service, Specification<Offering> specification) {
    GridUtils.configureGrid(grid, specification, service.getRepository());

    GridUtils.addColumn(grid, Offering::getName, "Nombre", "name");
    GridUtils.addColumn(grid, Offering::getDescription, "Descripción", "description");

    grid.addColumn(serviceEntity -> serviceEntity.getOfferingType().getDisplay())
        .setHeader("Tipo")
        .setSortable(true)
        .setAutoWidth(true);

    grid.addColumn(
            serviceEntity -> {
              NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("es", "DO"));
              return formatter.format(serviceEntity.getPrice());
            })
        .setHeader("Precio")
        .setTextAlign(ColumnTextAlign.END)
        .setSortable(true)
        .setAutoWidth(true);

    grid.addComponentColumn(this::renderStatus)
        .setHeader("Estado")
        .setTextAlign(ColumnTextAlign.CENTER);

    if (UserUtils.hasEmployeeRole(EmployeeRole.ADMINISTRATIVE)
        || UserUtils.hasEmployeeRole(EmployeeRole.CLINIC_MANAGER)) {
      var actionsColumn =
          grid.addComponentColumn(this::createActionsColumn)
              .setHeader("Acciones")
              .setAutoWidth(true);
      actionsColumn.setFrozenToEnd(true);
    }

    grid.asSingleSelect().addValueChangeListener(event -> {});
  }

  public Specification<Offering> createFilterSpecification() {
    return (root, query, builder) -> {
      Order order = builder.desc(root.get("createdAt"));
      if (query != null) {
        query.orderBy(order);
      }

      List<Predicate> predicates = new ArrayList<>();

      predicates.add(builder.isTrue(root.get("active")));

      Predicate searchPredicate = createSearchPredicate(root, builder);
      if (searchPredicate != null) {
        predicates.add(searchPredicate);
      }

      if (serviceCategoryFilter.getValue() != null) {
        predicates.add(
            builder.equal(root.get("serviceCategory"), serviceCategoryFilter.getValue()));
      }

      return builder.and(predicates.toArray(new Predicate[0]));
    };
  }

  private Predicate createSearchPredicate(Root<Offering> root, CriteriaBuilder builder) {
    return predicateForTextField(
        root, builder, new String[] {"name", "description"}, searchField.getValue());
  }

  private void refreshAll() {
    grid.getDataProvider().refreshAll();
    updateQuantityLabel();
  }

  private void updateQuantityLabel() {
    long count = offeringService.getAllActiveServices().size();
    quantity.setText("Servicios (" + count + ")");
  }

  private Component createFilter() {
    searchField.focus();
    searchField.setClearButtonVisible(true);
    searchField.setPlaceholder("Buscar por nombre o descripción...");
    searchField.setPrefixComponent(LumoIcon.SEARCH.create());
    searchField.setValueChangeMode(ValueChangeMode.EAGER);
    searchField.addValueChangeListener(e -> refreshAll());
    searchField.setWidth("40%");

    serviceCategoryFilter.setItems(OfferingType.values());
    serviceCategoryFilter.setItemLabelGenerator(OfferingType::getDisplay);
    serviceCategoryFilter.setClearButtonVisible(true);
    serviceCategoryFilter.setPlaceholder("Todos los tipos");
    serviceCategoryFilter.addValueChangeListener(e -> refreshAll());
    serviceCategoryFilter.setWidth("200px");

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
        LumoUtility.Background.PRIMARY,
        LumoUtility.Margin.Bottom.XSMALL);

    HorizontalLayout toolbar = new HorizontalLayout(searchField, serviceCategoryFilter, quantity);
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
    breadcrumb.add(new BreadcrumbItem("Servicios", OfferingView.class));

    Icon icon = InfoIcon.INFO_CIRCLE.create("Gestionar servicios médicos y de peluquería.");

    Div headerLayout = new Div(breadcrumb, icon);
    headerLayout.addClassNames(
        LumoUtility.Display.FLEX, LumoUtility.FlexDirection.ROW, LumoUtility.Margin.Top.SMALL);

    create.setText("Nuevo Servicio");
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

  private Component createActionsColumn(Offering offering) {
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

    edit.addClickListener(e -> offeringForm.openForEdit(offering));
    delete.addClickListener(e -> showDeleteConfirmationDialog(offering));

    HorizontalLayout actions = new HorizontalLayout(edit, delete);
    actions.setSpacing(true);
    actions.setPadding(false);
    actions.setMargin(false);
    actions.setWidth(null);
    return actions;
  }

  private void showDeleteConfirmationDialog(Offering offering) {
    Dialog confirmDialog = new Dialog();
    confirmDialog.setHeaderTitle("Confirmar eliminación");
    confirmDialog.setModal(true);
    confirmDialog.setWidth("400px");

    Span message =
        new Span(
            "¿Está seguro de que desea eliminar el servicio \""
                + offering.getName()
                + "\"? Esta acción no se puede deshacer.");
    message.getStyle().set("margin-bottom", "20px");

    Button confirmButton = new Button("Eliminar");
    confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
    confirmButton.addClickListener(
        e -> {
          try {
            deleteService(offering);
            confirmDialog.close();
          } catch (Exception ex) {
            NotificationUtils.error("Error al eliminar el servicio: " + ex.getMessage());
          }
        });

    Button cancelButton = new Button("Cancelar");
    cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
    cancelButton.addClickListener(e -> confirmDialog.close());

    HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, confirmButton);
    buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
    buttonLayout.setSpacing(true);

    VerticalLayout content = new VerticalLayout(message, buttonLayout);
    content.setPadding(false);
    content.setSpacing(true);

    confirmDialog.add(content);
    confirmDialog.open();
  }

  private void deleteService(Offering offering) {
    try {
      offeringService.deactivateService(offering.getId());
      NotificationUtils.success("Servicio eliminado exitosamente");
      refreshAll();
    } catch (Exception e) {
      log.error("Error deleting offering", e);
      NotificationUtils.error("Error al eliminar servicio: " + e.getMessage());
    }
  }

  private Component renderStatus(Offering offering) {
    boolean isActive = offering.getActive();
    Span badge = new Span(isActive ? "Activo" : "Inactivo");
    badge.getElement().getThemeList().add("badge pill");
    badge.getElement().getThemeList().add(isActive ? "success" : "error");
    return badge;
  }
}

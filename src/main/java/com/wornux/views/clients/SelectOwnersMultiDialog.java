package com.wornux.views.clients;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.wornux.data.entity.Client;
import com.wornux.services.interfaces.ClientService;
import java.util.List;
import java.util.function.Consumer;

public class SelectOwnersMultiDialog extends Dialog {

  private final Grid<Client> grid;
  private final ListDataProvider<Client> dataProvider;
  private Consumer<List<Client>> selectionCallback;

  public SelectOwnersMultiDialog(ClientService clientService) {
    setWidth("800px");
    setHeight("600px");

    this.dataProvider = new ListDataProvider<>(clientService.getAllActiveClients());
    grid = new Grid<>(Client.class, false);
    grid.setItems(dataProvider);

    TextField firstNameFilter = new TextField();
    firstNameFilter.setPlaceholder("Filtrar por nombre");
    firstNameFilter.setValueChangeMode(ValueChangeMode.EAGER);
    firstNameFilter.addValueChangeListener(
        e ->
            dataProvider.setFilter(
                Client::getFirstName,
                name ->
                    name != null
                        && name.toLowerCase().contains(firstNameFilter.getValue().toLowerCase())));

    TextField lastNameFilter = new TextField();
    lastNameFilter.setPlaceholder("Filtrar por apellido");
    lastNameFilter.setValueChangeMode(ValueChangeMode.EAGER);
    lastNameFilter.addValueChangeListener(
        e ->
            dataProvider.setFilter(
                Client::getLastName,
                last ->
                    last != null
                        && last.toLowerCase().contains(lastNameFilter.getValue().toLowerCase())));

    TextField cedulaFilter = new TextField();
    cedulaFilter.setPlaceholder("Filtrar por cédula");
    cedulaFilter.setValueChangeMode(ValueChangeMode.EAGER);
    cedulaFilter.addValueChangeListener(
        e ->
            dataProvider.setFilter(
                Client::getCedula, ced -> ced != null && ced.contains(cedulaFilter.getValue())));

    HorizontalLayout filterBar =
        new HorizontalLayout(firstNameFilter, lastNameFilter, cedulaFilter);

    grid.addColumn(Client::getFirstName).setHeader("Nombre");
    grid.addColumn(Client::getLastName).setHeader("Apellido");
    grid.addColumn(Client::getCedula).setHeader("Cédula");
    grid.addColumn(Client::getPhoneNumber).setHeader("Teléfono");

    grid.setSelectionMode(Grid.SelectionMode.MULTI);

    Button cancel = new Button("Cancelar", e -> close());
    Button selectButton =
        new Button(
            "Seleccionar",
            e -> {
              if (selectionCallback != null) {
                selectionCallback.accept(grid.getSelectedItems().stream().toList());
              }
              close();
            });

    HorizontalLayout buttons = new HorizontalLayout(cancel, selectButton);
    buttons.setJustifyContentMode(JustifyContentMode.END);

    Div content = new Div(new H3("Seleccionar"), filterBar, grid, buttons);
    content.getStyle().set("display", "flex").set("flexDirection", "column").set("gap", "1rem");

    add(content);
  }

  public void open(List<Client> preSelectedOwners, Consumer<List<Client>> selectionCallback) {
    this.selectionCallback = selectionCallback;

    grid.deselectAll();

    if (preSelectedOwners != null && !preSelectedOwners.isEmpty()) {
      preSelectedOwners.forEach(
          owner -> {
            dataProvider.getItems().stream()
                .filter(c -> c.getId().equals(owner.getId()))
                .findFirst()
                .ifPresent(grid::select);
          });
    }

    open();
  }
}

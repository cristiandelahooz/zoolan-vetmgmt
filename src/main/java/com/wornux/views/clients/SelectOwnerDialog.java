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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SelectOwnerDialog extends Dialog {

  private final Grid<Client> grid = new Grid<>(Client.class, false);
  private final TextField selectedOwner = new TextField("Dueño seleccionado");
  private Client selectedClient;
  private final ListDataProvider<Client> dataProvider;

  private final List<Consumer<Client>> listeners = new ArrayList<>();

  public SelectOwnerDialog(ClientService clientService) {
    setWidth("700px");

    this.dataProvider = new ListDataProvider<>(clientService.getAllActiveClients());
    grid.setItems(dataProvider);

    H3 title = new H3("Seleccionar Dueño");
    selectedOwner.setReadOnly(true);

    TextField firstNameFilter = new TextField();
    firstNameFilter.setPlaceholder("Filtrar por nombre");

    TextField lastNameFilter = new TextField();
    lastNameFilter.setPlaceholder("Filtrar por apellido");

    TextField cedulaFilter = new TextField();
    cedulaFilter.setPlaceholder("Filtrar por cédula");

    // TextField phoneFilter = new TextField();
    // phoneFilter.setPlaceholder("Filtrar por teléfono");

    HorizontalLayout filterBar =
        new HorizontalLayout(firstNameFilter, lastNameFilter, cedulaFilter);

    firstNameFilter.setValueChangeMode(ValueChangeMode.EAGER);
    firstNameFilter.addValueChangeListener(
        e ->
            dataProvider.setFilter(
                Client::getFirstName,
                name ->
                    name != null
                        && name.toLowerCase().contains(firstNameFilter.getValue().toLowerCase())));

    lastNameFilter.setValueChangeMode(ValueChangeMode.EAGER);
    lastNameFilter.addValueChangeListener(
        e ->
            dataProvider.setFilter(
                Client::getLastName,
                last ->
                    last != null
                        && last.toLowerCase().contains(lastNameFilter.getValue().toLowerCase())));

    cedulaFilter.setValueChangeMode(ValueChangeMode.EAGER);
    cedulaFilter.addValueChangeListener(
        e ->
            dataProvider.setFilter(
                Client::getCedula, ced -> ced != null && ced.contains(cedulaFilter.getValue())));

    /*phoneFilter.addValueChangeListener(e ->
        dataProvider.setFilter(Client::getPhoneNumber,
                phone -> phone != null && phone.contains(phoneFilter.getValue()))
    );*/

    grid.addColumn(Client::getFirstName).setHeader("Nombre");
    grid.addColumn(Client::getLastName).setHeader("Apellido");
    grid.addColumn(Client::getCedula).setHeader("Cédula");
    grid.addColumn(Client::getPhoneNumber).setHeader("Teléfono");

    Button cancel = new Button("Cancelar", e -> close());
    Button accept =
        new Button(
            "Aceptar",
            e -> {
              if (selectedClient != null) {
                listeners.forEach(l -> l.accept(selectedClient));
                close();
              }
            });
    accept.setEnabled(false);

    grid.asSingleSelect()
        .addValueChangeListener(
            event -> {
              selectedClient = event.getValue();
              accept.setEnabled(selectedClient != null);
              selectedOwner.setValue(
                  selectedClient != null
                      ? selectedClient.getFirstName() + " " + selectedClient.getLastName()
                      : "");
            });

    HorizontalLayout buttons = new HorizontalLayout(cancel, accept);
    buttons.setJustifyContentMode(JustifyContentMode.END);

    Div content = new Div(title, filterBar, grid, selectedOwner, buttons);
    content.getStyle().set("display", "flex").set("flexDirection", "column").set("gap", "1rem");
    add(content);
  }

  public void addClienteSeleccionadoListener(Consumer<Client> listener) {
    listeners.add(listener);
  }
}

package com.wornux.views.pets;

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
import com.wornux.data.entity.Pet;
import com.wornux.services.interfaces.PetService;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SelectPetDialog extends Dialog {

    private final Grid<Pet> grid = new Grid<>(Pet.class, false);
    private final TextField selectedPetField = new TextField("Mascota seleccionada");
    private Pet selectedPet;
    private final ListDataProvider<Pet> dataProvider;

    private final List<Consumer<Pet>> listeners = new ArrayList<>();

    public SelectPetDialog(PetService petService) {
        setWidth("700px");

        this.dataProvider = new ListDataProvider<>(petService.getAllPets()); // Implementar en PetService
        grid.setItems(dataProvider);

        H3 title = new H3("Seleccionar Mascota");
        selectedPetField.setReadOnly(true);

        TextField nameFilter = new TextField();
        nameFilter.setPlaceholder("Filtrar por nombre");

        TextField typeFilter = new TextField();
        typeFilter.setPlaceholder("Filtrar por tipo");

        TextField ownerFilter = new TextField();
        ownerFilter.setPlaceholder("Filtrar por dueño principal");

        HorizontalLayout filterBar = new HorizontalLayout(nameFilter, typeFilter, ownerFilter);

        nameFilter.setValueChangeMode(ValueChangeMode.EAGER);
        nameFilter.addValueChangeListener(e -> dataProvider.setFilter(Pet::getName, name -> name != null && name
                .toLowerCase().contains(nameFilter.getValue().toLowerCase())));

        typeFilter.setValueChangeMode(ValueChangeMode.EAGER);
        typeFilter.addValueChangeListener(e -> dataProvider.setFilter(pet -> pet.getType() != null && pet.getType()
                .toString().toLowerCase().contains(typeFilter.getValue().toLowerCase())));

        ownerFilter.setValueChangeMode(ValueChangeMode.EAGER);
        ownerFilter.addValueChangeListener(e -> dataProvider.setFilter(pet -> pet.getOwners() != null && !pet
                .getOwners().isEmpty() && (pet.getOwners().get(0).getFirstName() + " " + pet.getOwners().get(0)
                        .getLastName()).toLowerCase().contains(ownerFilter.getValue().toLowerCase())));

        grid.addColumn(Pet::getName).setHeader("Nombre");
        grid.addColumn(p -> p.getType().toString()).setHeader("Tipo");
        grid.addColumn(p -> p.getOwners() != null && !p.getOwners().isEmpty() ? p.getOwners().get(0)
                .getFirstName() + " " + p.getOwners().get(0).getLastName() : "Sin dueño").setHeader("Dueño principal");

        Button cancel = new Button("Cancelar", e -> close());
        Button accept = new Button("Aceptar", e -> {
            if (selectedPet != null) {
                listeners.forEach(l -> l.accept(selectedPet));
                close();
            }
        });
        accept.setEnabled(false);

        grid.asSingleSelect().addValueChangeListener(event -> {
            selectedPet = event.getValue();
            accept.setEnabled(selectedPet != null);
            selectedPetField.setValue(selectedPet != null ? selectedPet.getName() : "");
        });

        HorizontalLayout buttons = new HorizontalLayout(cancel, accept);
        buttons.setJustifyContentMode(JustifyContentMode.END);

        Div content = new Div(title, filterBar, grid, selectedPetField, buttons);
        content.getStyle().set("display", "flex").set("flexDirection", "column").set("gap", "1rem");
        add(content);
    }

    public void addPetSelectedListener(Consumer<Pet> listener) {
        listeners.add(listener);
    }
}

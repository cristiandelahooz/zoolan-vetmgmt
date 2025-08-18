package com.wornux.views.pets;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
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
    private final ListDataProvider<Pet> dataProvider;

    private Pet selectedPet;
    private final List<Consumer<Pet>> listeners = new ArrayList<>();

    private final Button acceptButton = new Button("Aceptar");
    private final Button cancelButton = new Button("Cancelar");

    public SelectPetDialog(PetService petService) {

        setHeaderTitle("Seleccionar Mascota");
        setWidth("740px");
        setMaxWidth("95vw");
        setMaxHeight("85vh");
        this.dataProvider = new ListDataProvider<>(petService.getAllPets());
        grid.setItems(dataProvider);

        // ====== Filtros ======
        TextField nameFilter  = new TextField();
        TextField typeFilter  = new TextField();
        TextField ownerFilter = new TextField();

        nameFilter.setPlaceholder("Filtrar por nombre");
        typeFilter.setPlaceholder("Filtrar por tipo");
        ownerFilter.setPlaceholder("Filtrar por dueño principal");

        nameFilter.setClearButtonVisible(true);
        typeFilter.setClearButtonVisible(true);
        ownerFilter.setClearButtonVisible(true);

        nameFilter.setValueChangeMode(ValueChangeMode.EAGER);
        typeFilter.setValueChangeMode(ValueChangeMode.EAGER);
        ownerFilter.setValueChangeMode(ValueChangeMode.EAGER);

        nameFilter.addValueChangeListener(e -> dataProvider.setFilter(
                Pet::getName,
                name -> name != null && name.toLowerCase().contains(nameFilter.getValue().toLowerCase())
        ));

        typeFilter.addValueChangeListener(e -> dataProvider.setFilter(
                pet -> pet.getType() != null &&
                        pet.getType().toString().toLowerCase().contains(typeFilter.getValue().toLowerCase())
        ));

        ownerFilter.addValueChangeListener(e -> dataProvider.setFilter(
                pet -> pet.getOwners() != null && !pet.getOwners().isEmpty() &&
                        (pet.getOwners().get(0).getFirstName() + " " + pet.getOwners().get(0).getLastName())
                                .toLowerCase().contains(ownerFilter.getValue().toLowerCase())
        ));

        HorizontalLayout filterBar = new HorizontalLayout(nameFilter, typeFilter, ownerFilter);
        filterBar.setWidthFull();

        // ====== Grid (solo el grid scrollea) ======
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addColumn(Pet::getName).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(p -> p.getType() != null ? p.getType().toString() : "—")
                .setHeader("Tipo").setAutoWidth(true);
        grid.addColumn(p ->
                        p.getOwners() != null && !p.getOwners().isEmpty()
                                ? p.getOwners().get(0).getFirstName() + " " + p.getOwners().get(0).getLastName()
                                : "Sin dueño")
                .setHeader("Dueño principal").setAutoWidth(true);

        grid.setWidthFull();
        grid.setHeight("32vh");
        selectedPetField.setReadOnly(true);
        selectedPetField.setWidthFull();


        grid.asSingleSelect().addValueChangeListener(event -> {
            selectedPet = event.getValue();
            selectedPetField.setValue(selectedPet != null ? selectedPet.getName() : "");
            acceptButton.setEnabled(selectedPet != null);
        });

        // ====== Botones "sticky" (siempre visibles) ======
        cancelButton.addClickListener(e -> close());
        acceptButton.addClickListener(e -> {
            if (selectedPet != null) {
                listeners.forEach(l -> l.accept(selectedPet));
                close();
            }
        });
        acceptButton.setEnabled(false);

        HorizontalLayout buttons = new HorizontalLayout(cancelButton, acceptButton);
        buttons.setWidthFull();
        buttons.setJustifyContentMode(JustifyContentMode.END);
         buttons.getStyle()
                .set("position", "sticky")
                .set("bottom", "0")
                .set("background", "var(--lumo-base-color)")
                .set("padding", "0.75rem 0")
                .set("border-top", "1px solid var(--lumo-contrast-10pct)");

        // ====== Contenido del diálogo ======
        Div content = new Div(filterBar, grid, selectedPetField, buttons);
        content.getStyle()
                .set("display", "flex")
                .set("flexDirection", "column")
                .set("gap", "1rem")
                .set("maxHeight", "75vh")
                .set("overflow", "auto");
        content.setWidthFull();

        add(content);
    }

    public void addPetSelectedListener(Consumer<Pet> listener) {
        listeners.add(listener);
    }
}

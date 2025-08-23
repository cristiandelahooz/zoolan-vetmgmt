package com.wornux.views.pets;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.data.entity.Client;
import com.wornux.data.entity.Pet;
import com.wornux.data.enums.PetType;
import com.wornux.services.implementations.PetServiceImpl;
import com.wornux.views.MainLayout;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Route(value = "mascotas-fusionar", layout = MainLayout.class)
@PageTitle("Fusionar Mascotas Duplicadas")
@RolesAllowed({ "ROLE_SYSTEM_ADMIN", "ROLE_MANAGER" })
public class PetMergeView extends Div {

    private final PetServiceImpl petService;

    private final TextField searchField = new TextField();
    private final Button searchBtn = new Button("Buscar");
    private final Grid<Pet> grid = new Grid<>(Pet.class, false);

    private final Button clearBtn = new Button("Limpiar selección");
    private final Button openMergeDialogBtn = new Button("Fusionar seleccionadas");
    private final Dialog confirmDialog = new Dialog();

    private final Div keepCard = new Div();
    private final Div removeCard = new Div();

    private final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private Pet keepPet;
    private Pet removePet;

    public PetMergeView(@Qualifier("petServiceImpl") PetServiceImpl petService) {
        this.petService = petService;
        setSizeFull();

        addClassNames(LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN, LumoUtility.Gap.SMALL);

        add(buildHeader());
        add(buildToolbar());
        add(buildResults());

        buildSelectionsArea();
        buildConfirmDialog();
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private ComponentRenderer<Div, Pet> ownersRenderer() {
        return new ComponentRenderer<>(pet -> {
            Div box = new Div();
            List<Client> owners = pet.getOwners();
            if (owners == null || owners.isEmpty()) {
                box.setText("Sin dueños");
                box.addClassNames(LumoUtility.TextColor.SECONDARY);
                box.getStyle().set("font-style", "italic");
                return box;
            }
            String txt = owners.stream().map(o -> (nvl(o.getFirstName()) + " " + nvl(o.getLastName())).trim())
                    .collect(Collectors.joining(", "));
            box.setText(txt);
            return box;
        });
    }

    private VerticalLayout buildHeader() {
        VerticalLayout head = new VerticalLayout();
        head.setPadding(true);
        head.setSpacing(false);
        head.add(new H2("Fusionar Mascotas Duplicadas"));
        Paragraph p = new Paragraph(
                "Busca mascotas por nombre para encontrar posibles duplicados. " + "Selecciona cuál mantener y cuál eliminar; los dueños se trasladarán a la mascota a mantener.");
        p.addClassNames(LumoUtility.TextColor.SECONDARY);
        head.add(p);
        return head;
    }

    private HorizontalLayout buildToolbar() {
        searchField.setPlaceholder("Buscar por nombre de mascota…");
        searchField.setClearButtonVisible(true);
        searchField.setWidth(350, Unit.PIXELS);
        searchField.addKeyDownListener(Key.ENTER, e -> handleSearch());

        searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchBtn.addClickListener(e -> handleSearch());

        clearBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        clearBtn.setWidth("200px");
        clearBtn.addClickListener(e -> {
            clearSelections();
            grid.getDataProvider().refreshAll();
        });

        openMergeDialogBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST);
        openMergeDialogBtn.setEnabled(false);
        openMergeDialogBtn.setWidth("220px");
        openMergeDialogBtn.addClickListener(e -> confirmDialog.open());

        HorizontalLayout bar = new HorizontalLayout(searchField, searchBtn, clearBtn, openMergeDialogBtn);
        bar.setAlignItems(FlexComponent.Alignment.END);
        bar.addClassNames(LumoUtility.Margin.Horizontal.MEDIUM, LumoUtility.Margin.Top.SMALL,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.MEDIUM);
        return bar;
    }

    private VerticalLayout buildResults() {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("320px");
        grid.setWidthFull();

        grid.addColumn(Pet::getName).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(p -> getTypeLabel(p.getType())).setHeader("Tipo").setAutoWidth(true);
        grid.addColumn(Pet::getBreed).setHeader("Raza").setAutoWidth(true);
        grid.addColumn(p -> p.getBirthDate() != null ? DATE_FMT.format(p.getBirthDate()) : "")
                .setHeader("F. Nacimiento").setAutoWidth(true);
        grid.addColumn(Pet::getColor).setHeader("Color").setAutoWidth(true);
        grid.addColumn(p -> p.getSize() != null ? p.getSize().name() : "").setHeader("Tamaño").setAutoWidth(true);
        grid.addColumn(ownersRenderer()).setHeader("Dueños").setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(pet -> {
                    Button keepBtn = new Button(isKeep(pet) ? "✓ Mantener" : "Mantener");
                    keepBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
                    if (isKeep(pet))
                        keepBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
                    keepBtn.setEnabled(!isKeep(pet));
                    keepBtn.addClickListener(e -> {
                        keepPet = pet;
                        updateMergeButtonState();
                        grid.getDataProvider().refreshAll();
                        refreshCards();
                    });

                    keepBtn.addClassNames(LumoUtility.Padding.NONE);
                    keepBtn.setWidth("100px");
                    keepBtn.setMinWidth("100px");
                    keepBtn.setHeight("32px");

                    Button removeBtn = new Button(LineAwesomeIcon.TRASH_SOLID.create());
                    removeBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE,
                            ButtonVariant.LUMO_SMALL);

                    removeBtn.addClassNames(LumoUtility.Padding.NONE);
                    removeBtn.setWidth("36px");
                    removeBtn.setMinWidth("36px");
                    removeBtn.setHeight("32px");

                    removeBtn.getElement().setProperty("title", "Eliminar");
                    removeBtn.setAriaLabel("Eliminar");

                    if (isRemove(pet)) {
                        removeBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
                        removeBtn.setEnabled(false);
                        removeBtn.getElement().setProperty("title", "Seleccionado para eliminar");
                    } else {
                        removeBtn.setEnabled(true);
                    }

                    removeBtn.addClickListener(e -> {
                        removePet = pet;
                        updateMergeButtonState();
                        grid.getDataProvider().refreshAll();
                        refreshCards();
                    });

                    HorizontalLayout hl = new HorizontalLayout(keepBtn, removeBtn);
                    hl.setSpacing(true);
                    hl.setPadding(false);
                    hl.setAlignItems(FlexComponent.Alignment.CENTER);
                    hl.addClassNames(LumoUtility.Gap.XSMALL);
                    return hl;
                })).setHeader("Acciones").setTextAlign(ColumnTextAlign.CENTER).setAutoWidth(false).setWidth("200px")
                .setFlexGrow(0);

        VerticalLayout gridWrapper = new VerticalLayout(grid);

        HorizontalLayout wrapper = new HorizontalLayout();
        wrapper.addClassNames(LumoUtility.Padding.SMALL, LumoUtility.Height.AUTO);
        wrapper.setWidthFull();
        wrapper.add(keepCard, removeCard);
        wrapper.setFlexGrow(1, keepCard, removeCard);

        gridWrapper.add(wrapper);

        return gridWrapper;
    }

    private void buildSelectionsArea() {
        styleKeepCard();
        styleRemoveCard();
        refreshCards();
    }

    private void buildConfirmDialog() {
        confirmDialog.setHeaderTitle("¡Confirmar Fusión de Mascotas!");
        confirmDialog.addClassNames(LumoUtility.Margin.LARGE, LumoUtility.Padding.MEDIUM);

        HorizontalLayout content = new HorizontalLayout();
        content.setSpacing(true);
        content.setPadding(true);

        Div willKeep = new Div();
        willKeep.addClassNames(LumoUtility.Border.ALL, LumoUtility.BorderColor.SUCCESS,
                LumoUtility.Background.SUCCESS_10, LumoUtility.BorderRadius.LARGE, LumoUtility.Padding.MEDIUM);
        willKeep.getStyle().set("border-width", "2px");

        Div willRemove = new Div();
        willRemove.addClassNames(LumoUtility.Border.ALL, LumoUtility.BorderColor.ERROR, LumoUtility.Background.ERROR_10,
                LumoUtility.BorderRadius.LARGE, LumoUtility.Padding.MEDIUM);
        willRemove.getStyle().set("border-width", "2px");

        Div resultInfo = new Div();
        resultInfo.addClassNames(LumoUtility.Border.ALL, LumoUtility.BorderColor.PRIMARY,
                LumoUtility.Background.PRIMARY_10, LumoUtility.BorderRadius.MEDIUM, LumoUtility.Padding.SMALL);
        resultInfo.setText("Resultado: La mascota a mantener conservará/recibirá todos los dueños de ambas mascotas.");

        content.add(willKeep, willRemove, resultInfo);

        Button cancel = new Button("Cancelar", e -> confirmDialog.close());
        Button ok = new Button("Confirmar", e -> doMerge());
        ok.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST);

        HorizontalLayout footer = new HorizontalLayout(cancel, ok);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        footer.setSpacing(true);

        confirmDialog.add(content, footer);

        confirmDialog.addOpenedChangeListener(ev -> {
            if (ev.isOpened()) {
                willKeep.removeAll();
                willKeep.add(new Paragraph("SE MANTENDRÁ:"), detailsBlock(keepPet));

                willRemove.removeAll();
                willRemove.add(new Paragraph("SE ELIMINARÁ:"), detailsBlock(removePet));
            }
        });

        add(confirmDialog);
    }

    private void handleSearch() {
        String term = searchField.getValue() != null ? searchField.getValue().trim() : "";
        if (term.isEmpty()) {
            Notification.show("Por favor ingresa un nombre para buscar", 3000, Notification.Position.MIDDLE);
            return;
        }
        try {
            List<Pet> results = petService.findSimilarPetsByName(term);
            grid.setItems(results);
            if (results.isEmpty()) {
                Notification.show("No se encontraron mascotas con ese nombre", 3000, Notification.Position.MIDDLE);
            } else if (results.size() == 1) {
                Notification.show("Solo se encontró una mascota. Necesitas al menos 2 para fusionar.", 3500,
                        Notification.Position.MIDDLE);
            }
            grid.getDataProvider().refreshAll();
        } catch (Exception ex) {
            log.error("Error buscando mascotas", ex);
            grid.setItems(List.of());
            Notification.show("Error al buscar mascotas", 3000, Notification.Position.MIDDLE);
        }
    }

    private void doMerge() {
        if (keepPet == null || removePet == null)
            return;
        if (Objects.equals(keepPet.getId(), removePet.getId())) {
            Notification.show("Debes seleccionar dos mascotas distintas", 3000, Notification.Position.MIDDLE);
            return;
        }
        try {
            petService.mergePets(keepPet.getId(), removePet.getId());
            Notification.show("Mascotas fusionadas exitosamente", 3000, Notification.Position.BOTTOM_CENTER);

            clearSelections();
            confirmDialog.close();
            handleSearch();
        } catch (Exception ex) {
            log.error("Error al fusionar mascotas", ex);
            Notification.show("Error al fusionar: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private void updateMergeButtonState() {
        boolean canMerge = keepPet != null && removePet != null && !Objects.equals(idOf(keepPet), idOf(removePet));
        openMergeDialogBtn.setEnabled(canMerge);
    }

    private void clearSelections() {
        keepPet = null;
        removePet = null;
        updateMergeButtonState();
        refreshCards();
    }

    private void styleKeepCard() {
        keepCard.addClassNames(LumoUtility.Border.ALL, LumoUtility.BorderColor.SUCCESS,
                LumoUtility.Background.SUCCESS_10, LumoUtility.BorderRadius.LARGE, LumoUtility.Padding.MEDIUM,
                LumoUtility.BoxShadow.MEDIUM, LumoUtility.Gap.MEDIUM);
    }

    private void styleRemoveCard() {
        removeCard.addClassNames(LumoUtility.Border.ALL, LumoUtility.BorderColor.ERROR, LumoUtility.Background.ERROR_10,
                LumoUtility.BorderRadius.LARGE, LumoUtility.Padding.MEDIUM, LumoUtility.BoxShadow.MEDIUM,
                LumoUtility.Gap.MEDIUM);
    }

    private void refreshCards() {
        keepCard.removeAll();
        removeCard.removeAll();

        SvgIcon keepIcon = LineAwesomeIcon.SHIELD_ALT_SOLID.create();
        keepIcon.addClassNames(LumoUtility.IconSize.LARGE, LumoUtility.TextColor.SUCCESS,
                LumoUtility.Background.SUCCESS_10, LumoUtility.BorderRadius.FULL, LumoUtility.Padding.SMALL);
        keepIcon.addClassNames(LumoUtility.Width.MEDIUM, LumoUtility.Height.MEDIUM);

        Paragraph keepText = new Paragraph("A mantener");
        keepText.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.TextColor.SUCCESS, LumoUtility.Margin.NONE,
                LumoUtility.FontSize.SMALL);

        HorizontalLayout keepHeader = new HorizontalLayout(keepIcon, keepText);
        keepHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        keepHeader.addClassNames(LumoUtility.Gap.SMALL, LumoUtility.Margin.Bottom.SMALL);

        keepCard.add(keepHeader);
        keepCard.add(keepPet != null ? summaryBlock(keepPet) : createEmptyStateMessage("Sin seleccionar"));

        SvgIcon removeIcon = LineAwesomeIcon.TRASH_SOLID.create();
        removeIcon.addClassNames(LumoUtility.IconSize.LARGE, LumoUtility.TextColor.ERROR,
                LumoUtility.Background.ERROR_10, LumoUtility.BorderRadius.FULL, LumoUtility.Padding.SMALL);
        removeIcon.addClassNames(LumoUtility.Width.MEDIUM, LumoUtility.Height.MEDIUM);

        Paragraph removeText = new Paragraph("A eliminar");
        removeText.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.TextColor.ERROR, LumoUtility.Margin.NONE,
                LumoUtility.FontSize.SMALL);

        HorizontalLayout removeHeader = new HorizontalLayout(removeIcon, removeText);
        removeHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        removeHeader.addClassNames(LumoUtility.Gap.SMALL, LumoUtility.Margin.Bottom.SMALL);

        removeCard.add(removeHeader);
        removeCard.add(removePet != null ? summaryBlock(removePet) : createEmptyStateMessage("Sin seleccionar"));
    }

    private Div summaryBlock(Pet p) {
        Div d = new Div();
        String ownersCount = p.getOwners() != null ? String.valueOf(p.getOwners().size()) : "0";

        Paragraph petInfo = new Paragraph(
                nvl(p.getName()) + " - " + getTypeLabel(p.getType()) + " (" + ownersCount + " dueño(s))");
        petInfo.addClassNames(LumoUtility.FontWeight.MEDIUM, LumoUtility.Margin.NONE, LumoUtility.Padding.SMALL,
                LumoUtility.Background.BASE, LumoUtility.BorderRadius.MEDIUM, LumoUtility.FontSize.SMALL);
        petInfo.getStyle().set("background", "rgba(255, 255, 255, 0.5)");

        d.add(petInfo);
        return d;
    }

    private Div createEmptyStateMessage(String message) {
        Div d = new Div();
        Paragraph emptyText = new Paragraph(message);
        emptyText.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.Margin.NONE, LumoUtility.Padding.SMALL,
                LumoUtility.TextAlignment.CENTER, LumoUtility.FontSize.SMALL);

        d.add(emptyText);
        return d;
    }

    private Div detailsBlock(Pet p) {
        Div d = new Div();
        if (p == null) {
            d.add(new Paragraph("Sin seleccionar"));
            return d;
        }
        d.add(new Paragraph("Nombre: " + nvl(p.getName())), new Paragraph("Tipo: " + getTypeLabel(p.getType())),
                new Paragraph("Raza: " + nvl(p.getBreed())),
                new Paragraph("Dueños: " + (p.getOwners() != null ? p.getOwners().size() : 0)));
        return d;
    }

    private boolean isKeep(Pet p) {
        return keepPet != null && Objects.equals(idOf(keepPet), idOf(p));
    }

    private boolean isRemove(Pet p) {
        return removePet != null && Objects.equals(idOf(removePet), idOf(p));
    }

    private Long idOf(Pet p) {
        return p != null ? p.getId() : null;
    }

    private String getTypeLabel(PetType type) {
        if (type == null)
            return "";
        return switch (type) {
            case PERRO -> "Perro";
            case GATO -> "Gato";
            case AVE -> "Ave";
            case CONEJO -> "Conejo";
            case HAMSTER -> "Hámster";
            case REPTIL -> "Reptil";
            case OTRO -> "Otro";
        };
    }

}

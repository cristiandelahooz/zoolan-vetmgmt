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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;

@Slf4j
@Route("mascotas/fusionar")
@PageTitle("Fusionar Mascotas Duplicadas")
public class PetMergeView extends Div {

  private final PetServiceImpl petService;

  private final TextField searchField = new TextField();
  private final Button searchBtn = new Button("Buscar");
  private final Grid<Pet> grid = new Grid<>(Pet.class, false);

  private Pet keepPet;
  private Pet removePet;

  private final Button clearBtn = new Button("Limpiar selección");
  private final Button openMergeDialogBtn = new Button("Fusionar seleccionadas");
  private final Dialog confirmDialog = new Dialog();

  private final Div keepCard = new Div();
  private final Div removeCard = new Div();

  private final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public PetMergeView(@Qualifier("petServiceImpl") PetServiceImpl petService) {
    this.petService = petService;

    setSizeFull();
    addClassNames(
        LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN, LumoUtility.Gap.SMALL);

    add(buildHeader());
    add(buildToolbar());
    add(buildResults());
    add(buildSelectionsArea());
    buildConfirmDialog();
  }

  private ComponentRenderer<Div, Pet> ownersRenderer() {
    return new ComponentRenderer<>(
        pet -> {
          Div box = new Div();
          List<Client> owners = pet.getOwners();
          if (owners == null || owners.isEmpty()) {
            box.setText("Sin dueños");
            box.getStyle().set("fontStyle", "italic").set("color", "#666");
            return box;
          }
          String txt =
              owners.stream()
                  .map(o -> (nvl(o.getFirstName()) + " " + nvl(o.getLastName())).trim())
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
    Paragraph p =
        new Paragraph(
            "Busca mascotas por nombre para encontrar posibles duplicados. "
                + "Selecciona cuál mantener y cuál eliminar; los dueños se trasladarán a la mascota a mantener.");
    p.getStyle().set("color", "#666");
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
    clearBtn.addClickListener(
        e -> {
          clearSelections();
          grid.getDataProvider().refreshAll();
        });

    openMergeDialogBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST);
    openMergeDialogBtn.setEnabled(false);
    openMergeDialogBtn.setWidth("220px");
    openMergeDialogBtn.addClickListener(e -> confirmDialog.open());

    HorizontalLayout bar =
        new HorizontalLayout(searchField, searchBtn, clearBtn, openMergeDialogBtn);
    bar.setAlignItems(FlexComponent.Alignment.END);
    bar.addClassNames(
        LumoUtility.Margin.Horizontal.MEDIUM,
        LumoUtility.Margin.Top.SMALL,
        LumoUtility.Padding.MEDIUM,
        LumoUtility.Gap.MEDIUM);
    return bar;
  }

  private Div buildResults() {
    grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    grid.setHeight("320px");
    grid.setWidthFull();

    grid.addColumn(Pet::getName).setHeader("Nombre").setAutoWidth(true);
    grid.addColumn(p -> getTypeLabel(p.getType())).setHeader("Tipo").setAutoWidth(true);
    grid.addColumn(Pet::getBreed).setHeader("Raza").setAutoWidth(true);
    grid.addColumn(p -> p.getBirthDate() != null ? DATE_FMT.format(p.getBirthDate()) : "")
        .setHeader("F. Nacimiento")
        .setAutoWidth(true);
    grid.addColumn(Pet::getColor).setHeader("Color").setAutoWidth(true);
    grid.addColumn(p -> p.getSize() != null ? p.getSize().name() : "")
        .setHeader("Tamaño")
        .setAutoWidth(true);
    grid.addColumn(ownersRenderer()).setHeader("Dueños").setAutoWidth(true);

    grid.addColumn(
            new ComponentRenderer<>(
                pet -> {
                  Button keepBtn = new Button(isKeep(pet) ? "✓ Mantener" : "Mantener");
                  keepBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
                  if (isKeep(pet)) keepBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
                  keepBtn.setEnabled(!isKeep(pet));
                  keepBtn.addClickListener(
                      e -> {
                        keepPet = pet;
                        updateMergeButtonState();
                        grid.getDataProvider().refreshAll();
                        refreshCards();
                      });

                  keepBtn
                      .getStyle()
                      .set("width", "100px")
                      .set("minWidth", "100px")
                      .set("height", "32px")
                      .set("padding", "0");

                  Button removeBtn = new Button(VaadinIcon.TRASH.create());
                  removeBtn.addThemeVariants(
                      ButtonVariant.LUMO_ERROR,
                      ButtonVariant.LUMO_TERTIARY_INLINE,
                      ButtonVariant.LUMO_SMALL);

                  removeBtn
                      .getStyle()
                      .set("width", "36px")
                      .set("minWidth", "36px")
                      .set("height", "32px")
                      .set("padding", "0");

                  removeBtn.getElement().setProperty("title", "Eliminar");
                  removeBtn.setAriaLabel("Eliminar");

                  if (isRemove(pet)) {
                    removeBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
                    removeBtn.setEnabled(false);
                    removeBtn.getElement().setProperty("title", "Seleccionado para eliminar");
                  } else {
                    removeBtn.setEnabled(true);
                  }

                  removeBtn.addClickListener(
                      e -> {
                        removePet = pet;
                        updateMergeButtonState();
                        grid.getDataProvider().refreshAll();
                        refreshCards();
                      });

                  HorizontalLayout hl = new HorizontalLayout(keepBtn, removeBtn);
                  hl.setSpacing(true);
                  hl.setPadding(false);
                  hl.setAlignItems(FlexComponent.Alignment.CENTER);
                  hl.getStyle().set("gap", "0.25rem");
                  return hl;
                }))
        .setHeader("Acciones")
        .setTextAlign(ColumnTextAlign.CENTER)
        .setAutoWidth(false)
        .setWidth("200px")
        .setFlexGrow(0);

    Div wrapper = new Div(grid);
    wrapper.addClassNames(
        LumoUtility.Margin.Horizontal.MEDIUM, LumoUtility.Padding.SMALL, LumoUtility.Height.FULL);
    return wrapper;
  }

  private HorizontalLayout buildSelectionsArea() {
    styleKeepCard();
    styleRemoveCard();
    refreshCards();
    keepCard.setWidth("40%");
    removeCard.setWidth("40%");

    HorizontalLayout hl = new HorizontalLayout(keepCard, removeCard);
    hl.setWidthFull();
    hl.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);

    return hl;
  }

  private void buildConfirmDialog() {
    confirmDialog.setHeaderTitle("¡Confirmar Fusión de Mascotas!");
    confirmDialog.setHeight("920px");

    VerticalLayout content = new VerticalLayout();
    content.setSpacing(true);
    content.setPadding(true);
    content.setWidth("480px");

    Div willKeep = new Div();
    willKeep
        .getStyle()
        .set("border", "2px solid #86efac")
        .set("background", "#f0fdf4")
        .set("borderRadius", "8px")
        .set("padding", "12px");

    Div willRemove = new Div();
    willRemove
        .getStyle()
        .set("border", "2px solid #fecaca")
        .set("background", "#fef2f2")
        .set("borderRadius", "8px")
        .set("padding", "12px");

    Div resultInfo = new Div();
    resultInfo
        .getStyle()
        .set("background", "#eff6ff")
        .set("border", "1px solid #bfdbfe")
        .set("borderRadius", "6px")
        .set("padding", "10px");
    resultInfo.setText(
        "Resultado: La mascota a mantener conservará/recibirá todos los dueños de ambas mascotas.");

    content.add(willKeep, willRemove, resultInfo);

    Button cancel = new Button("Cancelar", e -> confirmDialog.close());
    Button ok = new Button("Confirmar", e -> doMerge());
    ok.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST);

    HorizontalLayout footer = new HorizontalLayout(cancel, ok);
    footer.setWidthFull();
    footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
    footer.setSpacing(true);

    confirmDialog.add(content, footer);

    confirmDialog.addOpenedChangeListener(
        ev -> {
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
      Notification.show(
          "Por favor ingresa un nombre para buscar", 3000, Notification.Position.MIDDLE);
      return;
    }
    try {
      List<Pet> results = petService.findSimilarPetsByName(term);
      grid.setItems(results);
      if (results.isEmpty()) {
        Notification.show(
            "No se encontraron mascotas con ese nombre", 3000, Notification.Position.MIDDLE);
      } else if (results.size() == 1) {
        Notification.show(
            "Solo se encontró una mascota. Necesitas al menos 2 para fusionar.",
            3500,
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
    if (keepPet == null || removePet == null) return;
    if (Objects.equals(keepPet.getId(), removePet.getId())) {
      Notification.show(
          "Debes seleccionar dos mascotas distintas", 3000, Notification.Position.MIDDLE);
      return;
    }
    try {
      petService.mergePets(keepPet.getId(), removePet.getId());
      Notification.show(
          "Mascotas fusionadas exitosamente", 3000, Notification.Position.BOTTOM_CENTER);

      clearSelections();
      confirmDialog.close();
      handleSearch();
    } catch (Exception ex) {
      log.error("Error al fusionar mascotas", ex);
      Notification.show(
          "Error al fusionar: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
    }
  }

  private void updateMergeButtonState() {
    boolean canMerge =
        keepPet != null && removePet != null && !Objects.equals(idOf(keepPet), idOf(removePet));
    openMergeDialogBtn.setEnabled(canMerge);
  }

  private void clearSelections() {
    keepPet = null;
    removePet = null;
    updateMergeButtonState();
    refreshCards();
  }

  private void styleKeepCard() {
    keepCard
        .getStyle()
        .set("border", "2px solid #86efac")
        .set("background", "#f0fdf4")
        .set("borderRadius", "8px")
        .set("padding", "12px")
        .set("flex", "1");
  }

  private void styleRemoveCard() {
    removeCard
        .getStyle()
        .set("border", "2px solid #fecaca")
        .set("background", "#fef2f2")
        .set("borderRadius", "8px")
        .set("padding", "12px")
        .set("flex", "1");
  }

  private void refreshCards() {
    keepCard.removeAll();
    removeCard.removeAll();

    Icon checkIcon = VaadinIcon.CHECK.create();
    checkIcon.setColor("green");
    Paragraph keepText = new Paragraph("Mascota a mantener:");
    HorizontalLayout keepHeader = new HorizontalLayout(checkIcon, keepText);
    keepHeader.setAlignItems(FlexComponent.Alignment.CENTER);

    keepCard.add(keepHeader);
    keepCard.add(keepPet != null ? summaryBlock(keepPet) : new Paragraph("Sin seleccionar"));

    Icon closeIcon = VaadinIcon.CLOSE.create();
    closeIcon.setColor("red");
    Paragraph removeText = new Paragraph("Mascota a eliminar:");
    HorizontalLayout removeHeader = new HorizontalLayout(closeIcon, removeText);
    removeHeader.setAlignItems(FlexComponent.Alignment.CENTER);

    removeCard.add(removeHeader);
    removeCard.add(removePet != null ? summaryBlock(removePet) : new Paragraph("Sin seleccionar"));
  }

  private Div summaryBlock(Pet p) {
    Div d = new Div();
    String ownersCount = p.getOwners() != null ? String.valueOf(p.getOwners().size()) : "0";
    d.add(
        new Paragraph(
            nvl(p.getName())
                + " - "
                + getTypeLabel(p.getType())
                + " ("
                + ownersCount
                + " dueño(s))"));
    return d;
  }

  private Div detailsBlock(Pet p) {
    Div d = new Div();
    if (p == null) {
      d.add(new Paragraph("Sin seleccionar"));
      return d;
    }
    d.add(
        new Paragraph("Nombre: " + nvl(p.getName())),
        new Paragraph("Tipo: " + getTypeLabel(p.getType())),
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
    if (type == null) return "";
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

  private static String nvl(String s) {
    return s == null ? "" : s;
  }
}

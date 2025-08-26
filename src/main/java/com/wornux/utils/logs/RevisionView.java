package com.wornux.utils.logs;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.wornux.data.entity.Revision;
import com.wornux.services.AuditService;
import jakarta.annotation.security.PermitAll;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.Getter;
import org.hibernate.envers.RevisionType;

@PermitAll
public class RevisionView<T> {

  @Getter private final Grid<RevisionDto> grid = new Grid<>(RevisionDto.class, false);
  private final AuditService auditService;
  private final Class<T> tClass;
  private Long entityId;

  public RevisionView(AuditService auditService, Class<T> tClass) {
    this.auditService = auditService;
    this.tClass = tClass;
  }

  public void loadRevisions(Long entityId) {
    this.entityId = entityId;

    List<RevisionDto> revisionList = new ArrayList<>();
    for (Object object : auditService.getRowChanges(tClass, entityId)) {
      var result = (Object[]) object;

      RevisionDto dto = new RevisionDto();
      Revision revision = (Revision) result[1];
      RevisionType revisionType = (RevisionType) result[2];

      dto.setDocCode(entityId);
      dto.setId(revision.getId());
      dto.setDate(revision.getRevisionDate());
      dto.setModifierUser(revision.getModifierUser());
      dto.setIpAddress(revision.getIpAddress());
      dto.setType(EnversWrapper.revisionType(revisionType));
      revisionList.add(dto);
    }

    grid.setItems(revisionList);
  }

  public void configureGridRevision() {
    grid.setHeight("800px");
    grid.addColumn(rev -> formatDateToSpanish(rev.getDate())).setHeader("Fecha").setAutoWidth(true);
    grid.addColumn(RevisionDto::getModifierUser).setHeader("Usuario").setAutoWidth(true);
    grid.addColumn(rev -> rev.getIpAddress() != null ? rev.getIpAddress() : "Desconocida")
        .setHeader("Dirección IP")
        .setAutoWidth(true);
    grid.addColumn(rev -> translateOperationType(rev.getType()))
        .setHeader("Tipo")
        .setAutoWidth(true);

    grid.setItemDetailsRenderer(new ComponentRenderer<>(this::createDetailsComponent));
  }

  public Component createDetailsComponent(RevisionDto revision) {
    VerticalLayout detailsLayout = new VerticalLayout();
    detailsLayout.setPadding(false);
    detailsLayout.setSpacing(false);
    detailsLayout.setWidthFull();

    Map<String, Object[]> changes =
        auditService.getChangedProperties(tClass, entityId, revision.getId());
    if (changes.isEmpty()) {
      detailsLayout.add(new Text("No se encontraron cambios para esta revisión."));
      return detailsLayout;
    }

    List<ChangeDTO> list = new ArrayList<>();
    for (var entry : changes.entrySet()) {
      System.out.println("entry: " + entry);
      ChangeDTO dto = EnversWrapper.createChange(entry);
      list.add(dto);
    }

    Grid<ChangeDTO> changesGrid = new Grid<>(ChangeDTO.class, false);
    changesGrid.addColumn(ChangeDTO::getProperty).setHeader("Propiedad").setAutoWidth(true);
    changesGrid
        .addColumn(ChangeDTO::getPreviousValue)
        .setHeader("Valor Anterior")
        .setAutoWidth(true);
    changesGrid.addColumn(ChangeDTO::getCurrentValue).setHeader("Valor Actual").setAutoWidth(true);
    changesGrid.setItems(list);

    detailsLayout.add(new H4("Cambios en esta Revisión"), changesGrid);
    return detailsLayout;
  }

  private String formatDateToSpanish(java.util.Date date) {
    if (date == null) {
      return "Fecha no disponible";
    }
    DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern(
            "d 'de' MMMM 'de' yyyy 'a las' HH:mm", Locale.forLanguageTag("es-ES"));
    return date.toInstant().atZone(java.time.ZoneId.systemDefault()).format(formatter);
  }

  private String translateOperationType(String operationType) {
    if (operationType == null) {
      return "N/A";
    }
    return switch (operationType) {
      case "Add" -> "Creación";
      case "Mod" -> "Modificación";
      case "Del" -> "Eliminación";
      default -> operationType;
    };
  }
}

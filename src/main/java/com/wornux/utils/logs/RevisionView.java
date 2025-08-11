package com.wornux.utils.logs;

import com.wornux.data.entity.Revision;
import com.wornux.services.AuditService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import lombok.Getter;
import org.hibernate.envers.RevisionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RevisionView<T> {

    @Getter
    private final Grid<RevisionDto> grid = new Grid<>(RevisionDto.class, false);
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
        grid.addColumn(rev -> rev.getDate().toString()).setHeader("Date").setAutoWidth(true);
        grid.addColumn(RevisionDto::getModifierUser).setHeader("User").setAutoWidth(true);
        grid.addColumn(rev -> rev.getIpAddress() != null ? rev.getIpAddress() : "Unknown").setHeader("IP Address")
                .setAutoWidth(true);
        grid.addColumn(rev -> rev.getType() != null ? rev.getType() : "N/A").setHeader("Type").setAutoWidth(true);

        grid.setItemDetailsRenderer(new ComponentRenderer<>(this::createDetailsComponent));
    }

    public Component createDetailsComponent(RevisionDto revision) {
        VerticalLayout detailsLayout = new VerticalLayout();
        detailsLayout.setPadding(false);
        detailsLayout.setSpacing(false);
        detailsLayout.setWidthFull();

        Map<String, Object[]> changes = auditService.getChangedProperties(tClass, entityId, revision.getId());
        if (changes.isEmpty()) {
            detailsLayout.add(new Text("No changes found for this revision."));
            return detailsLayout;
        }

        List<ChangeDTO> list = new ArrayList<>();
        for (var entry : changes.entrySet()) {
            System.out.println("entry: " + entry);
            ChangeDTO dto = EnversWrapper.createChange(entry);
            list.add(dto);
        }

        Grid<ChangeDTO> changesGrid = new Grid<>(ChangeDTO.class, false);
        changesGrid.addColumn(ChangeDTO::getProperty).setHeader("Property").setAutoWidth(true);
        changesGrid.addColumn(ChangeDTO::getPreviousValue).setHeader("Previous Value").setAutoWidth(true);
        changesGrid.addColumn(ChangeDTO::getCurrentValue).setHeader("Current Value").setAutoWidth(true);
        changesGrid.setItems(list);

        detailsLayout.add(new H4("Changes in this Revision"), changesGrid);
        return detailsLayout;
    }
}

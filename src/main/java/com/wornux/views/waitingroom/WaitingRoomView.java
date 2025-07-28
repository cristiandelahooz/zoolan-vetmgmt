package com.wornux.views.waitingroom;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.data.entity.WaitingRoom;
import com.wornux.services.interfaces.ClientService;
import com.wornux.services.interfaces.PetService;
import com.wornux.services.interfaces.WaitingRoomService;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route("sala-espera")
@PageTitle("Sala de Espera")
public class WaitingRoomView extends VerticalLayout {

    private final WaitingRoomService waitingRoomService;
    private final ClientService clientService;
    private final PetService petService;

    private final Grid<WaitingRoom> grid = new Grid<>(WaitingRoom.class, false);
    private final WaitingRoomForm form;

    public WaitingRoomView(WaitingRoomService waitingRoomService,
            ClientService clientService,
            PetService petService) {
        this.waitingRoomService = waitingRoomService;
        this.clientService = clientService;
        this.petService = petService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setClassName(LumoUtility.Padding.LARGE);

        Span title = new Span("Sala de Espera");
        title.addClassNames(LumoUtility.FontWeight.BOLD, LumoUtility.FontSize.XLARGE);

        Icon infoIcon = VaadinIcon.INFO_CIRCLE_O.create();
        infoIcon.getStyle().set("cursor", "pointer").set("color", "var(--lumo-primary-color)");
        infoIcon.getElement().setProperty("title", "Aquí puedes gestionar las mascotas que están esperando ser atendidas.");

        HorizontalLayout titleWithInfo = new HorizontalLayout(title, infoIcon);
        titleWithInfo.setAlignItems(Alignment.CENTER);
        titleWithInfo.setSpacing(true);

        Button newEntryButton = new Button("Nueva Entrada", e -> openForm());
        newEntryButton.addThemeVariants(
                ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_CONTRAST,
                ButtonVariant.LUMO_SMALL
        );

        HorizontalLayout header = new HorizontalLayout(titleWithInfo, newEntryButton);
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        configureGrid();
        form = new WaitingRoomForm(waitingRoomService, clientService, petService);
        form.setOnSave(dto -> refreshGrid());

        add(header, grid);
        refreshGrid();
    }

    private void configureGrid() {
        grid.setSizeFull();

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        grid.addColumn(wr -> wr.getClient().getFirstName() + " " + wr.getClient().getLastName())
                .setHeader("Cliente");

        grid.addColumn(wr -> wr.getPet().getName())
                .setHeader("Mascota");

        grid.addColumn(WaitingRoom::getReasonForVisit)
                .setHeader("Razón de Visita");

        grid.addComponentColumn(this::renderPriority).setHeader("Prioridad");


        grid.addColumn(wr -> wr.getArrivalTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setHeader("Hora de Llegada");

        grid.addComponentColumn(this::renderStatus)
                .setHeader("Estado");

    }

    private void openForm() {
        form.openForNew();
    }

    private void refreshGrid() {
        List<WaitingRoom> entries = waitingRoomService.getCurrentWaitingRoom();
        grid.setItems(entries);
    }

    private Component renderPriority(WaitingRoom wr) {
        Span badge = new Span(wr.getPriority().name());
        badge.getElement().getThemeList().add("badge pill");

        switch (wr.getPriority()) {
        case URGENTE -> badge.getElement().getThemeList().add("error");
        case EMERGENCIA -> badge.getElement().getThemeList().add("warning");
        case NORMAL -> badge.getElement().getThemeList().add("success");
        }

        return badge;
    }

    private Component renderStatus(WaitingRoom wr) {
        if (wr.getStatus() == null) {
            return new Span("-");
        }

        Span badge = new Span(wr.getStatus().name().replace("_", " "));
        badge.getElement().getThemeList().add("badge pill");

        switch (wr.getStatus()) {
        case ESPERANDO -> badge.getElement().getThemeList().add("primary");
        case EN_CONSULTA -> badge.getElement().getThemeList().add("success");
        case COMPLETADO -> badge.getElement().getThemeList().add("contrast");
        case CANCELADO -> badge.getElement().getThemeList().add("error");
        default -> badge.getElement().getThemeList().add("badge");
        }

        return badge;
    }


}

package com.wornux.views.waitingroom;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
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
import com.wornux.data.enums.WaitingRoomStatus;
import com.wornux.services.interfaces.ClientService;
import com.wornux.services.interfaces.PetService;
import com.wornux.services.interfaces.WaitingRoomService;

import java.time.Duration;
import java.time.LocalDateTime;
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
    private final VerticalLayout cardContainer = new VerticalLayout();


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

        //add(header, grid);
        add(header, grid, cardContainer);
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

        grid.asSingleSelect().addValueChangeListener(event -> {
            WaitingRoom selected = event.getValue();
            if (selected != null) {
                openDetailsDialog(selected);
            }
        });

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

    private void openDetailsDialog(WaitingRoom wr) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Detalles de la Consulta");
        dialog.setModal(true);
        dialog.setDraggable(true);
        dialog.setResizable(true);
        dialog.setWidth("600px");

        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        layout.getStyle()
                .set("padding", "1.5rem")
                .set("border-radius", "10px")
                .set("background-color", "var(--lumo-base-color)")
                .set("box-shadow", "var(--lumo-box-shadow-m)");
        layout.setSpacing(true);
        layout.setPadding(false);

        // Encabezado: Prioridad y Estado
        Span priority = new Span(wr.getPriority().name());
        priority.getElement().getThemeList().add("badge pill");
        switch (wr.getPriority()) {
        case URGENTE -> priority.getElement().getThemeList().add("error");
        case EMERGENCIA -> priority.getElement().getThemeList().add("warning");
        case NORMAL -> priority.getElement().getThemeList().add("success");
        }

        Span status = new Span(wr.getStatus().name().replace("_", " "));
        status.getElement().getThemeList().add("badge pill");
        switch (wr.getStatus()) {
        case EN_CONSULTA -> status.getElement().getThemeList().add("success");
        case COMPLETADO -> status.getElement().getThemeList().add("contrast");
        case CANCELADO -> status.getElement().getThemeList().add("error");
        default -> status.getElement().getThemeList().add("primary");
        }

        HorizontalLayout header = new HorizontalLayout(priority, status);
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        // Datos del cliente
        Span clientName = new Span(wr.getClient().getFirstName() + " " + wr.getClient().getLastName());
        clientName.getElement().getStyle().set("font-weight", "bold").set("font-size", "1.2em");

        Span contactInfo = new Span("📞 " + wr.getClient().getPhoneNumber() + " • ✉ " + wr.getClient().getEmail());

        // Datos del animal
        Span petInfo = new Span(wr.getPet().getName() + " • " + wr.getPet().getType().name() + " • " + wr.getPet().getBreed() + " • " + wr.getPet().getGender());

        // Datos de visita
        Span reason = new Span("📝 Motivo: " + wr.getReasonForVisit());
        Span arrival = new Span("🕒 Hora de llegada: " + wr.getArrivalTime().format(DateTimeFormatter.ofPattern("hh:mm a")));

        Duration waitTime = Duration.ZERO;
        if (wr.getArrivalTime() != null && !wr.getArrivalTime().isAfter(LocalDateTime.now())) {
            waitTime = Duration.between(wr.getArrivalTime(), LocalDateTime.now());
        }
        String formattedWait = String.format("%dh %02dm", waitTime.toHours(), waitTime.toMinutesPart());
        Span waiting = new Span("⏳ Esperando: " + formattedWait);

        // Botones según el estado
        Button completeButton = new Button("Finalizar Consulta", e -> {
            wr.setStatus(WaitingRoomStatus.COMPLETADO);
            waitingRoomService.update(wr);
            refreshGrid();
            dialog.close();
        });
        completeButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        Button startButton = new Button("Iniciar Consulta", e -> {
            wr.setStatus(WaitingRoomStatus.EN_CONSULTA);
            waitingRoomService.update(wr);
            refreshGrid();
            dialog.close();
        });
        startButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        /*Button cancelButton = new Button("Cancelar", e -> {
            wr.setStatus(WaitingRoomStatus.CANCELADO);
            waitingRoomService.update(wr);
            refreshGrid();
            dialog.close();
        });*/

        Button cancelConsultation = new Button("Cancelar Consulta");
        cancelConsultation.addThemeVariants(ButtonVariant.LUMO_ERROR);

        HorizontalLayout actions = new HorizontalLayout(startButton, cancelConsultation);
        actions.setWidthFull();
        actions.setSpacing(true);

        startButton.setWidthFull();
        cancelConsultation.setWidthFull();


        cancelConsultation.addClickListener(e -> {
            ConfirmDialog dialogConfirm = new ConfirmDialog();
            dialogConfirm.setHeader("¿Estás seguro?");
            dialogConfirm.setText("Esto cancelará la consulta y marcará al paciente como cancelado.");

            dialogConfirm.setCancelable(true);
            dialogConfirm.setCancelText("No");
            dialogConfirm.setConfirmText("Sí, cancelar");

            dialogConfirm.addConfirmListener(event -> {
                wr.setStatus(WaitingRoomStatus.CANCELADO);
                waitingRoomService.update(wr);
                refreshGrid();
                dialog.close(); // cierra el modal original
            });

            dialogConfirm.open();
        });

        cancelConsultation.addThemeVariants(ButtonVariant.LUMO_ERROR);

        HorizontalLayout buttons = new HorizontalLayout();
        if (wr.getStatus() == WaitingRoomStatus.ESPERANDO) {
            buttons.add(startButton, cancelConsultation);
        } else if (wr.getStatus() == WaitingRoomStatus.EN_CONSULTA) {
            buttons.add(completeButton, cancelConsultation);
        }

        layout.add(header, clientName, contactInfo, petInfo, reason, arrival, waiting, buttons);
        dialog.add(layout);
        dialog.open();
    }



}

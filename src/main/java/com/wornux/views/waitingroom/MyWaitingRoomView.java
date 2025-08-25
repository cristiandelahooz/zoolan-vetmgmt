package com.wornux.views.waitingroom;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.wornux.data.entity.Employee;
import com.wornux.data.entity.WaitingRoom;
import com.wornux.data.enums.WaitingRoomStatus;
import com.wornux.security.UserUtils;
import com.wornux.services.implementations.InvoiceService;
import com.wornux.services.interfaces.*;
import com.wornux.utils.NotificationUtils;
import com.wornux.views.MainLayout;
import com.wornux.views.consultations.ConsultationsForm;
import jakarta.annotation.security.RolesAllowed;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Route(value = "mi-sala-espera", layout = MainLayout.class)
@PageTitle("Mi Sala de Espera")
@RolesAllowed({"ROLE_EMP_VETERINARIAN"})
public class MyWaitingRoomView extends VerticalLayout {

    private final WaitingRoomService waitingRoomService;
    private final ConsultationService consultationService;
    private final EmployeeService employeeService;
    private final PetService petService;
    private final InvoiceService invoiceService;
    private final ProductService productService;
    private final ServiceService serviceService;


    private final Grid<WaitingRoom> grid = new Grid<>(WaitingRoom.class, false);

    public MyWaitingRoomView(WaitingRoomService waitingRoomService,ServiceService serviceService,
                             ConsultationService consultationService, EmployeeService employeeService, PetService petService, InvoiceService invoiceService,ProductService productService) {
        this.waitingRoomService = waitingRoomService;
        this.consultationService = consultationService;
        this.employeeService = employeeService;
        this.petService = petService;
        this.invoiceService = invoiceService;
        this.productService = productService;
        this.serviceService = serviceService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        grid.addColumn(wr -> wr.getPet().getName()).setHeader("Mascota");
        grid.addColumn(wr -> wr.getClient().getFirstName() + " " + wr.getClient().getLastName()).setHeader("Cliente");
        grid.addColumn(WaitingRoom::getReasonForVisit).setHeader("Motivo");
        grid.addColumn(wr -> wr.getStatus().name()).setHeader("Estado");

        grid.addComponentColumn(wr -> {
            Button open = new Button("Atender", e -> openConsultationForm(wr));
            open.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            return open;
        }).setHeader("Acción");

        grid.asSingleSelect()
                .addValueChangeListener(
                        event -> {
                            WaitingRoom selected = event.getValue();
                            if (selected != null) {
                                openDetailsDialog(selected);
                            }
                        });

        add(grid);
        refreshGrid();
    }

    private void refreshGrid() {
        Employee currentVet = UserUtils.getCurrentEmployee().orElse(null);
        if (currentVet == null) {
            NotificationUtils.error("No se pudo identificar al veterinario logueado.");
            return;
        }
        var items = waitingRoomService.findByAssignedVeterinarian(currentVet.getId())
                .stream()
                .filter(wr -> wr.getStatus() == WaitingRoomStatus.ESPERANDO
                        || wr.getStatus() == WaitingRoomStatus.EN_CONSULTA)
                .toList();
        grid.setItems(items);
    }


    private void openConsultationForm(WaitingRoom wr) {
        Employee currentVet = UserUtils.getCurrentEmployee().orElse(null);
        if (currentVet == null) {
            NotificationUtils.error("No se pudo identificar al veterinario logueado.");
            return;
        }

        ConsultationsForm form = new ConsultationsForm(
                consultationService,
                employeeService,
                petService,
                serviceService,
                invoiceService,
                productService
        );
        form.openForNew();

        // bloquear vet (siempre en esta vista)
        form.presetForVetAndPet(wr.getPet(), currentVet, true);

        // pasar la entrada de waiting room para que el form la cierre al guardar
        form.attachWaitingRoom(wr);

        form.setOnSaveCallback(c -> {
            NotificationUtils.success("Consulta finalizada para " + wr.getPet().getName());
            refreshGrid();
        });

        form.open();
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
        layout
                .getStyle()
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

        Icon phoneIcon = VaadinIcon.PHONE.create();
        phoneIcon.setColor("var(--lumo-secondary-text-color)");

        Span phoneText = new Span(wr.getClient().getPhoneNumber());

        Icon mailIcon = VaadinIcon.ENVELOPE.create();
        mailIcon.setColor("var(--lumo-secondary-text-color)");

        Span emailText = new Span(wr.getClient().getEmail());

        HorizontalLayout contactInfo =
                new HorizontalLayout(phoneIcon, phoneText, new Span("•"), mailIcon, emailText);
        contactInfo.setAlignItems(FlexComponent.Alignment.CENTER);
        contactInfo.setSpacing(true);

        // Datos del animal
        Span petInfo =
                new Span(
                        wr.getPet().getName()
                                + " • "
                                + wr.getPet().getType().name()
                                + " • "
                                + wr.getPet().getBreed()
                                + " • "
                                + wr.getPet().getGender());
        petInfo
                .getElement()
                .getStyle()
                .set("font-weight", "600")
                .set("color", "var(--lumo-primary-text-color)")
                .set("font-size", "1.05em");

        // Datos de visita

        Icon reasonIcon = VaadinIcon.CLIPBOARD_TEXT.create();
        reasonIcon.setColor("var(--lumo-secondary-text-color)");
        Span reasonText = new Span("Motivo: " + wr.getReasonForVisit());
        HorizontalLayout reason = new HorizontalLayout(reasonIcon, reasonText);

        Icon notesIcon = VaadinIcon.NOTEBOOK.create();
        notesIcon.setColor("var(--lumo-secondary-text-color)");
        Span notesText =
                new Span(
                        "Notas: "
                                + (wr.getNotes() != null && !wr.getNotes().isBlank() ? wr.getNotes() : "N/A"));
        HorizontalLayout notes = new HorizontalLayout(notesIcon, notesText);

        Icon arrivalIcon = VaadinIcon.CLOCK.create();
        arrivalIcon.setColor("var(--lumo-secondary-text-color)");
        Span arrivalText =
                new Span(
                        "Hora de llegada: "
                                + wr.getArrivalTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
        HorizontalLayout arrival = new HorizontalLayout(arrivalIcon, arrivalText);

        Icon motivoIcon = VaadinIcon.CLIPBOARD_TEXT.create();
        motivoIcon.setColor("var(--lumo-secondary-text-color)");

        Duration waitTime = Duration.ZERO;
        if (wr.getArrivalTime() != null && !wr.getArrivalTime().isAfter(LocalDateTime.now())) {
            waitTime = Duration.between(wr.getArrivalTime(), LocalDateTime.now());
        }
        String formattedWait = String.format("%dh %02dm", waitTime.toHours(), waitTime.toMinutesPart());

        Icon waitIcon = VaadinIcon.HOURGLASS.create();
        waitIcon.setColor("var(--lumo-secondary-text-color)");
        Span waitingText = new Span("Esperando: " + formattedWait);
        HorizontalLayout waiting = new HorizontalLayout(waitIcon, waitingText);

        // Botones según el estado
        Button completeButton =
                new Button(
                        "Finalizar Consulta",
                        e -> {
                            wr.setStatus(WaitingRoomStatus.COMPLETADO);
                            waitingRoomService.update(wr);
                            refreshGrid();
                            dialog.close();
                        });
        completeButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        completeButton.setWidth("220px");

   /* Button startButton =
        new Button(
            "Iniciar Consulta",
            e -> {
              wr.setStatus(WaitingRoomStatus.EN_CONSULTA);
              waitingRoomService.update(wr);
              refreshGrid();
              dialog.close();
            });*/
        //startButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelConsultation = new Button("Cancelar Consulta");
        cancelConsultation.addThemeVariants(ButtonVariant.LUMO_ERROR);

        //HorizontalLayout actions = new HorizontalLayout(startButton, cancelConsultation);
        HorizontalLayout actions = new HorizontalLayout(cancelConsultation);
        actions.setSpacing(true);
        actions.setJustifyContentMode(JustifyContentMode.CENTER);
        actions.setWidthFull();

        actions.getStyle().set("margin-top", "1rem");

        //startButton.setMinWidth("200px");
        cancelConsultation.setMinWidth("200px");

        cancelConsultation.addClickListener(
                e -> {
                    ConfirmDialog dialogConfirm = new ConfirmDialog();
                    dialogConfirm.setHeader("¿Estás seguro?");
                    dialogConfirm.setText("Esto cancelará la consulta y marcará al paciente como cancelado.");

                    dialogConfirm.setCancelable(true);
                    dialogConfirm.setCancelText("No");
                    dialogConfirm.setConfirmText("Sí, cancelar");

                    dialogConfirm.addConfirmListener(
                            event -> {
                                if (wr.getStatus().equals(WaitingRoomStatus.EN_CONSULTA)) {
                                    NotificationUtils.error("No se puede cancelar una entrada que está en consulta.");
                                    return;
                                }
                                waitingRoomService.delete(wr.getId());
                                refreshGrid();
                                dialog.close();
                            });

                    dialogConfirm.open();
                });

        cancelConsultation.addThemeVariants(ButtonVariant.LUMO_ERROR);

        HorizontalLayout buttons = new HorizontalLayout();
        if (wr.getStatus() == WaitingRoomStatus.ESPERANDO) {
            //buttons.add(startButton, cancelConsultation);
            buttons.add(cancelConsultation);
        } else if (wr.getStatus() == WaitingRoomStatus.EN_CONSULTA) {
            //buttons.add(completeButton, cancelConsultation);
        }

        layout.add(header, clientName, contactInfo, petInfo, reason, notes, arrival, waiting, buttons);
        dialog.add(layout);
        dialog.open();
    }


}



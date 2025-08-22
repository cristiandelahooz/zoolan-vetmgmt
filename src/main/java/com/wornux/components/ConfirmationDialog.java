package com.wornux.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import java.util.function.Consumer;

public final class ConfirmationDialog {

    private ConfirmationDialog() {
    }

    public static void confirmation(Consumer<ConfirmDialog.ConfirmEvent> action, Component... components) {
        ConfirmDialog dialog = new ConfirmationDialogBuilder().withHeader("Confirmar cambios").withText(
                "Hay cambios sin guardar. ¿Quieres descartarlos o continuar?").withComponents(components)
                .withCancelText("Descartar").withConfirmText("Continuar").withIcon(VaadinIcon.QUESTION_CIRCLE_O)
                .onConfirm(action).build();
        dialog.open();
    }

    public static void delete(Consumer<ConfirmDialog.ConfirmEvent> action, Component... components) {
        ConfirmDialog dialog = new ConfirmationDialogBuilder().withHeader("Confirmar eliminación").withText(
                "Estás eliminando un registro. ¿Quieres descartarlo o continuar?").withComponents(components)
                .withCancelText("Descartar").withConfirmText("Continuar").withIcon(VaadinIcon.QUESTION_CIRCLE_O)
                .onConfirm(action).build();
        dialog.open();
    }

    public static void cancel(Consumer<ConfirmDialog.ConfirmEvent> action, Component... components) {

        ConfirmDialog dialog = new ConfirmationDialogBuilder().withHeader("Confirmar cancelación").withText(
                "Estás cancelando un registro. ¿Quieres descartarlo o continuar?").withComponents(components)
                .withConfirmText("Continuar").withCancelText("Descartar").withIcon(VaadinIcon.QUESTION_CIRCLE_O)
                .onConfirm(action).build();
        dialog.open();
    }

    public static void saveUpdate(Consumer<ConfirmDialog.ConfirmEvent> action, Component... components) {
        ConfirmDialog dialog = new ConfirmationDialogBuilder().withHeader("Confirmar actualización").withText(
                "Estás a punto de actualizar la factura. ¿Quieres continuar?").withComponents(components)
                .withCancelText("Cancelar").withConfirmText("Sí, actualizar").withIcon(VaadinIcon.QUESTION_CIRCLE_O)
                .onConfirm(action).build();
        dialog.open();
    }
}

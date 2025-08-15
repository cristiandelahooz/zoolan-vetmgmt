package com.wornux.views.customers;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.shared.HasClearButton;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.wornux.components.ConfirmationDialog;
import com.wornux.components.ConfirmationDialogBuilder;
import com.wornux.components.DecimalField;
import com.wornux.components.Sidebar;
import com.wornux.data.entity.Client;
import com.wornux.data.enums.ClientType;
import com.wornux.services.interfaces.ClientService;
import com.wornux.utils.CommonUtils;
import com.wornux.utils.NotificationUtils;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@Slf4j
public class ClientForm extends Div {

  private final TextField name = new TextField("Name");
  private final ComboBox<ClientType> type = new ComboBox("Type");
  private final EmailField email = new EmailField("Email");
  private final TextField phone = new TextField("Phone");
  private final TextArea address = new TextArea("Address");
  private final DecimalField hourlyRateField = new DecimalField("Hourly rate");
  private final TextField contactName = new TextField("Name");
  private final EmailField contactEmail = new EmailField("Email");
  private final TextArea notes = new TextArea("Notes");

  private final Sidebar sidebar = new Sidebar();
  private final ClientService service;
  private final Binder<Client> binder = new BeanValidationBinder<>(Client.class);
  private Client element;
  @Setter
  private Runnable callable;

  @Setter
  private Consumer<Client> consumer;

  public ClientForm(ClientService service) {
    this.service = service;

    CommonUtils.commentsFormat(address, 250);
    CommonUtils.commentsFormat(notes, 500);

    Arrays.asList(address, notes)
        .forEach(
            c -> {
              c.setMinRows(4);
              c.setMaxRows(4);
            });

    H4 contactPersonInformation = new H4("Contact person information");
    contactPersonInformation.addClassNames(LumoUtility.Margin.Top.MEDIUM);
    sidebar.addSubTitle("Fill out the form below to manage a customer!");

    name.setClearButtonVisible(true);
    name.setPrefixComponent(VaadinIcon.HOME.create());
    type.setClearButtonVisible(true);
    email.setClearButtonVisible(true);
    email.setPrefixComponent(VaadinIcon.ENVELOPE.create());
    phone.setClearButtonVisible(true);
    phone.setPrefixComponent(VaadinIcon.PHONE_LANDLINE.create());
    address.setClearButtonVisible(true);
    hourlyRateField.setClearButtonVisible(true);
    contactName.setClearButtonVisible(true);
    contactEmail.setClearButtonVisible(true);
    contactEmail.setPrefixComponent(VaadinIcon.ENVELOPE.create());

    type.setItems(ClientType.values());
    type.setItemLabelGenerator(ClientType::getDisplay);

    binder.bindInstanceFields(this);
    binder
        .getFields()
        .forEach(
            field -> {
              if (field instanceof HasClearButton clear) {
                clear.setClearButtonVisible(true);
              }
            });

    sidebar.createContent(
        new H4("General information"),
        name,
        type,
        email,
        phone,
        address,
        hourlyRateField,
        contactPersonInformation,
        contactName,
        contactEmail,
        new Hr(),
        notes);

    sidebar.setOnSaveClickListener(this::saveOrUpdate);
    sidebar.setOnCancelClickListener(this::cancel);
    sidebar.setOnDeleteClickListener(this::archive);

    add(sidebar);
  }

  private void archive(ClickEvent<Button> buttonClickEvent) {

    ConfirmDialog dialog =
        new ConfirmationDialogBuilder()
            .withHeader("Archive Client Confirmation")
            .withText(
                "Would you like to archive and disable <<%s>>?".formatted(element.getFirstName()))
            .withCancelText("No")
            .withConfirmText("Yes, archive")
            .withIcon(VaadinIcon.QUESTION_CIRCLE_O)
            .onConfirm(
                event -> {
                  service.archive(element);
                  NotificationUtils.success(
                      "<<<%s>>> has been archived in the following location Settings > Archived customers"
                          .formatted(element.getFirstName()));

                  Optional.ofNullable(callable).ifPresent(Runnable::run);

                  Optional.ofNullable(consumer).ifPresent(v -> v.accept(element));

                  populateForm(null);
                  sidebar.close();
                })
            .build();
    dialog.open();
  }

  private void cancel(ClickEvent<Button> buttonClickEvent) {
    populateForm(null);
    sidebar.close();
  }

  private void saveOrUpdate(ClickEvent<Button> buttonClickEvent) {
    try {

      if (element == null) {
        element = new Client();
      }

      binder.writeBean(this.element);

      ConfirmationDialog.confirmation(
          event -> {
            service.archive(element);

            Optional.ofNullable(callable).ifPresent(Runnable::run);
            Optional.ofNullable(consumer).ifPresent(v -> v.accept(element));

            sidebar.close();

            populateForm(element);
          });

    } catch (ObjectOptimisticLockingFailureException ex) {
      log.error(ex.getLocalizedMessage());
      NotificationUtils.error(
          "Error updating the data. Somebody else has updated the record while you were making changes.");
    } catch (ValidationException ex) {
      log.error(ex.getLocalizedMessage());
      NotificationUtils.error(ex);
    }
  }

  private void populateForm(Client value) {
    element = value;
    binder.readBean(element);

    name.focus();
  }

  public void close() {
    sidebar.close();
  }

  public void open() {
    open(null);
  }

  public void open(Client customer) {
    if (customer == null) {
      sidebar.newObject("New Client");
    } else {
      sidebar.getDelete().setText("Archive");
      sidebar.editObject("Edit Client");
    }
    populateForm(customer);
  }
}

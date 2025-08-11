package com.wornux.views.products;

import com.wornux.components.ConfirmationDialog;
import com.wornux.components.DecimalField;
import com.wornux.components.Sidebar;
import com.wornux.data.entity.Product;
import com.wornux.services.interfaces.ProductService;
import com.wornux.utils.CommonUtils;
import com.wornux.utils.NotificationUtils;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.shared.HasClearButton;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
public class ProductServiceForm extends Div {

    private final TextField name = new TextField("Name");
    private final DecimalField price = new DecimalField("Price");
    private final TextArea description = new TextArea("Description");

    private final Sidebar sidebar = new Sidebar();
    private final ProductService service;
    private final Binder<Product> binder = new BeanValidationBinder<>(Product.class);
    private Product element;
    @Setter
    private Runnable callable;

    @Setter
    private Consumer<Product> consumer;

    public ProductServiceForm(ProductService service) {
        this.service = service;

        CommonUtils.commentsFormat(description, 250);

        description.setMaxRows(4);
        description.setMinRows(4);

        sidebar.addSubTitle("Fill out the form below to manage a product & service!");

        name.setClearButtonVisible(true);
        price.setClearButtonVisible(true);

        binder.bindInstanceFields(this);
        binder.getFields().forEach(field -> {
            if (field instanceof HasClearButton clear) {
                clear.setClearButtonVisible(true);
            }
        });

        sidebar.createContent(new H4("General information"), name, description, price);

        sidebar.setOnSaveClickListener(this::saveOrUpdate);
        sidebar.setOnCancelClickListener(this::cancel);
        sidebar.setOnDeleteClickListener(this::delete);

        add(sidebar);
    }

    public void close() {
        sidebar.close();
    }

    public void open() {
        populateForm(null);
        sidebar.newObject("New Products & Services");
    }

    public void edit(Product item) {
        populateForm(item);
        sidebar.editObject("Edit Products & Services");
    }

    private void delete(ClickEvent<Button> buttonClickEvent) {
        ConfirmationDialog.delete(event -> {

            service.delete(element.getId());
            Optional.ofNullable(callable).ifPresent(Runnable::run);

            sidebar.close();
            NotificationUtils.success("The record was successfully deleted.");
        });
    }

    private void cancel(ClickEvent<Button> buttonClickEvent) {
        populateForm(null);
        sidebar.close();
    }

    private void saveOrUpdate(ClickEvent<Button> buttonClickEvent) {
        try {

            if (element == null) {
                element = new Product();
            }
            binder.writeBean(this.element);

            ConfirmationDialog.confirmation(event -> {
                service.update(element);
                Optional.ofNullable(callable).ifPresent(Runnable::run);
                Optional.ofNullable(consumer).ifPresent(c -> c.accept(element));

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

    private void populateForm(Product value) {
        element = value;
        binder.readBean(element);

        name.focus();
    }

}

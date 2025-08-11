package com.wornux.utils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.HasMenuItems;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinServletRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.jpa.domain.Specification;

import java.text.Normalizer;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * @author me@fredpena.dev
 * @created 15/11/2024 - 13:22
 */
public final class CommonUtils {

    private CommonUtils() {
    }

    public static void commentsFormat(TextArea comments) {
        commentsFormat(comments, 500);
    }

    public static void commentsFormat(TextArea comments, int charLimit) {
        comments.setClearButtonVisible(true);

        comments.setWidthFull();
        comments.setMaxLength(charLimit);
        comments.setValueChangeMode(ValueChangeMode.EAGER);
        comments.addValueChangeListener(e -> e.getSource().setHelperText(e.getValue().length() + "/" + charLimit));
    }

    public static String normalizeText(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase();
    }

    public static <C> ComboBox.ItemFilter<C> comboBoxItemFilter(Function<C, String> propertyExtractor,
            BiPredicate<String, String> filterLogic) {
        return (item, filterText) -> filterLogic.test(normalizeText(propertyExtractor.apply(item)),
                normalizeText(filterText));
    }

    public static <T> Renderer<T> disabledRenderer(ValueProvider<T, Boolean> provider) {
        return LitRenderer.<T> of(
                "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", value -> provider.apply(value) ? "check" : "minus").withProperty("color",
                        value -> provider.apply(value)
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");
    }

    public static <T> List<Long> createFilteredDisplacement(Specification<T> specification, Class<T> tClass,
            EntityManager entityManager, String field) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<T> root = query.from(tClass);

        query.select(root.get(field));
        if (specification != null) {
            Predicate predicate = specification.toPredicate(root, query, builder);
            query.where(predicate);
        }

        query.orderBy(builder.asc(root.get(field)));

        return entityManager.createQuery(query).getResultList();
    }

    public static MenuItem createIconItem(HasMenuItems menu, Component iconName, String label) {
        return createIconItem(menu, iconName, label, false);
    }

    public static MenuItem createIconItem(HasMenuItems menu, Component icon, String label, boolean isChild) {

        if (isChild) {
            icon.getStyle().set("width", "var(--lumo-icon-size-s)");
            icon.getStyle().set("height", "var(--lumo-icon-size-s)");
            icon.getStyle().set("marginRight", "var(--lumo-space-s)");
        }

        MenuItem item = menu.addItem(icon, e -> {
        });

        item.setAriaLabel(label);

        if (label != null) {
            item.add(new Text(label));
        }

        return item;
    }

    public static String getBaseUrl() {
        HttpServletRequest request = ((VaadinServletRequest) VaadinRequest.getCurrent()).getHttpServletRequest();
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        String portPart = (serverPort == 80 || serverPort == 443) ? "" : ":" + serverPort;
        return scheme + "://" + serverName + portPart;
    }

}

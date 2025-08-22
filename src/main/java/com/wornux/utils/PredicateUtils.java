package com.wornux.utils;

import static com.wornux.utils.CommonUtils.normalizeText;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.persistence.criteria.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public final class PredicateUtils {

    private PredicateUtils() {
    }

    public static <T> Predicate predicateLike(Root<T> root, CriteriaBuilder builder, String field, String filter) {
        return builder.like(builder.lower(root.get(field)), "%" + normalizeText(filter) + "%");
    }

    public static Predicate predicateUnaccentLike(CriteriaBuilder builder, Path<?> path, String field, String search) {
        return builder.like(builder.function("unaccent", String.class, builder.lower(path.get(field))),
                "%" + normalizeText(search) + "%");
    }

    public static <T> Predicate predicateUnaccentLike(Root<T> root, CriteriaBuilder builder, String field,
            String filter) {

        return builder.like(builder.function("unaccent", String.class, builder.lower(root.get(field))),
                "%" + normalizeText(filter) + "%");
    }

    public static <T> Predicate predicateUnaccentLike(Path<T> path, CriteriaBuilder builder, String field,
            String filter) {

        return builder.like(builder.function("unaccent", String.class, builder.lower(path.get(field))),
                "%" + normalizeText(filter) + "%");
    }

    public static Predicate predicateForNumericField(Root<?> root, CriteriaBuilder builder, String field, String filter,
            Class<?> fieldType) {
        return predicateForNumericField(builder, filter, fieldType, root.get(field), builder.literal(""));
    }

    public static Predicate predicateForNumericField(Path<?> root, CriteriaBuilder builder, String field, String filter,
            Class<?> fieldType) {
        return predicateForNumericField(builder, filter, fieldType, root.get(field), builder.literal(""));
    }

    private static Predicate predicateForNumericField(CriteriaBuilder builder, String filter, Class<?> fieldType,
            Expression<?>... expressions) {
        if (isBase10Number(filter)) {
            try {
                if (fieldType.equals(Integer.class) || fieldType.equals(Long.class)) {
                    return builder.like(builder.function("CONCAT", String.class, expressions), "%" + filter
                            .trim() + "%");
                } else {
                    throw new IllegalArgumentException("Field type not supported: " + fieldType);
                }
            } catch (NumberFormatException e) {
                return builder.conjunction();
            }
        } else {
            return builder.like(builder.function("CONCAT", String.class, expressions), "%" + filter.trim() + "%");
        }
    }

    public static Predicate createPredicateForOptionalValue(Optional<?> optional,
            Function<Object, Predicate> predicateFunction, CriteriaBuilder builder) {
        return optional.map(predicateFunction).orElseGet(builder::conjunction);
    }

    public static Predicate createPredicateForSelectedItems(Optional<Collection<?>> selectedItems,
            Function<Collection<?>, Predicate> predicateFunction, CriteriaBuilder builder) {
        return selectedItems.filter(items -> !items.isEmpty()).map(predicateFunction).orElseGet(builder::conjunction);
    }

    public static Predicate createPredicateForSelectedItemsMandatory(Optional<Collection<?>> selectedItems,
            Function<Collection<?>, Predicate> predicateFunction, CriteriaBuilder builder) {
        return selectedItems.filter(items -> !items.isEmpty()).map(predicateFunction).orElseGet(builder::disjunction);
    }

    public static Predicate createPredicateForCheckBox(Optional<Boolean> checkBoxValue,
            Function<Boolean, Predicate> predicateFunction, CriteriaBuilder builder) {
        return checkBoxValue.filter(checked -> checked).map(predicateFunction).orElseGet(builder::conjunction);
    }

    public static Predicate createPredicateForInstantRange(LocalDate initDate, LocalDate endDate,
            Path<Instant> pathGreater, Path<Instant> pathLess, CriteriaBuilder builder, String timeZone) {
        if (initDate != null && endDate != null) {
            Instant startInstant = initDate.atStartOfDay(ZoneId.of(timeZone)).toInstant();
            Instant endInstant = endDate.atTime(LocalTime.MAX).atZone(ZoneId.of(timeZone)).toInstant();
            return builder.and(builder.greaterThanOrEqualTo(pathGreater, startInstant), builder.lessThanOrEqualTo(
                    pathLess, endInstant));
        }
        return builder.conjunction();
    }

    public static Predicate createPredicateForInstantRange(LocalDate initDate, LocalDate endDate,
            Path<Instant> pathGreater, Path<Instant> pathLess, CriteriaBuilder builder, ZoneId timeZone) {
        if (initDate != null && endDate != null) {
            Instant startInstant = initDate.atStartOfDay(timeZone).toInstant();
            Instant endInstant = endDate.atTime(LocalTime.MAX).atZone(timeZone).toInstant();
            return builder.and(builder.greaterThanOrEqualTo(pathGreater, startInstant), builder.lessThanOrEqualTo(
                    pathLess, endInstant));
        }
        return builder.conjunction();
    }

    public static Predicate createPredicateForDateRange(LocalDate initDate, LocalDate endDate,
            Path<LocalDate> pathGreater, Path<LocalDate> pathLess, CriteriaBuilder builder) {
        if (initDate != null && endDate != null) {
            return builder.and(builder.greaterThanOrEqualTo(pathGreater, initDate), builder.lessThanOrEqualTo(pathLess,
                    endDate));
        }
        return builder.conjunction();
    }

    public static <T> void configureMultiSelectComboBox(MultiSelectComboBox<T> comboBox,
            ItemLabelGenerator<T> labelGenerator,
            HasValue.ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<MultiSelectComboBox<T>, Set<T>>> valueChangeListener) {
        comboBox.addClassNames(LumoUtility.Padding.Right.SMALL, LumoUtility.Padding.Top.NONE);
        comboBox.setItemLabelGenerator(labelGenerator);
        comboBox.setClearButtonVisible(true);
        comboBox.setAutoExpand(MultiSelectComboBox.AutoExpandMode.BOTH);
        comboBox.addValueChangeListener(valueChangeListener);
    }

    public static <T> void configureSingleSelectComboBox(ComboBox<T> comboBox, ItemLabelGenerator<T> labelGenerator,
            HasValue.ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ComboBox<T>, T>> valueChangeListener) {
        comboBox.addClassNames(LumoUtility.Padding.Right.SMALL, LumoUtility.Padding.Top.NONE);
        comboBox.setItemLabelGenerator(labelGenerator);
        comboBox.setClearButtonVisible(true);
        comboBox.addValueChangeListener(valueChangeListener);
    }

    public static void configureDateFilters(DatePicker initDate, DatePicker endDate, Grid<?> grid) {
        initDate.addClassNames(LumoUtility.Padding.Right.SMALL, LumoUtility.Padding.Top.NONE);
        endDate.addClassNames(LumoUtility.Padding.Right.SMALL, LumoUtility.Padding.Top.NONE);
        initDate.setClearButtonVisible(true);
        endDate.setClearButtonVisible(true);

        initDate.addValueChangeListener(e -> {
            endDate.setMin(e.getValue());
            grid.getDataProvider().refreshAll();
        });
        endDate.addValueChangeListener(e -> {
            initDate.setMax(e.getValue());
            grid.getDataProvider().refreshAll();
        });
    }

    public static void configureCheckBoxes(List<? extends Component> checkBoxes, Runnable runnable) {
        checkBoxes.forEach(c -> {
            c.addClassNames(LumoUtility.Padding.Right.SMALL, LumoUtility.Padding.Top.NONE);
            if (c instanceof HasValue<?, ?> hasValue) {
                hasValue.addValueChangeListener(event -> runnable.run());
            }
        });
    }

    public static <T> Predicate predicateForTextField(Root<T> root, CriteriaBuilder builder, String[] fields,
            String searchValue) {
        if (searchValue == null || searchValue.trim().isEmpty()) {
            return builder.conjunction();
        }

        String normalizedSearch = normalizeText(searchValue.trim());
        Predicate[] predicates = new Predicate[fields.length];

        for (int i = 0; i < fields.length; i++) {
            predicates[i] = builder.like(builder.lower(root.get(fields[i])), "%" + normalizedSearch + "%");
        }

        return builder.or(predicates);
    }

    private static boolean isBase10Number(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        return value.trim().matches("^-?\\d+$");
    }
}

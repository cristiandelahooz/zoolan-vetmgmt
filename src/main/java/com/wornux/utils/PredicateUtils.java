package com.wornux.utils;

import static com.wornux.utils.CommonUtils.normalizeText;

import jakarta.persistence.criteria.*;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

public final class PredicateUtils {

  private PredicateUtils() {}

  public static <T> Predicate predicateUnaccentLike(
      Path<T> path, CriteriaBuilder builder, String field, String filter) {

    return builder.like(
        builder.function("unaccent", String.class, builder.lower(path.get(field))),
        "%" + normalizeText(filter) + "%");
  }

  public static Predicate predicateForNumericField(
      Root<?> root, CriteriaBuilder builder, String field, String filter, Class<?> fieldType) {
    return predicateForNumericField(
        builder, filter, fieldType, root.get(field), builder.literal(""));
  }

  public static Predicate predicateForNumericField(
      Path<?> root, CriteriaBuilder builder, String field, String filter, Class<?> fieldType) {
    return predicateForNumericField(
        builder, filter, fieldType, root.get(field), builder.literal(""));
  }

  private static Predicate predicateForNumericField(
      CriteriaBuilder builder, String filter, Class<?> fieldType, Expression<?>... expressions) {
    if (isBase10Number(filter)) {
      try {
        if (fieldType.equals(Integer.class) || fieldType.equals(Long.class)) {
          return builder.like(
              builder.function("CONCAT", String.class, expressions), "%" + filter.trim() + "%");
        } else {
          throw new IllegalArgumentException("Field type not supported: " + fieldType);
        }
      } catch (NumberFormatException e) {
        return builder.conjunction();
      }
    } else {
      return builder.like(
          builder.function("CONCAT", String.class, expressions), "%" + filter.trim() + "%");
    }
  }

  public static Predicate createPredicateForSelectedItems(
      Optional<Collection<?>> selectedItems,
      Function<Collection<?>, Predicate> predicateFunction,
      CriteriaBuilder builder) {
    return selectedItems
        .filter(items -> !items.isEmpty())
        .map(predicateFunction)
        .orElseGet(builder::conjunction);
  }

  public static <T> Predicate predicateForTextField(
      Root<T> root, CriteriaBuilder builder, String[] fields, String searchValue) {
    if (searchValue == null || searchValue.trim().isEmpty()) {
      return builder.conjunction();
    }

    String normalizedSearch = normalizeText(searchValue.trim());
    Predicate[] predicates = new Predicate[fields.length];

    for (int i = 0; i < fields.length; i++) {
      predicates[i] =
          builder.like(builder.lower(root.get(fields[i])), "%" + normalizedSearch + "%");
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

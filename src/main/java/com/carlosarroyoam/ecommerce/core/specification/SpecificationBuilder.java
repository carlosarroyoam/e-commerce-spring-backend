package com.carlosarroyoam.ecommerce.core.specification;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.springframework.data.jpa.domain.Specification;

/**
 * Constructor fluido y null-safe de {@link Specification} de Criteria API para los endpoints de
 * listado con filtros por query params: cada método {@code *IfPresent} añade una condición solo
 * si el valor recibido está presente (no nulo, o no vacío/en blanco según el caso).
 *
 * @param <T> el tipo de entidad sobre el que se construye la especificación
 */
public class SpecificationBuilder<T> {
  private final List<Specification<T>> specs = new ArrayList<>();

  /**
   * Crea una nueva instancia vacía del builder.
   *
   * @param <T> el tipo de entidad
   * @return un nuevo {@link SpecificationBuilder}
   */
  public static <T> SpecificationBuilder<T> builder() {
    return new SpecificationBuilder<>();
  }

  /**
   * Combina todas las condiciones añadidas con AND lógico.
   *
   * @return la {@link Specification} resultante, o una que siempre es verdadera si no se añadió
   *     ninguna condición
   */
  public Specification<T> build() {
    return specs.stream().reduce(Specification::and).orElse((root, query, cb) -> cb.conjunction());
  }

  /**
   * Añade una condición de igualdad si {@code value} no es nulo.
   *
   * @param path función que resuelve el {@link Path} del atributo sobre el {@link Root}
   * @param value el valor a comparar, puede ser nulo
   * @return este mismo builder, para encadenar llamadas
   */
  public <Y> SpecificationBuilder<T> equalsIfPresent(Function<Root<T>, Path<Y>> path, Y value) {
    if (value != null) {
      specs.add((root, query, cb) -> cb.equal(path.apply(root), value));
    }
    return this;
  }

  /**
   * Añade una condición {@code LIKE} (insensible a mayúsculas) si {@code value} no es nulo ni
   * está en blanco.
   *
   * @param path función que resuelve el {@link Path} del atributo sobre el {@link Root}
   * @param value el texto a buscar, puede ser nulo o vacío
   * @return este mismo builder, para encadenar llamadas
   */
  public SpecificationBuilder<T> likeIfPresent(Function<Root<T>, Path<String>> path, String value) {
    if (value != null && !value.isBlank()) {
      specs.add(
          (root, query, cb) ->
              cb.like(cb.lower(path.apply(root)), "%" + value.toLowerCase() + "%"));
    }
    return this;
  }

  /**
   * Añade una condición de rango: entre {@code min} y {@code max} si ambos están presentes, o
   * solo el límite correspondiente si únicamente uno de ellos lo está.
   *
   * @param path función que resuelve el {@link Path} del atributo sobre el {@link Root}
   * @param min el valor mínimo (inclusive), puede ser nulo
   * @param max el valor máximo (inclusive), puede ser nulo
   * @return este mismo builder, para encadenar llamadas
   */
  public <Y extends Comparable<? super Y>> SpecificationBuilder<T> betweenIfPresent(
      Function<Root<T>, Path<Y>> path, Y min, Y max) {
    if (min != null && max != null) {
      specs.add((root, query, cb) -> cb.between(path.apply(root), min, max));
    } else if (min != null) {
      specs.add((root, query, cb) -> cb.greaterThanOrEqualTo(path.apply(root), min));
    } else if (max != null) {
      specs.add((root, query, cb) -> cb.lessThanOrEqualTo(path.apply(root), max));
    }
    return this;
  }

  /**
   * Añade una condición de rango de fechas: desde el inicio del día de {@code start} hasta el
   * final del día de {@code end}, cada límite solo si está presente.
   *
   * @param path función que resuelve el {@link Path} del atributo de fecha/hora sobre el
   *     {@link Root}
   * @param start la fecha mínima (inclusive), puede ser nula
   * @param end la fecha máxima (inclusive), puede ser nula
   * @return este mismo builder, para encadenar llamadas
   */
  public SpecificationBuilder<T> betweenDatesIfPresent(
      Function<Root<T>, Path<LocalDateTime>> path, LocalDate start, LocalDate end) {
    if (start != null) {
      specs.add(
          (root, query, cb) -> cb.greaterThanOrEqualTo(path.apply(root), start.atStartOfDay()));
    }

    if (end != null) {
      specs.add(
          (root, query, cb) -> cb.lessThanOrEqualTo(path.apply(root), end.atTime(23, 59, 59)));
    }

    return this;
  }

  /**
   * Añade una condición {@code IN} si {@code values} no es nulo ni está vacío.
   *
   * @param path función que resuelve el {@link Path} del atributo sobre el {@link Root}
   * @param values los valores permitidos, puede ser nulo o vacío
   * @return este mismo builder, para encadenar llamadas
   */
  public <Y> SpecificationBuilder<T> inIfPresent(Function<Root<T>, Path<Y>> path, List<Y> values) {
    if (values != null && !values.isEmpty()) {
      specs.add((root, query, cb) -> path.apply(root).in(values));
    }
    return this;
  }

  /**
   * Añade una condición {@code IS NULL} o {@code IS NOT NULL} según {@code isNull}, si este no es
   * nulo.
   *
   * @param path función que resuelve el {@link Path} del atributo sobre el {@link Root}
   * @param isNull {@code true} para filtrar por nulos, {@code false} para no nulos; nulo para
   *     omitir el filtro
   * @return este mismo builder, para encadenar llamadas
   */
  public <Y> SpecificationBuilder<T> isNullIfPresent(
      Function<Root<T>, Path<Y>> path, Boolean isNull) {
    if (isNull != null) {
      if (isNull) {
        specs.add((root, query, cb) -> cb.isNull(path.apply(root)));
      } else {
        specs.add((root, query, cb) -> cb.isNotNull(path.apply(root)));
      }
    }

    return this;
  }
}

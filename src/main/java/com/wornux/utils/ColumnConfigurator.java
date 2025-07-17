package com.wornux.utils;

import com.vaadin.flow.component.grid.Grid;

/**
 * @author me@fredpena.dev
 * @created 14/02/2025 - 14:07
 */
@FunctionalInterface
public interface ColumnConfigurator {
    void configure(Grid<Object> grid);
}

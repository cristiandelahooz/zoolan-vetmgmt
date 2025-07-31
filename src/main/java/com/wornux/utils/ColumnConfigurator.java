package com.wornux.utils;

import com.vaadin.flow.component.grid.Grid;

@FunctionalInterface
public interface ColumnConfigurator {
    void configure(Grid<Object> grid);
}

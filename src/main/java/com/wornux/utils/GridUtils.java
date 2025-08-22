package com.wornux.utils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public final class GridUtils {

    private GridUtils() {
    }

    public static <T> Grid<T> createBasicGrid(Class<T> entityClass) {
        Grid<T> grid = new Grid<>(entityClass, false);
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setMultiSort(true, Grid.MultiSortPriority.APPEND);
        grid.setEmptyStateText("No record found.");
        return grid;
    }

    public static <T> void configureGrid(Grid<T> grid, DataProvider<T, ?> dataProvider) {
        grid.setDataProvider(dataProvider);
    }

    public static <T> void configureGrid(Grid<T> grid, CallbackDataProvider.FetchCallback<T, Void> fetchCallback) {
        grid.setItems(fetchCallback);
    }

    public static <T> void configureGrid(Grid<T> grid, Specification<T> specification,
            JpaSpecificationExecutor<T> repository) {
        grid.setItems(query -> repository.findAll(specification, PageRequest.of(query.getPage(), query.getPageSize(),
                VaadinSpringDataHelpers.toSpringDataSort(query))).stream());
    }

    public static <T> Grid.Column<T> addColumn(Grid<T> grid, Renderer<T> renderer, String header,
            String... sortProperties) {
        Grid.Column<T> tColumn = grid.addColumn(renderer).setAutoWidth(true).setHeader(header);

        var isSort = (sortProperties != null && sortProperties.length > 0);
        if (isSort) {
            tColumn.setSortable(true);
            tColumn.setSortProperty(sortProperties);
        }

        return tColumn;
    }

    public static <T, V extends Component> Grid.Column<T> addComponentColumn(Grid<T> grid,
            ValueProvider<T, V> componentProvider, String header, String... sortProperties) {
        Grid.Column<T> tColumn = grid.addColumn(new ComponentRenderer<>(componentProvider)).setAutoWidth(true)
                .setHeader(header);

        var isSort = (sortProperties != null && sortProperties.length > 0);
        if (isSort) {
            tColumn.setSortable(true);
            tColumn.setSortProperty(sortProperties);
        }

        return tColumn;
    }

    public static <T> Grid.Column<T> addColumn(Grid<T> grid, ValueProvider<T, ?> componentProvider, String header,
            String... sortProperties) {
        return grid.addColumn(componentProvider).setAutoWidth(true).setHeader(header).setSortable(
                sortProperties != null && sortProperties.length > 0).setSortProperty(sortProperties);
    }
}

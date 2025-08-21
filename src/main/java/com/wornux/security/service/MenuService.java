package com.wornux.security.service;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.wornux.data.entity.User;
import com.wornux.views.*;
import com.wornux.views.appointments.AppointmentsCalendarView;
import com.wornux.views.clients.CompanyClientView;
import com.wornux.views.clients.IndividualClientView;
import com.wornux.views.consultations.ConsultationsView;
import com.wornux.views.employees.EmployeeView;
import com.wornux.views.inventory.InventoryView;
import com.wornux.views.medicalhistory.MedicalHistoryView;
import com.wornux.views.pets.PetMergeView;
import com.wornux.views.pets.PetView;
import com.wornux.views.suppliers.SupplierView;
import com.wornux.views.transactions.InvoiceView;
import com.wornux.views.waitingroom.WaitingRoomView;
import com.wornux.views.warehouses.WarehouseView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for building dynamic menus based on user permissions.
 * Filters menu items according to role-based access control.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {
    
    private final PermissionService permissionService;
    private final SecurityContextService securityContextService;
    
    /**
     * Menu item definition containing all necessary information for a navigation item.
     */
    public record MenuItem(
        String label,
        Class<? extends Component> viewClass,
        Icon icon,
        List<MenuItem> children,
        boolean isSection
    ) {
        public MenuItem(String label, Class<? extends Component> viewClass, Icon icon) {
            this(label, viewClass, icon, new ArrayList<>(), false);
        }
        
        public MenuItem(String label, Icon icon, List<MenuItem> children) {
            this(label, null, icon, children, true);
        }
    }
    
    /**
     * Get all menu items available for the current user based on their permissions.
     * 
     * @return List of menu items the user has access to
     */
    public List<MenuItem> getMenuItemsForCurrentUser() {
        Optional<User> currentUser = securityContextService.getCurrentUser();
        
        if (currentUser.isEmpty()) {
            log.warn("No current user found when building menu");
            return List.of();
        }
        
        return getMenuItemsForUser(currentUser.get());
    }
    
    /**
     * Get all menu items available for a specific user based on their permissions.
     * 
     * @param user The user to build the menu for
     * @return List of menu items the user has access to
     */
    public List<MenuItem> getMenuItemsForUser(User user) {
        log.debug("Building menu for user: {}", user.getUsername());
        
        List<MenuItem> allMenuItems = getAllMenuItems();
        List<MenuItem> filteredItems = new ArrayList<>();
        
        for (MenuItem item : allMenuItems) {
            MenuItem filteredItem = filterMenuItem(item);
            if (filteredItem != null) {
                filteredItems.add(filteredItem);
            }
        }
        
        log.debug("Menu built with {} items for user: {}", filteredItems.size(), user.getUsername());
        return filteredItems;
    }
    
    /**
     * Filter a menu item and its children based on permissions.
     * 
     * @param item The menu item to filter
     * @return The filtered menu item, or null if the user has no access
     */
    private MenuItem filterMenuItem(MenuItem item) {
        // If it's a section with children
        if (item.isSection) {
            List<MenuItem> filteredChildren = new ArrayList<>();
            
            for (MenuItem child : item.children) {
                MenuItem filteredChild = filterMenuItem(child);
                if (filteredChild != null) {
                    filteredChildren.add(filteredChild);
                }
            }
            
            // Only include the section if it has accessible children
            if (!filteredChildren.isEmpty()) {
                return new MenuItem(item.label, item.icon, filteredChildren);
            }
            return null;
        }
        
        // If it's a regular menu item with a view
        if (item.viewClass != null && hasPermissionForView(item.viewClass)) {
            // If the item has children, filter them too
            if (!item.children.isEmpty()) {
                List<MenuItem> filteredChildren = new ArrayList<>();
                for (MenuItem child : item.children) {
                    MenuItem filteredChild = filterMenuItem(child);
                    if (filteredChild != null) {
                        filteredChildren.add(filteredChild);
                    }
                }
                return new MenuItem(item.label, item.viewClass, item.icon, filteredChildren, false);
            }
            return item;
        }
        
        return null;
    }
    
    /**
     * Check if the current user has permission to access a view.
     * 
     * @param viewClass The view class to check
     * @return true if the user has permission
     */
    private boolean hasPermissionForView(Class<? extends Component> viewClass) {
        try {
            boolean hasPermission = permissionService.hasPermission(viewClass);
            log.info("Permission check for {}: {} (User: {}, SystemRole: {})", 
                viewClass.getSimpleName(), 
                hasPermission,
                securityContextService.getCurrentUsername(),
                securityContextService.getCurrentSystemRole().orElse(null));
            return hasPermission;
        } catch (Exception e) {
            log.error("Error checking permission for view: {}", viewClass.getSimpleName(), e);
            return false; // Deny access on error
        }
    }
    
    /**
     * Get the complete menu structure with all possible items.
     * This defines the full menu hierarchy.
     * 
     * @return List of all menu items
     */
    private List<MenuItem> getAllMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        
        // Dashboard/Home - typically available to all authenticated users
        items.add(new MenuItem("Inicio", DashboardView.class, VaadinIcon.HOME_O.create()));
        
        // Appointments
        items.add(new MenuItem("Citas", AppointmentsCalendarView.class, VaadinIcon.CALENDAR.create()));
        
        // Inventory Section
        List<MenuItem> inventoryChildren = new ArrayList<>();
        inventoryChildren.add(new MenuItem("Productos", InventoryView.class, VaadinIcon.CART.create()));
        inventoryChildren.add(new MenuItem("Almacenes", WarehouseView.class, VaadinIcon.TRUCK.create()));
        items.add(new MenuItem("Inventario", VaadinIcon.ARCHIVES.create(), inventoryChildren));
        
        // Consultations
        items.add(new MenuItem("Consultas", ConsultationsView.class, VaadinIcon.STETHOSCOPE.create()));
        
        // Transactions Section
        List<MenuItem> transactionChildren = new ArrayList<>();
        transactionChildren.add(new MenuItem("Facturas", InvoiceView.class, VaadinIcon.FILE_TEXT.create()));
        items.add(new MenuItem("Transacciones", VaadinIcon.CREDIT_CARD.create(), transactionChildren));
        
        // Waiting Room
        items.add(new MenuItem("Sala de Espera", WaitingRoomView.class, VaadinIcon.OFFICE.create()));
        
        // Clients Section
        List<MenuItem> clientChildren = new ArrayList<>();
        clientChildren.add(new MenuItem("Individuales", IndividualClientView.class, VaadinIcon.USER.create()));
        clientChildren.add(new MenuItem("Empresariales", CompanyClientView.class, VaadinIcon.BUILDING.create()));
        items.add(new MenuItem("Clientes", VaadinIcon.USER.create(), clientChildren));
        
        // Pets Section
        MenuItem petsItem = new MenuItem("Mascotas", PetView.class, VaadinIcon.HEART.create());
        petsItem.children.add(new MenuItem("Fusionar", PetMergeView.class, VaadinIcon.CONNECT.create()));
        items.add(petsItem);
        
        // Employees
        items.add(new MenuItem("Empleados", EmployeeView.class, VaadinIcon.DOCTOR.create()));
        
        // Suppliers
        items.add(new MenuItem("Suplidores", SupplierView.class, VaadinIcon.TRUCK.create()));
        
        // Medical History
        items.add(new MenuItem("Historial Médico", MedicalHistoryView.class, VaadinIcon.CLIPBOARD_TEXT.create()));
        
        return items;
    }
    
    /**
     * Convert MenuItem to Vaadin SideNavItem for use in the UI.
     * 
     * @param menuItem The menu item to convert
     * @return SideNavItem for the menu
     */
    public SideNavItem toSideNavItem(MenuItem menuItem) {
        if (menuItem.isSection || menuItem.viewClass == null) {
            // Create a section header
            SideNavItem section = new SideNavItem(menuItem.label);
            section.setPrefixComponent(menuItem.icon);
            
            // Add children
            for (MenuItem child : menuItem.children) {
                section.addItem(toSideNavItem(child));
            }
            
            return section;
        } else {
            // Create a regular navigation item
            SideNavItem item = new SideNavItem(menuItem.label, menuItem.viewClass, menuItem.icon);
            
            // Add children if any
            for (MenuItem child : menuItem.children) {
                item.addItem(toSideNavItem(child));
            }
            
            return item;
        }
    }
}
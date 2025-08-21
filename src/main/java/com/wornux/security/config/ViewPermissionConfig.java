package com.wornux.security.config;

import com.wornux.data.enums.EmployeeRole;
import com.wornux.data.enums.SystemRole;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration class that defines permission mappings for all views in the application.
 * This centralizes the permission logic and makes it easy to understand and modify access control.
 */
public class ViewPermissionConfig {
    
    /**
     * Permission configuration for a specific view.
     */
    @Getter
    @RequiredArgsConstructor
    public static class ViewPermission {
        private final String viewName;
        private final String route;
        private final List<SystemRole> allowedSystemRoles;
        private final List<EmployeeRole> allowedEmployeeRoles;
        private final String description;
    }
    
    /**
     * Get all view permission configurations.
     * This defines the complete access control matrix for the application.
     */
    public static List<ViewPermission> getAllViewPermissions() {
        return Arrays.asList(
            // Dashboard - accessible to all authenticated users
            new ViewPermission(
                "DashboardView",
                "",
                Arrays.asList(SystemRole.values()), // All system roles
                Arrays.asList(EmployeeRole.values()), // All employee roles
                "Dashboard principal accesible para todos los usuarios autenticados"
            ),
            
            // Employee Management - restricted to managers
            new ViewPermission(
                "EmployeeView",
                "empleados",
                Arrays.asList(SystemRole.SYSTEM_ADMIN, SystemRole.MANAGER),
                Arrays.asList(EmployeeRole.CLINIC_MANAGER),
                "Gestión de empleados - solo para administradores y gerentes"
            ),
            
            // Inventory Management
            new ViewPermission(
                "InventoryView",
                "inventario",
                Arrays.asList(SystemRole.MANAGER, SystemRole.USER),
                Arrays.asList(EmployeeRole.CLINIC_MANAGER, EmployeeRole.ADMINISTRATIVE),
                "Gestión de inventario - para gerentes y administrativos"
            ),
            
            // Warehouse Management
            new ViewPermission(
                "WarehouseView",
                "warehouses",
                Arrays.asList(SystemRole.MANAGER, SystemRole.USER),
                Arrays.asList(EmployeeRole.CLINIC_MANAGER, EmployeeRole.ADMINISTRATIVE),
                "Gestión de almacenes - para gerentes y administrativos"
            ),
            
            // Consultations - for veterinary staff
            new ViewPermission(
                "ConsultationsView",
                "consultations",
                Arrays.asList(SystemRole.USER),
                Arrays.asList(EmployeeRole.VETERINARIAN, EmployeeRole.KENNEL_ASSISTANT),
                "Consultas veterinarias - para veterinarios y asistentes"
            ),
            
            // Appointments - for reception and veterinary staff
            new ViewPermission(
                "AppointmentsCalendarView",
                "appointments",
                Arrays.asList(SystemRole.USER),
                Arrays.asList(EmployeeRole.RECEPTIONIST, EmployeeRole.VETERINARIAN, EmployeeRole.CLINIC_MANAGER),
                "Calendario de citas - para recepción y veterinarios"
            ),
            
            // Waiting Room - for reception and veterinary staff
            new ViewPermission(
                "WaitingRoomView",
                "sala-espera",
                Arrays.asList(SystemRole.USER),
                Arrays.asList(EmployeeRole.RECEPTIONIST, EmployeeRole.VETERINARIAN, EmployeeRole.KENNEL_ASSISTANT),
                "Sala de espera - para recepción y personal veterinario"
            ),
            
            // Invoices - for administrative staff
            new ViewPermission(
                "InvoiceView",
                "invoices",
                Arrays.asList(SystemRole.MANAGER, SystemRole.USER),
                Arrays.asList(EmployeeRole.ADMINISTRATIVE, EmployeeRole.CLINIC_MANAGER, EmployeeRole.RECEPTIONIST),
                "Facturas - para personal administrativo"
            ),
            
            // Pet Management - for most staff
            new ViewPermission(
                "PetView",
                "mascotas",
                Arrays.asList(SystemRole.USER),
                Arrays.asList(
                    EmployeeRole.VETERINARIAN,
                    EmployeeRole.RECEPTIONIST,
                    EmployeeRole.KENNEL_ASSISTANT,
                    EmployeeRole.CLINIC_MANAGER
                ),
                "Gestión de mascotas - para personal veterinario y recepción"
            ),
            
            // Pet Merge - administrative function
            new ViewPermission(
                "PetMergeView",
                "mascotas/fusionar",
                Arrays.asList(SystemRole.MANAGER, SystemRole.USER),
                Arrays.asList(EmployeeRole.CLINIC_MANAGER, EmployeeRole.ADMINISTRATIVE),
                "Fusión de mascotas duplicadas - función administrativa"
            ),
            
            // Individual Clients - for reception and administrative
            new ViewPermission(
                "IndividualClientView",
                "individual-clients",
                Arrays.asList(SystemRole.USER),
                Arrays.asList(EmployeeRole.RECEPTIONIST, EmployeeRole.ADMINISTRATIVE, EmployeeRole.CLINIC_MANAGER),
                "Clientes individuales - para recepción y administrativos"
            ),
            
            // Company Clients - for administrative
            new ViewPermission(
                "CompanyClientView",
                "business-clients",
                Arrays.asList(SystemRole.MANAGER, SystemRole.USER),
                Arrays.asList(EmployeeRole.ADMINISTRATIVE, EmployeeRole.CLINIC_MANAGER),
                "Clientes empresariales - para administrativos"
            ),
            
            // Suppliers - for managers
            new ViewPermission(
                "SupplierView",
                "proveedores",
                Arrays.asList(SystemRole.MANAGER),
                Arrays.asList(EmployeeRole.CLINIC_MANAGER, EmployeeRole.ADMINISTRATIVE),
                "Proveedores - para gerentes y administrativos"
            ),
            
            // Medical History - for veterinary staff
            new ViewPermission(
                "MedicalHistoryView",
                "historial-medico",
                Arrays.asList(SystemRole.USER),
                Arrays.asList(EmployeeRole.VETERINARIAN, EmployeeRole.KENNEL_ASSISTANT, EmployeeRole.LAB_TECHNICIAN),
                "Historial médico - para personal veterinario y laboratorio"
            )
        );
    }
    
    /**
     * Get permission configuration for a specific view by name.
     * 
     * @param viewName The name of the view class
     * @return ViewPermission or null if not found
     */
    public static ViewPermission getPermissionForView(String viewName) {
        return getAllViewPermissions().stream()
            .filter(vp -> vp.getViewName().equals(viewName))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Get permission configuration for a specific route.
     * 
     * @param route The route path
     * @return ViewPermission or null if not found
     */
    public static ViewPermission getPermissionForRoute(String route) {
        return getAllViewPermissions().stream()
            .filter(vp -> vp.getRoute().equals(route))
            .findFirst()
            .orElse(null);
    }
}
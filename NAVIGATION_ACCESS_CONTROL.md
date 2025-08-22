# Navigation Access Control Rules - Zoolan Vetmgmt

This document outlines the navigation access control rules and role-based permissions for the Zoolan Veterinary Management System.

## System Overview

The system uses a dual-role hierarchy:
- **System Roles**: Base authorization levels (SYSTEM_ADMIN, MANAGER, USER, GUEST)
- **Employee Roles**: Business-specific roles mapped to System Roles

## Role Hierarchy

### System Roles
- **SYSTEM_ADMIN**: Full system access, bypasses all restrictions
- **MANAGER**: Business management access with elevated permissions  
- **USER**: Basic system access for day-to-day operations
- **GUEST**: Limited access (currently not implemented in views)

### Employee Role Mapping to System Roles
| Employee Role | System Role | Business Function |
|--------------|-------------|-------------------|
| CLINIC_MANAGER | MANAGER | General clinic management |
| RECEPTIONIST | USER | Appointment management, client services |
| ADMINISTRATIVE | USER | Administrative tasks, accounting |
| VETERINARIAN | USER | Medical consultations, procedures |
| GROOMER | USER | Pet grooming services |
| KENNEL_ASSISTANT | USER | Animal care assistance |
| LAB_TECHNICIAN | USER | Laboratory analysis |

## Security Implementation

### Framework
- **Spring Security 6.5.0** with method-level security
- **Vaadin 24.8.3** with VaadinSecurityConfigurer
- **Jakarta Security Annotations** (@RolesAllowed, @PermitAll, @AnonymousAllowed)

### Access Control Method
The system relies primarily on **view-level security** using `@RolesAllowed` annotations on Vaadin view classes. The SecurityConfig provides basic authentication framework without specific route restrictions, allowing fine-grained control at the view level.

## Route Access Matrix

### Administrative Routes
| Route | View Class | Allowed Roles | Business Purpose |
|-------|------------|---------------|------------------|
| `/empleados` | EmployeeView | SYSTEM_ADMIN, MANAGER | Employee management |

### Clinical Operations
| Route | View Class | Allowed Roles | Business Purpose |
|-------|------------|---------------|------------------|
| `/consultations` | ConsultationsView | SYSTEM_ADMIN, MANAGER, USER | Veterinary consultations |
| `/mascotas` | PetView | SYSTEM_ADMIN, MANAGER, USER | Pet management |
| `/mascotas/fusionar` | PetMergeView | SYSTEM_ADMIN, MANAGER | Merge duplicate pets |
| `/historial-medico` | MedicalHistoryView | SYSTEM_ADMIN, MANAGER, USER | Medical history records |
| `/sala-espera` | WaitingRoomView | SYSTEM_ADMIN, MANAGER, USER | Waiting room management |

### Front Office Operations
| Route | View Class | Allowed Roles | Business Purpose |
|-------|------------|---------------|------------------|
| `/appointments` | AppointmentsCalendarView | SYSTEM_ADMIN, MANAGER, USER | Appointment scheduling |
| `/individual-clients` | IndividualClientView | SYSTEM_ADMIN, MANAGER, USER | Individual client management |
| `/business-clients` | CompanyClientView | SYSTEM_ADMIN, MANAGER, USER | Business client management |

### Financial Operations
| Route | View Class | Allowed Roles | Business Purpose |
|-------|------------|---------------|------------------|
| `/invoices` | InvoiceView | SYSTEM_ADMIN, MANAGER, USER | Invoice management |
| `/invoicetmp` | InvoiceViewTmp | SYSTEM_ADMIN, MANAGER, USER | Invoice template/testing |

### Inventory & Supply Management
| Route | View Class | Allowed Roles | Business Purpose |
|-------|------------|---------------|------------------|
| `/inventario` | InventoryView | SYSTEM_ADMIN, MANAGER, USER | Inventory management |
| `/warehouses` | WarehouseView | SYSTEM_ADMIN, MANAGER, USER | Warehouse management |
| `/proveedores` | SupplierView | SYSTEM_ADMIN, MANAGER, USER | Supplier management |

### General Access
| Route | View Class | Allowed Roles | Business Purpose |
|-------|------------|---------------|------------------|
| `/` | DashboardView | SYSTEM_ADMIN, MANAGER, USER | Main dashboard |

## Role-Based Business Logic

### SYSTEM_ADMIN (Super Admin)
- **Access**: Unrestricted access to all routes and functionality
- **Purpose**: System administration, configuration, and oversight
- **Special Privileges**: Can manage employees, access sensitive operations

### MANAGER (Clinic Manager)
- **Access**: All operational routes except employee management (requires SYSTEM_ADMIN)
- **Purpose**: Day-to-day clinic management and oversight
- **Focus Areas**: All business operations, reporting, administrative tasks

### USER (All Clinical Staff)
- **Access**: All day-to-day operational routes
- **Purpose**: Patient care, client services, inventory management
- **Role-Specific Focus**:
  - **VETERINARIAN**: Consultations, medical history, pet management
  - **RECEPTIONIST**: Appointments, client management, waiting room
  - **ADMINISTRATIVE**: Financial operations, suppliers, inventory
  - **Clinical Support** (KENNEL_ASSISTANT, LAB_TECHNICIAN, GROOMER): Patient care operations

## Security Configuration Details

### SecurityConfig.java
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.with(VaadinSecurityConfigurer.vaadin(), configurer -> {
            configurer.loginView(LoginView.class);
        });
        return http.build();
    }
}
```

### View-Level Security Example
```java
@Route("empleados")
@PageTitle("Empleados")
@RolesAllowed({"ROLE_SYSTEM_ADMIN", "ROLE_MANAGER"})
public class EmployeeView extends Div {
    // Employee management functionality
}
```

## Access Control Principles

1. **Principle of Least Privilege**: Users have minimum access needed for their role
2. **Business Logic Alignment**: Permissions align with veterinary clinic workflows
3. **Role Separation**: Clear distinction between administrative and operational roles
4. **Scalable Security**: Easy to add new roles and adjust permissions as needed

## Implementation Notes

- All routes require authentication (no anonymous access except login)
- Role names use "ROLE_" prefix as per Spring Security convention
- Vaadin handles view-level security automatically based on annotations
- No URL-based restrictions in SecurityConfig - all control at view level
- SYSTEM_ADMIN role provides complete system bypass capability

## Future Considerations

- GUEST role implementation for limited read-only access
- API endpoint security for future REST services
- Feature-level permissions within views based on employee roles
- Audit logging for security-sensitive operations
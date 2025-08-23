# Role-Based Access Control Table

## Quick Reference - Route Access by Role

| Route                 | View                     | SYSTEM_ADMIN | MANAGER | USER | Business Context                              |
|-----------------------|--------------------------|--------------|---------|------|-----------------------------------------------|
| `/`                   | DashboardView            | ✅            | ✅       | ✅    | Main dashboard - system overview              |
| `/empleados`          | EmployeeView             | ✅            | ✅       | ❌    | Employee management - sensitive HR data       |
| `/consultations`      | ConsultationsView        | ✅            | ✅       | ✅    | Veterinary consultations - core clinical work |
| `/mascotas`           | PetView                  | ✅            | ✅       | ✅    | Pet management - central patient records      |
| `/mascotas/fusionar`  | PetMergeView             | ✅            | ✅       | ❌    | Pet data merge - data integrity operation     |
| `/historial-medico`   | MedicalHistoryView       | ✅            | ✅       | ✅    | Medical history - clinical documentation      |
| `/appointments`       | AppointmentsCalendarView | ✅            | ✅       | ✅    | Appointment scheduling - front office         |
| `/sala-espera`        | WaitingRoomView          | ✅            | ✅       | ✅    | Waiting room management - patient flow        |
| `/individual-clients` | IndividualClientView     | ✅            | ✅       | ✅    | Individual client management                  |
| `/business-clients`   | CompanyClientView        | ✅            | ✅       | ✅    | Business client management                    |
| `/invoices`           | InvoiceView              | ✅            | ✅       | ✅    | Invoice management - financial operations     |
| `/invoicetmp`         | InvoiceViewTmp           | ✅            | ✅       | ✅    | Invoice templates - testing/development       |
| `/inventario`         | InventoryView            | ✅            | ✅       | ✅    | Inventory management - stock control          |
| `/warehouses`         | WarehouseView            | ✅            | ✅       | ✅    | Warehouse management - storage locations      |
| `/proveedores`        | SupplierView             | ✅            | ✅       | ✅    | Supplier management - vendor relations        |

## Employee Role Access Matrix

### By Employee Role and Business Function

| Employee Role        | System Role | Dashboard | Employees | Clinical | Front Office | Financial | Inventory |
|----------------------|-------------|-----------|-----------|----------|--------------|-----------|-----------|
| **CLINIC_MANAGER**   | MANAGER     | ✅         | ✅         | ✅        | ✅            | ✅         | ✅         |
| **RECEPTIONIST**     | USER        | ✅         | ❌         | Limited¹ | ✅            | Limited²  | Limited³  |
| **ADMINISTRATIVE**   | USER        | ✅         | ❌         | Limited¹ | ✅            | ✅         | ✅         |
| **VETERINARIAN**     | USER        | ✅         | ❌         | ✅        | Limited⁴     | Limited²  | Limited³  |
| **GROOMER**          | USER        | ✅         | ❌         | Limited⁵ | Limited⁴     | Limited²  | Limited³  |
| **KENNEL_ASSISTANT** | USER        | ✅         | ❌         | Limited⁶ | Limited⁴     | ❌         | Limited³  |
| **LAB_TECHNICIAN**   | USER        | ✅         | ❌         | Limited⁷ | Limited⁴     | ❌         | Limited³  |

### Access Level Definitions:

- ✅ **Full Access**: Complete functionality within the module
- ❌ **No Access**: Cannot access the route/module
- **Limited**: Can access but expected to focus on role-specific functions

### Limited Access Notes:

1. **Clinical Limited**: Focus on pet records and medical history relevant to their role
2. **Financial Limited**: May view invoices but typically no creation/modification
3. **Inventory Limited**: May view stock levels but no management functions
4. **Front Office Limited**: May view appointments but primary scheduling done by reception
5. **Grooming Limited**: Focus on grooming-related pets and services
6. **Kennel Limited**: Focus on hospitalized animals and care tasks
7. **Lab Limited**: Focus on lab results and testing-related medical records

## Security Enforcement

### Spring Security Annotations Used:

```java
@RolesAllowed({"ROLE_SYSTEM_ADMIN", "ROLE_MANAGER", "ROLE_USER"})  // Multiple roles
@RolesAllowed({"ROLE_SYSTEM_ADMIN", "ROLE_MANAGER"})              // Admin/Manager only
@PermitAll                                                         // Open access (not used)
```

### Role Hierarchy in Security Context:

1. **ROLE_SYSTEM_ADMIN** - Highest privilege, unrestricted access
2. **ROLE_MANAGER** - Business management, almost all operations
3. **ROLE_USER** - Standard operations, role-specific focus areas
4. **ROLE_GUEST** - Not currently implemented

## Business Rationale

### SYSTEM_ADMIN Exclusive Access:

- **Employee Management**: HR data, salary information, role assignments
- **Pet Merge Operations**: Data integrity and deduplication

### MANAGER Required Access:

- All operational oversight
- Business reporting and analytics
- Administrative approvals

### USER Level Access Principles:

- **Clinical Staff**: Focus on patient care and medical operations
- **Administrative Staff**: Focus on business operations and support
- **Reception Staff**: Focus on customer offering and scheduling
- **Support Staff**: Focus on their specialized functions

## Implementation Status

✅ **Completed:**

- View-level security annotations
- Role-based access control framework
- Spring Security integration
- Vaadin security configuration

🔄 **Future Enhancements:**

- Feature-level permissions within views
- Dynamic menu generation based on roles
- Audit logging for access attempts
- API endpoint security
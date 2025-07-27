export const ROUTES = {
  // Main application routes
  HOME: '/',
  APPOINTMENTS: '/appointments',

  // Client routes
  BUSINESS_CLIENTS: '/business-clients',
  INDIVIDUAL_CLIENTS: '/individual-clients',

  // Other feature routes
  CLIENT_EDIT: '/clients/edit',
  CLIENT_VIEW: '/clients/view',
  DASHBOARD: '/dashboard',
  SETTINGS: '/settings',

  // General routes
  LOGIN: '/login',

  // Pet routes
  LOGOUT: '/logout',
  PETS: '/pets',
  PET_NEW: '/pets/new',
  PET_EDIT: '/pets/edit',
  PET_MERGE: '/pets/merge',
  PET_VIEW: '/pets/view',

  //Employee routes
  EMPLOYEES: '/employees',
  EMPLOYEE_EDIT: '/employees/edit',
  EMPLOYEE_NEW: '/employees/form',

  //waiting room routes
  WAITING_ROOM: '/waitingroom',
  WAITING_ROOM_NEW: '/waitingroom/new',
  WAITING_ROOM_LIVE: '/waitingroom/live',

  //Consultations routes
  CONSULTATIONS: '/consultations',
  CONSULTATION_NEW: '/consultations/new',

  //product routes
  PRODUCTS: '/products',
  PRODUCT_EDIT: '/products/edit',
  PRODUCT_NEW: '/products/new',

  //supplier routes
  SUPPLIERS: '/suppliers',
  SUPPLIER_EDIT: '/suppliers/edit',
  SUPPLIER_NEW: '/suppliers/new',

  //inventory routes
  INVENTORY: '/inventory',
} as const

export const ROUTES = {
  // Main application routes
  HOME: '/',
  APPOINTMENTS: '/appointments',

  // Client routes
  CLIENTS: '/clients',
  CLIENT_NEW: '/clients/new',

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
  PET_VIEW: '/pets/view',
  EMPLOYEES: '/employees',
  EMPLOYEE_EDIT: '/employees/edit',

  EMPLOYEE_NEW: '/employees/new',
} as const

// Type for route values
export type Route = (typeof ROUTES)[keyof typeof ROUTES]

// Helper function for dynamic routes
export const createClientRoute = (id: string) => `${ROUTES.CLIENTS}/${id}`
export const createEditClientRoute = (id: string) => `${ROUTES.CLIENT_EDIT}/${id}`

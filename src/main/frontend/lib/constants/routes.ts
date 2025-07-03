export const ROUTES = {
  // Client routes
  CLIENTS: '/clients',
  CLIENT_NEW: '/clients/new',
  CLIENT_EDIT: '/clients/edit',
  CLIENT_VIEW: '/clients/view',

  // Other feature routes
  DASHBOARD: '/dashboard',
  SETTINGS: '/settings',
  LOGIN: '/login',
  LOGOUT: '/logout',

  // General routes
  HOME: '/',

  // Pet routes
  PETS: '/pets',
  PET_NEW: '/pets/new',
  PET_EDIT: '/pets/edit',
  PET_MERGE: '/pets/merge',
  PET_VIEW: '/pets/view',

  //Employee routes
  EMPLOYEES: '/employees',
  EMPLOYEE_EDIT: '/employees/edit',
  EMPLOYEE_NEW: '/employees/new',

  //waiting room routes
  WAITING_ROOM: '/waitingroom',
  WAITING_ROOM_NEW: '/waitingroom/new',
  WAITING_ROOM_LIVE: '/waitingroom/live',
} as const

// Type for route values
export type Route = (typeof ROUTES)[keyof typeof ROUTES]

// Helper function for dynamic routes
export const createClientRoute = (id: string) => `${ROUTES.CLIENTS}/${id}`
export const createEditClientRoute = (id: string) => `${ROUTES.CLIENT_EDIT}/${id}`

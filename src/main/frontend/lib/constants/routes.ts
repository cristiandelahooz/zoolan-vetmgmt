export const ROUTES = {
  // Client routes
  CLIENTS: '/clients',
  CLIENT_REGISTER: '/clients/register',
  CLIENT_EDIT: '/clients/edit',
  CLIENT_VIEW: '/clients/view',

  // Other feature routes
  DASHBOARD: '/dashboard',
  SETTINGS: '/settings',
  LOGIN: '/login',
  LOGOUT: '/logout',
} as const

// Type for route values
export type Route = (typeof ROUTES)[keyof typeof ROUTES]

// Helper function for dynamic routes
export const createClientRoute = (id: string) => `${ROUTES.CLIENTS}/${id}`
export const createEditClientRoute = (id: string) => `${ROUTES.CLIENT_EDIT}/${id}`

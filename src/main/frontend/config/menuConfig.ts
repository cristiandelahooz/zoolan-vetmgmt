import { ROUTES } from '@/lib/constants/routes'
import petsIcon from '@/public/icons/pets.svg'
import type { IconProps } from '@vaadin/react-components'

export interface MenuItemConfig {
  path: string | undefined
  title: string
  icon?: IconProps['icon']
  src?: string
  children?: MenuItemConfig[]
}

export const menuConfig: MenuItemConfig[] = [
  {
    path: ROUTES.HOME,
    title: 'Inicio',
    icon: 'vaadin:home-o',
  },
  {
    path: ROUTES.APPOINTMENTS,
    title: 'Citas',
    icon: 'vaadin:calendar',
  },
  {
    path: undefined,
    title: 'Inventario',
    icon: 'vaadin:archives',
    children: [
      {
        path: ROUTES.INVENTORY,
        title: 'Productos',
        icon: 'vaadin:cart',
      },
      {
        path: ROUTES.WAREHOUSE,
        title: 'Almacenes',
        icon: 'vaadin:truck',
      }
    ],
  },
  {
    path: ROUTES.CONSULTATIONS,
    title: 'Consultas',
    icon: 'vaadin:stethoscope',
  },
  {
    path: undefined,
    title: 'Transacciones',
    icon: 'vaadin:credit-card',
    children: [
      {
        path: '/invoices',
        title: 'Facturas',
        icon: 'lumo:align-left',
      },
    ],
  },
  {
    path: ROUTES.WAITING_ROOM,
    title: 'Sala de Espera',
    icon: 'vaadin:office',
  },
  {
    path: undefined,
    title: 'Clientes',
    icon: 'lumo:user',
    children: [
      {
        path: ROUTES.INDIVIDUAL_CLIENTS,
        title: 'Individuales',
        icon: 'lumo:align-left',
      },
      {
        path: ROUTES.BUSINESS_CLIENTS,
        title: 'Empresariales',
        icon: 'lumo:align-left',
      },
    ],
  },
  {
    path: ROUTES.PETS,
    title: 'Mascotas',
    src: petsIcon,
  },
  {
    path: ROUTES.EMPLOYEES,
    title: 'Empleados',
    icon: 'vaadin:doctor',
  },
  {
    path: ROUTES.SUPPLIERS,
    title: 'Suplidores',
    icon: 'vaadin:truck',
  },
]

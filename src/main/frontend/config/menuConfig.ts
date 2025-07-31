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
    path: undefined,
    title: 'Sala de Espera',
    icon: 'lumo:user',
    children: [
      {
        path: ROUTES.WAITING_ROOM,
        title: 'Listar',
        icon: 'lumo:align-left',
      },
      {
        path: ROUTES.WAITING_ROOM_NEW,
        title: 'Registrar',
        icon: 'lumo:edit',
      },
      {
        path: ROUTES.WAITING_ROOM_LIVE,
        title: 'Sala de Espera',
        icon: 'lumo:align-left',
      },
    ],
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
    path: undefined,
    title: 'Mascotas',
    src: petsIcon,
    children: [
      {
        path: ROUTES.PETS,
        title: 'Listar',
        icon: 'lumo:align-left',
      },
      {
        path: ROUTES.PET_NEW,
        title: 'Registrar',
        icon: 'lumo:edit',
      },
      {
        path: ROUTES.PET_MERGE,
        title: 'Fusionar',
        icon: 'lumo:edit',
      },
    ],
  },
  {
    path: ROUTES.EMPLOYEES,
    title: 'Empleados',
    icon: 'vaadin:doctor',
  },
  {
    path: undefined,
    title: 'Suplidores',
    icon: 'vaadin:truck',
    children: [
      {
        path: ROUTES.SUPPLIERS,
        title: 'Listar',
        icon: 'lumo:align-left',
      },
      {
        path: ROUTES.SUPPLIER_NEW,
        title: 'Registrar',
        icon: 'lumo:edit',
      },
    ],
  },
]

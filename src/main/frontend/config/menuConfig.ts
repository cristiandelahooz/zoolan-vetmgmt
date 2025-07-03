import type { IconProps } from '@vaadin/react-components'
import { ROUTES } from 'Frontend/lib/constants/routes'
import petsIcon from 'Frontend/public/icons/pets.svg'

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
    path: undefined,
    title: 'Clientes',
    icon: 'lumo:user',
    children: [
      { path: ROUTES.CLIENTS, title: 'Listar', icon: 'lumo:align-left' },
      { path: ROUTES.CLIENT_NEW, title: 'Registrar', icon: 'lumo:edit' },
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
    path: undefined,
    title: 'Empleados',
    icon: 'vaadin:doctor',
    children: [
      {
        path: ROUTES.EMPLOYEES,
        title: 'Listar',
        icon: 'lumo:align-left',
      },
      {
        path: ROUTES.EMPLOYEE_NEW,
        title: 'Registrar',
        icon: 'lumo:edit',
      },
    ],
  },
  {
    path: undefined,
    title: 'Sala de Espera',
    icon: 'lumo:user',
    children: [
      { path: ROUTES.WAITING_ROOM, title: 'Listar', icon: 'lumo:align-left' },
      { path: ROUTES.WAITING_ROOM_NEW, title: 'Registrar', icon: 'lumo:edit' },
      { path: ROUTES.WAITING_ROOM_LIVE, title: 'Sala de Espera', icon: 'lumo:align-left' },
    ],
  },
]

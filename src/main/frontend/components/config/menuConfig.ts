import type { IconProps } from '@vaadin/react-components'
import { ROUTES } from 'Frontend/lib/constants/routes'

export interface MenuItemConfig {
  path: string | undefined
  title: string
  icon?: IconProps['icon']
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
      { path: ROUTES.CLIENT_REGISTER, title: 'Registrar', icon: 'lumo:edit' },
    ],
  },
]

import type { IconProps } from '@vaadin/react-components'
import { ROUTES } from 'Frontend/lib/constants/routes'
import petsIcon from 'Frontend/lib/icons/pets-svgrepo-com.svg'

export interface MenuItemConfig {
  path: string | undefined
  title: string
  icon?: IconProps['icon']
  src?: string | undefined
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
        path: ROUTES.PET_REGISTER,
        title: 'Registrar',
        icon: 'lumo:edit',
      },
    ],
  },
]

import {ROUTES} from '@/lib/constants/routes'
import petsIcon from '@/public/icons/pets.svg'
import type {IconProps} from '@vaadin/react-components'

export interface MenuItemConfig {
    path: string | undefined
    title: string
    icon?: IconProps['icon']
    src?: string
    children?: MenuItemConfig[]
}

export const menuConfig: MenuItemConfig[] = [{
    path: ROUTES.HOME, title: 'Inicio', icon: 'vaadin:home-o',
}, {
    path: ROUTES.APPOINTMENTS, title: 'Citas', icon: 'vaadin:calendar',
}, {
    path: undefined,
    title: 'Consultas',
    icon: 'vaadin:stethoscope',
    children: [{
        path: ROUTES.CONSULTATIONS,
        title: 'Listar',
        icon: 'lumo:align-left'
    }, {path: ROUTES.CONSULTATION_NEW, title: 'Registrar', icon: 'lumo:edit'},]
}, {
    path: undefined, title: 'Clientes', icon: 'lumo:user', children: [{
        path: ROUTES.CLIENTS, title: 'Listar', icon: 'lumo:align-left'
    }, {path: ROUTES.CLIENT_NEW, title: 'Registrar', icon: 'lumo:edit'},],
}, {
    path: undefined, title: 'Mascotas', src: petsIcon, children: [{
        path: ROUTES.PETS, title: 'Listar', icon: 'lumo:align-left',
    }, {
        path: ROUTES.PET_NEW, title: 'Registrar', icon: 'lumo:edit',
    }, {
        path: ROUTES.PET_MERGE, title: 'Fusionar', icon: 'lumo:edit',
    },],
}, {
    path: undefined, title: 'Empleados', icon: 'vaadin:doctor', children: [{
        path: ROUTES.EMPLOYEES, title: 'Listar', icon: 'lumo:align-left',
    }, {
        path: ROUTES.EMPLOYEE_NEW, title: 'Registrar', icon: 'lumo:edit',
    },],
},]

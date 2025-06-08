import { IconProps } from '@vaadin/react-components';

export interface MenuItemConfig {
    path: string | undefined;
    title: string;
    icon?: IconProps['icon'];
    children?: MenuItemConfig[];
}

export const menuConfig: MenuItemConfig[] = [
    {
        path: undefined,
        title: 'Clientes',
        icon: 'vaadin:group',
        children: [
            { path: '/clients/', title: 'List', icon: 'vaadin:users' },
            { path: '/clients/register', title: 'Register', icon: 'vaadin:user' },
        ],
    },
];
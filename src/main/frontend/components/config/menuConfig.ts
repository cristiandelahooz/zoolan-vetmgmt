import type { IconProps } from "@vaadin/react-components";

export interface MenuItemConfig {
	path: string | undefined;
	title: string;
	icon?: IconProps["icon"];
	children?: MenuItemConfig[];
}

export const menuConfig: MenuItemConfig[] = [
	{
		path: undefined,
		title: "Clientes",
		icon: "vaadin:group",
		children: [
			{ path: "/clients/", title: "Listar", icon: "vaadin:users" },
			{ path: "/clients/new", title: "Registrar", icon: "vaadin:user" },
		],
	},
];

import type { ViewConfig } from "@vaadin/hilla-file-router/types.js";
import { AutoGrid } from "@vaadin/hilla-react-crud";
import ClientModel from "Frontend/generated/com/zoolandia/app/features/client/domain/ClientModel";
import { ClientServiceImpl } from "Frontend/generated/endpoints";

export const config: ViewConfig = {
	title: "Clients",
	menu: {
		icon: "vaadin:users",
		order: 0,
		title: "Clientes",
	},
};

export default function ClientsView() {
	return (
		<main className="w-full h-full flex flex-col box-border gap-s p-m">
			<AutoGrid service={ClientServiceImpl} model={ClientModel} />
		</main>
	);
}

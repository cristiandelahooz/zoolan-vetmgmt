import type { ViewConfig } from "@vaadin/hilla-file-router/types.js";
import { AutoForm } from "@vaadin/hilla-react-crud";
import { PasswordField } from "@vaadin/react-components";
import ClientCreateDTOModel from "Frontend/generated/com/zoolandia/app/features/client/service/dto/ClientCreateDTOModel";
import { ClientServiceImpl } from "Frontend/generated/endpoints";
import type React from "react";

export const config: ViewConfig = {
	title: "Registrar Cliente",
};

export default function Register() {
	return (
		<>
			<main className="w-full h-full flex flex-col box-border gap-s p-m">
				<AutoForm
					service={ClientServiceImpl}
					model={ClientCreateDTOModel}
					fieldOptions={{
						password: {
							renderer: ({ field }) => <PasswordField {...field} />,
						},
					}}
				/>
			</main>
		</>
	);
}

import type { ViewConfig } from "@vaadin/hilla-file-router/types.js";
import _ClientEntryForm from "Frontend/views/clients/_ClientEntryForm";

export const config: ViewConfig = {
	title: "Registrar Cliente",
};

export default function Register() {
	return (
		<>
			<main className="w-full h-full flex flex-col box-border gap-s p-m">
				<_ClientEntryForm />
			</main>
		</>
	);
}

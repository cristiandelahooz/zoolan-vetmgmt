import type { ViewConfig } from "@vaadin/hilla-file-router/types.js";
import { AutoForm } from "@vaadin/hilla-react-crud";
import PetCreateDTOModel from "Frontend/generated/com/zoolandia/app/features/pet/service/dto/PetCreateDTOModel";
import { PetServiceImpl } from "Frontend/generated/endpoints";

export const config: ViewConfig = {
    menu: { title: 'Registrar' },
    title: 'Registrar Mascota',
};

//agregar el success
export default function PetEntryFormView() {
    return (
        <main className="w-full h-full flex flex-col box-border gap-s p-m">
            <AutoForm service={PetServiceImpl} model={PetCreateDTOModel} />
        </main>
    );
}

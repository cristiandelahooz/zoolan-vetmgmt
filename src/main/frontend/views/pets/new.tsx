import type { ViewConfig } from "@vaadin/hilla-file-router/types.js";
import { AutoForm } from "@vaadin/hilla-react-crud";
import PetCreateDTOModel from "Frontend/generated/com/zoolandia/app/features/pet/service/dto/PetCreateDTOModel";
import { PetServiceImpl } from "Frontend/generated/endpoints";
import { useNavigate } from "react-router-dom";
import {Notification, TextField} from "@vaadin/react-components";
import { useState } from 'react';
import { SelectOwnerDialog } from './SelectOwnerDialog';

export const config: ViewConfig = {
    menu: { title: "Registrar" },
    title: "Registrar Mascota",
};

export default function PetEntryFormView() {
    const navigate = useNavigate();
    const [ownerId, setOwnerId] = useState<number | null>(null);
    const [ownerName, setOwnerName] = useState('');
    const [dialogOpen, setDialogOpen] = useState(false);

    function handleOnSubmitSuccess() {
        Notification.show('Mascota registrada exitosamente');
        navigate('/pet/view');
    }

    return (
        <main className="w-full h-full flex flex-col box-border gap-s p-m">
            <AutoForm
                service={PetServiceImpl}
                model={PetCreateDTOModel}
                onSubmitSuccess={handleOnSubmitSuccess}
                fieldOptions={{
                    name: { label: 'Nombre' },
                    type: { label: 'Tipo' },
                    breed: { label: 'Raza' },
                    gender: { label: 'Género' },
                    birthDate: { label: 'Fecha de Nacimiento' },
                    ownerId: {
                        label: 'ID del Dueño',
                        renderer: () => (
                            <>
                                <TextField
                                    label="Dueño"
                                    value={ownerName}
                                    readonly
                                    onClick={() => setDialogOpen(true)}
                                />
                            </>
                        )
                    }
                }}
            />
            <SelectOwnerDialog
                open={dialogOpen}
                onClose={() => setDialogOpen(false)}
                onSelect={(client) => {
                    setOwnerId(client.id);
                    setOwnerName(`${client.firstName} ${client.lastName}`);
                }}
            />
        </main>
    );
}
import { Dialog, Button, TextField } from '@vaadin/react-components';
import { AutoGrid } from '@vaadin/hilla-react-crud';
import { useState } from 'react';
import ClientModel from 'Frontend/generated/com/zoolandia/app/features/client/domain/ClientModel';
import { ClientServiceImpl } from 'Frontend/generated/endpoints';

export function SelectOwnerDialog({
                                      open,
                                      onClose,
                                      onSelect
                                  }: {
    open: boolean,
    onClose: () => void,
    onSelect: (client: any) => void
}) {
    const [filter, setFilter] = useState('');

    return (
        <Dialog opened={open} onOpenedChanged={({ detail }) => !detail.value && onClose()}>
            <div style={{ padding: '1rem', width: '700px' }}>
                <h3>Seleccionar Dueño</h3>
                <TextField
                    label="Buscar por nombre o cédula"
                    value={filter}
                    onValueChanged={({ detail }) => setFilter(detail.value)}
                />
                <AutoGrid
                    service={{
                        list: async () => {
                            const results = await ClientServiceImpl.searchClients(filter);
                            return results ?? [];
                        }
                    }}
                    model={ClientModel}
                    columnOptions={{
                        firstName: { header: 'Nombre' },
                        lastName: { header: 'Apellido' },
                        cedula: { header: 'Cédula' },
                        phoneNumber: { header: 'Teléfono' },
                    }}
                    visibleColumns={['firstName', 'LastName', 'cedula', 'phoneNumber']}
                />
                <Button theme="tertiary" onClick={onClose}>Cancelar</Button>
            </div>
        </Dialog>
    );
}

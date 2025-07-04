import { AutoGrid } from '@vaadin/hilla-react-crud'
import { Button, Dialog, TextField } from '@vaadin/react-components'
import { useState } from 'react'
import {ClientServiceImpl} from "@/generated/endpoints";
import ClientModel
    from "@/generated/com/zoolandia/app/features/client/domain/ClientModel";

export interface SelectedClient {
    firstName: string
    lastName: string
    cedula?: string
    passport?: string
}

interface SelectClientDialogProps {
    open: boolean
    onClose: () => void
    onSelect: (client: SelectedClient) => void
}

export function SelectClientDialog({ open, onClose, onSelect }: SelectClientDialogProps) {
    const [selectedClient, setSelectedClient] = useState<SelectedClient | null>(null)

    return (
        <Dialog opened={open} onOpenedChanged={({ detail }) => !detail.value && onClose()}>
            <div style={{ padding: '1rem', width: '700px', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <h3>Seleccionar Cliente</h3>

                <AutoGrid
                    service={ClientServiceImpl}
                    model={ClientModel}
                    columnOptions={{
                        firstName: { header: 'Nombre' },
                        lastName: { header: 'Apellido' },
                        cedula: { header: 'Cédula' },
                        passport: { header: 'Pasaporte' },
                    }}
                    visibleColumns={['firstName', 'lastName', 'cedula', 'passport']}
                    onActiveItemChanged={({ detail }) => {
                        if (detail.value) {
                            setSelectedClient(detail.value)
                        }
                    }}
                />

                <TextField
                    label="Cliente seleccionado"
                    value={selectedClient ? `${selectedClient.firstName} - ${selectedClient.lastName}` : ''}
                    readonly
                />

                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.3rem' }}>
                    <Button theme="tertiary" onClick={onClose}>
                        Cancelar
                    </Button>
                    <Button
                        theme="primary"
                        onClick={() => {
                            if (!selectedClient) return
                            onSelect(selectedClient)
                            onClose()
                        }}
                        disabled={!selectedClient}
                    >
                        Aceptar
                    </Button>
                </div>
            </div>
        </Dialog>
    )
}
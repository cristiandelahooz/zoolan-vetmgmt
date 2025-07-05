import ClientModel from '@/generated/com/wornux/data/entity/ClientModel'
import { ClientServiceImpl } from '@/generated/endpoints'
import { AutoGrid } from '@vaadin/hilla-react-crud'
import { Button, Dialog, TextField } from '@vaadin/react-components'
import { useState } from 'react'

export interface SelectedClient {
  id: number
  firstName: string
  lastName: string
}

type SelectOwnerDialogType = {
  open: boolean
  onClose: () => void
  onSelect: (client: SelectedClient) => void
}

export function SelectOwnerDialog({ open, onClose, onSelect }: SelectOwnerDialogType) {
  const [selectedClient, setSelectedClient] = useState<any>(null)

  return (
    <Dialog opened={open} onOpenedChanged={({ detail }) => !detail.value && onClose()}>
      <div style={{ padding: '1rem', width: '700px', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <h3>Seleccionar Dueño</h3>

        <AutoGrid
          service={ClientServiceImpl}
          model={ClientModel}
          columnOptions={{
            firstName: { header: 'Nombre' },
            lastName: { header: 'Apellido' },
            cedula: { header: 'Cédula' },
            phoneNumber: { header: 'Teléfono' },
          }}
          visibleColumns={['firstName', 'lastName', 'cedula', 'phoneNumber']}
          onActiveItemChanged={({ detail }) => {
            if (detail.value) {
              setSelectedClient(detail.value)
            }
          }}
        />

        <TextField
          label="Dueño seleccionado"
          value={selectedClient ? `${selectedClient.firstName} ${selectedClient.lastName}` : ''}
          readonly
        />

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem' }}>
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

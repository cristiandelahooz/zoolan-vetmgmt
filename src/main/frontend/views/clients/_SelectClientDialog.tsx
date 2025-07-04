import type Client from '@/generated/com/wornux/features/client/domain/Client'
import ClientModel from '@/generated/com/wornux/features/client/domain/ClientModel'
import { ClientServiceImpl } from '@/generated/endpoints'
import { AutoGrid } from '@vaadin/hilla-react-crud'
import { Button, Dialog } from '@vaadin/react-components'
import { useState } from 'react'

export type SelectedClient = Pick<Client, 'id' | 'firstName' | 'lastName' | 'cedula' | 'passport'>

interface SelectClientDialogProps {
  open: boolean
  onClose: () => void
  onSelect: (client: SelectedClient) => void
}

export function SelectClientDialog({ open, onClose, onSelect }: Readonly<SelectClientDialogProps>) {
  const [selectedClient, setSelectedClient] = useState<Client | null>(null)

  return (
    <Dialog
      headerTitle="Select a Client"
      opened={open}
      onOpenedChanged={({ detail }) => !detail.value && onClose()}
      footer={
        <div className="flex gap-s justify-end">
          <Button theme="tertiary" onClick={onClose}>
            Cancel
          </Button>
          <Button
            theme="primary"
            onClick={() => {
              if (selectedClient) {
                onSelect(selectedClient)
              }
            }}
            disabled={!selectedClient}
          >
            Select
          </Button>
        </div>
      }
    >
      <div className="p-m" style={{ width: '800px', height: '600px' }}>
        <AutoGrid
          service={ClientServiceImpl}
          model={ClientModel}
          onActiveItemChanged={({ detail }) => {
            if (detail.value) {
              setSelectedClient(detail.value)
            } else {
              setSelectedClient(null)
            }
          }}
          columnOptions={{
            firstName: { header: 'Nombre' },
            lastName: { header: 'Apellido' },
            cedula: { header: 'Cédula' },
            phoneNumber: { header: 'Teléfono' },
          }}
          visibleColumns={['firstName', 'lastName', 'cedula', 'email', 'phoneNumber']}
        />
      </div>
    </Dialog>
  )
}

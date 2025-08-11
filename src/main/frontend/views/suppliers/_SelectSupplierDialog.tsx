import SupplierListDtoModel from '@/generated/com/wornux/dto/response/SupplierListDtoModel'
import { SupplierServiceImpl } from '@/generated/endpoints'
import { AutoGrid } from '@vaadin/hilla-react-crud'
import { Button, Dialog, TextField } from '@vaadin/react-components'
import { useState } from 'react'

export interface SelectedSupplier {
  id: number
  name: string
  email?: string
}

type SelectSupplierDialogType = {
  open: boolean
  onClose: () => void
  onSelect: (supplier: SelectedSupplier) => void
}

export function SelectSupplierDialog({ open, onClose, onSelect }: SelectSupplierDialogType) {
  const [selectedSupplier, setSelectedSupplier] = useState<any>(null)

  return (
    <Dialog opened={open} onOpenedChanged={({ detail }) => !detail.value && onClose()}>
      <div style={{ padding: '1rem', width: '700px', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <h3>Seleccionar Suplidor</h3>

        <AutoGrid
          service={{
            list: async (...args) => {
              const result = await SupplierServiceImpl.listAsDto(...args)
              return (result ?? []).filter(Boolean)
            },
          }}
          model={SupplierListDtoModel}
          columnOptions={{
            companyName: { header: 'Nombre' },
            contactEmail: { header: 'Email' },
            contactPhone: { header: 'Teléfono' },
          }}
          visibleColumns={['companyName', 'contactEmail', 'contactPhone']}
          onActiveItemChanged={({ detail }) => {
            if (detail.value) {
              setSelectedSupplier(detail.value)
            }
          }}
        />

        <TextField
          label="Suplidor seleccionado"
          value={selectedSupplier ? selectedSupplier.companyName : ''}
          readonly
        />

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem' }}>
          <Button theme="tertiary" onClick={onClose}>
            Cancelar
          </Button>
          <Button
            theme="primary"
            onClick={() => {
              if (!selectedSupplier) return
              onSelect({
                id: selectedSupplier.id,
                name: selectedSupplier.companyName,
                email: selectedSupplier.contactEmail,
              })
              onClose()
            }}
            disabled={!selectedSupplier}
          >
            Aceptar
          </Button>
        </div>
      </div>
    </Dialog>
  )
}

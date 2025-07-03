import { AutoGrid } from '@vaadin/hilla-react-crud'
import { Button, Dialog, TextField } from '@vaadin/react-components'
import { useState } from 'react'
import { PetServiceImpl } from 'Frontend/generated/endpoints'
import PetModel from 'Frontend/generated/com/zoolandia/app/features/pet/domain/PetModel'
import PetType from "Frontend/generated/com/zoolandia/app/features/pet/domain/PetType"

export interface SelectedPet {
    id: number
    name: string
    type: PetType
    breed: string
    ownerName: string
}

interface SelectPetDialogProps {
    open: boolean
    onClose: () => void
    onSelect: (pet: SelectedPet) => void
}

export function SelectPetDialog({ open, onClose, onSelect }: SelectPetDialogProps) {
    const [selectedPet, setSelectedPet] = useState<any>(null)

    return (
        <Dialog opened={open} onOpenedChanged={({ detail }) => !detail.value && onClose()}>
            <div style={{ padding: '1rem', width: '700px', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <h3>Seleccionar Mascota</h3>

                <AutoGrid
                    service={PetServiceImpl}
                    model={PetModel}
                    columnOptions={{
                        name: { header: 'Nombre' },
                        type: { header: 'Tipo' },
                        breed: { header: 'Raza' },
                        ownerName: { header: 'Dueño' }
                    }}
                    visibleColumns={['name', 'type', 'breed', 'ownerName']}
                    onActiveItemChanged={({ detail }) => {
                        if (detail.value) {
                            setSelectedPet(detail.value)
                        }
                    }}
                />

                <TextField
                    label="Mascota seleccionada"
                    value={selectedPet ? `${selectedPet.name} - ${selectedPet.breed}` : ''}
                    readonly
                />

                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem' }}>
                    <Button theme="tertiary" onClick={onClose}>
                        Cancelar
                    </Button>
                    <Button
                        theme="primary"
                        onClick={() => {
                            if (!selectedPet) return
                            onSelect(selectedPet)
                            onClose()
                        }}
                        disabled={!selectedPet}
                    >
                        Aceptar
                    </Button>
                </div>
            </div>
        </Dialog>
    )
}
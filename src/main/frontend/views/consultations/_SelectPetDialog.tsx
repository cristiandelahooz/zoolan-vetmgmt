import { AutoGrid } from '@vaadin/hilla-react-crud'
import { Button, Dialog, TextField } from '@vaadin/react-components'
import { useState } from 'react'
import { PetServiceImpl } from '@/generated/endpoints'
import type Pet from '@/generated/com/wornux/data/entity/Pet'
import type PetType from '@/generated/com/wornux/data/enums/PetType'
import PetModel from '@/generated/com/wornux/data/entity/PetModel'

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

const createPetListService = () => ({
    list: async (pageable?: any, filter?: any) => {
        try {
            const result = await PetServiceImpl.list(pageable, filter)
            return (result || []).filter((pet): pet is Pet => pet !== undefined)
        } catch (error) {
            console.error('Error loading pets:', error)
            return []
        }
    },
    save: async (item: any) => {
        try {
            return await PetServiceImpl.save(item)
        } catch (error) {
            console.error('Error saving pet:', error)
            throw error
        }
    },
    delete: async (id: number) => {
        try {
            return await PetServiceImpl.delete(id)
        } catch (error) {
            console.error('Error deleting pet:', error)
            throw error
        }
    }
})

export function SelectPetDialog({ open, onClose, onSelect }: SelectPetDialogProps) {
    const [selectedPet, setSelectedPet] = useState<any>(null)
    const petListService = createPetListService()

    return (
        <Dialog opened={open} onOpenedChanged={({ detail }) => !detail.value && onClose()}>
            <div style={{ padding: '1rem', width: '700px', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <h3>Seleccionar Mascota</h3>

                <AutoGrid
                    service={petListService}
                    model={PetModel}
                    columnOptions={{
                        name: { header: 'Nombre' },
                        type: { header: 'Tipo' },
                        breed: { header: 'Raza' },
                        ownerName: { header: 'Dueño' },
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
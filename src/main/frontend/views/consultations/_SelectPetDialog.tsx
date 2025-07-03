import { Dialog, Grid, GridColumn, Button } from '@vaadin/react-components'
import { useSignal } from '@vaadin/hilla-react-signals'
import { useEffect } from 'react'
import { PetServiceImpl } from 'Frontend/generated/endpoints'
import type PetSummaryDTO from 'Frontend/generated/com/zoolandia/app/features/pet/service/dto/PetSummaryDTO'
import PetType from "Frontend/generated/com/zoolandia/app/features/pet/domain/PetType";

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
    const pets = useSignal<PetSummaryDTO[]>([])

    useEffect(() => {
        if (open) {
            loadPets()
        }
    }, [open])

    const loadPets = async () => {
        try {
            const petsData = await PetServiceImpl.getAllPets(undefined)
            pets.value = petsData?.filter((pet): pet is PetSummaryDTO => pet !== undefined) || []
        } catch (error) {
            console.error('Error loading pets:', error)
        }
    }

    const handleSelect = (pet: PetSummaryDTO) => {
        if (pet.id && pet.name && pet.type && pet.breed && pet.ownerName) {
            onSelect({
                id: pet.id,
                name: pet.name,
                type: pet.type,
                breed: pet.breed,
                ownerName: pet.ownerName
            })
        }
    }

    return (
        <Dialog open={open} onOpenedChanged={(e) => !e.detail.value && onClose()}>
            <div slot="header-content">Seleccionar Mascota</div>
            <Grid items={pets.value} onActiveItemChanged={(e) => e.detail.value && handleSelect(e.detail.value)}>
                <GridColumn path="name" header="Nombre" />
                <GridColumn path="type" header="Tipo" />
                <GridColumn path="breed" header="Raza" />
                <GridColumn path="ownerName" header="Dueño" />
            </Grid>
            <Button slot="footer" onClick={onClose}>Cancelar</Button>
        </Dialog>
    )
}
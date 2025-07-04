import PetModel from '@/generated/com/wornux/features/pet/domain/PetModel'
import { AutoGrid } from '@vaadin/hilla-react-crud'
import { Button, Dialog, TextField } from '@vaadin/react-components'
import { PetServiceImpl } from 'Frontend/generated/endpoints'
import { useEffect, useState } from 'react'

export interface SelectedPet {
  id: number
  name: string
  type: string
  breed: string
}

type SelectPetDialogType = {
  open: boolean
  ownerId: number | undefined
  onClose: () => void
  onSelect: (pet: SelectedPet) => void
}

export function SelectPetDialog({ open, ownerId, onClose, onSelect }: SelectPetDialogType) {
  const [selectedPet, setSelectedPet] = useState<any>(null)
  const [pets, setPets] = useState<any[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (open && ownerId) {
      loadPets()
    }
  }, [open, ownerId])

  const loadPets = async () => {
    if (!ownerId) return

    setLoading(true)
    try {
      console.log('Loading pets for owner ID:', ownerId)

      const pageable = {
        pageNumber: 0,
        pageSize: 1000,
        sort: {
          orders: [],
        },
      }

      const petsResult = await PetServiceImpl.getPetsByOwnerId(ownerId, pageable)
      console.log('Pets result:', petsResult)

      const validPets = (petsResult || []).filter((pet) => pet !== undefined && pet !== null)
      console.log('Valid pets:', validPets)

      setPets(validPets)
    } catch (error) {
      console.error('Error loading pets:', error)
      setPets([])
    } finally {
      setLoading(false)
    }
  }

  const mockPetService = {
    list: async () => pets,
    count: async () => pets.length,
    save: PetServiceImpl.save,
    delete: PetServiceImpl.delete,
    get: PetServiceImpl.get,
  }

  if (!ownerId) {
    return null
  }

  return (
    <Dialog opened={open} onOpenedChanged={({ detail }) => !detail.value && onClose()}>
      <div style={{ padding: '1rem', width: '700px', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <h3>Seleccionar Mascota del Dueño</h3>

        {loading && <div>Cargando mascotas...</div>}

        {!loading && pets.length === 0 && (
          <div style={{ padding: '1rem', textAlign: 'center', color: 'var(--lumo-secondary-text-color)' }}>
            Este dueño no tiene mascotas registradas
          </div>
        )}

        {!loading && pets.length > 0 && (
          <>
            <div style={{ fontSize: '0.9em', color: 'var(--lumo-secondary-text-color)' }}>
              Se encontraron {pets.length} mascota(s) para este dueño
            </div>
            <AutoGrid
              service={mockPetService}
              model={PetModel}
              columnOptions={{
                name: { header: 'Nombre' },
                type: { header: 'Tipo' },
                breed: { header: 'Raza' },
                birthDate: { header: 'Fecha de Nacimiento' },
                gender: { header: 'Género' },
              }}
              visibleColumns={['name', 'type', 'breed', 'birthDate', 'gender']}
              onActiveItemChanged={({ detail }) => {
                if (detail.value) {
                  setSelectedPet(detail.value)
                }
              }}
            />
          </>
        )}

        <TextField
          label="Mascota seleccionada"
          value={selectedPet ? `${selectedPet.name} (${selectedPet.type} - ${selectedPet.breed})` : ''}
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
              setSelectedPet(null)
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

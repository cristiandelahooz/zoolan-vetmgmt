import type { ViewConfig } from '@vaadin/hilla-file-router/types.js'
import {
  Button,
  Dialog,
  Grid,
  GridColumn,
  HorizontalLayout,
  Notification,
  TextField,
  VerticalLayout,
} from '@vaadin/react-components'
import type Pet from 'Frontend/generated/com/zoolandia/app/features/pet/domain/Pet'
import { PetServiceImpl } from 'Frontend/generated/endpoints'
import React, { useState } from 'react'

export const config: ViewConfig = {
  menu: { title: 'Fusionar' },
  title: 'Fusionar Mascotas Duplicadas',
}

interface SelectedPets {
  keepPet: Pet | null
  removePet: Pet | null
}

const getTypeLabel = (type: string) => {
  const typeLabels: Record<string, string> = {
    DOG: 'Perro',
    CAT: 'Gato',
    BIRD: 'Ave',
    RABBIT: 'Conejo',
    HAMSTER: 'Hámster',
    REPTILE: 'Reptil',
    OTHER: 'Otro',
  }
  return typeLabels[type] || type
}

export default function PetMergeView() {
  const [searchTerm, setSearchTerm] = useState('')
  const [searchResults, setSearchResults] = useState<Pet[]>([])
  const [selectedPets, setSelectedPets] = useState<SelectedPets>({
    keepPet: null,
    removePet: null,
  })
  const [dialogOpen, setDialogOpen] = useState(false)
  const [loading, setLoading] = useState(false)

  const handleSearch = async () => {
    if (!searchTerm.trim()) {
      Notification.show('Por favor ingresa un nombre para buscar', { theme: 'error' })
      return
    }

    setLoading(true)
    try {
      const results = await PetServiceImpl.findSimilarPetsByName(searchTerm)

      // Filtrar elementos undefined y establecer el estado
      const validResults = (results || []).filter((pet): pet is Pet => pet !== undefined)
      setSearchResults(validResults)

      if (validResults.length === 0) {
        Notification.show('No se encontraron mascotas con ese nombre')
      } else if (validResults.length === 1) {
        Notification.show('Solo se encontró una mascota. Necesitas al menos 2 para fusionar.')
      }
    } catch (error) {
      console.error('Error searching pets:', error)
      Notification.show('Error al buscar mascotas', { theme: 'error' })
      setSearchResults([])
    } finally {
      setLoading(false)
    }
  }

  const handlePetSelection = (pet: Pet, role: 'keep' | 'remove') => {
    setSelectedPets((prev) => ({
      ...prev,
      [role === 'keep' ? 'keepPet' : 'removePet']: pet,
    }))
  }

  const handleMerge = async () => {
    if (!selectedPets.keepPet || !selectedPets.removePet) return

    setLoading(true)
    try {
      // Los IDs deberían ser números directamente en Pet
      const keepPetId = selectedPets.keepPet.id
      const removePetId = selectedPets.removePet.id

      if (keepPetId === undefined || removePetId === undefined) {
        throw new Error('IDs de mascotas no válidos')
      }

      await PetServiceImpl.mergePets(keepPetId, removePetId)
      Notification.show('Mascotas fusionadas exitosamente', { theme: 'success' })

      // Limpiar selecciones y buscar de nuevo
      setSelectedPets({ keepPet: null, removePet: null })
      handleSearch()
      setDialogOpen(false)
    } catch (error: any) {
      console.error('Error merging pets:', error)
      Notification.show(`Error al fusionar: ${error.message || 'Error desconocido'}`, { theme: 'error' })
    } finally {
      setLoading(false)
    }
  }

  const clearSelections = () => {
    setSelectedPets({ keepPet: null, removePet: null })
  }

  const canMerge =
    selectedPets.keepPet && selectedPets.removePet && selectedPets.keepPet.id !== selectedPets.removePet.id

  const OwnersList = ({ owners }: { owners: any[] }) => (
    <div>
      {owners && owners.length > 0 ? (
        owners.map((owner, index) => (
          <div key={index} style={{ fontSize: '0.875rem', marginBottom: '2px' }}>
            {owner.firstName || ''} {owner.lastName || ''}
            {owner.cedula && ` (${owner.cedula})`}
          </div>
        ))
      ) : (
        <span style={{ fontStyle: 'italic', color: '#666' }}>Sin dueños</span>
      )}
    </div>
  )

  const ActionButtons = ({ pet }: { pet: Pet }) => {
    const isSelectedToKeep = selectedPets.keepPet?.id === pet.id
    const isSelectedToRemove = selectedPets.removePet?.id === pet.id

    return (
      <HorizontalLayout theme="spacing-s">
        <Button
          theme={isSelectedToKeep ? 'primary contrast' : 'primary small'}
          onClick={() => handlePetSelection(pet, 'keep')}
          disabled={isSelectedToKeep}
        >
          {isSelectedToKeep ? '✓ Mantener' : 'Mantener'}
        </Button>
        <Button
          theme={isSelectedToRemove ? 'error contrast' : 'secondary small'}
          onClick={() => handlePetSelection(pet, 'remove')}
          disabled={isSelectedToRemove}
        >
          {isSelectedToRemove ? '✓ Eliminar' : 'Eliminar'}
        </Button>
      </HorizontalLayout>
    )
  }

  return (
    <>
      <main className="w-full h-full flex flex-col box-border gap-s p-m">
        <VerticalLayout className="w-full">
          <h2>Fusionar Mascotas Duplicadas</h2>
          <p style={{ color: '#666', marginBottom: '1rem' }}>
            Busca mascotas por nombre para encontrar posibles duplicados. Podrás mantener una y transferir todos los
            dueños a ella.
          </p>

          <HorizontalLayout theme="spacing" className="w-full mb-4">
            <TextField
              placeholder="Buscar por nombre de mascota..."
              value={searchTerm}
              onInput={(e) => {
                const target = e.target as HTMLInputElement
                setSearchTerm(target.value)
              }}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
              className="flex-grow"
            />
            <Button theme="primary" onClick={handleSearch} disabled={loading}>
              {loading ? 'Buscando...' : 'Buscar'}
            </Button>
          </HorizontalLayout>

          {searchResults.length > 0 && (
            <VerticalLayout className="w-full">
              <div className="flex justify-between items-center mb-4">
                <h3>Resultados de búsqueda ({searchResults.length})</h3>
                <HorizontalLayout theme="spacing">
                  {(selectedPets.keepPet || selectedPets.removePet) && (
                    <Button theme="tertiary small" onClick={clearSelections}>
                      Limpiar Selección
                    </Button>
                  )}
                  <Button theme="primary" onClick={() => setDialogOpen(true)} disabled={!canMerge}>
                    Fusionar Seleccionadas
                  </Button>
                </HorizontalLayout>
              </div>

              <Grid items={searchResults} className="w-full" style={{ height: '400px' }}>
                <GridColumn header="Nombre" renderer={({ item }) => <span>{item.name || ''}</span>} width="130px" />
                <GridColumn
                  header="Tipo"
                  renderer={({ item }) => <span>{getTypeLabel(item.type || '')}</span>}
                  width="100px"
                />
                <GridColumn header="Raza" renderer={({ item }) => <span>{item.breed || ''}</span>} width="140px" />
                <GridColumn
                  header="F. Nacimiento"
                  renderer={({ item }) => <span>{item.birthDate || ''}</span>}
                  width="120px"
                />
                <GridColumn
                  header="Dueños"
                  renderer={({ item }) => <OwnersList owners={item.owners || []} />}
                  width="200px"
                />
                <GridColumn header="Acciones" renderer={({ item }) => <ActionButtons pet={item} />} width="180px" />
              </Grid>

              {selectedPets.keepPet && (
                <div className="mt-4 p-4 border-2 border-green-300 bg-green-50 rounded-lg">
                  <strong style={{ color: '#166534' }}>✓ Mascota a mantener:</strong>
                  <div>
                    {selectedPets.keepPet.name} - {getTypeLabel(selectedPets.keepPet.type || '')} (
                    {selectedPets.keepPet.owners?.length || 0} dueño(s))
                  </div>
                </div>
              )}

              {selectedPets.removePet && (
                <div className="mt-2 p-4 border-2 border-red-300 bg-red-50 rounded-lg">
                  <strong style={{ color: '#dc2626' }}>✗ Mascota a eliminar:</strong>
                  <div>
                    {selectedPets.removePet.name} - {getTypeLabel(selectedPets.removePet.type || '')} (
                    {selectedPets.removePet.owners?.length || 0} dueño(s))
                  </div>
                </div>
              )}
            </VerticalLayout>
          )}
        </VerticalLayout>
      </main>

      <Dialog
        opened={dialogOpen}
        onOpenedChanged={(e) => setDialogOpen(e.detail.value)}
        headerTitle="⚠️ Confirmar Fusión de Mascotas"
      >
        <VerticalLayout style={{ width: '450px', gap: '1rem' }}>
          <p>
            <strong>¿Estás segura de que quieres fusionar estas mascotas?</strong>
          </p>

          {selectedPets.keepPet && (
            <div className="p-4 border-2 border-green-300 bg-green-50 rounded-lg">
              <strong style={{ color: '#166534' }}>🐾 SE MANTENDRÁ:</strong>
              <div>
                <strong>Nombre:</strong> {selectedPets.keepPet.name || ''}
              </div>
              <div>
                <strong>Tipo:</strong> {getTypeLabel(selectedPets.keepPet.type || '')}
              </div>
              <div>
                <strong>Raza:</strong> {selectedPets.keepPet.breed || ''}
              </div>
              <div>
                <strong>Dueños actuales:</strong> {selectedPets.keepPet.owners?.length || 0}
              </div>
            </div>
          )}

          {selectedPets.removePet && (
            <div className="p-4 border-2 border-red-300 bg-red-50 rounded-lg">
              <strong style={{ color: '#dc2626' }}>🗑️ SE ELIMINARÁ:</strong>
              <div>
                <strong>Nombre:</strong> {selectedPets.removePet.name || ''}
              </div>
              <div>
                <strong>Tipo:</strong> {getTypeLabel(selectedPets.removePet.type || '')}
              </div>
              <div>
                <strong>Raza:</strong> {selectedPets.removePet.breed || ''}
              </div>
              <div>
                <strong>Dueños que se trasladarán:</strong> {selectedPets.removePet.owners?.length || 0}
              </div>
            </div>
          )}

          <div className="p-3 bg-blue-50 border border-blue-200 rounded">
            <strong>📝 Resultado:</strong> La mascota "{selectedPets.keepPet?.name || ''}" tendrá todos los dueños de
            ambas mascotas.
          </div>

          <HorizontalLayout theme="spacing" className="mt-4" style={{ justifyContent: 'flex-end' }}>
            <Button theme="secondary" onClick={() => setDialogOpen(false)}>
              Cancelar
            </Button>
            <Button theme="primary error" onClick={handleMerge} disabled={loading}>
              {loading ? 'Fusionando...' : '🔄 Confirmar Fusión'}
            </Button>
          </HorizontalLayout>
        </VerticalLayout>
      </Dialog>
    </>
  )
}

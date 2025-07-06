import type { ViewConfig } from '@vaadin/hilla-file-router/types.js'
import { AutoGrid } from '@vaadin/hilla-react-crud'
import PetModel from '@/generated/com/wornux/data/entity/PetModel'
import { PetServiceImpl } from '@/generated/endpoints'
import type React from 'react'

const OwnerRenderer: React.FC<{ item: PetModel }> = ({ item }) => {
  const ownersArray = item.owners?.valueOf() || []

  if (ownersArray.length === 0) {
    return <span>Sin dueño</span>
  }

  const primaryOwner = ownersArray[0]
  return <span>{`${primaryOwner.firstName} ${primaryOwner.lastName}`}</span>
}

const CedulaRenderer: React.FC<{ item: PetModel }> = ({ item }) => {
  const ownersArray = item.owners?.valueOf() || []

  if (ownersArray.length === 0) {
    return <span>-</span>
  }

  const primaryOwner = ownersArray[0]
  return <span>{primaryOwner.cedula || '-'}</span>
}

const ActiveRenderer: React.FC<{ item: PetModel }> = ({ item }) => {
  const isActive = item.active?.valueOf()
  return (
    <span
      style={{
        color: isActive ? '#16a34a' : '#dc2626',
        fontWeight: 'bold',
      }}
    >
      {isActive ? 'Activa' : 'Inactiva'}
    </span>
  )
}

export const config: ViewConfig = {
  menu: { title: 'Ver' },
  title: 'Lista de Mascotas',
}

export default function PetListView() {
  return (
    <main className="w-full h-full flex flex-col box-border gap-s p-m">
      <AutoGrid
        service={{
          ...PetServiceImpl,
          list: async (pageable, filter) => {
            const result = await PetServiceImpl.list(pageable, filter)
            return result ?? [] // si viene undefined, devolver []
          },
        }}
        model={PetModel}
        visibleColumns={['name', 'type', 'breed', 'birthDate', 'owners', 'cedula', 'gender', 'active']}
        columnOptions={{
          name: { header: 'Nombre' },
          type: { header: 'Tipo' },
          breed: { header: 'Raza' },
          birthDate: { header: 'Fecha de Nacimiento' },
          owners: {
            header: 'Dueño',
            renderer: OwnerRenderer,
            filterable: false,
            sortable: false,
          },
          cedula: {
            header: 'Cédula',
            renderer: CedulaRenderer,
            filterable: true,
            sortable: false,
            width: '120px',
          },
          gender: { header: 'Género' },
          active: {
            header: 'Estado',
            renderer: ActiveRenderer,
            filterable: true,
            sortable: true,
            width: '100px',
          },
        }}
      />
    </main>
  )
}

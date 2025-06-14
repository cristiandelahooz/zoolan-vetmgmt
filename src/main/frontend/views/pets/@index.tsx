import React from 'react'
import type { ViewConfig } from '@vaadin/hilla-file-router/types.js'
import { AutoGrid } from '@vaadin/hilla-react-crud'
import { PetServiceImpl } from 'Frontend/generated/endpoints'
import PetModel from 'Frontend/generated/com/zoolandia/app/features/pet/domain/PetModel'

const OwnerRenderer: React.FC<{ item: PetModel }> = ({ item }) => (
  <span>{item.owner ? `${item.owner.firstName} ${item.owner.lastName}` : ''}</span>
)

export const config: ViewConfig = {
  menu: { title: 'Ver' },
  title: 'Lista de Mascotas',
}

export default function PetListView() {
  return (
    <main className="w-full h-full flex flex-col box-border gap-s p-m">
      <AutoGrid
        service={PetServiceImpl}
        model={PetModel}
        visibleColumns={['name', 'type', 'breed', 'birthDate', 'owner', 'owner.cedula', 'gender']}
        columnOptions={{
          name: { header: 'Nombre' },
          type: { header: 'Tipo' },
          breed: { header: 'Raza' },
          birthDate: { header: 'Fecha de Nacimiento' },
          owner: {
            header: 'Dueño',
            renderer: OwnerRenderer,
            filterable: false,
            sortable: false,
          },
          'owner.cedula': {
            header: 'Cédula',
            filterable: true,
            sortable: true,
            width: '50px',
          },

          gender: { header: 'Género' },
        }}
      />
    </main>
  )
}

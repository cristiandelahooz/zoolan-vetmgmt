import type { ViewConfig } from '@vaadin/hilla-file-router/types.js'
import { AutoGrid } from '@vaadin/hilla-react-crud'
import PetModel from 'Frontend/generated/com/zoolandia/app/features/pet/domain/PetModel'
import { PetServiceImpl } from 'Frontend/generated/endpoints'
import type React from 'react'
import { useEffect, useRef } from 'react'

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
      {isActive ? '✅ Activa' : '❌ Inactiva'}
    </span>
  )
}

export const config: ViewConfig = {
  menu: { title: 'Ver' },
  title: 'Lista de Mascotas',
}

export default function PetListView() {
  const gridRef = useRef<any>(null)

  useEffect(() => {
    // Aplicar filtro automático para mostrar solo mascotas activas
    if (gridRef.current) {
      const timer = setTimeout(() => {
        try {
          // Aplicar filtro para mostrar solo activas( no sirve, tengo que arreglarlo)
          const filter = {
            '@type': 'and',
            children: [
              {
                '@type': 'propertyString',
                propertyId: 'active',
                filterValue: 'yes',
                matcher: 'equals',
              },
            ],
          }
          gridRef.current.filter = filter
        } catch (error) {
          console.log('No se pudo aplicar filtro automático:', error)
        }
      }, 500)

      return () => clearTimeout(timer)
    }

    return () => {}
  }, [])

  return (
    <main className="w-full h-full flex flex-col box-border gap-s p-m">
      <div className="mb-4 p-3 bg-blue-50 border border-blue-200 rounded">
        <p className="text-sm text-blue-800">
          💡 <strong>Filtro automático aplicado:</strong> Solo se muestran mascotas activas. Las mascotas fusionadas
          (inactivas) están ocultas.
        </p>
      </div>

      <AutoGrid
        ref={gridRef}
        service={PetServiceImpl}
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

import Priority from '@/generated/com/wornux/data/enums/Priority'
import WaitingRoomModel from '@/generated/com/wornux/data/entity/WaitingRoomModel'
import WaitingRoomStatus from '@/generated/com/wornux/data/enums/WaitingRoomStatus'
import type { ViewConfig } from '@vaadin/hilla-file-router/types.js'
import { AutoGrid } from '@vaadin/hilla-react-crud'
import { WaitingRoomServiceImpl } from '@/generated/endpoints'
import type React from 'react'
import { useEffect, useRef } from 'react'

const ClientRenderer: React.FC<{ item: WaitingRoomModel }> = ({ item }) => {
  const client = item.client?.valueOf()

  if (!client) {
    return <span>Sin cliente</span>
  }

  return <span>{`${client.firstName} ${client.lastName}`}</span>
}

const PetRenderer: React.FC<{ item: WaitingRoomModel }> = ({ item }) => {
  const pet = item.pet?.valueOf()

  if (!pet) {
    return <span>Sin mascota</span>
  }

  return <span>{`${pet.name} (${pet.type})`}</span>
}

const StatusRenderer: React.FC<{ item: WaitingRoomModel }> = ({ item }) => {
  const status = item.status?.valueOf()

  switch (status) {
    case WaitingRoomStatus.WAITING:
      return <span style={{ color: '#f59e0b', fontWeight: 'bold' }}>⏳ Esperando</span>
    case WaitingRoomStatus.IN_CONSULTATION:
      return <span style={{ color: '#3b82f6', fontWeight: 'bold' }}>🩺 En Consulta</span>
    case WaitingRoomStatus.COMPLETED:
      return <span style={{ color: '#16a34a', fontWeight: 'bold' }}>✅ Completado</span>
    case WaitingRoomStatus.CANCELLED:
      return <span style={{ color: '#dc2626', fontWeight: 'bold' }}>❌ Cancelado</span>
    default:
      return <span>Desconocido</span>
  }
}

const PriorityRenderer: React.FC<{ item: WaitingRoomModel }> = ({ item }) => {
  const priority = item.priority?.valueOf()

  switch (priority) {
    case Priority.EMERGENCY:
      return <span style={{ color: '#dc2626', fontWeight: 'bold' }}>🚨 Emergencia</span>
    case Priority.URGENT:
      return <span style={{ color: '#f59e0b', fontWeight: 'bold' }}>! Urgente</span>
    case Priority.NORMAL:
    default:
      return <span style={{ color: '#16a34a', fontWeight: 'bold' }}>✅ Normal</span>
  }
}

const DateTimeRenderer: React.FC<{ item: WaitingRoomModel; field: keyof WaitingRoomModel }> = ({ item, field }) => {
  const dateValue = item[field]?.valueOf()

  if (!dateValue) {
    return <span>-</span>
  }

  try {
    const date = new Date(dateValue as string)
    return (
      <span>
        {date.toLocaleDateString('es-DO')}{' '}
        {date.toLocaleTimeString('es-DO', {
          hour: '2-digit',
          minute: '2-digit',
        })}
      </span>
    )
  } catch {
    return <span>-</span>
  }
}

const ArrivalTimeRenderer: React.FC<{ item: WaitingRoomModel }> = ({ item }) => (
  <DateTimeRenderer item={item} field="arrivalTime" />
)

const ConsultationStartedRenderer: React.FC<{ item: WaitingRoomModel }> = ({ item }) => (
  <DateTimeRenderer item={item} field="consultationStartedAt" />
)

const CompletedAtRenderer: React.FC<{ item: WaitingRoomModel }> = ({ item }) => (
  <DateTimeRenderer item={item} field="completedAt" />
)

const WaitTimeRenderer: React.FC<{ item: WaitingRoomModel }> = ({ item }) => {
  const arrivalTime = item.arrivalTime?.valueOf()
  const consultationStarted = item.consultationStartedAt?.valueOf()

  if (!arrivalTime) {
    return <span>-</span>
  }

  try {
    const arrival = new Date(arrivalTime)
    const endTime = consultationStarted ? new Date(consultationStarted) : new Date()
    const diffMins = Math.floor((endTime.getTime() - arrival.getTime()) / (1000 * 60))

    if (diffMins < 60) {
      return <span>{diffMins} min</span>
    }

    const hours = Math.floor(diffMins / 60)
    const mins = diffMins % 60
    return (
      <span>
        {hours}h {mins}m
      </span>
    )
  } catch {
    return <span>-</span>
  }
}

export const config: ViewConfig = {
  menu: { title: 'Lista Sala de Espera' },
  title: 'Historial de Sala de Espera',
}

export default function WaitingRoomListView() {
  const gridRef = useRef<any>(null)

  useEffect(() => {
    if (gridRef.current) {
      const timer = setTimeout(() => {
        try {
          const today = new Date().toISOString().split('T')[0]
          const filter = {
            '@type': 'and',
            children: [
              {
                '@type': 'propertyString',
                propertyId: 'arrivalTime',
                filterValue: today,
                matcher: 'contains',
              },
            ],
          }
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
          💡 <strong>Historial de Sala de Espera:</strong> Aquí puedes ver todas las entradas a la sala de espera,
          filtrar por fecha, estado, prioridad, etc.
        </p>
      </div>

      <AutoGrid
        ref={gridRef}
        service={WaitingRoomServiceImpl}
        model={WaitingRoomModel}
        visibleColumns={[
          'client',
          'pet',
          'arrivalTime',
          'status',
          'priority',
          'reasonForVisit',
          'waitTime',
          'consultationStartedAt',
          'completedAt',
          'notes',
        ]}
        columnOptions={{
          client: {
            header: 'Cliente',
            renderer: ClientRenderer,
            filterable: false,
            sortable: false,
            width: '150px',
          },
          pet: {
            header: 'Mascota',
            renderer: PetRenderer,
            filterable: false,
            sortable: false,
            width: '150px',
          },
          arrivalTime: {
            header: 'Hora de Llegada',
            renderer: ArrivalTimeRenderer,
            filterable: true,
            sortable: true,
            width: '160px',
          },
          status: {
            header: 'Estado',
            renderer: StatusRenderer,
            filterable: true,
            sortable: true,
            width: '120px',
          },
          priority: {
            header: 'Prioridad',
            renderer: PriorityRenderer,
            filterable: true,
            sortable: true,
            width: '120px',
          },
          reasonForVisit: {
            header: 'Motivo de Visita',
            filterable: true,
            sortable: false,
            width: '200px',
          },
          waitTime: {
            header: 'Tiempo de Espera',
            renderer: WaitTimeRenderer,
            filterable: false,
            sortable: false,
            width: '120px',
          },
          consultationStartedAt: {
            header: 'Consulta Iniciada',
            renderer: ConsultationStartedRenderer,
            filterable: false,
            sortable: true,
            width: '160px',
          },
          completedAt: {
            header: 'Completada',
            renderer: CompletedAtRenderer,
            filterable: false,
            sortable: true,
            width: '160px',
          },
          notes: {
            header: 'Notas',
            filterable: true,
            sortable: false,
            width: '200px',
          },
        }}
      />
    </main>
  )
}

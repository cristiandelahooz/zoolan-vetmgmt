import type Sort from '@/generated/com/vaadin/hilla/mappedtypes/Sort'
import type Client from '@/generated/com/wornux/data/entity/Client'
import type Pet from '@/generated/com/wornux/data/entity/Pet'
import type WaitingRoom from '@/generated/com/wornux/data/entity/WaitingRoom'
import Priority from '@/generated/com/wornux/data/enums/Priority'
import WaitingRoomStatus from '@/generated/com/wornux/data/enums/WaitingRoomStatus'
import { ClientServiceImpl, PetServiceImpl, WaitingRoomServiceImpl } from '@/generated/endpoints'
import type { ViewConfig } from '@vaadin/hilla-file-router/types.js'
import {
  Button,
  Card,
  ComboBox,
  Dialog,
  HorizontalLayout,
  Notification,
  TextArea,
  TextField,
  VerticalLayout,
} from '@vaadin/react-components'
import { useEffect, useState } from 'react'

export const config: ViewConfig = {
  menu: { title: 'Sala de Espera' },
  title: 'Sala de Espera',
}

export default function WaitingRoomView() {
  const [waitingList, setWaitingList] = useState<WaitingRoom[]>([])
  const [stats, setStats] = useState({ waiting: 0, inConsultation: 0, todayTotal: 0 })
  const [loading, setLoading] = useState(true)
  const [addDialogOpen, setAddDialogOpen] = useState(false)
  const [clients, setClients] = useState<(Client | undefined)[]>([])
  const [pets, setPets] = useState<Pet[] | undefined>([])
  const [selectedClientId, setSelectedClientId] = useState<string>('')
  const [selectedPetId, setSelectedPetId] = useState<string>('')
  const [reasonForVisit, setReasonForVisit] = useState('')
  const [priority, setPriority] = useState<Priority>(Priority.NORMAL)
  const [notes, setNotes] = useState('')

  const createEmptySort = (): Sort => ({
    orders: [],
  })

  useEffect(() => {
    loadWaitingRoom()
    loadStats()
    loadClients()

    const interval = setInterval(() => {
      loadWaitingRoom()
      loadStats()
    }, 30000)

    return () => clearInterval(interval)
  }, [])

  const loadWaitingRoom = async () => {
    try {
      const data = await WaitingRoomServiceImpl.getCurrentWaitingRoom()
      const validData = (data || []).filter((item): item is WaitingRoom => item !== undefined)
      setWaitingList(validData)
    } catch (error) {
      console.error('Error cargando sala de espera:', error)
      Notification.show('Error cargando datos', { theme: 'error' })
    } finally {
      setLoading(false)
    }
  }

  const loadStats = async () => {
    try {
      const [waiting, inConsultation, todayTotal] = await Promise.all([
        WaitingRoomServiceImpl.getWaitingCount(),
        WaitingRoomServiceImpl.getInConsultationCount(),
        WaitingRoomServiceImpl.getTodayCount(),
      ])
      setStats({ waiting, inConsultation, todayTotal })
    } catch (error) {
      console.error('Error cargando estadísticas:', error)
    }
  }

  const loadClients = async () => {
    try {
      const pageable = {
        pageNumber: 0,
        pageSize: 100,
        sort: createEmptySort(),
      }
      const filter = undefined
      const clientsData = await ClientServiceImpl.list(pageable, filter)
      setClients(clientsData || [])
    } catch (error) {
      console.error('Error cargando clientes:', error)
      try {
        const pageable = {
          pageNumber: 0,
          pageSize: 100,
          sort: createEmptySort(),
        }
        const clientsData = await ClientServiceImpl.getAllClients(pageable)
        setClients(clientsData || [])
      } catch (altError) {
        console.error('Error con getAllClients:', altError)
        setClients([])
      }
    }
  }

  const loadPetsForClient = async (clientId: number) => {
    try {
      const pageable = {
        pageNumber: 0,
        pageSize: 1000,
        sort: createEmptySort(),
      }
      const petsData = await PetServiceImpl.getPetsByOwnerId(clientId, pageable)
      const validPets = (petsData || []).filter((pet): pet is Pet => pet !== undefined)
      setPets(validPets)
    } catch (error) {
      console.error('Error cargando mascotas por owner:', error)
      try {
        const pageable = {
          pageNumber: 0,
          pageSize: 1000,
          sort: createEmptySort(),
        }
        const filter = undefined
        const allPets = await PetServiceImpl.list(pageable, filter)
        const validPets = allPets?.filter((pet): pet is Pet => pet?.id === clientId)
        setPets(validPets)
      } catch (altError) {
        console.error('Error cargando todas las mascotas:', altError)
        setPets([])
      }
    }
  }

  const handleAddToWaitingRoom = async () => {
    if (!selectedClientId || !selectedPetId || !reasonForVisit.trim()) {
      Notification.show('Por favor completa todos los campos requeridos', { theme: 'error' })
      return
    }

    try {
      await WaitingRoomServiceImpl.addToWaitingRoom(
        Number.parseInt(selectedClientId),
        Number.parseInt(selectedPetId),
        reasonForVisit,
        priority,
        notes || undefined,
      )

      Notification.show('Agregado a sala de espera exitosamente', { theme: 'success' })
      setAddDialogOpen(false)
      resetAddForm()
      loadWaitingRoom()
      loadStats()
    } catch (error: any) {
      Notification.show(`Error: ${error.message}`, { theme: 'error' })
    }
  }

  const handleMoveToConsultation = async (id: number) => {
    try {
      await WaitingRoomServiceImpl.moveToConsultation(id)
      Notification.show('Paciente movido a consulta', { theme: 'success' })
      loadWaitingRoom()
      loadStats()
    } catch (error: any) {
      Notification.show(`Error: ${error.message}`, { theme: 'error' })
    }
  }

  const handleCompleteConsultation = async (id: number) => {
    try {
      await WaitingRoomServiceImpl.completeConsultation(id)
      Notification.show('Consulta completada exitosamente', { theme: 'success' })
      loadWaitingRoom()
      loadStats()
    } catch (error: any) {
      Notification.show(`Error: ${error.message}`, { theme: 'error' })
    }
  }

  const handleCancelEntry = async (id: number) => {
    const reason = prompt('Motivo de cancelación:')
    if (!reason) return

    try {
      await WaitingRoomServiceImpl.cancelEntry(id, reason)
      Notification.show('Entrada cancelada', { theme: 'success' })
      loadWaitingRoom()
      loadStats()
    } catch (error: any) {
      Notification.show(`Error: ${error.message}`, { theme: 'error' })
    }
  }

  const resetAddForm = () => {
    setSelectedClientId('')
    setSelectedPetId('')
    setPets([])
    setReasonForVisit('')
    setPriority(Priority.NORMAL)
    setNotes('')
  }

  const getStatusDisplay = (status: WaitingRoomStatus) => {
    switch (status) {
      case WaitingRoomStatus.ESPERANDO:
        return { icon: '⏳', text: 'Esperando', theme: 'badge contrast' }
      case WaitingRoomStatus.EN_CONSULTA:
        return { icon: '🩺', text: 'En Consulta', theme: 'badge primary' }
      case WaitingRoomStatus.COMPLETADO:
        return { icon: '✅', text: 'Completado', theme: 'badge success' }
      case WaitingRoomStatus.CANCELADO:
        return { icon: '❌', text: 'Cancelado', theme: 'badge error' }
      default:
        return { icon: '❓', text: 'Desconocido', theme: 'badge' }
    }
  }

  const getPriorityDisplay = (priority: Priority) => {
    switch (priority) {
      case Priority.EMERGENCIA:
        return { icon: '', text: 'Emergencia', theme: 'badge error primary' }
      case Priority.URGENTE:
        return { icon: '', text: 'Urgente', theme: 'badge contrast primary' }
      case Priority.NORMAL:
      default:
        return { icon: '', text: 'Normal', theme: 'badge success primary' }
    }
  }

  const formatTime = (dateString: string) => {
    try {
      return new Date(dateString).toLocaleTimeString('es-DO', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: true,
      })
    } catch {
      return dateString
    }
  }

  const getWaitTime = (arrivalTime: string) => {
    try {
      const arrival = new Date(arrivalTime)
      const now = new Date()
      const diffMins = Math.floor((now.getTime() - arrival.getTime()) / (1000 * 60))

      if (diffMins < 60) return `${diffMins} min`
      const hours = Math.floor(diffMins / 60)
      const mins = diffMins % 60
      return `${hours}h ${mins}m`
    } catch {
      return '-'
    }
  }

  const clientItems = clients.map((client) => ({
    label: `${client?.firstName} ${client?.lastName} - ${client?.cedula || client?.passport || client?.rnc || 'Sin documento'}`,
    value: client?.id?.toString() ?? '',
  }))

  const petItems = pets?.map((pet) => ({
    label: `${pet.name} (${pet.type}${pet.breed ? ` - ${pet.breed}` : ''})`,
    value: pet.id?.toString() ?? '',
  }))

  const priorityItems = [
    { label: '✅ Normal - Consulta de rutina', value: Priority.NORMAL },
    { label: '! Urgente - Requiere atención pronta', value: Priority.URGENTE },
    { label: '🚨 Emergencia - Atención inmediata', value: Priority.EMERGENCIA },
  ]

  if (loading) {
    return (
      <main className="w-full h-full flex items-center justify-center">
        <VerticalLayout theme="spacing" className="text-center">
          <span style={{ fontSize: '3rem' }}>🏥</span>
          <span className="text-l text-secondary">Cargando sala de espera...</span>
        </VerticalLayout>
      </main>
    )
  }

  return (
    <main className="w-full h-full flex flex-col box-border gap-l p-m">
      <HorizontalLayout theme="spacing wrap" className="w-full min-w-full">
        <Card className="flex items-center gap-m p-m">
          <div
            style={{
              fontSize: '2.5rem',
              background: 'var(--lumo-warning-color-10pct)',
              padding: 'var(--lumo-space-s)',
              borderRadius: 'var(--lumo-border-radius-m)',
            }}
          ></div>
          <VerticalLayout theme="spacing-xs">
            <span className="text-2xl font-bold text-primary">{stats.waiting}</span>
            <span className="text-s text-secondary">Esperando</span>
          </VerticalLayout>
        </Card>

        <Card className="flex items-center gap-m p-m">
          <div
            style={{
              fontSize: '2.5rem',
              background: 'var(--lumo-primary-color-10pct)',
              padding: 'var(--lumo-space-s)',
              borderRadius: 'var(--lumo-border-radius-m)',
            }}
          ></div>
          <VerticalLayout theme="spacing-xs">
            <span className="text-2xl font-bold text-primary">{stats.inConsultation}</span>
            <span className="text-s text-secondary">En Consulta</span>
          </VerticalLayout>
        </Card>

        <Card className="flex items-center gap-m p-m">
          <div
            style={{
              fontSize: '2.5rem',
              background: 'var(--lumo-success-color-10pct)',
              padding: 'var(--lumo-space-s)',
              borderRadius: 'var(--lumo-border-radius-m)',
            }}
          ></div>
          <VerticalLayout theme="spacing-xs">
            <span className="text-2xl font-bold text-primary">{stats.todayTotal}</span>
            <span className="text-s text-secondary">Total Hoy</span>
          </VerticalLayout>
        </Card>

        <Button theme="primary large" onClick={() => setAddDialogOpen(true)} style={{ marginLeft: 'auto' }}>
          ➕ Agregar Paciente
        </Button>
      </HorizontalLayout>

      <Dialog
        opened={addDialogOpen}
        onOpenedChanged={(e) => setAddDialogOpen(e.detail.value)}
        headerTitle="Agregar Paciente a Sala de Espera"
        footerRenderer={() => (
          <HorizontalLayout theme="spacing" className="justify-end">
            <Button onClick={() => setAddDialogOpen(false)}>Cancelar</Button>
            <Button theme="primary" onClick={handleAddToWaitingRoom}>
              ➕ Agregar a Sala de Espera
            </Button>
          </HorizontalLayout>
        )}
      >
        <VerticalLayout theme="spacing" style={{ minWidth: '400px', padding: 'var(--lumo-space-m)' }}>
          <h4 className="m-0 text-primary">Seleccionar Cliente y Mascota</h4>

          <ComboBox
            label="Cliente *"
            placeholder="Buscar y seleccionar cliente..."
            items={clientItems}
            itemLabelPath="label"
            itemValuePath="value"
            value={selectedClientId}
            onValueChanged={(e) => {
              setSelectedClientId(e.detail.value)
              if (e.detail.value) {
                loadPetsForClient(Number.parseInt(e.detail.value))
              } else {
                setPets([])
              }
              setSelectedPetId('')
            }}
            clearButtonVisible
          />

          <ComboBox
            label="Mascota *"
            placeholder="Seleccionar mascota del cliente..."
            items={petItems}
            itemLabelPath="label"
            itemValuePath="value"
            value={selectedPetId}
            onValueChanged={(e) => setSelectedPetId(e.detail.value)}
            disabled={!selectedClientId}
            clearButtonVisible
          />

          <h4 className="m-0 text-primary" style={{ marginTop: 'var(--lumo-space-m)' }}>
            Detalles de la Visita
          </h4>

          <TextField
            label="Motivo de la visita *"
            placeholder="Ej: Consulta general, vacunación, revisión..."
            value={reasonForVisit}
            onValueChanged={(e) => setReasonForVisit(e.detail.value)}
            clearButtonVisible
          />

          <ComboBox
            label="Nivel de Prioridad"
            items={priorityItems}
            itemLabelPath="label"
            itemValuePath="value"
            value={priority}
            onValueChanged={(e) => setPriority(e.detail.value as Priority)}
          />

          <TextArea
            label="Notas adicionales"
            placeholder="Información adicional sobre la condición del paciente, síntomas observados, etc..."
            value={notes}
            onValueChanged={(e) => setNotes(e.detail.value)}
            style={{ minHeight: '80px' }}
          />
        </VerticalLayout>
      </Dialog>
      <VerticalLayout theme="spacing" className="flex-1 overflow-auto p-m">
        {waitingList.length === 0 ? (
          <Card className="flex flex-col items-center justify-center p-xl text-center">
            <div style={{ fontSize: '4rem', marginBottom: 'var(--lumo-space-m)' }}>🏥</div>
            <h2 className="m-0 text-primary">Sala de espera vacía</h2>
            <span className="text-secondary">No hay pacientes esperando en este momento</span>
            <Button theme="primary" onClick={() => setAddDialogOpen(true)} style={{ marginTop: 'var(--lumo-space-m)' }}>
              ➕ Agregar Primer Paciente
            </Button>
          </Card>
        ) : (
          waitingList.map((entry) => {
            const statusDisplay = getStatusDisplay(entry.status!)
            const priorityDisplay = getPriorityDisplay(entry.priority!)

            return (
              <Card
                key={entry.id}
                className="p-m"
                style={{
                  borderLeft: `4px solid ${
                    entry.status === WaitingRoomStatus.ESPERANDO
                      ? 'var(--lumo-warning-color)'
                      : entry.status === WaitingRoomStatus.EN_CONSULTA
                        ? 'var(--lumo-primary-color)'
                        : entry.status === WaitingRoomStatus.COMPLETADO
                          ? 'var(--lumo-success-color)'
                          : 'var(--lumo-error-color)'
                  }`,
                }}
              >
                <HorizontalLayout theme="spacing" className="w-full justify-between mb-m">
                  <span className={priorityDisplay.theme}>
                    {priorityDisplay.icon} {priorityDisplay.text}
                  </span>
                  <span className={statusDisplay.theme}>
                    {statusDisplay.icon} {statusDisplay.text}
                  </span>
                </HorizontalLayout>

                <HorizontalLayout theme="spacing" className="w-full mb-m">
                  <VerticalLayout theme="spacing-xs" className="flex-1">
                    <h3 className="m-0 text-primary">
                      {entry.client?.firstName} {entry.client?.lastName}
                    </h3>
                    <span className="text-s text-secondary">
                      {entry.client?.cedula && `Cédula: ${entry.client.cedula}`}
                      {entry.client?.phoneNumber && ` • 📞 ${entry.client.phoneNumber}`}
                      {entry.client?.email && ` • ✉ ${entry.client.email}`}
                    </span>
                  </VerticalLayout>

                  <VerticalLayout theme="spacing-xs" className="flex-1">
                    <h3 className="m-0 text-primary">🐾 {entry.pet?.name}</h3>
                    <span className="text-s text-secondary">
                      {entry.pet?.type} {entry.pet?.breed && `- ${entry.pet.breed}`}
                      {entry.pet?.gender && ` • ${entry.pet.gender}`}
                    </span>
                  </VerticalLayout>
                </HorizontalLayout>

                <VerticalLayout theme="spacing-xs" className="mb-m">
                  <HorizontalLayout theme="spacing" className="w-full">
                    <span className="flex-1">
                      <strong>Motivo de la visita:</strong> {entry.reasonForVisit}
                    </span>
                  </HorizontalLayout>

                  <HorizontalLayout theme="spacing" className="w-full">
                    <span className="text-s text-secondary flex-1">
                      <strong>Hora de llegada:</strong> {formatTime(entry.arrivalTime!)}
                    </span>
                    <span className="text-s text-secondary">
                      <strong>Tiempo esperando:</strong> {getWaitTime(entry.arrivalTime!)}
                    </span>
                  </HorizontalLayout>

                  {entry.notes && (
                    <span className="text-s text-secondary">
                      <strong>Notas:</strong> {entry.notes}
                    </span>
                  )}
                </VerticalLayout>

                <HorizontalLayout theme="spacing" className="justify-end">
                  {entry.status === WaitingRoomStatus.ESPERANDO && (
                    <>
                      <Button theme="primary small" onClick={() => handleMoveToConsultation(entry.id!)}>
                        Iniciar Consulta
                      </Button>
                      <Button theme="error small" onClick={() => handleCancelEntry(entry.id!)}>
                        Cancelar
                      </Button>
                    </>
                  )}
                  {entry.status === WaitingRoomStatus.EN_CONSULTA && (
                    <Button theme="success small" onClick={() => handleCompleteConsultation(entry.id!)}>
                      Completar Consulta
                    </Button>
                  )}
                  {(entry.status === WaitingRoomStatus.COMPLETADO || entry.status === WaitingRoomStatus.CANCELADO) && (
                    <span className="text-s text-secondary">
                      {entry.status === WaitingRoomStatus.COMPLETADO ? 'Consulta finalizada' : 'Visita cancelada'}
                    </span>
                  )}
                </HorizontalLayout>
              </Card>
            )
          })
        )}
      </VerticalLayout>
    </main>
  )
}

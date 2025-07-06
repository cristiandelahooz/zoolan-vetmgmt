import AppointmentStatus from '@/generated/com/wornux/data/enums/AppointmentStatus'
import ServiceType from '@/generated/com/wornux/data/enums/ServiceType'
import type AppointmentUpdateDTO from '@/generated/com/wornux/dto/request/AppointmentUpdateRequestDto'
import type AppointmentResponseDTO from '@/generated/com/wornux/dto/response/AppointmentResponseDto'
import { useAppointments } from '@/stores/useAppointments'
import { SelectClientDialog, type SelectedClient } from '@/views/clients/_SelectClientDialog'
import { SelectPetDialog, type SelectedPet } from '@/views/consultations/_SelectPetDialog'
import { Button, ComboBox, DateTimePicker, Dialog, TextArea, TextField } from '@vaadin/react-components'
import { useEffect, useState } from 'react'
import { Controller, useForm } from 'react-hook-form'

interface EditAppointmentModalProps {
  appointment: AppointmentResponseDTO | null
  isOpen: boolean
  onClose: (isSuccess: boolean) => void
}

const mapAppointmentToFormData = (appointment: AppointmentResponseDTO | null) => {
  if (!appointment) return {}

  return {
    startAppointmentDate: appointment.startAppointmentDate,
    endAppointmentDate: appointment.endAppointmentDate,
    serviceType: appointment.serviceType,
    status: appointment.status,
    reason: appointment.reason,
    notes: appointment.notes,
    guestClientInfo: appointment.guestClientInfo,
  }
}

const transformDateForField = (value: string | null) => {
  const date = value ? new Date(value) : null
  return date ? date.toISOString() : ''
}

const parseClientName = (clientName: string | null | undefined): SelectedClient | null => {
  if (!clientName) return null
  return { firstName: clientName, lastName: '' }
}

const parsePetName = (petName: string | null | undefined, breed: string | undefined): SelectedPet | null => {
  if (!petName) return null
  return { name: petName, breed: breed }
}

export function EditAppointmentModal({ appointment, isOpen, onClose }: Readonly<EditAppointmentModalProps>) {
  const {
    handleSubmit,
    reset,
    control,
    setValue,
    formState: { errors },
  } = useForm<AppointmentUpdateDTO>({
    defaultValues: mapAppointmentToFormData(appointment),
  })

  const [isClientSelectorOpen, setIsClientSelectorOpen] = useState(false)
  const [selectedClient, setSelectedClient] = useState<SelectedClient | null>(null)
  const [isPetSelectorOpen, setIsPetSelectorOpen] = useState(false)
  const [selectedPet, setSelectedPet] = useState<SelectedPet | null>(null)
  const { updateAppointment } = useAppointments()

  useEffect(() => {
    if (appointment) {
      reset(mapAppointmentToFormData(appointment))
      setSelectedClient(parseClientName(appointment.clientName))
      setSelectedPet(parsePetName(appointment.petName, appointment.petBreed))
    }
  }, [appointment, reset])

  const onSubmit = async (data: AppointmentUpdateDTO) => {
    if (!appointment?.eventId) return
    try {
      await updateAppointment(appointment.eventId, data)
      onClose(true)
      reset()
    } catch (error: any) {
      console.error('Failed to update appointment:', error)
      onClose(false)
    }
  }

  const handleClientSelection = (client: SelectedClient) => {
    setSelectedClient(client)
    setValue('clientId', client.id)
    setIsClientSelectorOpen(false)
  }

  const handlePetSelection = (pet: SelectedPet) => {
    setSelectedPet(pet)
    setValue('petId', pet.id)
    setIsPetSelectorOpen(false)
  }

  if (!isOpen) return null

  return (
    <>
      <Dialog
        headerTitle="Edit Appointment"
        opened={isOpen}
        onOpenedChanged={({ detail }) => !detail.value && onClose(false)}
        footer={
          <div className="flex gap-s">
            <Button onClick={() => onClose(false)}>Cancel</Button>
            <Button theme="primary" onClick={handleSubmit(onSubmit)}>
              Save Changes
            </Button>
          </div>
        }
      >
        <form className="flex flex-col gap-m">
          <Controller
            name="startAppointmentDate"
            control={control}
            rules={{ required: 'Start time is required' }}
            render={({ field }) => (
              <DateTimePicker
                label="Appointment Start Time"
                value={field.value}
                onChange={(e) => field.onChange(transformDateForField(e.target.value))}
                invalid={!!errors.startAppointmentDate}
                errorMessage={errors.startAppointmentDate?.message}
              />
            )}
          />
          <Controller
            name="endAppointmentDate"
            control={control}
            rules={{ required: 'End time is required' }}
            render={({ field }) => (
              <DateTimePicker
                label="Appointment End Time"
                value={field.value}
                onChange={(e) => field.onChange(transformDateForField(e.target.value))}
                invalid={!!errors.endAppointmentDate}
                errorMessage={errors.endAppointmentDate?.message}
              />
            )}
          />
          <Controller
            name="serviceType"
            control={control}
            rules={{ required: 'Service type is required' }}
            render={({ field }) => (
              <ComboBox
                label="Service Type"
                items={Object.values(ServiceType)}
                itemLabelPath="displayName"
                itemValuePath="name"
                {...field}
                invalid={!!errors.serviceType}
                errorMessage={errors.serviceType?.message}
              />
            )}
          />
          <Controller
            name="status"
            control={control}
            render={({ field }) => (
              <ComboBox
                label="Status"
                items={Object.values(AppointmentStatus)}
                itemLabelPath="displayName"
                itemValuePath="name"
                {...field}
              />
            )}
          />
          <TextField label="Reason" {...control.register('reason')} />
          <TextArea label="Notes" {...control.register('notes')} />
          <div className="flex gap-s items-end">
            <TextField
              label="Selected Client"
              readonly
              value={selectedClient ? `${selectedClient.firstName} ${selectedClient.lastName}` : ''}
            />
            <Button onClick={() => setIsClientSelectorOpen(true)}>Select Client</Button>
          </div>
          <div className="flex gap-s items-end">
            <TextField label="Pet" readonly value={selectedPet ? `${selectedPet.name} (${selectedPet.breed})` : ''} />
            <Button onClick={() => setIsPetSelectorOpen(true)}>Select Pet</Button>
          </div>
        </form>
      </Dialog>
      <SelectClientDialog
        open={isClientSelectorOpen}
        onClose={() => setIsClientSelectorOpen(false)}
        onSelect={handleClientSelection}
      />
      <SelectPetDialog
        open={isPetSelectorOpen}
        onClose={() => setIsPetSelectorOpen(false)}
        onSelect={handlePetSelection}
      />
    </>
  )
}

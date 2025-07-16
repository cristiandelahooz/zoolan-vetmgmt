import AppointmentStatus from '@/generated/com/wornux/data/enums/AppointmentStatus'
import ServiceType from '@/generated/com/wornux/data/enums/ServiceType'
import type AppointmentCreateDTO from '@/generated/com/wornux/dto/request/AppointmentCreateRequestDto'
import { useAppointments } from '@/stores/useAppointments'
import { SelectClientDialog, type SelectedClient } from '@/views/clients/_SelectClientDialog'
import { SelectPetDialog, type SelectedPet } from '@/views/consultations/_SelectPetDialog'
import { Button, ComboBox, DateTimePicker, Dialog, TextArea, TextField } from '@vaadin/react-components'
import { useEffect, useState } from 'react'
import { Controller, useForm } from 'react-hook-form'

interface CreateAppointmentModalProps {
  isOpen: boolean
  onClose: (isSuccess: boolean) => void
  selectedDate: Date | null
}

export function CreateAppointmentModal({ isOpen, onClose, selectedDate }: Readonly<CreateAppointmentModalProps>) {
  const ONE_HOUR_IN_MILLIS = 60 * 60 * 1000

  const {
    register,
    handleSubmit,
    reset,
    control,
    setValue,
    formState: { errors },
  } = useForm<AppointmentCreateDTO>({
    defaultValues: {
      startAppointmentDate: selectedDate ? selectedDate.toISOString().slice(0, 16) : '',
      endAppointmentDate: selectedDate
        ? new Date(selectedDate.getTime() + ONE_HOUR_IN_MILLIS).toISOString().slice(0, 16)
        : '',
    },
  })

  useEffect(() => {
    const startDateTime = selectedDate ? selectedDate.toISOString().slice(0, 16) : ''
    const endDateTime = selectedDate
      ? new Date(selectedDate.getTime() + ONE_HOUR_IN_MILLIS).toISOString().slice(0, 16)
      : ''
    reset({
      startAppointmentDate: startDateTime,
      endAppointmentDate: endDateTime,
      serviceType: ServiceType.CONSULTA_GENERAL,
    })
  }, [selectedDate, reset])
  const [isClientSelectorOpen, setIsClientSelectorOpen] = useState(false)
  const [selectedClient, setSelectedClient] = useState<SelectedClient | null>(null)
  const [isPetSelectorOpen, setIsPetSelectorOpen] = useState(false)
  const [selectedPet, setSelectedPet] = useState<SelectedPet | null>(null)
  const { createAppointment } = useAppointments()

  const onSubmit = async (data: AppointmentCreateDTO) => {
    try {
      await createAppointment(data)
      onClose(true)
      reset()
    } catch (error) {
      console.error('Failed to create appointment:', error)
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
        headerTitle="Create New Appointment"
        opened={isOpen}
        onOpenedChanged={({ detail }) => !detail.value && onClose(false)}
        footer={
          <div className="flex gap-s backdrop-blur-md">
            <Button onClick={() => onClose(false)}>Cancel</Button>
            <Button theme="primary" onClick={handleSubmit(onSubmit)}>
              Create
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
                key={selectedDate?.getTime() || 'initial'}
                label="Appointment Start Time"
                value={field.value}
                onChange={(e) => {
                  const date = e.target.value ? new Date(e.target.value) : null
                  field.onChange(date ? date.toISOString() : '')
                }}
                invalid={!!errors.startAppointmentDate}
                errorMessage={errors.startAppointmentDate?.message}
                date-readonly="true"
              />
            )}
          />
          <Controller
            name="endAppointmentDate"
            control={control}
            rules={{ required: 'End time is required' }}
            render={({ field }) => (
              <DateTimePicker
                key={selectedDate?.getTime() || 'initial'}
                label="Appointment End Time"
                value={field.value}
                onChange={(e) => {
                  const date = e.target.value ? new Date(e.target.value) : null
                  field.onChange(date ? date.toISOString() : '')
                }}
                invalid={!!errors.endAppointmentDate}
                errorMessage={errors.endAppointmentDate?.message}
                date-readonly="true"
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
          <TextField label="Reason" {...register('reason')} />

          <TextArea label="Notes" {...register('notes')} />

          <div className="flex gap-s items-end">
            <TextField
              label="Selected Client"
              readonly
              value={selectedClient ? `${selectedClient.firstName} ${selectedClient.lastName}` : ''}
            />
            <Button onClick={() => setIsClientSelectorOpen(true)}>Select Client</Button>
          </div>

          <div className="flex gap-s items-end">
            <TextField
              label="Selected Pet"
              readonly
              value={selectedPet ? `${selectedPet.name} (${selectedPet.breed})` : ''}
            />
            <Button onClick={() => setIsPetSelectorOpen(true)}>Seleccionar Mascota</Button>
          </div>
        </form>
      </Dialog>
      <SelectClientDialog
        open={isClientSelectorOpen}
        onClose={() => {
          setIsClientSelectorOpen(false)
          setSelectedClient(null)
        }}
        onSelect={handleClientSelection}
      />
      <SelectPetDialog
        open={isPetSelectorOpen}
        onClose={() => {
          setIsPetSelectorOpen(false)
          setSelectedPet(null)
        }}
        onSelect={handlePetSelection}
      />
    </>
  )
}

import AppointmentStatus from '@/generated/com/wornux/data/enums/AppointmentStatus'
import ServiceType from '@/generated/com/wornux/data/enums/ServiceType'
import type AppointmentUpdateDTO from '@/generated/com/wornux/dto/request/AppointmentUpdateRequestDto'
import { useAppointments } from '@/stores/useAppointments'
import { useEmployees } from '@/stores/useEmployees'
import { usePets } from '@/stores/usePets'
import { SelectClientDialog, type SelectedClient } from '@/views/clients/_SelectClientDialog'
import { Button, ComboBox, DateTimePicker, Dialog, TextArea, TextField } from '@vaadin/react-components'
import { useEffect, useState } from 'react'
import { Controller, useForm } from 'react-hook-form'

interface EditAppointmentModalProps {
  appointment: AppointmentUpdateDTO | null
  isOpen: boolean
  onClose: (isSuccess: boolean) => void
}

export function EditAppointmentModal({ appointment, isOpen, onClose }: Readonly<EditAppointmentModalProps>) {
  const {
    handleSubmit,
    reset,
    control,
    setValue,
    formState: { errors },
  } = useForm<AppointmentUpdateDTO>({
    defaultValues: appointment
      ? {
          startAppointmentDate: appointment.startAppointmentDate,
          endAppointmentDate: appointment.endAppointmentDate,
          serviceType: appointment.serviceType,
          status: appointment.status,
          reason: appointment.reason,
          notes: appointment.notes,
          clientId: appointment.clientId ?? undefined,
          petId: appointment.petId,
          assignedEmployeeId: appointment.assignedEmployeeId,
          guestClientInfo: appointment.guestClientInfo,
        }
      : {},
  })
  const [isClientSelectorOpen, setIsClientSelectorOpen] = useState(false)
  const [selectedClient, setSelectedClient] = useState<SelectedClient | null>(null)
  const { pets, fetchPets } = usePets()
  const { employees } = useEmployees()
  const { updateAppointment } = useAppointments()

  useEffect(() => {
    if (appointment?.clientId) {
      // Assuming you have a way to fetch client details by ID
      // For now, we'll just set a placeholder selectedClient
      setSelectedClient({ id: appointment.clientId })
    }
  }, [appointment])

  useEffect(() => {
    if (selectedClient?.id) {
      fetchPets(selectedClient.id)
    }
  }, [selectedClient, fetchPets])

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
                onChange={(e) => {
                  const date = e.target.value ? new Date(e.target.value) : null
                  field.onChange(date ? date.toISOString() : '')
                }}
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
                onChange={(e) => {
                  const date = e.target.value ? new Date(e.target.value) : null
                  field.onChange(date ? date.toISOString() : '')
                }}
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
          <Controller
            name="petId"
            control={control}
            render={({ field }) => (
              <ComboBox
                label="Pet"
                items={pets}
                itemLabelPath="name"
                itemValuePath="id"
                {...(field as any)}
                disabled={!selectedClient}
              />
            )}
          />
          <Controller
            name="assignedEmployeeId"
            control={control}
            render={({ field }) => (
              <ComboBox
                label="Assign to Employee"
                items={employees}
                itemLabelPath="fullName"
                itemValuePath="id"
                {...(field as any)}
              />
            )}
          />
        </form>
      </Dialog>
      <SelectClientDialog
        open={isClientSelectorOpen}
        onClose={() => setIsClientSelectorOpen(false)}
        onSelect={handleClientSelection}
      />
    </>
  )
}

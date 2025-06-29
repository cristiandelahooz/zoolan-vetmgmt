import { ClientComboBox } from '@/components/ui/ClientComboBox'
import type AppointmentResponseDTO from '@/generated/com/zoolandia/app/features/appointments/dtos/AppointmentResponseDTO'
import type AppointmentUpdateDTO from '@/generated/com/zoolandia/app/features/appointments/dtos/AppointmentUpdateDTO'
import { useAppointments } from '@/stores/useAppointments'
import { Button, Dialog, TextField } from '@vaadin/react-components'
import { Controller, useForm } from 'react-hook-form'

interface EditAppointmentModalProps {
  appointment: AppointmentResponseDTO | null
  isOpen: boolean
  onClose: () => void
}

export function EditAppointmentModal({ appointment, isOpen, onClose }: EditAppointmentModalProps) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
    control,
  } = useForm<AppointmentUpdateDTO>({
    defaultValues: appointment
      ? {
          ...appointment,
          start: appointment.start ? new Date(appointment.start).toISOString().substring(0, 16) : '',
          end: appointment.end ? new Date(appointment.end).toISOString().substring(0, 16) : '',
        }
      : {},
  })
  const { updateAppointment } = useAppointments()

  const onSubmit = async (data: AppointmentUpdateDTO) => {
    if (appointment) {
      try {
        await updateAppointment(appointment.id as number, data)
        onClose()
        reset()
      } catch (error) {
        console.error('Failed to update appointment:', error)
      }
    }
  }

  if (!isOpen) return null

  return (
    <Dialog
      headerTitle="Edit Appointment"
      opened={isOpen}
      onOpenedChanged={({ detail }) => !detail.value && onClose()}
      footer={
        <div className="flex gap-s">
          <Button onClick={onClose}>Cancel</Button>
          <Button theme="primary" onClick={handleSubmit(onSubmit)}>
            Save Changes
          </Button>
        </div>
      }
    >
      <form className="flex flex-col gap-m">
        <TextField
          label="Appointment Date Time"
          type="datetime-local"
          {...register('appointmentDateTime', { required: 'Appointment date is required' })}
          error-text={errors.appointmentDateTime?.message}
        />
        <Controller
          name="clientId"
          control={control}
          rules={{ required: 'Client is required' }}
          render={({ field }) => (
            <ClientComboBox
              label="Client"
              value={field.value?.toString()}
              onChange={field.onChange}
              error={errors.clientId?.message}
            />
          )}
        />
        <TextField
          label="Start Time"
          type="datetime-local"
          {...register('start', { required: 'Start time is required' })}
          error-text={errors.start?.message}
        />
        <TextField
          label="End Time"
          type="datetime-local"
          {...register('end', { required: 'End time is required' })}
          error-text={errors.end?.message}
        />
        {/* Add more fields as necessary for your AppointmentUpdateDTO */}
      </form>
    </Dialog>
  )
}

import { ClientComboBox } from '@/components/ui/ClientComboBox'
import type AppointmentResponseDTO from '@/generated/com/wornux/features/appointments/dtos/AppointmentResponseDTO'
import type AppointmentUpdateDTO from '@/generated/com/wornux/features/appointments/dtos/AppointmentUpdateDTO'
import { useAppointments } from '@/stores/useAppointments'
import { Button, Dialog, TextField, DateTimePicker } from '@vaadin/react-components'
import { Controller, useForm } from 'react-hook-form'

interface EditAppointmentModalProps {
  appointment: AppointmentResponseDTO | null
  isOpen: boolean
  onClose: (isSuccess: boolean) => void
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
          startAppointmentDate: appointment.startAppointmentDate,
          endAppointmentDate: appointment.endAppointmentDate,
          // Add other fields from AppointmentResponseDTO to AppointmentUpdateDTO
        }
      : {},
  })
  const { updateAppointment } = useAppointments()

  const onSubmit = async (data: AppointmentUpdateDTO) => {
    if (!appointment?.eventId) return
    try {
      await updateAppointment(appointment.eventId, data)
      onClose(true)
      reset()
    } catch (error) {
      console.error('Failed to update appointment:', error)
      onClose(false)
    }
  }

  if (!isOpen) return null

  return (
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
        <DateTimePicker
          label="Appointment Start Time"
          {...(register('startAppointmentDate', { required: 'Start time is required' }) as any)}
          invalid={!!errors.startAppointmentDate}
          errorMessage={errors.startAppointmentDate?.message}
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
        <DateTimePicker
          label="Appointment End Time"
          {...(register('endAppointmentDate', { required: 'End time is required' }) as any)}
          invalid={!!errors.endAppointmentDate}
          errorMessage={errors.endAppointmentDate?.message}
        />
        {/* Add more fields as necessary for your AppointmentUpdateDTO */}
      </form>
    </Dialog>
  )
}

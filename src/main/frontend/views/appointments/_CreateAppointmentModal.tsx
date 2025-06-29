import { ClientComboBox } from '@/components/ui/ClientComboBox'
import type AppointmentCreateDTO from '@/generated/com/zoolandia/app/features/appointments/dtos/AppointmentCreateDTO'
import { AppointmentServiceImpl } from '@/generated/endpoints'
import { Button, Dialog, TextField } from '@vaadin/react-components'
import { Controller, useForm } from 'react-hook-form'

interface CreateAppointmentModalProps {
  isOpen: boolean
  onClose: () => void
  selectedDate: Date | null
}

export function CreateAppointmentModal({ isOpen, onClose, selectedDate }: CreateAppointmentModalProps) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
    control,
  } = useForm<AppointmentCreateDTO>()

  const onSubmit = async (data: AppointmentCreateDTO) => {
    try {
      await AppointmentServiceImpl.createAppointment(data)
      onClose()
      reset()
    } catch (error) {
      console.error('Failed to create appointment:', error)
    }
  }

  if (!isOpen) return null

  return (
    <Dialog
      headerTitle="Create New Appointment"
      opened={isOpen}
      onOpenedChanged={({ detail }) => !detail.value && onClose()}
      footer={
        <div className="flex gap-s">
          <Button onClick={onClose}>Cancel</Button>
          <Button theme="primary" onClick={handleSubmit(onSubmit)}>
            Create
          </Button>
        </div>
      }
    >
      <form className="flex flex-col gap-m">
        <TextField
          label="Appointment Date Time"
          type="datetime-local"
          defaultValue={selectedDate?.toISOString().substring(0, 16)}
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
          defaultValue={selectedDate?.toISOString().substring(0, 16)}
          {...register('start', { required: 'Start time is required' })}
          error-text={errors.start?.message}
        />
        <TextField
          label="End Time"
          type="datetime-local"
          {...register('end', { required: 'End time is required' })}
          error-text={errors.end?.message}
        />
        {/* Add more fields as necessary for your AppointmentCreateDTO */}
      </form>
    </Dialog>
  )
}

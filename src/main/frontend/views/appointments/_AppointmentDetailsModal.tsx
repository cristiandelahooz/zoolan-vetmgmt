import type AppointmentResponseDTO from '@/generated/com/zoolandia/app/features/appointments/dtos/AppointmentResponseDTO'
import { useAppointments } from '@/stores/useAppointments'
import { Button, Dialog } from '@vaadin/react-components'

interface AppointmentDetailsModalProps {
  appointment: AppointmentResponseDTO | null
  isOpen: boolean
  onClose: () => void
  onEdit: (appointment: AppointmentResponseDTO) => void
}

export function AppointmentDetailsModal({ appointment, isOpen, onClose, onEdit }: AppointmentDetailsModalProps) {
  const handleDelete = async () => {
    if (appointment) {
      onClose();
    }
  };

  if (!isOpen || !appointment) return null

  return (
    <Dialog
      headerTitle="Appointment Details"
      opened={isOpen}
      onOpenedChanged={({ detail }) => !detail.value && onClose()}
      footer={
        <div className="flex gap-s justify-between">
          <Button theme="error" onClick={handleDelete}>
            Delete
          </Button>
          <div>
            <Button onClick={onClose}>Close</Button>
            <Button theme="primary" onClick={() => onEdit(appointment)}>
              Edit
            </Button>
          </div>
        </div>
      }
    >
      <div>
        <p>
          <strong>Servicio:</strong> {appointment.serviceType}
        </p>
        <p>
          <strong>Status:</strong> {appointment.status}
        </p>
        <p>
          <strong>Fecha:</strong> {new Date(appointment.appointmentDateTime as string).toLocaleString()}
        </p>
        {/* Display more appointment details as needed */}
      </div>
    </Dialog>
  )
}

import type AppointmentCreateDTO
    from '@/generated/com/zoolandia/app/features/appointments/dtos/AppointmentCreateDTO'
import {AppointmentServiceImpl} from '@/generated/endpoints'
import {
    Button, Dialog, DateTimePicker
} from '@vaadin/react-components'
import {useForm} from 'react-hook-form'
import {SelectClientDialog} from "@/views/clients/_SelectClientDialog";

interface CreateAppointmentModalProps {
    isOpen: boolean
    onClose: () => void
    selectedDate: Date | null
}

export function CreateAppointmentModal({
                                           isOpen, onClose, selectedDate
                                       }: CreateAppointmentModalProps) {
    const {
        register, handleSubmit, reset, formState: {errors},
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

    function handleClientSelect() {

    }

    return (<Dialog
        headerTitle="Create New Appointment"
        opened={isOpen}
        onOpenedChanged={({detail}) => !detail.value && onClose()}
        footer={<div className="flex gap-s">
            <Button onClick={onClose}>Cancel</Button>
            <Button theme="primary" onClick={handleSubmit(onSubmit)}>
                Create
            </Button>
        </div>}
    >
        <form className="flex flex-col gap-m">
            <DateTimePicker
                label="Appointment Date Time"
                defaultValue={selectedDate?.toISOString().substring(0, 16)}
                {...register('appointmentDateTime', {required: 'Appointment date is required'})}
                error-text={errors.appointmentDateTime?.message}
            />
            <SelectClientDialog onSelect={handleClientSelect}
                                onClose={onClose} open={true}/>
            <DateTimePicker
                label="Start Time"
                defaultValue={selectedDate?.toISOString().substring(0, 16)}
                {...register('startAppointmentDate', {required: 'Start time is required'})}
                error-text={errors.startAppointmentDate?.message}
            />
            <DateTimePicker
                label="End Time"
                {...register('endAppointmentDate', {required: 'End time is required'})}
                error-text={errors.endAppointmentDate?.message}
            />
            {/* Add more fields as necessary for your AppointmentCreateDTO */}
        </form>
    </Dialog>)
}

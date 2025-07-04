import type AppointmentCreateDTO from '@/generated/com/wornux/features/appointments/dtos/AppointmentCreateDTO';
import { AppointmentServiceImpl } from '@/generated/endpoints';
import {
  Button,
  ComboBox,
  DateTimePicker,
  Dialog,
  TextArea,
  TextField,
} from '@vaadin/react-components';
import { useForm, Controller } from 'react-hook-form';
import { SelectClientDialog, type SelectedClient } from '@/views/clients/_SelectClientDialog';
import { useEffect, useState } from 'react';
import { usePets } from '@/stores/usePets';
import { useEmployees } from '@/stores/useEmployees';
import ServiceType from '@/generated/com/wornux/features/appointments/domain/ServiceType';
import AppointmentStatus from '@/generated/com/wornux/features/appointments/domain/AppointmentStatus';

interface CreateAppointmentModalProps {
  isOpen: boolean;
  onClose: (isSuccess: boolean) => void;
  selectedDate: Date | null;
}

export function CreateAppointmentModal({ isOpen, onClose, selectedDate }: CreateAppointmentModalProps) {
  const { register, handleSubmit, reset, control, setValue, formState: { errors } } = useForm<AppointmentCreateDTO>();
  const [isClientSelectorOpen, setIsClientSelectorOpen] = useState(false);
  const [selectedClient, setSelectedClient] = useState<SelectedClient | null>(null);
  const { pets, fetchPets } = usePets();
  const { employees } = useEmployees();

  useEffect(() => {
    if (selectedClient?.id) {
      fetchPets(selectedClient.id);
    }
  }, [selectedClient, fetchPets]);

  const onSubmit = async (data: AppointmentCreateDTO) => {
    try {
      await AppointmentServiceImpl.createAppointment(data);
      onClose(true);
      reset();
    } catch (error) {
      console.error('Failed to create appointment:', error);
      onClose(false);
    }
  };

  const handleClientSelection = (client: SelectedClient) => {
    setSelectedClient(client);
    setValue('clientId', client.id);
    setIsClientSelectorOpen(false);
  };

  if (!isOpen) return null;

  return (
    <>
      <Dialog
        headerTitle="Create New Appointment"
        opened={isOpen}
        onOpenedChanged={({ detail }) => !detail.value && onClose(false)}
        footer={
          <div className="flex gap-s">
            <Button onClick={() => onClose(false)}>Cancel</Button>
            <Button theme="primary" onClick={handleSubmit(onSubmit)}>
              Create
            </Button>
          </div>
        }
      >
        <form className="flex flex-col gap-m">
          <DateTimePicker
            label="Appointment Start Time"
            defaultValue={selectedDate?.toISOString().substring(0, 16)}
            {...(register('startAppointmentDate', { required: 'Start time is required' }) as any)}
            invalid={!!errors.startAppointmentDate}
            errorMessage={errors.startAppointmentDate?.message}
          />
          <DateTimePicker
            label="Appointment End Time"
            {...(register('endAppointmentDate', { required: 'End time is required' }) as any)}
            invalid={!!errors.endAppointmentDate}
            errorMessage={errors.endAppointmentDate?.message}
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
          <TextField
            label="Reason"
            {...register('reason')}
          />
          <TextArea
            label="Notes"
            {...register('notes')}
          />
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
  );
}

import { AutoGrid } from "@vaadin/hilla-react-crud";
import { Dialog, TextField, Button } from '@vaadin/react-components';
import { useState } from 'react';
import { EmployeeServiceImpl } from 'Frontend/generated/endpoints';
import EmployeeModel from "Frontend/generated/com/zoolandia/app/features/employee/domain/EmployeeModel";

export interface SelectedVeterinarian {
    id: number;
    firstName: string;
    lastName: string;
    specialization?: string;
    licenseNumber?: string;
}

interface SelectVeterinarianDialogProps {
    isOpen: boolean;
    onDialogClose: () => void;
    onVeterinarianSelect: (veterinarian: SelectedVeterinarian) => void;
}

const createVeterinarianFilteredService = () => ({
    list: async () => {
        try {
            const veterinarians = await EmployeeServiceImpl.getVeterinarians();
            return veterinarians || [];
        } catch (error) {
            console.error('Error fetching veterinarians:', error);
            return [];
        }
    },
    save: EmployeeServiceImpl.save,
    delete: EmployeeServiceImpl.delete
});

const formatVeterinarianDisplayName = (veterinarian: any): string => {
    const baseDisplayName = `Dr. ${veterinarian.firstName} ${veterinarian.lastName}`;
    return veterinarian.specialization
        ? `${baseDisplayName} - ${veterinarian.specialization}`
        : baseDisplayName;
};

const createSelectedVeterinarianFromEmployee = (employee: any): SelectedVeterinarian => ({
    id: employee.id,
    firstName: employee.firstName,
    lastName: employee.lastName,
    specialization: employee.specialization,
    licenseNumber: employee.licenseNumber
});

export function SelectVeterinarianDialog({
                                             isOpen,
                                             onDialogClose,
                                             onVeterinarianSelect
                                         }: SelectVeterinarianDialogProps) {
    const [currentSelectedVeterinarian, setCurrentSelectedVeterinarian] = useState<any>(null);
    const veterinarianFilteredService = createVeterinarianFilteredService();

    const handleVeterinarianSelection = (): void => {
        if (!currentSelectedVeterinarian) return;

        const selectedVeterinarian = createSelectedVeterinarianFromEmployee(currentSelectedVeterinarian);
        onVeterinarianSelect(selectedVeterinarian);
        onDialogClose();
        resetSelectedVeterinarian();
    };

    const handleDialogClose = (): void => {
        onDialogClose();
        resetSelectedVeterinarian();
    };

    const resetSelectedVeterinarian = (): void => {
        setCurrentSelectedVeterinarian(null);
    };

    const handleVeterinarianRowSelection = ({ detail }: any): void => {
        if (detail.value) {
            setCurrentSelectedVeterinarian(detail.value);
        }
    };

    const veterinarianGridColumnOptions = {
        firstName: { header: 'Nombre' },
        lastName: { header: 'Apellido' },
        employeeRole: { header: 'Rol' },
        specialization: { header: 'Especialización' },
        licenseNumber: { header: 'Número de Licencia' }
    };

    const visibleVeterinarianColumns = ['firstName', 'lastName', 'employeeRole', 'specialization', 'licenseNumber'];

    const selectedVeterinarianDisplayValue = currentSelectedVeterinarian
        ? formatVeterinarianDisplayName(currentSelectedVeterinarian)
        : '';

    const isSelectionConfirmationDisabled = !currentSelectedVeterinarian;

    return (
        <Dialog
            opened={isOpen}
            onOpenedChanged={({ detail }) => !detail.value && handleDialogClose()}
        >
            <div style={{ padding: '1rem', width: '800px', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <h3>Seleccionar Veterinario</h3>

                <AutoGrid
                    service={veterinarianFilteredService}
                    model={EmployeeModel}
                    columnOptions={veterinarianGridColumnOptions}
                    visibleColumns={visibleVeterinarianColumns}
                    onActiveItemChanged={handleVeterinarianRowSelection}
                    style={{ height: '400px' }}
                />

                <TextField
                    label="Veterinario seleccionado"
                    value={selectedVeterinarianDisplayValue}
                    readonly
                />

                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem' }}>
                    <Button
                        theme="tertiary"
                        onClick={handleDialogClose}
                    >
                        Cancelar
                    </Button>
                    <Button
                        theme="primary"
                        onClick={handleVeterinarianSelection}
                        disabled={isSelectionConfirmationDisabled}
                    >
                        Seleccionar
                    </Button>
                </div>
            </div>
        </Dialog>
    );
}
import {
    AutoForm,
    type AutoFormLayoutRendererProps
} from '@vaadin/hilla-react-crud'
import {AutoGrid} from "@vaadin/hilla-react-crud";
import {
    ConsultationServiceImpl,
    PetServiceImpl
} from 'Frontend/generated/endpoints'
import CreateConsultationDTOModel
    from "Frontend/generated/com/wornux/features/consultation/service/dto/CreateConsultationDTOModel"
import {useNavigate} from 'react-router'
import {
    HorizontalLayout,
    Notification,
    VerticalLayout,
    Grid,
    GridColumn,
    TextField,
    Button,
    Dialog
} from '@vaadin/react-components'
import type {SubmitErrorEvent} from '@vaadin/hilla-react-crud/'
import {useSignal} from '@vaadin/hilla-react-signals'
import {useState} from 'react'
import PetModel from "@/generated/com/wornux/features/pet/domain/PetModel";
import {
    SelectVeterinarianDialog,
    type SelectedVeterinarian
} from './_SelectVeterinarianDialog';
import {ROUTES} from "Frontend/lib/constants/routes";
import type CreateConsultationDTO
    from "@/generated/com/wornux/features/consultation/service/dto/CreateConsultationDTO";

const createPetListService = () => ({
    list: PetServiceImpl.list,
    save: PetServiceImpl.save,
    delete: PetServiceImpl.delete
});

export interface SelectedPet {
    id: number;
    name: string;
    type: string;
    breed: string;
    ownerName: string;
}

interface SelectPetDialogProps {
    isOpen: boolean;
    onDialogClose: () => void;
    onPetSelect: (pet: SelectedPet) => void;
}

const createSelectedPetFromGridItem = (item: any): SelectedPet => ({
    id: item.id,
    name: item.name,
    type: item.type,
    breed: item.breed,
    ownerName: `${item.owner.firstName} ${item.owner.lastName}`
});

const formatPetDisplayName = (pet: SelectedPet): string => `${pet.name} (${pet.type} - ${pet.breed})`;

const formatVeterinarianDisplayName = (veterinarian: SelectedVeterinarian): string => {
    const baseDisplayName = `Dr. ${veterinarian.firstName} ${veterinarian.lastName}`;
    return veterinarian.specialization ? `${baseDisplayName} - ${veterinarian.specialization}` : baseDisplayName;
};

const formatConsultationDateTime = (dateString: string): string => {
    try {
        const date = new Date(dateString);
        return date.toLocaleDateString('es-ES', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        });
    } catch {
        return dateString;
    }
};

export function SelectPetDialog({
                                    isOpen,
                                    onDialogClose,
                                    onPetSelect
                                }: SelectPetDialogProps) {
    const [currentSelectedPet, setCurrentSelectedPet] = useState<any>(null);
    const petListService = createPetListService();

    const handlePetSelection = (): void => {
        if (!currentSelectedPet) return;

        const selectedPet = createSelectedPetFromGridItem(currentSelectedPet);
        onPetSelect(selectedPet);
        onDialogClose();
        resetSelectedPet();
    };

    const handleDialogClose = (): void => {
        onDialogClose();
        resetSelectedPet();
    };

    const resetSelectedPet = (): void => {
        setCurrentSelectedPet(null);
    };

    const handlePetRowSelection = ({detail}: any): void => {
        if (detail.value) {
            setCurrentSelectedPet(detail.value);
        }
    };

    const petGridColumnOptions = {
        name: {header: 'Nombre'},
        type: {header: 'Tipo'},
        breed: {header: 'Raza'},
        'owner.firstName': {header: 'Propietario'},
        'owner.lastName': {header: 'Apellido'}
    };

    const visiblePetColumns = ['name', 'type', 'breed', 'owner.firstName', 'owner.lastName'];

    const selectedPetDisplayValue = currentSelectedPet ? formatPetDisplayName(createSelectedPetFromGridItem(currentSelectedPet)) : '';

    const isPetSelectionConfirmationDisabled = !currentSelectedPet;

    return (<Dialog opened={isOpen}
                    onOpenedChanged={({detail}) => !detail.value && handleDialogClose()}>
            <div style={{
                padding: '1rem',
                width: '800px',
                display: 'flex',
                flexDirection: 'column',
                gap: '1rem'
            }}>
                <h3>Seleccionar Mascota</h3>

                <AutoGrid
                    service={petListService}
                    model={PetModel}
                    columnOptions={petGridColumnOptions}
                    visibleColumns={visiblePetColumns}
                    onActiveItemChanged={handlePetRowSelection}
                    style={{height: '400px'}}
                />

                <TextField
                    label="Mascota seleccionada"
                    value={selectedPetDisplayValue}
                    readonly
                />

                <div style={{
                    display: 'flex',
                    justifyContent: 'flex-end',
                    gap: '0.5rem'
                }}>
                    <Button theme="tertiary" onClick={handleDialogClose}>
                        Cancelar
                    </Button>
                    <Button
                        theme="primary"
                        onClick={handlePetSelection}
                        disabled={isPetSelectionConfirmationDisabled}
                    >
                        Seleccionar
                    </Button>
                </div>
            </div>
        </Dialog>);
}

function ConsultationLayoutRenderer({children}: AutoFormLayoutRendererProps<CreateConsultationDTOModel>) {
    return (<VerticalLayout style={{alignItems: 'stretch'}}>
            <div
                style={{display: 'flex', flexDirection: 'column', gap: '1rem'}}>
                {children}
            </div>
        </VerticalLayout>);
}

export default function NewConsultationView() {
    const navigate = useNavigate();

    const [isPetDialogOpen, setIsPetDialogOpen] = useState(false);
    const [selectedPetDisplayName, setSelectedPetDisplayName] = useState('');
    const [selectedPetId, setSelectedPetId] = useState<number | undefined>();

    const [isVeterinarianDialogOpen, setIsVeterinarianDialogOpen] = useState(false);
    const [selectedVeterinarianDisplayName, setSelectedVeterinarianDisplayName] = useState('');
    const [selectedVeterinarianId, setSelectedVeterinarianId] = useState<number | undefined>();

    const [consultationFormKey, setConsultationFormKey] = useState(0);
    const [petConsultationHistory, setPetConsultationHistory] = useState<any[]>([]);

    const isConsultationHistoryLoading = useSignal(false);

    const loadPetConsultationHistory = async (petId: number): Promise<void> => {
        if (!petId) {
            setPetConsultationHistory([]);
            return;
        }

        isConsultationHistoryLoading.value = true;
        try {
            const consultationsList = await PetServiceImpl.getConsultationsByPetId(petId);
            setPetConsultationHistory(consultationsList || []);
        } catch (error) {
            console.error('Error loading consultation history:', error);
            setPetConsultationHistory([]);
        } finally {
            isConsultationHistoryLoading.value = false;
        }
    };

    const handlePetSelection = (selectedPet: SelectedPet): void => {
        const petDisplayName = formatPetDisplayName(selectedPet);
        setSelectedPetDisplayName(petDisplayName);
        setSelectedPetId(selectedPet.id);
        loadPetConsultationHistory(selectedPet.id);
        refreshConsultationForm();
    };

    const handleVeterinarianSelection = (selectedVeterinarian: SelectedVeterinarian): void => {
        const veterinarianDisplayName = formatVeterinarianDisplayName(selectedVeterinarian);
        setSelectedVeterinarianDisplayName(veterinarianDisplayName);
        setSelectedVeterinarianId(selectedVeterinarian.id);
        refreshConsultationForm();
    };

    const refreshConsultationForm = (): void => {
        setConsultationFormKey(prev => prev + 1);
    };

    const handleConsultationSubmitSuccess = ({item}: {
        item: CreateConsultationDTO
    }): void => {
        showSuccessNotification('Consulta creada exitosamente');
        if (selectedPetId) {
            loadPetConsultationHistory(selectedPetId);
        }
        navigate(ROUTES.CONSULTATIONS);
    };

    const handleConsultationSubmitError = (error: SubmitErrorEvent): void => {
        console.error('Error creating consultation:', error);
        showErrorNotification('Error al crear la consulta. Por favor, intente nuevamente.');
    };

    const showSuccessNotification = (message: string): void => {
        Notification.show(message, {theme: 'success'});
    };

    const showErrorNotification = (message: string): void => {
        Notification.show(message, {theme: 'error'});
    };

    const createInitialConsultationData = (): Partial<CreateConsultationDTO> | undefined => {
        if (!selectedPetId || !selectedVeterinarianId) return undefined;

        return {
            petId: selectedPetId, veterinarianId: selectedVeterinarianId
        };
    };

    const consultationFormFieldOptions = {
        petId: {
            renderer: () => (<TextField
                    label="Mascota"
                    value={selectedPetDisplayName}
                    readonly
                    onClick={() => setIsPetDialogOpen(true)}
                    placeholder="Haz click para seleccionar una mascota"
                    style={{cursor: 'pointer', width: '100%'}}
                />),
        }, veterinarianId: {
            renderer: () => (<TextField
                    label="Veterinario"
                    value={selectedVeterinarianDisplayName}
                    readonly
                    onClick={() => setIsVeterinarianDialogOpen(true)}
                    placeholder="Haz click para seleccionar un veterinario"
                    style={{cursor: 'pointer', width: '100%'}}
                />),
        },
    };

    const consultationGridColumnDefinitions = [{
        path: 'consultationDate',
        header: 'Fecha',
        renderer: ({item}: any) => formatConsultationDateTime(item.consultationDate)
    }, {path: 'diagnosis', header: 'Diagnóstico'}, {
        path: 'treatment',
        header: 'Tratamiento'
    }, {path: 'notes', header: 'Notas'}];

    const renderConsultationHistorySection = (): JSX.Element => {
        if (!selectedPetId) return <></>;

        return (<VerticalLayout style={{flex: '1', minWidth: '300px'}}>
                <h4><strong>Historial de Consultas:</strong></h4>
                {isConsultationHistoryLoading.value ? (<div>Cargando
                        historial...</div>) : petConsultationHistory.length === 0 ? (
                    <div style={{
                        padding: '1rem',
                        textAlign: 'center',
                        color: '#666'
                    }}>
                        No hay consultas disponibles para esta mascota
                    </div>) : (<Grid items={petConsultationHistory}
                                     style={{height: '400px'}}>
                        {consultationGridColumnDefinitions.map((column, index) => (
                            <GridColumn
                                key={index}
                                path={column.path}
                                header={column.header}
                                renderer={column.renderer}
                            />))}
                    </Grid>)}
            </VerticalLayout>);
    };

    return (<>
            <main className="w-full h-full flex flex-col box-border gap-s p-m">
                <h2>Nueva Consulta</h2>

                <HorizontalLayout
                    style={{alignItems: 'flex-start', gap: '2rem'}}>
                    <VerticalLayout style={{flex: '1', minWidth: '400px'}}>
                        <AutoForm
                            key={consultationFormKey}
                            service={ConsultationServiceImpl}
                            model={CreateConsultationDTOModel}
                            onSubmitSuccess={handleConsultationSubmitSuccess}
                            onSubmitError={handleConsultationSubmitError}
                            item={createInitialConsultationData()}
                            fieldOptions={consultationFormFieldOptions}
                            layoutRenderer={ConsultationLayoutRenderer}
                        />
                    </VerticalLayout>

                    {renderConsultationHistorySection()}
                </HorizontalLayout>
            </main>

            <SelectPetDialog
                isOpen={isPetDialogOpen}
                onDialogClose={() => setIsPetDialogOpen(false)}
                onPetSelect={handlePetSelection}
            />

            <SelectVeterinarianDialog
                isOpen={isVeterinarianDialogOpen}
                onDialogClose={() => setIsVeterinarianDialogOpen(false)}
                onVeterinarianSelect={handleVeterinarianSelection}
            />
        </>);
}
import { AutoForm, AutoFormLayoutRendererProps } from '@vaadin/hilla-react-crud'
import { AutoGrid } from "@vaadin/hilla-react-crud";
import { ConsultationServiceImpl, PetServiceImpl, MedicalHistoryServiceImpl } from 'Frontend/generated/endpoints'
import CreateConsultationDTOModel from "Frontend/generated/com/zoolandia/app/features/consultation/service/dto/CreateConsultationDTOModel"
import { useNavigate } from 'react-router'
import { HorizontalLayout, Notification, VerticalLayout, Grid, GridColumn, TextField, Button, Dialog } from '@vaadin/react-components'
import type CreateConsultationDTO from "Frontend/generated/com/zoolandia/app/features/consultation/service/dto/CreateConsultationDTO"
import { SubmitErrorEvent } from '@vaadin/hilla-react-crud/'
import { useSignal } from '@vaadin/hilla-react-signals'
import { useEffect, useState } from 'react'
import MedicalHistory from 'Frontend/generated/com/zoolandia/app/features/medicalHistory/domain/MedicalHistory'
import PetModel from "Frontend/generated/com/zoolandia/app/features/pet/domain/PetModel";

const petListService = {
    list: PetServiceImpl.list,
    save: PetServiceImpl.save,
    delete: PetServiceImpl.delete
}

export interface SelectedPet {
    id: number
    name: string
    type: string
    breed: string
    ownerName: string
}

interface SelectPetDialogProps {
    open: boolean
    onClose: () => void
    onSelect: (pet: SelectedPet) => void
}

export function SelectPetDialog({ open, onClose, onSelect }: SelectPetDialogProps) {
    const [selectedPet, setSelectedPet] = useState<any>(null)

    return (
        <Dialog opened={open} onOpenedChanged={({ detail }) => !detail.value && onClose()}>
            <div style={{ padding: '1rem', width: '700px', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <h3>Seleccionar Mascota</h3>

                <AutoGrid
                    service={petListService}
                    model={PetModel}
                    columnOptions={{
                        name: { header: 'Nombre' },
                        type: { header: 'Tipo' },
                        breed: { header: 'Raza' }
                    }}
                    visibleColumns={['name', 'type', 'breed']}
                    onActiveItemChanged={({ detail }) => {
                        if (detail.value) {
                            setSelectedPet(detail.value)
                        }
                    }}
                />

                <TextField
                    label="Mascota seleccionada"
                    value={selectedPet ? `${selectedPet.name}` : ''}
                    readonly
                />

                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem' }}>
                    <Button theme="tertiary" onClick={onClose}>
                        Cancelar
                    </Button>
                    <Button
                        theme="primary"
                        onClick={() => {
                            if (!selectedPet) return
                            onSelect(selectedPet)
                            onClose()
                        }}
                        disabled={!selectedPet}
                    >
                        Aceptar
                    </Button>
                </div>
            </div>
        </Dialog>
    )
}

function ConsultationLayoutRenderer({ children }: AutoFormLayoutRendererProps<CreateConsultationDTOModel>) {
  const fieldsMapping = new Map<string, JSX.Element>()
  children.forEach((field) => {
    const fieldName = field.props?.propertyInfo?.name
    if (fieldName) {
      fieldsMapping.set(fieldName, field)
    }
  })

  return (
      <VerticalLayout>
        <h4><strong>Información Básica de la Consulta:</strong></h4>
        <VerticalLayout style={{ marginBottom: '1.5rem' }}>
          <HorizontalLayout theme="spacing" className="pb-l">
            {fieldsMapping.get('consultationDate')}
            {fieldsMapping.get('veterinarianId')}
          </HorizontalLayout>
        </VerticalLayout>

        <h4><strong>Detalles Médicos:</strong></h4>
        <VerticalLayout style={{ marginBottom: '1.5rem' }}>
          {fieldsMapping.get('diagnosis')}
          {fieldsMapping.get('treatment')}
          {fieldsMapping.get('prescription')}
        </VerticalLayout>

        <h4><strong>Notas:</strong></h4>
        <VerticalLayout style={{ marginBottom: '1.5rem' }}>
          {fieldsMapping.get('notes')}
        </VerticalLayout>
      </VerticalLayout>
  )
}

export default function NewConsultationView() {
    const navigate = useNavigate()
    const [dialogOpen, setDialogOpen] = useState(false)
    const [petName, setPetName] = useState('')
    const [selectedPetId, setSelectedPetId] = useState<number | undefined>()
    const [formKey, setFormKey] = useState(0) // Para forzar re-render del form
    const medicalHistory = useSignal<MedicalHistory[]>([])
    const isLoading = useSignal(false)

    const loadMedicalHistory = async (petId: number) => {
        if (!petId) {
            medicalHistory.value = []
            return
        }

        isLoading.value = true
        try {
            const history = await MedicalHistoryServiceImpl.findByPetId(petId)
            if (!history) {
                medicalHistory.value = []
            } else {
                medicalHistory.value = history ? [history] : []
            }
        } catch (error) {
            console.error('Error loading medical history:', error)
            medicalHistory.value = []
        } finally {
            isLoading.value = false
        }
    }

    const handlePetSelect = (pet: SelectedPet) => {
        setPetName(`${pet.name} (${pet.type} - ${pet.breed})`)
        setSelectedPetId(pet.id)
        setDialogOpen(false)
        loadMedicalHistory(pet.id)
        setFormKey(prev => prev + 1) // Forzar re-render del form
    }

    const handleOnSubmitSuccess = ({ item }: { item: CreateConsultationDTO }) => {
        Notification.show('Consulta creada exitosamente')
        navigate('/consultations')
    }

    const handleOnSubmitError = (error: SubmitErrorEvent) => {
        console.error('Error creating consultation:', error)
        showErrorNotification('Error al crear la consulta')
    }

    const showErrorNotification = (message: string) => {
        Notification.show(message, { theme: 'error' })
    }

    const formatDate = (dateString: string): string => {
        return new Date(dateString).toLocaleDateString('es-ES')
    }

    // Crear objeto inicial con petId para el formulario
    const initialItem = selectedPetId ? { petId: selectedPetId } : undefined

    const fieldOptions = {
        petId: {
            renderer: () => (
                <TextField
                    label="Mascota"
                    value={petName}
                    readonly
                    onClick={() => setDialogOpen(true)}
                    placeholder="Haz click para seleccionar una mascota"
                    style={{ cursor: 'pointer', width: '100%' }}
                />
            ),
        },
    }

    return (
        <>
            <main className="w-full h-full flex flex-col box-border gap-s p-m">
                <h2>Nueva Consulta</h2>

                <HorizontalLayout style={{ width: '100%', alignItems: 'flex-start', gap: '2rem' }}>
                    <VerticalLayout style={{ flex: '2' }}>
                        <h4><strong>Seleccionar Mascota:</strong></h4>
                        <VerticalLayout className="w-full mb-6">
                            <TextField
                                className="w-full cursor-pointer"
                                label="Mascota"
                                value={petName}
                                readonly
                                onClick={() => setDialogOpen(true)}
                                placeholder="Haz click para seleccionar una mascota"
                            />
                        </VerticalLayout>

                        <AutoForm
                            key={formKey} // Usar key para forzar re-render
                            service={ConsultationServiceImpl}
                            model={CreateConsultationDTOModel}
                            item={initialItem} // Pasar item inicial con petId
                            onSubmitSuccess={handleOnSubmitSuccess}
                            onSubmitError={handleOnSubmitError}
                            layoutRenderer={ConsultationLayoutRenderer}
                            fieldOptions={fieldOptions}
                        />
                    </VerticalLayout>

                    {selectedPetId && (
                        <VerticalLayout style={{ flex: '1', minWidth: '300px' }}>
                            <h4><strong>Historial Médico:</strong></h4>
                            {isLoading.value ? (
                                <div>Cargando historial...</div>
                            ) : medicalHistory.value.length === 0 ? (
                                <div style={{ padding: '1rem', textAlign: 'center', color: '#666' }}>
                                    No hay historial médico disponible para esta mascota
                                </div>
                            ) : (
                                <Grid items={medicalHistory.value} style={{ height: '400px' }}>
                                    <GridColumn
                                        header="Fecha"
                                        renderer={({ item }) => (
                                            <span>{formatDate(item.createdAt || item.updatedAt || '')}</span>
                                        )}
                                    />
                                    <GridColumn path="diagnosis" header="Diagnóstico" />
                                    <GridColumn path="treatment" header="Tratamiento" />
                                </Grid>
                            )}
                        </VerticalLayout>
                    )}
                </HorizontalLayout>
            </main>

            <SelectPetDialog
                open={dialogOpen}
                onClose={() => setDialogOpen(false)}
                onSelect={handlePetSelect}
            />
        </>
    )
}
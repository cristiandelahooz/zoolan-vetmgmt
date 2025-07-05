import { AutoGrid } from '@vaadin/hilla-react-crud'
import { ConsultationServiceImpl } from '@/generated/endpoints'
import ConsultationModel from '@/generated/com/wornux/data/entity/ConsultationModel'

export default function ConsultationsView() {
  return (
    <main className="w-full h-full flex flex-col box-border gap-s p-m">
      <AutoGrid
        service={ConsultationServiceImpl}
        model={ConsultationModel}
        columnOptions={{
          'pet.name': {
            header: 'Mascota',
            renderer: ({ item }) => item.pet?.name || 'N/A',
          },
          'pet.owner.firstName': {
            header: 'Propietario',
            renderer: ({ item }) =>
              `${item.pet?.owner?.firstName || ''} ${item.pet?.owner?.lastName || ''}`.trim() || 'N/A',
          },
          'veterinarian.firstName': {
            header: 'Veterinario',
            renderer: ({ item }) =>
              `Dr. ${item.veterinarian?.firstName || ''} ${item.veterinarian?.lastName || ''}`.trim(),
          },
          consultationDate: {
            header: 'Fecha',
            renderer: ({ item }) =>
              new Date(item.consultationDate).toLocaleDateString('es-DO', {
                year: 'numeric',
                month: 'short',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit',
              }),
          },
          diagnosis: {
            header: 'Diagnóstico',
            renderer: ({ item }) => item.diagnosis || 'Pendiente',
          },
          notes: {
            header: 'Notas',
            renderer: ({ item }) => (item.notes?.length > 50 ? `${item.notes.substring(0, 50)}...` : item.notes || ''),
          },
        }}
        visibleColumns={[
          'pet.name',
          'pet.owner.firstName',
          'veterinarian.firstName',
          'consultationDate',
          'diagnosis',
          'notes',
        ]}
      />
    </main>
  )
}

import type { ViewConfig } from '@vaadin/hilla-file-router/types.js'
import { AutoGrid } from '@vaadin/hilla-react-crud'
import ClientModel from 'Frontend/generated/com/zoolandia/app/features/client/domain/ClientModel'
import { ClientServiceImpl } from 'Frontend/generated/endpoints'

export const config: ViewConfig = {
  title: 'Clients',
  menu: {
    icon: 'vaadin:users',
    order: 0,
    title: 'Clientes',
  },
}

export default function ClientsView() {
  return (
    <main className="w-full h-full flex flex-col box-border gap-s p-m">
      <AutoGrid
        service={ClientServiceImpl}
        model={ClientModel}
        columnOptions={{
          username: {
            header: 'Nombre de Usuario',
          },
          password: {
            header: 'Contraseña',
          },
          email: {
            header: 'Correo Electrónico',
          },
          firstName: {
            header: 'Nombre',
          },
          lastName: {
            header: 'Apellido',
          },
          phoneNumber: {
            header: 'Número de Teléfono',
          },
          birthDate: {
            header: 'Fecha de Nacimiento',
          },
          gender: {
            header: 'Género',
          },
          nationality: {
            header: 'Nacionalidad',
          },
          address: {
            header: 'Dirección',
          },
          profilePictureUrl: {
            header: 'URL de Foto de Perfil',
          },
          active: {
            header: 'Activo',
          },
          createdAt: {
            header: 'Fecha de Creación',
          },
          updatedAt: {
            header: 'Fecha de Actualización',
          },
          role: {
            header: 'Rol',
          },
          cedula: {
            header: 'Cédula',
          },
          passport: {
            header: 'Pasaporte',
          },
          rnc: {
            header: 'RNC',
          },
          companyName: {
            header: 'Nombre de la Empresa',
          },
          preferredContactMethod: {
            header: 'Método de Contacto Preferido',
          },
          emergencyContactName: {
            header: 'Nombre del Contacto de Emergencia',
          },
          emergencyContactNumber: {
            header: 'Número de Contacto de Emergencia',
          },
          rating: {
            header: 'Calificación',
          },
          creditLimit: {
            header: 'Límite de Crédito',
          },
          currentBalance: {
            header: 'Balance Actual',
          },
          paymentTermsDays: {
            header: 'Días de Términos de Pago',
          },
          notes: {
            header: 'Notas',
          },
          referenceSource: {
            header: 'Fuente de Referencia',
          },
          province: {
            header: 'Provincia',
          },
          municipality: {
            header: 'Municipio',
          },
          sector: {
            header: 'Sector',
          },
          streetAddress: {
            header: 'Dirección de Calle',
          },
          referencePoints: {
            header: 'Puntos de Referencia',
          },
          verified: {
            header: 'Verificado',
          },
        }}
      />
    </main>
  )
}

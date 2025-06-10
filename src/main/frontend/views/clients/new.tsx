import type { ViewConfig } from '@vaadin/hilla-file-router/types.js'
import { AutoForm } from '@vaadin/hilla-react-crud'
import { Notification, PasswordField } from '@vaadin/react-components'
import type ClientCreateDTO from 'Frontend/generated/com/zoolandia/app/features/client/service/dto/ClientCreateDTO'
import ClientCreateDTOModel from 'Frontend/generated/com/zoolandia/app/features/client/service/dto/ClientCreateDTOModel'
import { ClientServiceImpl } from 'Frontend/generated/endpoints'
import { ROUTES } from 'Frontend/lib/constants/routes'
import type React from 'react'
import { useNavigate } from 'react-router'

export const config: ViewConfig = {
  title: 'Registrar Cliente',
}

export default function Register() {
  const navigate = useNavigate()

  const handleOnSubmitSuccess = ({ item }: { item: ClientCreateDTO }) => {
    Notification.show('Cliente registrado', { duration: 3000, position: 'bottom-end', theme: 'success' })
    console.log(item)
    navigate(ROUTES.CLIENTS)
  }
  return (
    <main className="w-full h-full flex flex-col box-border gap-s p-m">
      <AutoForm
        service={ClientServiceImpl}
        model={ClientCreateDTOModel}
        onSubmitSuccess={handleOnSubmitSuccess}
        fieldOptions={{
          username: {
            label: 'Nombre de Usuario',
          },
          password: {
            renderer: ({ field }) => <PasswordField {...field} />,
            label: 'Contraseña',
          },
          email: {
            label: 'Correo Electrónico',
          },
          firstName: {
            label: 'Nombre',
          },
          lastName: {
            label: 'Apellido',
          },
          phoneNumber: {
            label: 'Número de Teléfono',
          },
          birthDate: {
            label: 'Fecha de Nacimiento',
          },
          gender: {
            label: 'Género',
          },
          nationality: {
            label: 'Nacionalidad',
          },
          cedula: {
            label: 'Cédula',
          },
          passport: {
            label: 'Pasaporte',
          },
          rnc: {
            label: 'RNC',
          },
          companyName: {
            label: 'Nombre de la Empresa',
          },
          preferredContactMethod: {
            label: 'Método de Contacto Preferido',
          },
          emergencyContactName: {
            label: 'Nombre del Contacto de Emergencia',
          },
          emergencyContactNumber: {
            label: 'Número de Contacto de Emergencia',
          },
          rating: {
            label: 'Calificación',
          },
          creditLimit: {
            label: 'Límite de Crédito',
          },
          paymentTermsDays: {
            label: 'Días de Términos de Pago',
          },
          notes: {
            label: 'Notas',
          },
          referenceSource: {
            label: 'Fuente de Referencia',
          },
          province: {
            label: 'Provincia',
          },
          municipality: {
            label: 'Municipio',
          },
          sector: {
            label: 'Sector',
          },
          streetAddress: {
            label: 'Dirección',
          },
          referencePoints: {
            label: 'Puntos de Referencia',
          },
        }}
      />
    </main>
  )
}

import type ClientCreateDTO from '@/generated/com/zoolandia/app/features/client/service/dto/ClientCreateDTO'
import type { ViewConfig } from '@vaadin/hilla-file-router/types.js'
import type { AutoFormLayoutRendererProps, SubmitErrorEvent } from '@vaadin/hilla-react-crud/'

import ClientCreateDTOModel from '@/generated/com/zoolandia/app/features/client/service/dto/ClientCreateDTOModel'
import { ClientServiceImpl } from '@/generated/endpoints'
import { ROUTES } from '@/lib/constants/routes'
import { useClientFormStore } from '@/stores/useClientAutoFormStore'
import { AutoForm } from '@vaadin/hilla-react-crud'
import { HorizontalLayout, Notification, VerticalLayout } from '@vaadin/react-components'
import { useNavigate } from 'react-router'

export const config: ViewConfig = {
  title: 'Registrar Cliente',
}

export default function Register() {
  const navigate = useNavigate()
  const { fieldOptions, resetForm } = useClientFormStore()

  const handleOnSubmitSuccess = ({ item }: { item: ClientCreateDTO }) => {
    Notification.show('Cliente registrado', { duration: 3000, position: 'bottom-end', theme: 'success' })
    console.log(item)
    resetForm()
    navigate(ROUTES.CLIENTS)
  }

  const handleOnSubmitError = ({ error }: SubmitErrorEvent) => {
    Notification.show(`Error al registrar cliente: ${error.message}`, {
      duration: 5000,
      position: 'bottom-end',
      theme: 'error',
    })
  }

  return (
    <main className="w-full h-full flex flex-col box-border gap-s p-m">
      <AutoForm
        service={ClientServiceImpl}
        model={ClientCreateDTOModel}
        layoutRenderer={GroupingLayoutRenderer}
        onSubmitSuccess={handleOnSubmitSuccess}
        onSubmitError={handleOnSubmitError}
        fieldOptions={fieldOptions}
      />
    </main>
  )
}

function GroupingLayoutRenderer({ children }: AutoFormLayoutRendererProps<ClientCreateDTOModel>) {
  const fieldsMapping = new Map<string, JSX.Element>()

  for (const field of children) {
    const fieldName = field.props?.propertyInfo?.name
    fieldsMapping.set(fieldName, field)
  }

  return (
    <VerticalLayout>
      <h4>
        <strong>Información de Login:</strong>
      </h4>
      <HorizontalLayout theme="spacing" className="pb-l">
        {fieldsMapping.get('email')}
      </HorizontalLayout>

      <h4>
        <strong>Información Personal:</strong>
      </h4>
      <HorizontalLayout theme="spacing" className="pb-l">
        {fieldsMapping.get('firstName')}
        {fieldsMapping.get('lastName')}
        {fieldsMapping.get('birthDate')}
      </HorizontalLayout>
      <HorizontalLayout theme="spacing" className="pb-l">
        {fieldsMapping.get('gender')}
        {fieldsMapping.get('nationality')}
        {fieldsMapping.get('cedula')}
        {fieldsMapping.get('passport')}
      </HorizontalLayout>

      <h4>
        <strong>Información de Contacto:</strong>
      </h4>
      <HorizontalLayout theme="spacing" className="pb-l">
        {fieldsMapping.get('phoneNumber')}
        {fieldsMapping.get('preferredContactMethod')}
      </HorizontalLayout>

      <h4>
        <strong>Información de Emergencia:</strong>
      </h4>
      <HorizontalLayout theme="spacing" className="pb-l">
        {fieldsMapping.get('emergencyContactName')}
        {fieldsMapping.get('emergencyContactNumber')}
      </HorizontalLayout>

      <h4>
        <strong>Información de Empresa:</strong>
      </h4>
      <HorizontalLayout theme="spacing" className="pb-l">
        {fieldsMapping.get('companyName')}
        {fieldsMapping.get('rnc')}
        {fieldsMapping.get('creditLimit')}
      </HorizontalLayout>
      <HorizontalLayout theme="spacing" className="pb-l">
        {fieldsMapping.get('paymentTermsDays')}
      </HorizontalLayout>

      <h4>
        <strong>Dirección:</strong>
      </h4>
      <HorizontalLayout theme="spacing" className="pb-l">
        {fieldsMapping.get('province')}
        {fieldsMapping.get('municipality')}
        {fieldsMapping.get('sector')}
      </HorizontalLayout>
      <HorizontalLayout theme="spacing" className="pb-l">
        {fieldsMapping.get('streetAddress')}
        {fieldsMapping.get('referencePoints')}
      </HorizontalLayout>

      <h4>
        <strong>Otros:</strong>
      </h4>
      <HorizontalLayout theme="spacing" className="pb-l">
        {fieldsMapping.get('rating')}
        {fieldsMapping.get('notes')}
        {fieldsMapping.get('referenceSource')}
      </HorizontalLayout>
    </VerticalLayout>
  )
}

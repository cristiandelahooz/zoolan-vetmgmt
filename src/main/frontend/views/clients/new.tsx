import type { ViewConfig } from '@vaadin/hilla-file-router/types.js'
import type { SubmitErrorEvent } from '@vaadin/hilla-react-crud/'
import type ClientCreateDTO from 'Frontend/generated/com/zoolandia/app/features/client/service/dto/ClientCreateDTO'
import type React from 'react'

import { AutoForm } from '@vaadin/hilla-react-crud'
import { Notification } from '@vaadin/react-components'
import ClientCreateDTOModel from 'Frontend/generated/com/zoolandia/app/features/client/service/dto/ClientCreateDTOModel'
import { ClientServiceImpl } from 'Frontend/generated/endpoints'
import { AUTO_FORM_CLIENT_FIELD_OPTIONS } from 'Frontend/lib/constants/client-field-config'
import { ROUTES } from 'Frontend/lib/constants/routes'
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
        onSubmitSuccess={handleOnSubmitSuccess}
        onSubmitError={handleOnSubmitError}
        fieldOptions={AUTO_FORM_CLIENT_FIELD_OPTIONS}
      />
    </main>
  )
}

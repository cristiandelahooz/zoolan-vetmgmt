import { useState } from 'react'
import { AutoForm, AutoFormLayoutRendererProps } from '@vaadin/hilla-react-crud'
import { TextField, Notification, IntegerField, HorizontalLayout, VerticalLayout } from '@vaadin/react-components'
import { useNavigate } from 'react-router-dom'
import PetCreateDTOModel from 'Frontend/generated/com/zoolandia/app/features/pet/service/dto/PetCreateDTOModel'
import { PetServiceImpl } from 'Frontend/generated/endpoints'
import { SelectOwnerDialog } from './SelectOwnerDialog'

function PetFormLayoutRenderer({ children }: AutoFormLayoutRendererProps<PetCreateDTOModel>) {
  const fieldsMapping = new Map<string, JSX.Element>()

  children.forEach((field) => fieldsMapping.set(field.props?.propertyInfo?.name, field))

  return (
    <VerticalLayout>
      <h4>
        <strong>Información Básica de la Mascota:</strong>
      </h4>
      <VerticalLayout style={{ marginBottom: '1.5rem' }}>
        <HorizontalLayout theme="spacing" className="pb-l">
          {fieldsMapping.get('name')}
          {fieldsMapping.get('type')}
          {fieldsMapping.get('breed')}
        </HorizontalLayout>
        <HorizontalLayout theme="spacing" className="pb-l">
          {fieldsMapping.get('birthDate')}
          {fieldsMapping.get('gender')}
        </HorizontalLayout>
      </VerticalLayout>

      <div style={{ display: 'none' }}>{fieldsMapping.get('ownerId')}</div>
    </VerticalLayout>
  )
}

export default function PetEntryFormView() {
  const navigate = useNavigate()
  const [dialogOpen, setDialogOpen] = useState(false)
  const [ownerName, setOwnerName] = useState('')

  function handleOnSubmitSuccess() {
    Notification.show('Mascota registrada exitosamente')
    navigate('/pets')
  }

  const fieldOptions = {
    name: {
      label: 'Nombre',
    },
    type: {
      label: 'Tipo de Animal',
    },
    breed: {
      label: 'Raza',
    },
    birthDate: {
      label: 'Fecha de Nacimiento',
    },
    gender: {
      label: 'Género',
    },
    ownerId: {
      label: 'ID del Dueño',
      renderer: ({ field }: { field: any }) => {
        return <IntegerField {...field} />
      },
    },
  }

  return (
    <>
      <main className="w-full h-full flex flex-col box-border gap-s p-m">
        <div style={{ marginBottom: '1.5rem' }}>
          <h4>
            <strong>Seleccionar Dueño:</strong>
          </h4>
          <TextField
            label="Dueño de la Mascota"
            value={ownerName}
            readonly
            onClick={() => setDialogOpen(true)}
            placeholder="Haz clic para seleccionar un dueño"
            style={{ width: '100%', cursor: 'pointer' }}
          />
        </div>

        <AutoForm
          service={PetServiceImpl}
          model={PetCreateDTOModel}
          onSubmitSuccess={handleOnSubmitSuccess}
          layoutRenderer={PetFormLayoutRenderer}
          fieldOptions={fieldOptions}
        />
      </main>

      <SelectOwnerDialog
        open={dialogOpen}
        onClose={() => setDialogOpen(false)}
        onSelect={(client) => {
          setOwnerName(`${client.firstName} ${client.lastName}`)

          setTimeout(() => {
            const ownerIdInput = document.querySelector('vaadin-integer-field[name="ownerId"]') as any

            if (ownerIdInput) {
              ownerIdInput.value = client.id.toString()

              ownerIdInput.dispatchEvent(
                new CustomEvent('change', {
                  detail: { value: client.id.toString() },
                  bubbles: true,
                }),
              )
              ownerIdInput.dispatchEvent(
                new CustomEvent('input', {
                  detail: { value: client.id.toString() },
                  bubbles: true,
                }),
              )

              console.log('Value set and events dispatched')
            } else {
              console.error('Could not find ownerId field')
            }
          }, 0)

          setDialogOpen(false)
        }}
      />
    </>
  )
}

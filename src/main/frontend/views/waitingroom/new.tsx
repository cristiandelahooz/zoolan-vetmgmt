import type WaitingRoomCreateDTO from '@/generated/com/wornux/dto/request/WaitingRoomCreateRequestDto'
import WaitingRoomCreateDTOModel from '@/generated/com/wornux/dto/request/WaitingRoomCreateRequestDtoModel'
import { WaitingRoomServiceImpl } from '@/generated/endpoints'
import { AutoForm, type AutoFormLayoutRendererProps, type SubmitErrorEvent } from '@vaadin/hilla-react-crud'
import {
  HorizontalLayout,
  IntegerField,
  type IntegerFieldChangeEvent,
  type IntegerFieldProps,
  Notification,
  TextField,
  VerticalLayout,
} from '@vaadin/react-components'
import { useState } from 'react'
import { SelectOwnerDialog, type SelectedClient } from '../pets/_SelectOwnerDialog'
import { SelectPetDialog, type SelectedPet } from './_SelectPetDialog'

export default function WaitingRoomNewView() {
  const [ownerDialogOpen, setOwnerDialogOpen] = useState(false)
  const [petDialogOpen, setPetDialogOpen] = useState(false)
  const [ownerName, setOwnerName] = useState('')
  const [petName, setPetName] = useState('')
  const [ownerId, setOwnerId] = useState<number | undefined>()

  const handleOnSubmitSuccess = ({ item }: { item: WaitingRoomCreateDTO }) => {
    Notification.show('Paciente agregado a sala de espera exitosamente', {
      duration: 3000,
      position: 'bottom-end',
      theme: 'success',
    })
    setOwnerName('')
    setPetName('')
    setOwnerId(undefined)
  }

  const handleOnSubmitError = (error: SubmitErrorEvent) => {
    console.error('Submit error:', error)

    let message = 'Error desconocido'

    try {
      if (error.error) {
        const errorObj = error.error as any
        if (typeof errorObj === 'string') {
          message = errorObj
        } else if (errorObj.message) {
          message = errorObj.message
        } else if (errorObj.cause && errorObj.cause.message) {
          message = errorObj.cause.message
        } else if (errorObj.detail) {
          message = errorObj.detail
        }
      }
    } catch (e) {
      console.error('Error parsing error message:', e)
    }

    if (
      message.includes('ya están en la sala de espera') ||
      message.includes('already in waiting room') ||
      message.includes('El cliente y mascota ya están')
    ) {
      Notification.show('El cliente y mascota ya están registrados en la sala de espera', {
        duration: 5000,
        position: 'bottom-end',
        theme: 'error',
      })
    } else {
      Notification.show(`Error al agregar paciente: ${message}`, {
        duration: 5000,
        position: 'bottom-end',
        theme: 'error',
      })
    }
  }

  const fieldOptions = {
    clientId: {
      label: 'ID del Cliente',
      renderer: ({ field }: { field: IntegerFieldProps }) => (
        <IntegerField
          hidden={true}
          {...field}
          onChange={(e: IntegerFieldChangeEvent) => {
            // Mantiene la funcionalidad del campo pero permanece oculto
          }}
        />
      ),
    },
    petId: {
      label: 'ID de la Mascota',
      renderer: ({ field }: { field: IntegerFieldProps }) => (
        <IntegerField hidden={true} {...field} onChange={(e: IntegerFieldChangeEvent) => {}} />
      ),
    },
  }

  function handleOwnerSelect(client: SelectedClient) {
    if (!client.firstName || !client.lastName) {
      console.warn('Client data is incomplete')
      return
    }

    setOwnerName(`${client.firstName} ${client.lastName}`)
    setOwnerId(client.id)

    setPetName('')

    setTimeout(() => {
      const clientIdInput = document.querySelector('vaadin-integer-field[name="clientId"]') as HTMLInputElement
      if (clientIdInput) {
        clientIdInput.value = client.id.toString()
        clientIdInput.dispatchEvent(
          new CustomEvent('change', {
            detail: { value: client.id.toString() },
            bubbles: true,
          }),
        )
        clientIdInput.dispatchEvent(
          new CustomEvent('input', {
            detail: { value: client.id.toString() },
            bubbles: true,
          }),
        )
      }

      const petIdInput = document.querySelector('vaadin-integer-field[name="petId"]') as HTMLInputElement
      if (petIdInput) {
        petIdInput.value = ''
        petIdInput.dispatchEvent(
          new CustomEvent('change', {
            detail: { value: '' },
            bubbles: true,
          }),
        )
        petIdInput.dispatchEvent(
          new CustomEvent('input', {
            detail: { value: '' },
            bubbles: true,
          }),
        )
      }
    }, 0)
    setOwnerDialogOpen(false)
  }

  function handlePetSelect(pet: SelectedPet) {
    if (!pet.name) {
      console.warn('Pet data is incomplete')
      return
    }

    setPetName(`${pet.name} (${pet.type} - ${pet.breed})`)

    setTimeout(() => {
      const petIdInput = document.querySelector('vaadin-integer-field[name="petId"]') as HTMLInputElement
      if (petIdInput) {
        petIdInput.value = pet.id.toString()
        petIdInput.dispatchEvent(
          new CustomEvent('change', {
            detail: { value: pet.id.toString() },
            bubbles: true,
          }),
        )
        petIdInput.dispatchEvent(
          new CustomEvent('input', {
            detail: { value: pet.id.toString() },
            bubbles: true,
          }),
        )
      }
    }, 0)
    setPetDialogOpen(false)
  }

  function WaitingRoomFormLayoutRenderer({ children }: AutoFormLayoutRendererProps<WaitingRoomCreateDTOModel>) {
    const fieldsMapping = new Map<string, JSX.Element>()
    for (const field of children) {
      fieldsMapping.set(field.props?.propertyInfo?.name, field)
    }

    return (
      <VerticalLayout>
        <h4>
          <strong>Seleccionar Dueño:</strong>
        </h4>
        <VerticalLayout className="w-full mb-6">
          <TextField
            className="w-full cursor-pointer"
            label="Dueño del Paciente"
            value={ownerName}
            readonly
            onClick={() => setOwnerDialogOpen(true)}
            placeholder="Haz click para seleccionar un dueño"
          />
        </VerticalLayout>

        <h4>
          <strong>Seleccionar Mascota:</strong>
        </h4>
        <VerticalLayout className="w-full mb-6">
          <TextField
            className="w-full cursor-pointer"
            label="Mascota del Paciente"
            value={petName}
            readonly
            disabled={!ownerId}
            onClick={() => {
              if (ownerId) {
                setPetDialogOpen(true)
              } else {
                Notification.show('Primero debe seleccionar un dueño', {
                  duration: 3000,
                  position: 'bottom-end',
                  theme: 'contrast',
                })
              }
            }}
            placeholder={ownerId ? 'Haz click para seleccionar una mascota' : 'Primero selecciona un dueño'}
          />
        </VerticalLayout>

        <h4>
          <strong>Información del Paciente:</strong>
        </h4>
        <VerticalLayout className="w-full mb-6">
          <HorizontalLayout theme="spacing" className="pb-l">
            {fieldsMapping.get('reasonForVisit')}
            {fieldsMapping.get('priority')}
          </HorizontalLayout>
          <HorizontalLayout theme="spacing" className="pb-l">
            {fieldsMapping.get('notes')}
            {fieldsMapping.get('clientId')}
            {fieldsMapping.get('petId')}
          </HorizontalLayout>
        </VerticalLayout>
      </VerticalLayout>
    )
  }

  return (
    <>
      <VerticalLayout
        theme="spacing"
        style={{
          padding: 'var(--lumo-space-l)',
          maxWidth: '600px',
        }}
      >
        <h2>Registrar paciente en sala de espera</h2>
        <AutoForm
          service={WaitingRoomServiceImpl}
          model={WaitingRoomCreateDTOModel}
          onSubmitSuccess={handleOnSubmitSuccess}
          onSubmitError={handleOnSubmitError}
          layoutRenderer={WaitingRoomFormLayoutRenderer}
          fieldOptions={fieldOptions}
          style={{ width: '100%' }}
        />
      </VerticalLayout>

      <SelectOwnerDialog
        open={ownerDialogOpen}
        onClose={() => setOwnerDialogOpen(false)}
        onSelect={handleOwnerSelect}
      />

      <SelectPetDialog
        open={petDialogOpen}
        ownerId={ownerId}
        onClose={() => setPetDialogOpen(false)}
        onSelect={handlePetSelect}
      />
    </>
  )
}

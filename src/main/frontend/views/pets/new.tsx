import { AutoForm, type AutoFormLayoutRendererProps } from '@vaadin/hilla-react-crud'
import {
  HorizontalLayout,
  IntegerField,
  Notification,
  Select,
  TextField,
  VerticalLayout,
} from '@vaadin/react-components'
import PetCreateDTOModel from 'Frontend/generated/com/zoolandia/app/features/pet/service/dto/PetCreateDTOModel'
import { PetServiceImpl } from 'Frontend/generated/endpoints'
import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router'
import { SelectOwnerDialog } from './SelectOwnerDialog'

// 🔥 COMPONENTE ESTÁTICO para evitar re-renderizaciones
const PetFormLayoutRenderer = ({ children }: AutoFormLayoutRendererProps<PetCreateDTOModel>) => {
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
        </HorizontalLayout>
        <HorizontalLayout theme="spacing" className="pb-l">
          {fieldsMapping.get('birthDate')}
          {fieldsMapping.get('gender')}
        </HorizontalLayout>
      </VerticalLayout>

      <div style={{ display: 'none' }}>
        {fieldsMapping.get('ownerId')}
        {fieldsMapping.get('breed')}
      </div>
    </VerticalLayout>
  )
}

export default function PetEntryFormView() {
  const navigate = useNavigate()
  const [dialogOpen, setDialogOpen] = useState(false)
  const [ownerName, setOwnerName] = useState('')

  // Estados para el filtrado dinámico
  const [typeOptions, setTypeOptions] = useState<{ label: string; value: string }[]>([])
  const [breedOptions, setBreedOptions] = useState<{ label: string; value: string }[]>([])
  const [selectedType, setSelectedType] = useState<string | undefined>()
  const [selectedBreed, setSelectedBreed] = useState<string>('')
  const [typeBreedMap, setTypeBreedMap] = useState<Record<string, string[]>>({})
  const [loading, setLoading] = useState(true)

  // 🔥 REF para controlar la sincronización (tipo correcto para navegador)
  const syncTimeoutRef = useRef<number | null>(null)

  // 🔧 CARGAR tipos y razas desde el backend
  useEffect(() => {
    async function fetchTypesAndBreeds() {
      try {
        setLoading(true)
        const map = await PetServiceImpl.getPetTypeAndBreeds()

        if (!map || typeof map !== 'object') {
          console.warn('Invalid data received')
          return
        }

        const cleanMap: Record<string, string[]> = {}
        Object.entries(map).forEach(([key, value]) => {
          if (key && Array.isArray(value)) {
            cleanMap[key] = value.filter(
              (breed): breed is string => typeof breed === 'string' && breed.trim().length > 0,
            )
          }
        })

        setTypeBreedMap(cleanMap)

        const validTypes = Object.keys(cleanMap).filter((type) => cleanMap[type] && cleanMap[type].length > 0)

        setTypeOptions(
          validTypes.map((type) => ({
            value: type,
            label: type.charAt(0) + type.slice(1).toLowerCase(),
          })),
        )

        console.log('✅ Data loaded successfully')
      } catch (error) {
        console.error('❌ Error fetching data:', error)
      } finally {
        setLoading(false)
      }
    }
    fetchTypesAndBreeds()
  }, [])

  // 🎯 FILTRAR razas según el tipo seleccionado
  useEffect(() => {
    if (!selectedType || !typeBreedMap[selectedType] || typeBreedMap[selectedType].length === 0) {
      setBreedOptions([])
      setSelectedBreed('')
      return
    }

    const breedOpts = typeBreedMap[selectedType].map((breed: string) => ({
      value: breed,
      label: breed,
    }))
    setBreedOptions(breedOpts)
    setSelectedBreed('')
  }, [selectedType, typeBreedMap])

  // 🔥 FUNCIÓN para sincronizar la raza de manera robusta
  const syncBreedValue = (value: string) => {
    console.log('🔄 Starting breed sync for:', value)

    // Limpiar timeout anterior
    if (syncTimeoutRef.current !== null) {
      clearTimeout(syncTimeoutRef.current)
    }

    // Establecer nuevo timeout
    syncTimeoutRef.current = window.setTimeout(() => {
      const breedField = document.querySelector('vaadin-text-field[name="breed"]') as any
      if (breedField) {
        console.log('✅ Syncing breed field with:', value)
        breedField.value = value

        // Disparar eventos necesarios
        breedField.dispatchEvent(new Event('input', { bubbles: true }))
        breedField.dispatchEvent(new Event('change', { bubbles: true }))
        breedField.dispatchEvent(
          new CustomEvent('value-changed', {
            detail: { value },
            bubbles: true,
          }),
        )

        console.log('✅ Breed sync completed:', breedField.value)
      } else {
        console.log('❌ Breed field not found')
      }
    }, 200)
  }

  // 🔥 EFECTO para sincronizar cuando cambie selectedBreed
  useEffect(() => {
    if (selectedBreed) {
      syncBreedValue(selectedBreed)
    }
  }, [selectedBreed])

  function handleOnSubmitSuccess() {
    console.log('✅ Form submitted successfully!')
    Notification.show('Mascota registrada exitosamente')
    navigate('/pets')
  }

  // 🔥 FIELDOPTIONS ESTÁTICOS (sin dependencias que cambien)
  const fieldOptions = {
    name: {
      label: 'Nombre',
    },
    type: {
      label: 'Tipo de Animal',
      renderer: ({ field }: { field: any }) => (
        <Select
          {...field}
          items={typeOptions}
          disabled={loading}
          placeholder={loading ? 'Cargando tipos...' : 'Selecciona un tipo'}
          onChange={(e) => {
            const value = (e.target as any).value
            console.log('🔄 Type changed to:', value)
            setSelectedType(value)
            if (field.onChange) field.onChange(e)
          }}
        />
      ),
    },
    breed: {
      label: 'Raza',
      renderer: ({ field }: { field: any }) => <TextField {...field} style={{ display: 'none' }} />,
    },
    birthDate: {
      label: 'Fecha de Nacimiento',
    },
    gender: {
      label: 'Género',
    },
    ownerId: {
      label: 'ID del Dueño',
      renderer: ({ field }: { field: any }) => <IntegerField {...field} />,
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

        <div style={{ marginBottom: '1.5rem' }}>
          <h4>
            <strong>Seleccionar Raza:</strong>
          </h4>
          <Select
            label="Raza de la Mascota"
            items={breedOptions}
            value={selectedBreed}
            disabled={!selectedType || loading || breedOptions.length === 0}
            placeholder={
              loading
                ? 'Cargando...'
                : selectedType
                  ? breedOptions.length > 0
                    ? 'Selecciona una raza'
                    : 'No hay razas disponibles'
                  : 'Primero selecciona el tipo'
            }
            style={{ width: '100%' }}
            onChange={(e) => {
              const value = (e.target as any).value
              console.log('🐕 Breed selected:', value)
              setSelectedBreed(value)
            }}
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
              console.log('✅ Owner set')
            }
          }, 0)

          setDialogOpen(false)
        }}
      />
    </>
  )
}

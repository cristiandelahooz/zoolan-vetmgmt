import { AutoForm, type AutoFormLayoutRendererProps } from '@vaadin/hilla-react-crud'
import {
  HorizontalLayout,
  IntegerField,
  type IntegerFieldChangeEvent,
  type IntegerFieldProps,
  Notification,
  Select,
  type SelectChangeEvent,
  type SelectProps,
  TextField,
  type TextFieldChangeEvent,
  VerticalLayout,
} from '@vaadin/react-components'
import PetCreateDTOModel from 'Frontend/generated/com/zoolandia/app/features/pet/service/dto/PetCreateDTOModel'
import { PetServiceImpl } from 'Frontend/generated/endpoints'
import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router'
import { usePetFormStore } from '../../stores/petAutoFormStore'
import { SelectOwnerDialog, type SelectedClient } from './_SelectOwnerDialog'

export default function PetEntryFormView() {
  const navigate = useNavigate()
  const [dialogOpen, setDialogOpen] = useState(false)
  const [ownerName, setOwnerName] = useState('')

  const { type, breed, ownerId, setField, reset } = usePetFormStore()

  const { typeOptions, breedOptions, loading, setSelectedType, selectedType } = usePetTypeBreedData()

  useEffect(() => {
    setField('type', selectedType || '')
  }, [selectedType, setField])

  const handleOnSubmitSuccess = () => {
    Notification.show('Mascota registrada exitosamente')
    reset()
    navigate('/pets')
  }

  const fieldOptions = {
    name: {
      label: 'Nombre',
      onChange: (e: TextFieldChangeEvent) => setField('name', e.target.value),
    },
    type: {
      label: 'Tipo de Animal',
      renderer: ({ field }: { field: React.ComponentProps<typeof Select> }) => (
        <Select
          {...field}
          items={typeOptions}
          value={type}
          disabled={loading}
          placeholder={loading ? 'Cargando tipos...' : 'Selecciona un tipo'}
          onChange={(e: SelectChangeEvent) => {
            const value = e.target?.value ?? ''
            setSelectedType(value)
            setField('type', value)
          }}
        />
      ),
    },
    breed: {
      renderer: ({ field }: { field: SelectProps }) => (
        <Select
          {...field}
          label="Raza de la Mascota"
          items={breedOptions}
          value={breed}
          disabled={!type}
          className={'w-full'}
          onChange={(e: SelectChangeEvent) => {
            const value = e.target?.value ?? ''
            setField('breed', value)
          }}
        />
      ),
      validators: undefined,
    },
    birthDate: {
      label: 'Fecha de Nacimiento',
      onChange: (e: TextFieldChangeEvent) => setField('birthDate', e.target.value),
    },
    gender: {
      label: 'Género',
      onChange: (e: SelectChangeEvent) => setField('gender', e.target.value ?? ''),
    },
    ownerId: {
      label: 'ID del Dueño',
      renderer: ({ field }: { field: IntegerFieldProps }) => (
        <IntegerField
          hidden={true}
          {...field}
          value={ownerId?.toString() ?? ''}
          onChange={(e: IntegerFieldChangeEvent) => {
            const value = e.target.value ? Number.parseInt(e.target.value, 10) : undefined
            setField('ownerId', value)
          }}
        />
      ),
    },
  }

  function handleOwnerSelect(client: SelectedClient) {
    if (!client.firstName || !client.lastName) {
      console.warn('Client data is incomplete')
      return
    }

    setOwnerName(`${client.firstName} ${client.lastName}`)
    setTimeout(() => {
      const ownerIdInput = document.querySelector('vaadin-integer-field[name="ownerId"]') as HTMLInputElement
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
      }
    }, 0)
    setDialogOpen(false)
  }

  function PetFormLayoutRenderer({ children }: AutoFormLayoutRendererProps<PetCreateDTOModel>) {
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
            label="Dueño de la Mascota"
            value={ownerName}
            readonly
            onClick={() => setDialogOpen(true)}
            placeholder="Haz click para seleccionar un dueño"
          />
        </VerticalLayout>
        <h4>
          <strong>Seleccionar Tipo:</strong>
        </h4>
        <VerticalLayout className="w-full mb-6">{fieldsMapping.get('type')}</VerticalLayout>
        <h4>
          <strong>Información Básica de la Mascota:</strong>
        </h4>
        <VerticalLayout className="w-full mb-6">
          <HorizontalLayout theme="spacing" className="pb-l">
            {fieldsMapping.get('name')}
            {fieldsMapping.get('breed')}
          </HorizontalLayout>
          <HorizontalLayout theme="spacing" className="pb-l">
            {fieldsMapping.get('birthDate')}
            {fieldsMapping.get('gender')}
            {fieldsMapping.get('ownerId')}
          </HorizontalLayout>
        </VerticalLayout>
      </VerticalLayout>
    )
  }

  return (
    <>
      <main className="w-full h-full flex flex-col box-border gap-s p-m">
        <AutoForm
          service={PetServiceImpl}
          model={PetCreateDTOModel}
          onSubmitSuccess={handleOnSubmitSuccess}
          layoutRenderer={PetFormLayoutRenderer}
          fieldOptions={fieldOptions}
        />
      </main>
      <SelectOwnerDialog open={dialogOpen} onClose={() => setDialogOpen(false)} onSelect={handleOwnerSelect} />
    </>
  )
}

const MINIMUM_BREED_NAME_LENGTH = 1

const isValidBreedName = (breed: unknown): breed is string => {
  return typeof breed === 'string' && breed.trim().length > MINIMUM_BREED_NAME_LENGTH
}

const isValidTypeKey = (key: string): boolean => {
  return Boolean(key)
}

const isBreedArray = (value: unknown): value is string[] => {
  return Array.isArray(value)
}

const hasValidBreeds = (breeds: string[]): boolean => {
  return breeds.length > 0
}

const filterValidBreeds = (breeds: unknown[]): string[] => {
  return breeds.filter(isValidBreedName)
}

const processBreedList = (value: unknown): string[] | null => {
  if (!isBreedArray(value)) return null

  const validBreeds = filterValidBreeds(value)
  return hasValidBreeds(validBreeds) ? validBreeds : null
}

const isValidRawMap = (rawMap: unknown): boolean => {
  return Boolean(rawMap) && typeof rawMap === 'object'
}

const processTypeEntry = (key: string, value: unknown): [string, string[]] | null => {
  if (!isValidTypeKey(key)) return null

  const processedBreeds = processBreedList(value)
  return processedBreeds ? [key, processedBreeds] : null
}

const buildCleanMap = (rawMap: Record<string, unknown>): Record<string, string[]> => {
  const cleanMap: Record<string, string[]> = {}

  for (const [key, value] of Object.entries(rawMap)) {
    const processedEntry = processTypeEntry(key, value)
    if (processedEntry) {
      const [typeKey, breeds] = processedEntry
      cleanMap[typeKey] = breeds
    }
  }

  return cleanMap
}

const sanitizeTypeBreedMap = (rawMap: unknown): Record<string, string[]> => {
  if (!isValidRawMap(rawMap)) return {}
  return buildCleanMap(rawMap as Record<string, unknown>)
}

const capitalizeText = (text: string): string => {
  return text.charAt(0).toUpperCase()
}

const lowercaseText = (text: string): string => {
  return text.slice(1).toLowerCase()
}

const formatTypeLabel = (type: string): string => {
  return capitalizeText(type) + lowercaseText(type)
}

const createTypeOption = (type: string) => ({
  value: type,
  label: formatTypeLabel(type),
})

const mapTypesToOptions = (typeBreedMap: Record<string, string[]>) => {
  return Object.keys(typeBreedMap).map(createTypeOption)
}

const createBreedOption = (breed: string) => ({
  value: breed,
  label: breed,
})

const mapBreedsToOptions = (breeds: string[]) => {
  return breeds.map(createBreedOption)
}

const findBreedsByType = (typeBreedMap: Record<string, string[]>, selectedType: string): string[] => {
  return typeBreedMap[selectedType] || []
}

const hasSelectedType = (selectedType: string | undefined): selectedType is string => {
  return Boolean(selectedType)
}

const getAvailableBreeds = (typeBreedMap: Record<string, string[]>, selectedType: string | undefined): string[] => {
  if (!hasSelectedType(selectedType)) return []
  return findBreedsByType(typeBreedMap, selectedType)
}

const fetchPetData = async (): Promise<unknown> => {
  return await PetServiceImpl.getPetTypeAndBreeds()
}

const processFetchedData = (rawData: unknown): Record<string, string[]> => {
  return sanitizeTypeBreedMap(rawData)
}

function usePetTypeBreedData() {
  const [typeBreedMap, setTypeBreedMap] = useState<Record<string, string[]>>({})
  const [loading, setLoading] = useState(true)
  const [selectedType, setSelectedType] = useState<string | undefined>()

  useEffect(() => {
    let isMounted = true

    const loadPetData = async () => {
      setLoading(true)
      try {
        const rawData = await fetchPetData()
        if (!isMounted) return

        const processedData = processFetchedData(rawData)
        setTypeBreedMap(processedData)
      } finally {
        if (isMounted) {
          setLoading(false)
        }
      }
    }

    loadPetData()

    return () => {
      isMounted = false
    }
  }, [])

  const typeOptions = useMemo(() => mapTypesToOptions(typeBreedMap), [typeBreedMap])

  const breedOptions = useMemo(() => {
    const availableBreeds = getAvailableBreeds(typeBreedMap, selectedType)
    return mapBreedsToOptions(availableBreeds)
  }, [selectedType, typeBreedMap])

  return {
    typeOptions,
    breedOptions,
    loading,
    setSelectedType,
    selectedType,
  }
}

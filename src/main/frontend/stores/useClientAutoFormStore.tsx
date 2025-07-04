import {
  CLIENT_FIELDS,
  type FieldOptions,
  type IdentificationDocuments,
  getFieldsToEnableByField,
  isValidDominicanCedula,
  isValidPassport,
  isValidRnc,
} from '@/lib/constants/client-field-config'
import { TextField, type TextFieldProps } from '@vaadin/react-components'
import { create } from 'zustand'

interface ClientFormState {
  identificationDocumentValue: IdentificationDocuments
  fieldOptions: Record<string, FieldOptions>
  setIdentificationDocumentValue?: (value: IdentificationDocuments) => void
  updateFieldOptions: () => void
  resetForm: () => void
}

const enabledClientFields = ['cedula', 'passport', 'rnc']

export const useClientFormStore = create<ClientFormState>((set, get) => {
  const initialIdentificationDocumentValue: IdentificationDocuments = {
    cedula: '',
    passport: '',
    rnc: '',
  }
  const store = {
    identificationDocumentValue: initialIdentificationDocumentValue,

    fieldOptions: {} as Record<string, FieldOptions>,

    setIdentificationDocumentValue: (value: IdentificationDocuments) => {
      set({ identificationDocumentValue: value })
      get().updateFieldOptions()
    },

    updateFieldOptions: () => {
      const { identificationDocumentValue } = get()
      const newFieldOptions = createAutoFormClientFieldOptions(
        identificationDocumentValue,
        store.setIdentificationDocumentValue,
      )
      set({ fieldOptions: newFieldOptions })
    },

    resetForm: () => {
      const initialFieldOptions = createAutoFormClientFieldOptions(
        initialIdentificationDocumentValue,
        store.setIdentificationDocumentValue,
      )
      set({
        identificationDocumentValue: initialIdentificationDocumentValue,
        fieldOptions: initialFieldOptions,
      })
    },
  }

  function initializeFieldOptions(store: ClientFormState) {
    return createAutoFormClientFieldOptions(initialIdentificationDocumentValue, store.setIdentificationDocumentValue)
  }
  store.fieldOptions = initializeFieldOptions(store)

  return store
})

function createAutoFormClientFieldOptions(
  identificationDocumentValue?: IdentificationDocuments,
  setIdentificationDocumentValue?: (value: IdentificationDocuments) => void,
): Record<string, FieldOptions> {
  const isPassportValid = identificationDocumentValue?.passport
    ? isValidPassport(identificationDocumentValue.passport)
    : false

  const isRncValid = identificationDocumentValue?.rnc ? isValidRnc(identificationDocumentValue.rnc) : false

  const isCedulaValid = identificationDocumentValue?.cedula
    ? isValidDominicanCedula(identificationDocumentValue.cedula)
    : false

  let docType: keyof IdentificationDocuments | null = null
  if (isCedulaValid) {
    docType = 'cedula'
  } else if (isPassportValid) {
    docType = 'passport'
  } else if (isRncValid) {
    docType = 'rnc'
  }

  const fieldsToEnable = docType ? [...getFieldsToEnableByField(docType)] : enabledClientFields
  return CLIENT_FIELDS.reduce(
    (acc, [key, label, options]) => {
      let fieldConfig: FieldOptions = {
        label,
        disabled: !fieldsToEnable.includes(key),
        ...options,
      }

      if (enabledClientFields.includes(key) && setIdentificationDocumentValue) {
        fieldConfig = {
          ...fieldConfig,
          renderer: createInitialEnabledClientFieldInputRenderer(
            key as keyof IdentificationDocuments,
            !fieldsToEnable.includes(key),
            setIdentificationDocumentValue,
          ),
        }
      }

      acc[key] = fieldConfig
      return acc
    },
    {} as Record<string, FieldOptions>,
  )
}

function createInitialEnabledClientFieldInputRenderer(
  doctype: keyof IdentificationDocuments,
  isDisabled: boolean,
  setFieldValue: (value: IdentificationDocuments) => void,
) {
  return ({ field }: { field: TextFieldProps }) => (
    <TextField
      {...field}
      disabled={isDisabled}
      onInput={(e: Event) => {
        field.onInput?.(e)
        const { value } = e.target as HTMLInputElement
        const identificationDocumentValue: IdentificationDocuments = {
          cedula: '',
          passport: '',
          rnc: '',
        }
        identificationDocumentValue[doctype] = value
        setFieldValue(identificationDocumentValue)
      }}
    />
  )
}

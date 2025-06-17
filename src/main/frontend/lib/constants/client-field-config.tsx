import type { Validator } from '@vaadin/hilla-lit-form'
import type { FieldDirectiveResult } from '@vaadin/hilla-react-form'
import type { PasswordFieldProps } from '@vaadin/react-components/PasswordField.js'
import type { CSSProperties } from 'react'
import { PasswordField } from '@vaadin/react-components'

export type FieldOptions = {
  id?: string
  className?: string
  style?: CSSProperties
  label?: string
  placeholder?: string
  helperText?: string
  colspan?: number
  disabled?: boolean
  readonly?: boolean
  element?: JSX.Element
  renderer?(props: {
    field: CustomFormFieldProps
  }): JSX.Element
  validators?: Validator[]
}

export type CustomFormFieldProps = FieldDirectiveResult &
  Readonly<{
    label?: string
    disabled?: boolean
  }>

const CLIENT_FIELDS_GROUPED: Record<string, string[]> = {
  login: ['email'],
  personal: ['firstName', 'lastName', 'birthDate', 'gender', 'nationality', 'cedula', 'passport'],
  contacto: ['phoneNumber', 'preferredContactMethod'],
  emergencia: ['emergencyContactName', 'emergencyContactNumber'],
  empresa: ['companyName', 'rnc', 'creditLimit', 'paymentTermsDays'],
  direccion: ['province', 'municipality', 'sector', 'streetAddress', 'referencePoints'],
  otros: ['rating', 'notes', 'referenceSource'],
}

const CLIENT_FIELDS_TO_EXCLUDE_BY_IDENTIFICATION_DOCUMENT: Record<string, string[]> = {
  cedula: ['passport', ...CLIENT_FIELDS_GROUPED.empresa],
  passport: ['cedula', ...CLIENT_FIELDS_GROUPED.empresa],
  rnc: ['cedula', ...CLIENT_FIELDS_GROUPED.personal],
}
export interface IdentificationDocuments {
  cedula?: string
  passport?: string
  rnc?: string
}

export const isValidDominicanCedula = (cedula: string | undefined): boolean => {
  const cleanCedula = cedula?.replace(/[-\s]/g, '')

  return typeof cleanCedula === 'string' && /^\d{11}$/.test(cleanCedula)
}
export const isValidPassport = (passport: string | undefined): boolean => {
  const cleanPassport = passport?.replace(/[-\s]/g, '')

  return typeof cleanPassport === 'string' && /^[A-Za-z0-9]{9}$/.test(cleanPassport)
}

export const isValidRnc = (rnc: string | undefined): boolean => {
  const cleanRnc = rnc?.replace(/[-\s]/g, '')

  return typeof cleanRnc === 'string' && /^\d{9}$/.test(cleanRnc)
}

export const getFieldsToEnableByField = (field: keyof IdentificationDocuments): string[] => {
  const fieldsToExclude = new Set(CLIENT_FIELDS_TO_EXCLUDE_BY_IDENTIFICATION_DOCUMENT[field] || [])
  return Object.values(CLIENT_FIELDS_GROUPED)
    .flat()
    .filter((f) => !fieldsToExclude.has(f))
}

export const CLIENT_FIELDS: Array<[key: string, label: string, options?: Partial<FieldOptions>]> = [
  ['username', 'Nombre de Usuario'],
  [
    'password',
    'Contraseña',
    {
      renderer: ({ field }: { field: PasswordFieldProps }) => <PasswordField {...field} />,
    },
  ],
  ['email', 'Correo Electrónico'],
  ['firstName', 'Nombre'],
  ['lastName', 'Apellido'],
  ['phoneNumber', 'Número de Teléfono'],
  ['birthDate', 'Fecha de Nacimiento'],
  ['gender', 'Género'],
  ['nationality', 'Nacionalidad'],
  ['cedula', 'Cédula'],
  ['passport', 'Pasaporte'],
  ['rnc', 'RNC'],
  ['companyName', 'Nombre de la Empresa'],
  ['preferredContactMethod', 'Método de Contacto Preferido'],
  ['emergencyContactName', 'Nombre del Contacto de Emergencia'],
  ['emergencyContactNumber', 'Número de Contacto de Emergencia'],
  ['rating', 'Calificación'],
  ['creditLimit', 'Límite de Crédito'],
  ['paymentTermsDays', 'Días de Términos de Pago'],
  ['notes', 'Notas'],
  ['referenceSource', 'Fuente de Referencia'],
  ['province', 'Provincia'],
  ['municipality', 'Municipio'],
  ['sector', 'Sector'],
  ['streetAddress', 'Dirección de Calle'],
  ['referencePoints', 'Puntos de Referencia'],
]

const GRID_EXTRA_FIELDS = [
  ['profilePictureUrl', 'URL de Foto de Perfil'],
  ['active', 'Activo'],
  ['createdAt', 'Fecha de Creación'],
  ['updatedAt', 'Fecha de Actualización'],
  ['role', 'Rol'],
  ['currentBalance', 'Balance Actual'],
  ['verified', 'Verificado'],
]

export const AUTO_GRID_CLIENT_COLUMN_OPTIONS = [...CLIENT_FIELDS, ...GRID_EXTRA_FIELDS].reduce(
  (acc, [key, header]) => {
    acc[key] = { header }
    return acc
  },
  {} as Record<string, { header: string }>,
)

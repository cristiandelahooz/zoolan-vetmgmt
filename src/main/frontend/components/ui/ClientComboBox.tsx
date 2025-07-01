import { ComboBox, type ComboBoxDataProvider } from '@vaadin/react-components'
import { ClientServiceImpl } from '@/generated/endpoints'
import type Client from '@/generated/com/zoolandia/app/features/client/domain/Client'
import { useMemo } from 'react'
import type Pageable from '@/generated/com/vaadin/hilla/mappedtypes/Pageable'

interface ClientComboBoxProps {
  label: string
  value: string | undefined
  onChange: (value: string) => void
  error?: string
}

export function ClientComboBox({ label, value, onChange, error }: ClientComboBoxProps) {
  const dataProvider: ComboBoxDataProvider<Client> = useMemo(
    () => async (params, callback) => {
      const pageable: Pageable = { pageNumber: params.page, pageSize: params.pageSize, sort: { orders: [] } }
      const clients = await ClientServiceImpl.searchClients(params.filter, pageable)
      const items = clients.values().reduce((acc, client) => {
        acc.push(client)
        return acc
      }, [] as Client[])
      callback(items ?? [], clients.length ?? 0)
    },
    [],
  )

  return (
    <ComboBox
      label={label}
      value={value}
      onValueChanged={(e) => onChange(e.detail.value)}
      dataProvider={dataProvider}
      itemLabelPath="name"
      itemValuePath="id"
      invalid={!!error}
      errorMessage={error}
    />
  )
}

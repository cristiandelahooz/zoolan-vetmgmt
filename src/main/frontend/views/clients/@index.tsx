import type { ViewConfig } from '@vaadin/hilla-file-router/types.js'
import { AutoGrid } from '@vaadin/hilla-react-crud'
import ClientModel from 'Frontend/generated/com/zoolandia/app/features/client/domain/ClientModel'
import { ClientServiceImpl } from 'Frontend/generated/endpoints'
import { AUTO_GRID_CLIENT_COLUMN_OPTIONS } from 'Frontend/lib/constants/client-field-config'

export const config: ViewConfig = {
  title: 'Clients',
}

export default function ClientsView() {
  return (
    <main className="w-full h-full flex flex-col box-border gap-s p-m">
      <AutoGrid service={ClientServiceImpl} model={ClientModel} columnOptions={AUTO_GRID_CLIENT_COLUMN_OPTIONS} />
    </main>
  )
}

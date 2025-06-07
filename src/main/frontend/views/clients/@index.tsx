import { ViewConfig } from '@vaadin/hilla-file-router/types.js'
import { useDataProvider } from '@vaadin/hilla-react-crud'
import { useSignal } from '@vaadin/hilla-react-signals'
import { Button, Grid, GridColumn, TextField } from '@vaadin/react-components'
import { Notification } from '@vaadin/react-components/Notification'
import { Group } from 'Frontend/components/ViewToolbar'
import Client from 'Frontend/generated/com/zoolandia/app/features/client/domain/Client'
import { ClientServiceImpl } from 'Frontend/generated/endpoints'
import handleError from 'Frontend/views/_ErrorHandler'

export const config: ViewConfig = {
  title: 'Clients',
  menu: {
    icon: 'vaadin:users',
    order: 0,
    title: 'Clientes >'
  }
}

function SearchBar({ onSearch }: { onSearch: (term: string) => void }) {
  const searchTerm = useSignal('')

  const handleSearch = () => {
    onSearch(searchTerm.value)
  }

  return (
    <div className="flex gap-s">
      <TextField
        placeholder="Buscar clientes..."
        aria-label="Búsqueda de clientes"
        value={searchTerm.value}
        onValueChanged={evt => (searchTerm.value = evt.detail.value)}
      />
      <Button onClick={handleSearch} theme="primary">
        Buscar
      </Button>
    </div>
  )
}

function ClientGrid({ dataProvider }: { dataProvider: any }) {
  return (
    <Grid dataProvider={dataProvider}>
      <GridColumn header="Nombre" path="firstName" />
      <GridColumn header="Apellido" path="lastName" />
      <GridColumn header="Cédula" path="cedula" />
      <GridColumn header="Rating" path="rating" />
      <GridColumn header="Provincia" path="province" />
      <GridColumn header="Estado" path="active">
        {({ item }) => (item.active ? 'Activo' : 'Inactivo')}
      </GridColumn>
    </Grid>
  )
}

export default function ClientsView() {
  const dataProvider = useDataProvider<Client>({
    list: pageable => ClientServiceImpl.getAllClients(pageable)
  })

  const handleSearch = async (searchTerm: string) => {
    try {
      const dataProvider = {
        list: (pageable: any) => ClientServiceImpl.searchClients(searchTerm, pageable)
      }
      await dataProvider.list({ page: 0, size: 10 })
    } catch (error) {
      handleError(error)
      Notification.show('Error al buscar clientes', { theme: 'error' })
    }
  }

  return (
    <main className="w-full h-full flex flex-col box-border gap-s p-m">
      <Group>
        <SearchBar onSearch={handleSearch} />
      </Group>
      <ClientGrid dataProvider={dataProvider.dataProvider} />
    </main>
  )
}

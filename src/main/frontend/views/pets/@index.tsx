import type { ViewConfig } from '@vaadin/hilla-file-router/types.js'
import { AutoGrid } from '@vaadin/hilla-react-crud'
import { PetServiceImpl } from 'Frontend/generated/endpoints'
import PetModel from 'Frontend/generated/com/zoolandia/app/features/pet/domain/PetModel'


const OwnerRenderer: React.FC<{ item: PetModel }> = ({ item }) => {
    return (
        <span>
      {item.owner
          ? `${item.owner.firstName} ${item.owner.lastName}`
          : ''}
    </span>
    );
};

export const config: ViewConfig = {
  menu: { title: 'Ver' },
  title: 'Lista de Mascotas',
}

export default function PetListView() {
  return (
    <main className="w-full h-full flex flex-col box-border gap-s p-m">
        <AutoGrid
            service={PetServiceImpl}
            model={PetModel}
            columnOptions={{
                name:           { header: 'Nombre' },
                type:           { header: 'Tipo' },
                breed:          { header: 'Raza' },
                gender:         { header: 'Género' },
                birthDate:      { header: 'Fecha de Nacimiento' },
                owner: {
                    header:   'Dueño',
                    renderer: OwnerRenderer,
                },
            }}
        />
    </main>
  )
}

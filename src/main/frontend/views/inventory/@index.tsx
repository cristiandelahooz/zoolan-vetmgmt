import { AutoCrud } from '@vaadin/hilla-react-crud'

import ProductModel from '@/generated/com/wornux/data/entity/ProductModel'
import { ProductServiceImpl } from '@/generated/endpoints'

export default function InventoryView() {
  return (
      <main className="w-full h-full flex flex-col box-border gap-s p-m">
        <AutoCrud
            service={ProductServiceImpl}
            model={ProductModel}
            formProps={{
              fieldOptions: {
                name: {
                  readonly: true,
                  label: 'Nombre',
                },
                description: {
                  readonly: true,
                  label: 'Descripción',
                },
                price: {
                  readonly: true,
                  label: 'Precio',
                },
                supplier: {
                  readonly: true,
                  label: 'Proveedor',
                },
                category: {
                  readonly: true,
                  label: 'Categoría',
                },
                reorderLevel: {
                  readonly: true,
                  label: 'Nivel de Reorden',
                },
                active: {
                  readonly: true,
                  label: 'Activo',
                },
              }
            }}
            gridProps={{
              columnOptions: {
                name: { header: 'Nombre' },
                description: { header: 'Descripción' },
                price: { header: 'Precio' },
                supplier: { header: 'Proveedor' },
                category: { header: 'Categoría' },
                reorderLevel: { header: 'Nivel de Reorden' },
                active: { header: 'Activo' },
              },
            }}
            style={{ flexGrow: '1' }}
        />
      </main>
  )
}
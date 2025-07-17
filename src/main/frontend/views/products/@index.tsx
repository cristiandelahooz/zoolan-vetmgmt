import ProductListDtoModel from '@/generated/com/wornux/dto/response/ProductListDtoModel'
import { ProductServiceImpl } from '@/generated/endpoints'
import { AutoGrid } from '@vaadin/hilla-react-crud'

const columnOptions = {
    name: { header: 'Nombre' },
    description: { header: 'Descripción' },
    price: { header: 'Precio' },
    stock: { header: 'Inventario' },
    category: { header: 'Categoría' },
    supplierName: { header: 'Proveedor' }
}

export default function ProductsView() {
    return (
        <main className="w-full h-full flex flex-col box-border gap-s p-m">
            <AutoGrid
                service={{
                    list: async (...args) => {
                        const result = await ProductServiceImpl.listAsDto(...args);
                        return (result ?? []).filter(Boolean);
                    }
                }}
                model={ProductListDtoModel}
                columnOptions={columnOptions}
            />
        </main>
    )
}
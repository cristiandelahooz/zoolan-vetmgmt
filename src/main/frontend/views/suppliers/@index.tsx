import SupplierModel from '@/generated/com/wornux/data/entity/SupplierModel';
import type Supplier from '@/generated/com/wornux/data/entity/SupplierModel';
import { SupplierServiceImpl } from '@/generated/endpoints';
import { AutoGrid } from '@vaadin/hilla-react-crud';

const columnOptions = {
    rnc: { header: 'RNC' },
    companyName: { header: 'Empresa' },
    contactPerson: { header: 'Contacto' },
    contactPhone: { header: 'Teléfono' },
    contactEmail: { header: 'Email' },
    province: { header: 'Provincia' },
    municipality: { header: 'Municipio' },
    sector: { header: 'Sector' },
    streetAddress: { header: 'Dirección' },
    active: {
        header: 'Activo',
        renderer: ({ item }: { item: Supplier }) => (item.active ? 'Sí' : 'No'),
    },
};

export default function SuppliersView() {
    return (
        <main className="w-full h-full flex flex-col box-border gap-s p-m">
            <AutoGrid
                service={{
                    list: async (...args) => {
                        const result = await SupplierServiceImpl.list(...args);
                        return (result ?? []).filter(Boolean);
                    },
                }}
                model={SupplierModel}
                columnOptions={columnOptions}
            />
        </main>
    );
}

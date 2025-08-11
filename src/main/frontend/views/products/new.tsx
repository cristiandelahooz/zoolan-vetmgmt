import { ProductServiceImpl } from '@/generated/endpoints'
import { AutoForm, type AutoFormLayoutRendererProps, type SubmitErrorEvent } from '@vaadin/hilla-react-crud'
import {
  HorizontalLayout,
  IntegerField,
  type IntegerFieldChangeEvent,
  type IntegerFieldProps,
  Notification,
  TextField,
  VerticalLayout,
} from '@vaadin/react-components'
import { useState } from 'react'
import { useNavigate } from 'react-router'
import ProductCreateRequestDtoModel from '@/generated/com/wornux/dto/request/ProductCreateRequestDtoModel'
import type ProductCreateRequestDto from '@/generated/com/wornux/dto/request/ProductCreateRequestDto'
import { SelectSupplierDialog, type SelectedSupplier } from '@/views/suppliers/_SelectSupplierDialog'

export default function ProductsRegisterView() {
  const navigate = useNavigate()
  const [dialogOpen, setDialogOpen] = useState(false)
  const [supplierName, setSupplierName] = useState('')
  const [supplierId, setSupplierId] = useState<number | undefined>(undefined)

  const handleOnSubmitSuccess = ({ item }: { item: ProductCreateRequestDto }) => {
    Notification.show('Producto registrado exitosamente', { duration: 3000, position: 'bottom-end', theme: 'success' })
    navigate('/products/', { replace: true })
  }

  const handleOnSubmitError = (error: SubmitErrorEvent) => {
    const message = error.error?.message || 'Error desconocido'
    Notification.show(`Error al registrar el producto: ${message}`, {
      duration: 5000,
      position: 'bottom-end',
      theme: 'error',
    })
  }

  function handleSupplierSelect(supplier: SelectedSupplier) {
    if (!supplier.name) {
      console.warn('Supplier data is incomplete')
      return
    }

    setSupplierName(supplier.name)
    setSupplierId(supplier.id)

    setTimeout(() => {
      const supplierIdInput = document.querySelector('vaadin-integer-field[name="supplierId"]') as HTMLInputElement
      if (supplierIdInput) {
        supplierIdInput.value = supplier.id.toString()
        supplierIdInput.dispatchEvent(
          new CustomEvent('change', {
            detail: { value: supplier.id.toString() },
            bubbles: true,
          }),
        )
        supplierIdInput.dispatchEvent(
          new CustomEvent('input', {
            detail: { value: supplier.id.toString() },
            bubbles: true,
          }),
        )
      }
    }, 0)
    setDialogOpen(false)
  }

  const fieldOptions = {
    name: { label: 'Nombre' },
    description: { label: 'Descripción' },
    price: { label: 'Precio' },
    stock: { label: 'Inventario' },
    category: { label: 'Categoría' },
    supplierId: {
      label: 'ID del Proveedor',
      renderer: ({ field }: { field: IntegerFieldProps }) => (
        <IntegerField
          hidden={true}
          {...field}
          value={supplierId?.toString() ?? ''}
          onChange={(e: IntegerFieldChangeEvent) => {
            const value = e.target.value ? Number.parseInt(e.target.value, 10) : undefined
            setSupplierId(value)
          }}
        />
      ),
    },
  }

  function GroupingLayoutRenderer({ children }: AutoFormLayoutRendererProps<ProductCreateRequestDtoModel>) {
    const fieldsMapping = new Map<string, JSX.Element>()
    for (const field of children) {
      fieldsMapping.set(field.props?.propertyInfo?.name, field)
    }

    return (
      <VerticalLayout>
        <h4>
          <strong>Seleccionar Proveedor:</strong>
        </h4>
        <VerticalLayout className="w-full mb-6">
          <TextField
            className="w-full cursor-pointer"
            label="Proveedor"
            value={supplierName}
            readonly
            onClick={() => setDialogOpen(true)}
            placeholder="Haz click para seleccionar un proveedor"
          />
        </VerticalLayout>
        <h4>
          <strong>Información del producto</strong>
        </h4>
        <VerticalLayout style={{ marginBottom: '1.5rem' }}>
          <HorizontalLayout theme="spacing" className="pb-l">
            {fieldsMapping.get('name')}
            {fieldsMapping.get('description')}
          </HorizontalLayout>
          <HorizontalLayout theme="spacing" className="pb-l">
            {fieldsMapping.get('price')}
            {fieldsMapping.get('stock')}
          </HorizontalLayout>
          <HorizontalLayout theme="spacing" className="pb-l">
            {fieldsMapping.get('category')}
            {fieldsMapping.get('supplierId')}
          </HorizontalLayout>
        </VerticalLayout>
      </VerticalLayout>
    )
  }

  return (
    <>
      <main className="w-full h-full flex flex-col box-border gap-s p-m">
        <AutoForm
          service={{
            ...ProductServiceImpl,
            save: ProductServiceImpl.createProduct,
          }}
          model={ProductCreateRequestDtoModel}
          fieldOptions={fieldOptions}
          onSubmitSuccess={handleOnSubmitSuccess}
          onSubmitError={handleOnSubmitError}
          layoutRenderer={GroupingLayoutRenderer}
        />
      </main>

      <SelectSupplierDialog open={dialogOpen} onClose={() => setDialogOpen(false)} onSelect={handleSupplierSelect} />
    </>
  )
}

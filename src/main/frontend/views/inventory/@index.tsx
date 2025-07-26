import { AutoCrud } from '@vaadin/hilla-react-crud'
import { useState, useCallback } from 'react'

import ProductListDtoModel from '@/generated/com/wornux/dto/response/ProductListDtoModel'
import { ProductServiceImpl } from '@/generated/endpoints'
import ProductListDto from '@/generated/com/wornux/dto/response/ProductListDto'
import { AUTO_FORM_PRODUCT_FIELD_OPTIONS, AUTO_GRID_PRODUCT_FIELD_OPTIONS } from '@/lib/constants/product-field-config'

export default function InventoryView() {
  const [refreshKey, setRefreshKey] = useState(0)

  const handleStockUpdate = useCallback(async (item: ProductListDto) => {
    if (item.id && item.stock !== undefined) {
      try {
        const updatedProduct = await ProductServiceImpl.updateStock(item.id, item.stock)
        setRefreshKey((prev) => prev + 1)
        return updatedProduct
      } catch (error) {
        console.error('Error updating stock:', error)
        throw error
      }
    }
    throw new Error('Invalid item data')
  }, [])

  return (
    <main className="w-full h-full flex flex-col box-border gap-s p-m">
      <AutoCrud
        service={{
          ...ProductServiceImpl,
          list: async (filter, sort) => {
            const result = await ProductServiceImpl.listAsDto(filter, sort)
            return result?.filter((item): item is NonNullable<typeof item> => item !== undefined) ?? []
          },
          save: handleStockUpdate,
        }}
        model={ProductListDtoModel}
        formProps={{
          fieldOptions: AUTO_FORM_PRODUCT_FIELD_OPTIONS,
          hiddenFields: ['id', 'createdAt', 'updatedAt'],
        }}
        gridProps={{
          columnOptions: AUTO_GRID_PRODUCT_FIELD_OPTIONS,
          hiddenColumns: ['id', 'createdAt', 'updatedAt'],
        }}
        style={{ flexGrow: '1' }}
        noNewButton={true}
      />
    </main>
  )
}

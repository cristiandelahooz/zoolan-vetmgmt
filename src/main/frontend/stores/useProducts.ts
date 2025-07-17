import type Product from '@/generated/com/wornux/data/entity/Product'
import type Pageable from '@/generated/com/vaadin/hilla/mappedtypes/Pageable'
import { ProductServiceImpl } from '@/generated/endpoints'
import { useCallback, useEffect, useState } from 'react'

export function useProducts() {
  const [products, setProducts] = useState<(Product | undefined)[] | undefined>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const fetchProducts = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const pageable: Pageable = { pageNumber: 0, pageSize: 100, sort: { orders: [] } }
      const fetchedProducts = await ProductServiceImpl.list(pageable, undefined)
      setProducts(fetchedProducts)
    } catch (e: any) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchProducts()
  }, [fetchProducts])

  const addProductToState = (product: Product) => {
    setProducts((prev) => [...(prev || []), product])
  }

  const updateProductInState = (id: number, updatedProduct: Product) => {
    setProducts((prev) => prev?.map((p) => (p?.id === id ? updatedProduct : p)))
  }

  const removeProductFromState = (id: number) => {
    setProducts((prev) => prev?.filter((p) => p?.id !== id))
  }

  const clearProducts = () => {
    setProducts([])
  }

  const setLoadingState = (isLoading: boolean) => {
    setLoading(isLoading)
  }

  const setErrorState = (errorMessage: string | null) => {
    setError(errorMessage)
  }

  return {
    products,
    loading,
    error,

    refetch: fetchProducts,
    addProductToState,
    updateProductInState,
    removeProductFromState,
    clearProducts,
    setLoadingState,
    setErrorState,
  }
}

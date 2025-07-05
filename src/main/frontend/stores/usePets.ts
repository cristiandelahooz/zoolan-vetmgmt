import type Pageable from '@/generated/com/vaadin/hilla/mappedtypes/Pageable'
import type Sort from '@/generated/com/vaadin/hilla/mappedtypes/Sort'
import type Pet from '@/generated/com/wornux/data/entity/Pet'
import { PetServiceImpl } from '@/generated/endpoints'
import { useCallback, useState } from 'react'

const unpagedPageable: Pageable = { pageNumber: 0, pageSize: 2000, sort: { orders: [] } as Sort }

export const usePets = () => {
  const [pets, setPets] = useState<Pet[]>([])

  const fetchPets = useCallback(async (clientId: number) => {
    try {
      const petList = await PetServiceImpl.getPetsByOwnerId(clientId, unpagedPageable)
      setPets((petList as any).content ?? [])
    } catch (error) {
      console.error('Error fetching pets:', error)
      setPets([])
    }
  }, [])

  return { pets, fetchPets }
}

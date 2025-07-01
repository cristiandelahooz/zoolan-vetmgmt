import { ClientServiceImpl } from '@/generated/endpoints'
import type Client from '@/generated/com/zoolandia/app/features/client/domain/Client'
import { useEffect, useState } from 'react'
import type Pageable from '@/generated/com/vaadin/hilla/mappedtypes/Pageable'

export function useClients() {
  const [clients, setClients] = useState<Client[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchClients = async () => {
      try {
        const pageable: Pageable = { pageNumber: 0, pageSize: 10, sort: { orders: [] } }
        const fetchedClients = await ClientServiceImpl.getAllClients(pageable)
        setClients(fetchedClients)
      } catch (e: any) {
        setError(e.message)
      } finally {
        setLoading(false)
      }
    }

    fetchClients()
  }, [])

  return { clients, loading, error }
}

import type AppointmentUpdateRequestDto from '@/generated/com/wornux/dto/request/AppointmentUpdateRequestDto'
import type AppointmentResponseDTO from '@/generated/com/wornux/dto/response/AppointmentResponseDto'
import { AppointmentServiceImpl } from '@/generated/endpoints'
import { useCallback, useEffect, useState } from 'react'

export function useAppointments() {
  const [appointments, setAppointments] = useState<(AppointmentResponseDTO | undefined)[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const fetchAppointments = useCallback(async (start: string, end: string) => {
    setLoading(true)
    try {
      const fetchedAppointments: (AppointmentResponseDTO | undefined)[] =
        (await AppointmentServiceImpl.getAppointmentsByDateRange(start, end)) || []
      setAppointments(fetchedAppointments)
    } catch (e: any) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    const today = new Date()
    const firstDayOfMonth = new Date(today.getFullYear(), today.getMonth(), 1)
    const lastDayOfMonth = new Date(today.getFullYear(), today.getMonth() + 1, 0)

    const initialStart = firstDayOfMonth.toISOString()
    const initialEnd = lastDayOfMonth.toISOString()

    fetchAppointments(initialStart, initialEnd)
  }, [fetchAppointments])

  const refetch = useCallback(
    (start: string, end: string) => {
      fetchAppointments(start, end)
    },
    [fetchAppointments],
  )

  const updateAppointment = async (id: number, appointment: AppointmentUpdateRequestDto) => {
    try {
      await AppointmentServiceImpl.updateAppointment(id, appointment)
    } catch (e) {
      console.error('Failed to update appointment:', e)
      throw e
    }
  }

  const deleteAppointment = async (id: number) => {
    try {
      await AppointmentServiceImpl.deleteAppointment(id)
    } catch (e) {
      console.error('Failed to delete appointment:', e)
      throw e
    }
  }

  return { appointments, loading, error, refetch, updateAppointment, deleteAppointment }
}

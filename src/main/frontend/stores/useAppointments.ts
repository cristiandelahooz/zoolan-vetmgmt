import type AppointmentResponseDTO from '@/generated/com/wornux/dto/response/AppointmentResponseDto'
import { AppointmentServiceImpl } from '@/generated/endpoints'
import { useCallback, useEffect, useState } from 'react'

export function useAppointments() {
  const [appointments, setAppointments] = useState<(AppointmentResponseDTO | null)[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const fetchAppointments = useCallback(async (start?: string, end?: string) => {
    setLoading(true)
    try {
      const fetchedAppointments = (await AppointmentServiceImpl.getCalendarEvents(start, end)) || []
      setAppointments(fetchedAppointments.filter((a): a is AppointmentResponseDTO => a !== undefined && a !== null))
    } catch (e: any) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchAppointments()
  }, [fetchAppointments])

  const refetch = useCallback(
    (start?: string, end?: string) => {
      fetchAppointments(start, end)
    },
    [fetchAppointments],
  )

  const updateAppointment = async (id: number, appointment: any) => {
    try {
      await AppointmentServiceImpl.updateAppointment(id, appointment)
      refetch()
    } catch (e) {
      console.error('Failed to update appointment:', e)
      throw e
    }
  }

  const deleteAppointment = async (id: number) => {
    try {
      await AppointmentServiceImpl.deleteAppointment(id)
      refetch()
    } catch (e) {
      console.error('Failed to delete appointment:', e)
      throw e
    }
  }

  return { appointments, loading, error, refetch, updateAppointment, deleteAppointment }
}

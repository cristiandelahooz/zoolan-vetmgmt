import type AppointmentCreateRequestDto from '@/generated/com/wornux/dto/request/AppointmentCreateRequestDto'
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

  const createAppointment = async (appointment: AppointmentCreateRequestDto) => {
    try {
      const newAppointment = await AppointmentServiceImpl.createAppointment(appointment)
      setAppointments((prev) => [...prev, newAppointment])
    } catch (e) {
      console.error('Failed to create appointment:', e)
      throw e
    }
  }

  const updateAppointment = async (id: number, appointment: AppointmentUpdateRequestDto) => {
    try {
      const updatedAppointment = await AppointmentServiceImpl.updateAppointment(id, appointment)
      setAppointments((prev) => prev.map((a) => (a?.eventId === id ? updatedAppointment : a)))
      return updatedAppointment
    } catch (e) {
      console.error('Failed to update appointment:', e)
      throw e
    }
  }

  const deleteAppointment = async (id: number) => {
    try {
      await AppointmentServiceImpl.deleteAppointment(id)
      setAppointments((prev) => prev.filter((a) => a?.eventId !== id))
    } catch (e) {
      console.error('Failed to delete appointment:', e)
      throw e
    }
  }

  return { appointments, loading, error, refetch, createAppointment, updateAppointment, deleteAppointment }
}

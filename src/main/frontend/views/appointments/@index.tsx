import {AppNotification} from '@/components/ui/Notification'
import type AppointmentResponseDTO
    from '@/generated/com/wornux/dto/response/AppointmentResponseDto'
import {useAppointments} from '@/stores/useAppointments'
import type {
    DatesSetArg, EventClickArg, EventContentArg, EventDropArg
} from '@fullcalendar/core/index.js'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin, {type DateClickArg} from '@fullcalendar/interaction'
import FullCalendar from '@fullcalendar/react'
import timeGridPlugin from '@fullcalendar/timegrid'
import {Button} from '@vaadin/react-components'
import {useCallback, useMemo, useRef, useState} from 'react'
import {AppointmentDetailsModal} from './_AppointmentDetailsModal'
import {CreateAppointmentModal} from './_CreateAppointmentModal'
import {EditAppointmentModal} from './_EditAppointmentModal'
import '@/themes/zoolan-vetmgmt/view/AppointmentsCalendarView.css'

interface DateRange {
  start: string
  end: string
}

const useCalendarState = () => {
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false)
  const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false)
  const [isEditModalOpen, setIsEditModalOpen] = useState(false)
  const [selectedDate, setSelectedDate] = useState<Date | null>(null)
  const [selectedAppointment, setSelectedAppointment] = useState<AppointmentResponseDTO | null>(null)
  const [notification, setNotification] = useState({ message: '', isOpen: false })

  const showNotification = (message: string) => setNotification({ message, isOpen: true })
  const hideNotification = () => setNotification({ message: '', isOpen: false })

  return {
    isCreateModalOpen,
    setIsCreateModalOpen,
    isDetailsModalOpen,
    setIsDetailsModalOpen,
    isEditModalOpen,
    setIsEditModalOpen,
    selectedDate,
    setSelectedDate,
    selectedAppointment,
    setSelectedAppointment,
    notification,
    showNotification,
    hideNotification,
  }
}

const useCalendarHandlers = (
  appointments: (AppointmentResponseDTO | undefined)[],
  updateAppointment: Function,
  refetch: Function,
  state: ReturnType<typeof useCalendarState>,
) => {
  const currentDateRange = useRef<DateRange | null>(null)

  const handleDatesSet = useCallback(
    (arg: DatesSetArg) => {
      const newStart = arg.start.toISOString()
      const newEnd = arg.end.toISOString()
      if (
        !currentDateRange.current ||
        newStart !== currentDateRange.current.start ||
        newEnd !== currentDateRange.current.end
      ) {
        currentDateRange.current = { start: newStart, end: newEnd }
        refetch(newStart, newEnd)
      }
    },
    [refetch],
  )

  const handleDateClick = (arg: DateClickArg) => {
    state.setSelectedDate(arg.date)
    state.setIsCreateModalOpen(true)
  }

  const handleEventClick = (clickInfo: EventClickArg) => {
    const appointment = appointments.find((a) => a?.eventId === Number(clickInfo.event.id))
    if (appointment) {
      state.setSelectedAppointment(appointment)
      state.setIsDetailsModalOpen(true)
    }
  }

  const handleEventDrop = async (dropInfo: EventDropArg) => {
    const { event } = dropInfo
    if (!event.start) {
      dropInfo.revert()
      return
    }
    try {
      await updateAppointment(Number(event.id), {
        startAppointmentDate: event.start.toISOString(),
        endAppointmentDate: event.end ? event.end.toISOString() : event.start.toISOString(),
      })
      refetch()
      state.showNotification('Appointment rescheduled successfully.')
    } catch (e) {
      console.log(`Error rescheduling appointment: ${e}`)
      dropInfo.revert()
      state.showNotification('Failed to reschedule appointment.')
    }
  }

  const handleCreateModalClose = (isSuccess: boolean) => {
    state.setIsCreateModalOpen(false)
    if (isSuccess) {
      refetch()
      state.showNotification('Appointment created successfully.')
    }
  }

  const handleEditModalClose = (isSuccess: boolean) => {
    state.setIsEditModalOpen(false)
    if (isSuccess) {
      refetch()
      state.showNotification('Appointment updated successfully.')
    }
  }

  const handleDetailsModalClose = () => state.setIsDetailsModalOpen(false)

  const handleEditRequest = (appointment: AppointmentResponseDTO) => {
    state.setSelectedAppointment(appointment)
    state.setIsDetailsModalOpen(false)
    state.setIsEditModalOpen(true)
  }

  return {
    handleDatesSet,
    handleDateClick,
    handleEventClick,
    handleEventDrop,
    handleCreateModalClose,
    handleEditModalClose,
    handleDetailsModalClose,
    handleEditRequest,
  }
}

const mapAppointmentsToEvents = (appointments: (AppointmentResponseDTO | undefined)[]) => {
  return appointments
    .filter((a) => a !== undefined && a !== null)
    .map((a) => ({
      id: String(a.eventId),
      title: a.appointmentTitle,
      start: a.startAppointmentDate,
      end: a.endAppointmentDate,
      extendedProps: {
        serviceType: a.serviceType,
        petName: a.petName,
      },
    }))
}

const renderEventContent = (eventInfo: EventContentArg) => (
  <div className="appointment-event p-xs">
    <div className="event-time font-semibold text-xs">{eventInfo.timeText}</div>
    <div className="event-title text-sm">{eventInfo.event.extendedProps.serviceType}</div>
    <div className="event-pet text-xs opacity-90">{eventInfo.event.extendedProps.petName}</div>
  </div>
)

export default function AppointmentsCalendarView() {
  const calendarRef = useRef<FullCalendar>(null)
  const [weekendsVisible, setWeekendsVisible] = useState(true)
  const { appointments, loading, error, updateAppointment, refetch } = useAppointments()
  const state = useCalendarState()
  const handlers = useCalendarHandlers(appointments, updateAppointment, refetch, state)

  const calendarEvents = useMemo(() => mapAppointmentsToEvents(appointments), [appointments])

  if (loading) return <div>Loading appointments...</div>
  if (error) return <div>Error loading appointments: {error}</div>

  return (
    <div className="appointments-calendar p-m">
      <div className="calendar-header mb-m flex justify-between items-center">
        <h2 className="text-xl font-bold">Calendario de Citas</h2>
        <Button onClick={() => setWeekendsVisible(!weekendsVisible)}>
          {weekendsVisible ? 'Hide Weekends' : 'Show Weekends'}
        </Button>
      </div>

      <FullCalendar
        ref={calendarRef}
        plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
        headerToolbar={{
          left: 'prev,next today',
          center: 'title',
          right: 'dayGridMonth,timeGridWeek,timeGridDay',
        }}
        initialView="dayGridMonth"
        datesSet={handlers.handleDatesSet}
        dateClick={handlers.handleDateClick}
        eventClick={handlers.handleEventClick}
        eventDrop={handlers.handleEventDrop}
        events={calendarEvents}
        eventContent={renderEventContent}
        locale="es"
        firstDay={1}
        slotMinTime="08:00:00"
        slotMaxTime="18:00:00"
        businessHours={{
          daysOfWeek: [1, 2, 3, 4, 5, 6],
          startTime: '08:00',
          endTime: '18:00',
        }}
        weekends={weekendsVisible}
        selectable={true}
        selectMirror={true}
        dayMaxEvents={true}
        eventDisplay="block"
        editable={true}
      />

      <CreateAppointmentModal
        isOpen={state.isCreateModalOpen}
        onClose={(isSuccess) => handlers.handleCreateModalClose(isSuccess)}
        selectedDate={state.selectedDate}
      />

      <AppointmentDetailsModal
        appointment={state.selectedAppointment}
        isOpen={state.isDetailsModalOpen}
        onClose={handlers.handleDetailsModalClose}
        onEdit={handlers.handleEditRequest}
      />

      <EditAppointmentModal
        appointment={state.selectedAppointment}
        isOpen={state.isEditModalOpen}
        onClose={(isSuccess) => handlers.handleEditModalClose(isSuccess)}
      />

      <AppNotification
        message={state.notification.message}
        isOpen={state.notification.isOpen}
        onClose={state.hideNotification}
      />
    </div>
  )
}

import {AppNotification} from '@/components/ui/Notification'
import type AppointmentResponseDTO
    from '@/generated/com/zoolandia/app/features/appointments/dtos/AppointmentResponseDTO'
import {useAppointments} from '@/stores/useAppointments'
import type {
    DatesSetArg, EventClickArg, EventContentArg, EventDropArg
} from '@fullcalendar/core/index.js'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin, {type DateClickArg} from '@fullcalendar/interaction'
import FullCalendar from '@fullcalendar/react'
import timeGridPlugin from '@fullcalendar/timegrid'
import {useCallback, useRef, useState} from 'react'
import {AppointmentDetailsModal} from './_AppointmentDetailsModal'
import {CreateAppointmentModal} from './_CreateAppointmentModal'
import {EditAppointmentModal} from './_EditAppointmentModal'
import '@/themes/zoolan-vetmgmt/view/AppointmentsCalendarView.css'

interface DateRange {
    start: string
    end: string
}

export default function AppointmentsCalendarView() {
    const calendarRef = useRef<FullCalendar>(null)
    const [weekendsVisible, setWeekendsVisible] = useState(false)
    const {
        appointments, loading, error, updateAppointment, refetch
    } = useAppointments()
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false)
    const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false)
    const [isEditModalOpen, setIsEditModalOpen] = useState(false)
    const [selectedDate, setSelectedDate] = useState<Date | null>(null)
    const [selectedAppointment, setSelectedAppointment] = useState<AppointmentResponseDTO | null>(null)
    const [notification, setNotification] = useState<{
        message: string; isOpen: boolean
    }>({message: '', isOpen: false})


    //TODO: Implement a toggle for weekends visibility
    function handleToggleWeekends() {
        setWeekendsVisible(!weekendsVisible);
    }

    const showNotification = (message: string) => {
        setNotification({message, isOpen: true})
    }

    const currentDateRange = useRef<DateRange | null>(null)

    const handleDatesSet = useCallback((arg: DatesSetArg) => {
        const newStart = arg.start.toISOString()
        const newEnd = arg.end.toISOString()

        if (!currentDateRange.current || newStart !== currentDateRange.current.start || newEnd !== currentDateRange.current.end) {
            currentDateRange.current = {start: newStart, end: newEnd}
            refetch(newStart, newEnd)
        }
    }, [refetch],)

    const handleDateClick = (arg: DateClickArg) => {
        setSelectedDate(arg.date)
        setIsCreateModalOpen(true)
    }

    const handleEventClick = (clickInfo: EventClickArg) => {
        const appointment = appointments.find((a) => a?.eventId === Number(clickInfo.event.id))
        if (appointment) {
            setSelectedAppointment(appointment)
            setIsDetailsModalOpen(true)
        }
    }

    const handleEventDrop = async (dropInfo: EventDropArg) => {
        const {event} = dropInfo
        const {id, start, end} = event

        if (!start) {
            console.error('Event drop failed: new start date is null.')
            dropInfo.revert()
            return
        }

        const startAppointmentDate = start.toISOString()
        const endAppointmentDate = end ? end.toISOString() : startAppointmentDate

        try {
            await updateAppointment(Number(id), {
                startAppointmentDate: startAppointmentDate,
                endAppointmentDate: endAppointmentDate,
            })
            refetch()
            showNotification('Appointment rescheduled successfully.')
        } catch (e) {
            console.error('Failed to update appointment:', e)
            dropInfo.revert()
            showNotification('Failed to reschedule appointment.')
        }
    }

    const renderEventContent = (eventInfo: EventContentArg) => {
        const {event} = eventInfo
        const {serviceType, petName} = event.extendedProps

        return (<div className="appointment-event p-xs">
            <div
                className="event-time font-semibold text-xs">{eventInfo.timeText}</div>
            <div className="event-title text-sm">{serviceType}</div>
            <div className="event-pet text-xs opacity-90">{petName}</div>
        </div>)
    }

    if (loading) return <div>Loading appointments...</div>
    if (error) return <div>Error loading appointments: {error}</div>

    return (<div className="appointments-calendar p-m">
        <div className="calendar-header mb-m">
            <h2 className="text-xl font-bold mb-s">Calendario de Citas</h2>
            <div className="calendar-controls flex gap-s mb-m"/>
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
            datesSet={handleDatesSet}
            dateClick={handleDateClick}
            eventClick={handleEventClick}
            eventDrop={handleEventDrop}
            events={appointments.map((a) => ({
                id: String(a?.eventId),
                title: a?.appointmentTitle,
                start: a?.startAppointmentDate,
                end: a?.endAppointmentDate,
                extendedProps: {
                    serviceType: a?.serviceType, petName: a?.petName,
                },
            }))}
            eventContent={renderEventContent}
            height="auto"
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
            isOpen={isCreateModalOpen}
            onClose={() => {
                setIsCreateModalOpen(false)
                refetch()
                showNotification('Appointment created successfully.')
            }}
            selectedDate={selectedDate}
        />

        <AppointmentDetailsModal
            appointment={selectedAppointment}
            isOpen={isDetailsModalOpen}
            onClose={() => setIsDetailsModalOpen(false)}
            onEdit={(appointment) => {
                setSelectedAppointment(appointment)
                setIsDetailsModalOpen(false)
                setIsEditModalOpen(true)
            }}
        />

        <EditAppointmentModal
            appointment={selectedAppointment}
            isOpen={isEditModalOpen}
            onClose={() => {
                setIsEditModalOpen(false)
                refetch()
                showNotification('Appointment updated successfully.')
            }}
        />

        <AppNotification
            message={notification.message}
            isOpen={notification.isOpen}
            onClose={() => setNotification({message: '', isOpen: false})}
        />
    </div>)
}

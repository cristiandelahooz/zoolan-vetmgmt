import { AppNotification } from '@/components/ui/Notification';
import type AppointmentResponseDTO from '@/generated/com/zoolandia/app/features/appointments/dtos/AppointmentResponseDTO';
import { useAppointments } from '@/stores/useAppointments';
import { useCallback, useRef, useState } from 'react';
import FullCalendar from '@fullcalendar/react';
import type { EventClickArg, EventContentArg, EventDropArg } from '@fullcalendar/core/index.js';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin, { type DateClickArg } from '@fullcalendar/interaction';
import timeGridPlugin from '@fullcalendar/timegrid';
import { Button } from '@vaadin/react-components';
import { AppointmentDetailsModal } from './_AppointmentDetailsModal';
import { CreateAppointmentModal } from './_CreateAppointmentModal';
import { EditAppointmentModal } from './_EditAppointmentModal';

export default function AppointmentsCalendarView() {
  const calendarRef = useRef<FullCalendar>(null);
  const { appointments, loading, error, updateAppointment, refetch } = useAppointments();
  const [isCreateModalOpen, setCreateModalOpen] = useState(false);
  const [isDetailsModalOpen, setDetailsModalOpen] = useState(false);
  const [isEditModalOpen, setEditModalOpen] = useState(false);
  const [selectedDate, setSelectedDate] = useState<Date | null>(null);
  const [selectedAppointment, setSelectedAppointment] = useState<AppointmentResponseDTO | null>(null);
  const [notification, setNotification] = useState<{ message: string; isOpen: boolean }>({ message: '', isOpen: false });

  const showNotification = (message: string) => {
    setNotification({ message, isOpen: true });
  };

  const handleDatesSet = useCallback(
    (arg: any) => {
      const start = arg.start.toISOString();
      const end = arg.end.toISOString();
      refetch(start, end);
    },
    [refetch]
  );

  const handleDateClick = (arg: DateClickArg) => {
    setSelectedDate(arg.date)
    setCreateModalOpen(true)
  }

  const handleEventClick = (clickInfo: EventClickArg) => {
    const appointment = appointments.find((a) => a.id === Number(clickInfo.event.id))
    if (appointment) {
      setSelectedAppointment(appointment)
      setDetailsModalOpen(true)
    }
  }

  const handleEventDrop = async (dropInfo: EventDropArg) => {
    const { event } = dropInfo
    const { id, start, end } = event

    if (!start) {
      console.error('Event drop failed: new start date is null.')
      dropInfo.revert()
      return
    }

    const updatedAppointment = {
      id: Number(id),
      start: start.toISOString(),
      end: end ? end.toISOString() : start.toISOString(),
    }

    try {
      await updateAppointment(Number(id), updatedAppointment)
      refetch()
      showNotification('Appointment rescheduled successfully.')
    } catch (e) {
      console.error('Failed to update appointment:', e)
      dropInfo.revert()
      showNotification('Failed to reschedule appointment.')
    }
  }

  const renderEventContent = (eventInfo: EventContentArg) => {
    const { event } = eventInfo
    const { petName, petType, appointmentType } = event.extendedProps

    return (
      <div className="appointment-event p-xs">
        <div className="event-time font-semibold text-xs">{eventInfo.timeText}</div>
        <div className="event-title text-sm">{appointmentType}</div>
        <div className="event-pet text-xs opacity-90">
          {petName} ({petType})
        </div>
      </div>
    )
  }

  if (loading) return <div>Loading appointments...</div>
  if (error) return <div>Error loading appointments: {error}</div>

  return (
    <div className="appointments-calendar p-m">
      <div className="calendar-header mb-m">
        <h2 className="text-xl font-bold mb-s">Calendario de Citas</h2>
        <div className="calendar-controls flex gap-s mb-m">
        </div>
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
        events={appointments.map((a) => ({ ...a, start: a.appointmentDateTime, end: a.appointmentDateTime }))}
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
        weekends={true}
        selectable={true}
        selectMirror={true}
        dayMaxEvents={true}
        eventDisplay="block"
        editable={true}
      />

      <CreateAppointmentModal
        isOpen={isCreateModalOpen}
        onClose={() => {
          setCreateModalOpen(false)
          refetch()
          showNotification('Appointment created successfully.')
        }}
        selectedDate={selectedDate}
      />

      <AppointmentDetailsModal
        appointment={selectedAppointment}
        isOpen={isDetailsModalOpen}
        onClose={() => setDetailsModalOpen(false)}
        onEdit={(appointment) => {
          setSelectedAppointment(appointment)
          setDetailsModalOpen(false)
          setEditModalOpen(true)
        }}
      />

      <EditAppointmentModal
        appointment={selectedAppointment}
        isOpen={isEditModalOpen}
        onClose={() => {
          setEditModalOpen(false)
          refetch()
          showNotification('Appointment updated successfully.')
        }}
      />

      <AppNotification
        message={notification.message}
        isOpen={notification.isOpen}
        onClose={() => setNotification({ message: '', isOpen: false })}
      />

      <style>{`
        .appointment-event {
          font-size: 12px;
          line-height: 1.2;
        }
        
        .fc-event {
          border-radius: 4px;
          padding: 2px 4px;
        }
        
        .fc-event-title {
          font-weight: 500;
        }
        
        .fc-daygrid-event {
          margin: 1px 0;
        }
      `}</style>
    </div>
  )
}

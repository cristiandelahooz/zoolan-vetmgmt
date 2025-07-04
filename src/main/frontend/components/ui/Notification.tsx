import { Notification } from '@vaadin/react-components'

interface NotificationProps {
  message: string
  isOpen: boolean
  onClose: () => void
}

export function AppNotification({ message, isOpen, onClose }: NotificationProps) {
  return (
    <Notification
      theme="contrast"
      duration={5000}
      position="bottom-center"
      opened={isOpen}
      onOpenedChanged={({ detail }) => !detail.value && onClose()}
    >
      {message}
    </Notification>
  )
}

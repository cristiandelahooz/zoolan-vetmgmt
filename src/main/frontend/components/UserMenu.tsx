import { Avatar, MenuBar, MenuBarItem, MenuBarItemSelectedEvent } from '@vaadin/react-components'

type UserMenuItem = MenuBarItem<{ action?: () => void }>

export const UserMenu = () => {
  // TODO Replace with real user information and actions
  const items: Array<UserMenuItem> = [
    {
      component: (
        <>
          <Avatar theme="xsmall" name="Sindy" colorIndex={5} className="mr-s" />
          Sindy
        </>
      ),
      children: [
        {
          text: 'View Profile',
          disabled: true,
          action: () => console.log('View Profile'),
        },
        {
          text: 'Manage Settings',
          disabled: true,
          action: () => console.log('Manage Settings'),
        },
        {
          text: 'Logout',
          disabled: true,
          action: () => console.log('Logout'),
        },
      ],
    },
  ]
  const onItemSelected = (event: MenuBarItemSelectedEvent<UserMenuItem>) => {
    event.detail.value.action?.()
  }
  return <MenuBar theme="tertiary-inline" items={items} onItemSelected={onItemSelected} className="m-m" slot="drawer" />
}

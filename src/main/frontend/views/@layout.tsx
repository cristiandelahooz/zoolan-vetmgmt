import { createMenuItems } from '@vaadin/hilla-file-router/runtime.js'
import {
  AppLayout,
  Avatar,
  Icon,
  MenuBar,
  MenuBarItem,
  MenuBarItemSelectedEvent,
  ProgressBar,
  Scroller,
  SideNav,
  SideNavItem
} from '@vaadin/react-components'
import { ViewToolbar } from 'Frontend/components/ViewToolbar'
import { Suspense, useState } from 'react'
import { Outlet, useLocation, useNavigate } from 'react-router'

function Header({ isSideNavOpened }: { isSideNavOpened: boolean }) {
  // TODO Replace with real application logo and name
  return (
    <>
      <div className="flex items-center" {...(isSideNavOpened ? { slot: 'drawer' } : {})}>
        <ViewToolbar title={isSideNavOpened ? 'Zoolandia' : ''} />
      </div>
    </>
  )
}

function MainMenu() {
  const navigate = useNavigate()
  const location = useLocation()

  return (
    <SideNav className="mx-m" onNavigate={({ path }) => path != null && navigate(path)} location={location}>
      {createMenuItems().map(({ to, icon, title }) => (
        <SideNavItem path={to} key={to}>
          {icon && <Icon icon={icon} slot="prefix" />}
          {title}
        </SideNavItem>
      ))}
    </SideNav>
  )
}

type UserMenuItem = MenuBarItem<{ action?: () => void }>

function UserMenu() {
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
        { text: 'View Profile', disabled: true, action: () => console.log('View Profile') },
        { text: 'Manage Settings', disabled: true, action: () => console.log('Manage Settings') },
        { text: 'Logout', disabled: true, action: () => console.log('Logout') }
      ]
    }
  ]
  const onItemSelected = (event: MenuBarItemSelectedEvent<UserMenuItem>) => {
    event.detail.value.action?.()
  }
  return <MenuBar theme="tertiary-inline" items={items} onItemSelected={onItemSelected} className="m-m" slot="drawer" />
}

export default function MainLayout() {
  const [isSideNavOpened, setSideNavOpened] = useState(true)

  const handleDrawerToggle: () => void = () => {
    setSideNavOpened(!isSideNavOpened)
  }
  return (
    <AppLayout primarySection="drawer" onDrawerOpenedChanged={handleDrawerToggle}>
      <Header isSideNavOpened={isSideNavOpened} />
      <Scroller slot="drawer">
        <MainMenu />
      </Scroller>
      <UserMenu />
      <Suspense fallback={<ProgressBar indeterminate={true} className="m-0" />}>
        <Outlet />
      </Suspense>
    </AppLayout>
  )
}

import { AppLayout, DrawerToggle, Icon, ProgressBar, Scroller, SideNav, SideNavItem } from '@vaadin/react-components'
import { UserMenu } from 'Frontend/components/UserMenu'
import { type MenuItemConfig, menuConfig } from 'Frontend/components/config/menuConfig'
import { Suspense } from 'react'
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router'

function MainMenu() {
  const navigate = useNavigate()
  const location = useLocation()

  return (
    <SideNav
      className="mx-m layout-side-nav"
      onNavigate={({ path }) => path != null && navigate(path)}
      location={location}
    >
      {menuConfig.map(({ path, icon, title, children }: MenuItemConfig) => (
        <SideNavItem path={path} key={path}>
          {icon && <Icon icon={icon} className="mr-s" slot="prefix" />}
          {title}
          {children?.map((child: MenuItemConfig) => (
            <SideNavItem path={child.path} key={child.path} slot="children">
              {child.icon && <Icon icon={child.icon} className="mr-s" slot="prefix" />}
              {child.title}
            </SideNavItem>
          ))}
        </SideNavItem>
      ))}
    </SideNav>
  )
}

export default function MainLayout() {
  return (
    <AppLayout>
      <DrawerToggle slot="navbar" />
      <NavLink to="/" slot="navbar">
        Zoolandia
      </NavLink>
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

import {createMenuItems} from '@vaadin/hilla-file-router/runtime.js'
import {
    AppLayout,
    Icon,
    ProgressBar,
    Scroller,
    SideNav,
    SideNavItem
} from '@vaadin/react-components'
import {ViewToolbar} from 'Frontend/components/ViewToolbar'
import {Suspense, useState} from 'react'
import {Outlet, useLocation, useNavigate} from 'react-router'
import UserMenu from 'Frontend/components/UserMenu.js'

function Header({isSideNavOpened}: { isSideNavOpened: boolean }) {
    // TODO Replace with real application logo and name
    return (
        <>
            <div
                className="flex items-center" {...(isSideNavOpened ? {slot: 'drawer'} : {})}>
                <ViewToolbar title={isSideNavOpened ? 'Zoolandia' : ''}/>
            </div>
        </>
    )
}

function MainMenu() {
    const navigate = useNavigate()
    const location = useLocation()

    return (
        <SideNav className="mx-m"
                 onNavigate={({path}) => path != null && navigate(path)}
                 location={location}>
            {createMenuItems().map(({to, icon, title}) => (
                <SideNavItem path={to} key={to}>
                    {icon && <Icon icon={icon} slot="prefix"/>}
                    {title}
                </SideNavItem>
            ))}
        </SideNav>
    )
}


export default function MainLayout() {
    const [isSideNavOpened, setSideNavOpened] = useState(true)

    const handleDrawerToggle: () => void = () => {
        setSideNavOpened(!isSideNavOpened)
    }
    return (
        <AppLayout primarySection="drawer"
                   onDrawerOpenedChanged={handleDrawerToggle}>
            <Header isSideNavOpened={isSideNavOpened}/>
            <Scroller slot="drawer">
                <MainMenu/>
            </Scroller>
            <UserMenu/>
            <Suspense
                fallback={<ProgressBar indeterminate={true} className="m-0"/>}>
                <Outlet/>
            </Suspense>
        </AppLayout>
    )
}

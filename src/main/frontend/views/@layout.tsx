import {
	AppLayout,
	Icon,
	ProgressBar,
	Scroller,
	SideNav,
	SideNavItem,
} from "@vaadin/react-components";
import { UserMenu } from "Frontend/components/UserMenu";
import { ViewToolbar } from "Frontend/components/ViewToolbar";
import {
	type MenuItemConfig,
	menuConfig,
} from "Frontend/components/config/menuConfig";
import { Suspense, useState } from "react";
import { Outlet, useLocation, useNavigate } from "react-router";

function Header({ isSideNavOpened }: { isSideNavOpened: boolean }) {
	// TODO Replace with real application logo and name
	return (
		<>
			<div
				className="flex items-center"
				{...(isSideNavOpened ? { slot: "drawer" } : {})}
			>
				<ViewToolbar title={isSideNavOpened ? "Zoolandia" : ""} />
			</div>
		</>
	);
}

function MainMenu() {
	const navigate = useNavigate();
	const location = useLocation();

	return (
		<SideNav
			className="mx-m"
			onNavigate={({ path }) => path != null && navigate(path)}
			location={location}
		>
			{menuConfig.map(({ path, icon, title, children }: MenuItemConfig) => (
				<SideNavItem path={path} key={path}>
					{icon && <Icon icon={icon} className="mr-s" slot="prefix" />}
					{title}
					{children?.map((child: MenuItemConfig) => (
						<SideNavItem path={child.path} key={child.path} slot="children">
							{child.icon && (
								<Icon icon={child.icon} className="mr-s" slot="prefix" />
							)}
							{child.title}
						</SideNavItem>
					))}
				</SideNavItem>
			))}
		</SideNav>
	);
}

export default function MainLayout() {
	const [isSideNavOpened, setSideNavOpened] = useState(true);

	const handleDrawerToggle: () => void = () => {
		setSideNavOpened(!isSideNavOpened);
	};
	return (
		<AppLayout
			primarySection="drawer"
			onDrawerOpenedChanged={handleDrawerToggle}
		>
			<Header isSideNavOpened={isSideNavOpened} />
			<Scroller slot="drawer">
				<MainMenu />
			</Scroller>
			<UserMenu />
			<Suspense fallback={<ProgressBar indeterminate={true} className="m-0" />}>
				<Outlet />
			</Suspense>
		</AppLayout>
	);
}

import { DrawerToggle } from '@vaadin/react-components'
import { PropsWithChildren } from 'react'

export function Group({ children }: PropsWithChildren) {
  return <div className="flex flex-col items-stretch gap-s md:flex-row md:items-center">{children}</div>
}

export type ViewToolbarProps = {
  title: string
} & PropsWithChildren

export function ViewToolbar({ title, children }: ViewToolbarProps) {
  return (
    <header className="flex flex-col justify-between items-stretch gap-m md:flex-row md:items-center">
      <div className="flex items-center">
        <DrawerToggle className="m-0" />
        <span className="font-semibold text-l">{title}</span>
      </div>
      {children && <div className="flex flex-col justify-between flex-grow md:flex-row gap-s">{children}</div>}
    </header>
  )
}

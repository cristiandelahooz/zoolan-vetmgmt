declare module '*.svg' {
  const content: string
  export default content
}

declare module '*.svg?raw' {
  const content: string
  export default content
}

declare module '*.svg?component' {
  import type { IconSvgLiteral } from '@vaadin/react-components'
  const content: IconSvgLiteral
  export default content
}

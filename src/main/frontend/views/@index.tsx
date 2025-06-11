import type { ViewConfig } from '@vaadin/hilla-file-router/types.js'
import { Chart } from '@vaadin/react-components-pro/Chart'
import { ChartSeries } from '@vaadin/react-components-pro/ChartSeries'

export const config: ViewConfig = {
  menu: {
    exclude: true,
  },
}

export default function MainView() {
  return (
    <main className="w-full h-full">
      <Chart type="bubble" title="" style={{ width: '100%' }}>
        <ChartSeries
          title="Zoolandia"
          values={[
            { x: 1, y: 2, z: 2 },
            { x: 2, y: 4, z: 1 },
            { x: 3, y: 6, z: 4 },
            { x: 1, y: 5, z: 3 },
          ]}
        />
      </Chart>
      <Chart
        type="line"
        title="Ventas por mes (2025)"
        additionalOptions={{
          xAxis: {
            categories: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
          },
          yAxis: { title: { text: 'DOP ()' } },
        }}
      >
        <ChartSeries
          title="Neurovet Perros"
          values={[42112, 58698, 12276, 33202, 74518, 45498, 42477, 17896, 44297, 22456, 38547, 12621]}
        />
        <ChartSeries
          title="C Fagia Gatos"
          values={[70972, 48589, 94434, 58270, 77282, 7108, 54085, 44401, 28868, 79643, 14383, 76036]}
        />
      </Chart>
    </main>
  )
}

import EmployeeCreateDTOModel from '@/generated/com/wornux/dto/request/EmployeeCreateRequestDtoModel'
import { EmployeeServiceImpl } from '@/generated/endpoints'
import { AUTO_GRID_EMPLOYEE_FIELD_OPTIONS } from '@/lib/constants/employee-field-config'
import { AutoGrid } from '@vaadin/hilla-react-crud'

export default function EmployeesView() {
  return (
    <main className="w-full h-full flex flex-col box-border gap-s p-m">
      <AutoGrid
        service={EmployeeServiceImpl}
        model={EmployeeCreateDTOModel}
        columnOptions={AUTO_GRID_EMPLOYEE_FIELD_OPTIONS}
      />
    </main>
  )
}

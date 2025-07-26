import {useState, useCallback, useEffect} from 'react'
import { EmployeeServiceImpl } from '@/generated/endpoints'
import {
  AUTO_FORM_EMPLOYEE_FIELD_OPTIONS,
  AUTO_GRID_EMPLOYEE_FIELD_OPTIONS,
} from '@/lib/constants/employee-field-config'
import { AutoCrud } from '@vaadin/hilla-react-crud'
import EmployeeUpdateRequestDtoModel from '@/generated/com/wornux/dto/request/EmployeeUpdateRequestDtoModel'
import EmployeeUpdateRequestDto from '@/generated/com/wornux/dto/request/EmployeeUpdateRequestDto'

export default function EmployeesView() {
  const [refreshKey, setRefreshKey] = useState(0)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const handleEmployeeUpdate = useCallback(async (dto: EmployeeUpdateRequestDto) => {
    if (dto.id) {
      try {
        const updatedEmployee = await EmployeeServiceImpl.update(dto)
        return updatedEmployee
      } catch (error) {
        setErrorMessage('Error actualizando empleado')
        throw error
      }
    }
    throw new Error('Datos del empleado inválidos')
  }, [])

   return (
    <main className="w-full h-full flex flex-col box-border gap-s p-m">
        <h3 className="text-xl font-semibold"> Editar Empleados </h3>
      <AutoCrud
          className=".auto-crud-form-header: { display: none; }"
          service={{
            ...EmployeeServiceImpl,
            save: handleEmployeeUpdate,
          }}
          model={EmployeeUpdateRequestDtoModel}
          gridProps={{
            columnOptions: AUTO_GRID_EMPLOYEE_FIELD_OPTIONS,
            hiddenColumns: ['id', 'password'],
          }}
          formProps={{
            fieldOptions: AUTO_FORM_EMPLOYEE_FIELD_OPTIONS,
            hiddenFields: ['id', 'password'],
          }}
          style={{ flexGrow: '1' }}
          noNewButton={true}
      />
        <style>
            {`
      .auto-crud-form-header {
        display: none !important;
      }
    `}
        </style>
    </main>
  )
}

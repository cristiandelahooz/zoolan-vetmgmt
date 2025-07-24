import { useState, useCallback } from 'react'
import { EmployeeServiceImpl } from '@/generated/endpoints'
import { AUTO_FORM_EMPLOYEE_FIELD_OPTIONS, AUTO_GRID_EMPLOYEE_FIELD_OPTIONS } from '@/lib/constants/employee-field-config'
import {AutoCrud} from "@vaadin/hilla-react-crud";
import EmployeeListDto from "@/generated/com/wornux/dto/response/EmployeeListDto";
import EmployeeListDtoModel from "@/generated/com/wornux/dto/response/EmployeeListDtoModel";

export default function EmployeesView() {
    const [refreshKey, setRefreshKey] = useState(0)

    const handleEmployeeUpdate = useCallback(async (dto: EmployeeListDto) => {
        if (dto.id) {
            try {
                const updatedEmployee = await EmployeeServiceImpl.updateEmployeeFromListDto(dto)
                setRefreshKey((prev) => prev + 1)
                return updatedEmployee
            } catch (error) {
                console.error('Error updating employee:', error)
                throw error
            }
        }
        throw new Error('Invalid employee data')
    }, [])

    return (
        <main className="w-full h-full flex flex-col box-border gap-s p-m">
            <AutoCrud
                key={refreshKey}
                service={{
                    ...EmployeeServiceImpl,
                    save: handleEmployeeUpdate,
                }}
                model={EmployeeListDtoModel}
                gridProps={{
                    columnOptions: AUTO_GRID_EMPLOYEE_FIELD_OPTIONS,
                    hiddenColumns: ['id','password'],
                }}
                formProps={{
                    fieldOptions: AUTO_FORM_EMPLOYEE_FIELD_OPTIONS,
                    hiddenFields: ['id','password'],
                }}
                style={{ flexGrow: '1' }}
                noNewButton={true}
            />
        </main>
    )
}
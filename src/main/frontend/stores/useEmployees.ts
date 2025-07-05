import type Pageable from '@/generated/com/vaadin/hilla/mappedtypes/Pageable'
import type Sort from '@/generated/com/vaadin/hilla/mappedtypes/Sort'
import type Employee from '@/generated/com/wornux/data/entity/Employee'
import { EmployeeServiceImpl } from '@/generated/endpoints'
import { useEffect, useState } from 'react'

const unpagedPageable: Pageable = { pageNumber: 0, pageSize: 2000, sort: { orders: [] } as Sort }

export const useEmployees = () => {
  const [employees, setEmployees] = useState<Employee[]>([])

  useEffect(() => {
    const fetchEmployees = async () => {
      try {
        const employeeList = await EmployeeServiceImpl.getAllEmployees(unpagedPageable)
        setEmployees((employeeList as any)?.content ?? [])
      } catch (error) {
        console.error('Error fetching employees:', error)
      }
    }

    fetchEmployees()
  }, [])

  return { employees }
}

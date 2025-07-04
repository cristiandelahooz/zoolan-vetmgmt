import { EmployeeServiceImpl } from '@/generated/endpoints';
import type Employee from '@/generated/com/zoolandia/app/features/employee/domain/Employee';
import { useEffect, useState } from 'react';
import type Pageable from 'Frontend/generated/com/vaadin/hilla/mappedtypes/Pageable';
import type Sort from 'Frontend/generated/com/vaadin/hilla/mappedtypes/Sort';

const unpagedPageable: Pageable = { pageNumber: 0, pageSize: 2000, sort: { orders: [] } as Sort };

export const useEmployees = () => {
  const [employees, setEmployees] = useState<Employee[]>([]);

  useEffect(() => {
    const fetchEmployees = async () => {
      try {
        const employeeList = await EmployeeServiceImpl.getAllEmployees(unpagedPageable);
        setEmployees((employeeList as any)?.content ?? []);
      } catch (error) {
        console.error('Error fetching employees:', error);
      }
    };

    fetchEmployees();
  }, []);

  return { employees };
};

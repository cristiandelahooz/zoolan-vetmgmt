import {AutoForm, AutoGrid} from '@vaadin/hilla-react-crud';
import  EmployeeModel from 'Frontend/generated/com/zoolandia/app/features/employee/domain/EmployeeModel';
import { EmployeeServiceImpl } from 'Frontend/generated/endpoints';
import EmployeeCreateDTOModel
    from "Frontend/generated/com/zoolandia/app/features/employee/service/dto/EmployeeCreateDTOModel";
import {AUTO_GRID_EMPLOYEE_FIELD_OPTIONS} from "Frontend/lib/constants/employee-field-config";

export default function EmployeesView() {
    return (
            <main className="w-full h-full flex flex-col box-border gap-s p-m">
                <AutoGrid service={EmployeeServiceImpl} model={EmployeeCreateDTOModel}
                          columnOptions={AUTO_GRID_EMPLOYEE_FIELD_OPTIONS}
                />
            </main>
    );
}
import { AutoGrid } from '@vaadin/hilla-react-crud';
import  EmployeeModel from 'Frontend/generated/com/zoolandia/app/features/employee/domain/EmployeeModel';
import { EmployeeServiceImpl } from 'Frontend/generated/endpoints';

export default function EmployeesView() {
    return <AutoGrid service={EmployeeServiceImpl} model={EmployeeModel} />;
}
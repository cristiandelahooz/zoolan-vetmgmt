import {AutoForm, AutoGrid} from '@vaadin/hilla-react-crud';
import  EmployeeModel from 'Frontend/generated/com/zoolandia/app/features/employee/domain/EmployeeModel';
import { EmployeeServiceImpl } from 'Frontend/generated/endpoints';
import EmployeeCreateDTOModel
    from "Frontend/generated/com/zoolandia/app/features/employee/service/dto/EmployeeCreateDTOModel";
import {PasswordField} from "@vaadin/react-components";

export default function EmployeesView() {
    //return <AutoGrid service={EmployeeServiceImpl} model={EmployeeModel} />;
    return (
            <main className="w-full h-full flex flex-col box-border gap-s p-m">
                <AutoGrid service={EmployeeServiceImpl} model={EmployeeCreateDTOModel}
                          columnOptions={{
                              username: {
                                  header: 'Nombre de Usuario',
                              },
                              password: {
                                  renderer: ({ item }) => <span> ••••••</span>,
                                  header: 'Contraseña',
                              },
                              email: {
                                  header: 'Correo Electrónico',
                              },
                              firstName: {
                                  header: 'Nombre',
                              },
                              lastName: {
                                  header: 'Apellido',
                              },
                              phoneNumber: {
                                  header: 'Número de Teléfono',
                              },
                              birthDate: {
                                  header: 'Fecha de Nacimiento',
                              },
                              gender: {
                                  header: 'Género',
                              },
                              nationality: {
                                  header: 'Nacionalidad',
                              },
                              employeeRole: {
                                  header: 'Rol del Empleado',
                              },
                              municipality: {
                                  header: 'Municipio',
                              },
                              province: {
                                  header: 'Provincia',
                              },
                              streetAddress: {
                                  header: 'Calle'
                              },
                              hireDate: {
                                  header: 'Fecha de Contratación',
                              },
                              salary: {
                                  header: 'Salario',
                              },
                              notes: {
                                  header: 'Notas',
                              },
                              available: {
                                  header: 'Disponible',
                              },
                              active: {
                                  header: 'Activo',
                              },
                          }}
                />
            </main>
    );
}
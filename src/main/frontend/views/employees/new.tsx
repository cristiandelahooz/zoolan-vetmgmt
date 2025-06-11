import { AutoForm } from "@vaadin/hilla-react-crud";
import { EmployeeServiceImpl } from "Frontend/generated/endpoints";
import EmployeeCreateDTOModel from "Frontend/generated/com/zoolandia/app/features/employee/service/dto/EmployeeCreateDTOModel";
import type { ViewConfig } from '@vaadin/hilla-file-router/types.js'
import { useNavigate } from 'react-router';
import { Notification, PasswordField } from '@vaadin/react-components'
import type EmployeeCreateDTO from 'Frontend/generated/com/zoolandia/app/features/employee/service/dto/EmployeeCreateDTO';

export const config: ViewConfig = {
    title: 'Registrar Empleado',
}

export default function EmployeesRegisterView() {
    const navigate = useNavigate();
    const handleOnSubmitSuccess = ({ item }: { item: EmployeeCreateDTO }) => {
        Notification.show('Empleado registrado', { duration: 3000, position: 'bottom-end', theme: 'success' });
        console.log(item);
        navigate(`/employees/`, { replace: true });
    }
    return (
        <main className="w-full h-full flex flex-col box-border gap-s p-m">
            <AutoForm service={EmployeeServiceImpl} model={EmployeeCreateDTOModel} onSubmitSuccess={handleOnSubmitSuccess}
                      fieldOptions={{
                          username: {
                              label: 'Nombre de Usuario',
                          },
                          password: {
                              renderer: ({ field }) => <PasswordField {...field} />,
                              label: 'Contraseña',
                          },
                          email: {
                              label: 'Correo Electrónico',
                          },
                          firstName: {
                              label: 'Nombre',
                          },
                          lastName: {
                              label: 'Apellido',
                          },
                          phoneNumber: {
                              label: 'Número de Teléfono',
                          },
                          birthDate: {
                              label: 'Fecha de Nacimiento',
                          },
                          gender: {
                              label: 'Género',
                          },
                          nationality: {
                              label: 'Nacionalidad',
                          },
                          employeeRole: {
                              label: 'Rol del Empleado',
                          },
                          municipality: {
                              label: 'Municipio',
                          },
                          province: {
                              label: 'Provincia',
                          },
                          streetAddress: {
                              label: 'Calle'
                            },
                          hireDate: {
                              label: 'Fecha de Contratación',
                          },
                          salary: {
                              label: 'Salario',
                          },
                          notes: {
                              label: 'Notas',
                          },
                          available: {
                                label: 'Disponible',
                          },
                          active: {
                                label: 'Activo',
                          },
                      }}
            />
        </main>
    );
}
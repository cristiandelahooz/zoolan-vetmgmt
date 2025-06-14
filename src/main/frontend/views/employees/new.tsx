import { AutoForm, AutoFormLayoutRendererProps } from '@vaadin/hilla-react-crud'
import { AUTO_FORM_EMPLOYEE_FIELD_OPTIONS } from 'Frontend/lib/constants/employee-field-config'
import { EmployeeServiceImpl } from 'Frontend/generated/endpoints'
import EmployeeCreateDTOModel from 'Frontend/generated/com/zoolandia/app/features/employee/service/dto/EmployeeCreateDTOModel'
import { useNavigate } from 'react-router'
import { HorizontalLayout, Notification, VerticalLayout } from '@vaadin/react-components'
import type EmployeeCreateDTO from 'Frontend/generated/com/zoolandia/app/features/employee/service/dto/EmployeeCreateDTO'
import { SubmitErrorEvent } from '@vaadin/hilla-react-crud/'

function GroupingLayoutRenderer({ children }: AutoFormLayoutRendererProps<EmployeeCreateDTOModel>) {
  const fieldsMapping = new Map<string, JSX.Element>()
  children.forEach((field) => fieldsMapping.set(field.props?.propertyInfo?.name, field))

  return (
    <VerticalLayout>
      <h4>
        {' '}
        <strong>Informacion del usuario:</strong>{' '}
      </h4>
      <VerticalLayout style={{ marginBottom: '1.5rem' }}>
        <HorizontalLayout theme="spacing" className="pb-l">
          {fieldsMapping.get('username')}
          {fieldsMapping.get('password')}
          {fieldsMapping.get('firstName')}
          {fieldsMapping.get('lastName')}
          {fieldsMapping.get('email')}
        </HorizontalLayout>
        <HorizontalLayout theme="spacing" className="pb-l">
          {fieldsMapping.get('phoneNumber')}
          {fieldsMapping.get('birthDate')}
          {fieldsMapping.get('gender')}
          {fieldsMapping.get('nationality')}
          {fieldsMapping.get('profilePicture')}
        </HorizontalLayout>
      </VerticalLayout>
      <h4>
        <strong> Direccion del empleado: </strong>
      </h4>
      <VerticalLayout style={{ marginBottom: '1.5rem' }}>
        <HorizontalLayout theme="spacing" className="pb-l items-baseline">
          {fieldsMapping.get('province')}
          {fieldsMapping.get('municipality')}
          {fieldsMapping.get('sector')}
          {fieldsMapping.get('streetAddress')}
        </HorizontalLayout>
      </VerticalLayout>
      <h4>
        <strong> Informacion del empleado: </strong>
      </h4>
      <VerticalLayout style={{ marginBottom: '1.5rem' }}>
        <HorizontalLayout theme="spacing" className="pb-l items-baseline">
          {fieldsMapping.get('employeeRole')}
          {fieldsMapping.get('salary')}
          {fieldsMapping.get('hireDate')}
          {fieldsMapping.get('available')}
          {fieldsMapping.get('isActive')}
          {fieldsMapping.get('workSchedule')}
        </HorizontalLayout>
        <HorizontalLayout theme="spacing" className="pb-l items-baseline">
          {fieldsMapping.get('emergencyContactName')}
          {fieldsMapping.get('emergencyContactPhone')}
        </HorizontalLayout>
      </VerticalLayout>
    </VerticalLayout>
  )
}

export default function EmployeesRegisterView() {
  const navigate = useNavigate()
  const handleOnSubmitSuccess = ({ item }: { item: EmployeeCreateDTO }) => {
    Notification.show('Empleado registrado', { duration: 3000, position: 'bottom-end', theme: 'success' })
    navigate(`/employees/`, { replace: true })
  }

  const handleOnSubmitError = (error: SubmitErrorEvent) => {
    const message = error.error?.message || 'Unknown error'
    Notification.show(`Error al registrar el empleado: ${message}`, {
      duration: 5000,
      position: 'bottom-end',
      theme: 'error',
    })
  }

  return (
    <main className="w-full h-full flex flex-col box-border gap-s p-m">
      <AutoForm
        service={EmployeeServiceImpl}
        model={EmployeeCreateDTOModel}
        onSubmitSuccess={handleOnSubmitSuccess}
        onSubmitError={handleOnSubmitError}
        fieldOptions={AUTO_FORM_EMPLOYEE_FIELD_OPTIONS}
        layoutRenderer={GroupingLayoutRenderer}
      />
    </main>
  )
}

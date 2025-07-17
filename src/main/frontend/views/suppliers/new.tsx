import type SupplierCreateRequestDto from '@/generated/com/wornux/dto/request/SupplierCreateRequestDto'
import SupplierCreateRequestDtoModel from '@/generated/com/wornux/dto/request/SupplierCreateRequestDtoModel'
import { SupplierServiceImpl } from '@/generated/endpoints'
import { AutoForm, type AutoFormLayoutRendererProps, type SubmitErrorEvent } from '@vaadin/hilla-react-crud'
import { HorizontalLayout, Notification, VerticalLayout } from '@vaadin/react-components'
import { useNavigate } from 'react-router'

function GroupingLayoutRenderer({ children }: AutoFormLayoutRendererProps<SupplierCreateRequestDtoModel>) {
    const fieldsMapping = new Map<string, JSX.Element>()
    for (const field of children) {
        fieldsMapping.set(field.props?.propertyInfo?.name, field)
    }

    return (
        <VerticalLayout>
            <h4><strong>Información del Suplidor</strong></h4>
            <VerticalLayout style={{ marginBottom: '1.5rem' }}>
                <HorizontalLayout theme="spacing" className="pb-l">
                    {fieldsMapping.get('rnc')}
                    {fieldsMapping.get('companyName')}
                </HorizontalLayout>
                <HorizontalLayout theme="spacing" className="pb-l">
                    {fieldsMapping.get('contactPerson')}
                    {fieldsMapping.get('contactPhone')}
                    {fieldsMapping.get('contactEmail')}
                </HorizontalLayout>
                <h4><strong>Dirección</strong></h4>
                <HorizontalLayout theme="spacing" className="pb-l">
                    {fieldsMapping.get('province')}
                    {fieldsMapping.get('municipality')}
                    {fieldsMapping.get('sector')}
                    {fieldsMapping.get('streetAddress')}
                </HorizontalLayout>
                <HorizontalLayout theme="spacing" className="pb-l">
                    {fieldsMapping.get('active')}
                </HorizontalLayout>
            </VerticalLayout>
        </VerticalLayout>
    )
}

export default function SuppliersRegisterView() {
    const navigate = useNavigate()

    const handleOnSubmitSuccess = ({ item }: { item: SupplierCreateRequestDto }) => {
        Notification.show('Suplidor registrado exitosamente', {
            duration: 3000,
            position: 'bottom-end',
            theme: 'success',
        })
        navigate('/suppliers/', { replace: true })
    }

    const handleOnSubmitError = (error: SubmitErrorEvent) => {
        const message = error.error?.message || 'Error desconocido'
        Notification.show(`Error al registrar el suplidor: ${message}`, {
            duration: 5000,
            position: 'bottom-end',
            theme: 'error',
        })
    }

    return (
        <main className="w-full h-full flex flex-col box-border gap-s p-m">
            <AutoForm
                service={SupplierServiceImpl}
                model={SupplierCreateRequestDtoModel}
                onSubmitSuccess={handleOnSubmitSuccess}
                onSubmitError={handleOnSubmitError}
                layoutRenderer={GroupingLayoutRenderer}
            />
        </main>
    )
}

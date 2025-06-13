import { useState } from 'react';
import { AutoForm } from '@vaadin/hilla-react-crud';
import { TextField, Notification, IntegerField } from '@vaadin/react-components';
import { useNavigate } from 'react-router';
import PetCreateDTOModel from 'Frontend/generated/com/zoolandia/app/features/pet/service/dto/PetCreateDTOModel';
import { PetServiceImpl } from 'Frontend/generated/endpoints';
import { SelectOwnerDialog } from './SelectOwnerDialog';

export default function PetEntryFormView() {
    const navigate = useNavigate();
    const [dialogOpen, setDialogOpen] = useState(false);
    const [ownerName, setOwnerName] = useState('');
    const [ownerId, setOwnerId] = useState(0);

    function handleOnSubmitSuccess() {
        Notification.show('Mascota registrada exitosamente');
        navigate('/pets');
    }

    return (
        <>
            <main className="w-full h-full flex flex-col box-border gap-s p-m">
                <AutoForm
                    service={PetServiceImpl}
                    model={PetCreateDTOModel}
                    onSubmitSuccess={handleOnSubmitSuccess}
                    fieldOptions={{
                        ownerId: {
                            label: 'Dueño',
                            renderer: ({ field }) => {
                                return (
                                    <IntegerField
                                       hidden  {...field}
                                    />
                                );
                            }
                        }
                    }}
                />


                <div>
                    <TextField
                        label="Seleccionar Dueño"
                        value={ownerName}
                        readonly
                        onClick={() => setDialogOpen(true)}
                    />
                </div>
            </main>

            <SelectOwnerDialog
                open={dialogOpen}
                onClose={() => setDialogOpen(false)}
                onSelect={(client) => {
                    setOwnerName(`${client.firstName} ${client.lastName}`);
                    setOwnerId(client.id);


                    setTimeout(() => {

                        const ownerIdInput = document.querySelector('vaadin-integer-field[name="ownerId"]') as any;

                        if (ownerIdInput) {

                            ownerIdInput.value = client.id.toString();


                            ownerIdInput.dispatchEvent(new CustomEvent('change', {
                                detail: { value: client.id.toString() },
                                bubbles: true
                            }));
                            ownerIdInput.dispatchEvent(new CustomEvent('input', {
                                detail: { value: client.id.toString() },
                                bubbles: true
                            }));

                            console.log('Value set and events dispatched');
                        } else {
                            console.error('Could not find ownerId field');
                        }
                    }, 0);

                    setDialogOpen(false);
                }}
            />
        </>
    );
}
import { AutoGrid } from '@vaadin/hilla-react-crud';
import { Button, Dialog } from '@vaadin/react-components';
import { useState } from 'react';
import { ClientServiceImpl } from '@/generated/endpoints';
import ClientModel from '@/generated/com/zoolandia/app/features/client/domain/ClientModel';
import type Client from '@/generated/com/zoolandia/app/features/client/domain/Client';

export type SelectedClient = Pick<Client, 'id' | 'firstName' | 'lastName' | 'cedula' | 'passport'>;

interface SelectClientDialogProps {
  open: boolean;
  onClose: () => void;
  onSelect: (client: SelectedClient) => void;
}

export function SelectClientDialog({ open, onClose, onSelect }: SelectClientDialogProps) {
  const [selectedClient, setSelectedClient] = useState<Client | null>(null);

  return (
    <Dialog
      headerTitle="Select a Client"
      opened={open}
      onOpenedChanged={({ detail }) => !detail.value && onClose()}
      footer={
        <div className="flex gap-s justify-end">
          <Button theme="tertiary" onClick={onClose}>
            Cancel
          </Button>
          <Button
            theme="primary"
            onClick={() => {
              if (selectedClient) {
                onSelect(selectedClient);
              }
            }}
            disabled={!selectedClient}
          >
            Select
          </Button>
        </div>
      }
    >
      <div className="p-m" style={{ width: '800px', height: '600px' }}>
        <AutoGrid
          service={ClientServiceImpl}
          model={ClientModel}
          onActiveItemChanged={({ detail }) => {
            if (detail.value) {
              setSelectedClient(detail.value);
            } else {
              setSelectedClient(null);
            }
          }}
          visibleColumns={['firstName', 'lastName', 'cedula', 'email', 'phoneNumber']}
        />
      </div>
    </Dialog>
  );
}

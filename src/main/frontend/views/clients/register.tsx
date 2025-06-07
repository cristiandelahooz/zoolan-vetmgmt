import React, { useState } from 'react';
import { TextField } from '@vaadin/react-components/TextField';
import { Button } from '@vaadin/react-components/Button';
import { Notification } from '@vaadin/react-components/Notification';
import { HorizontalLayout } from '@vaadin/react-components/HorizontalLayout';
import type { ViewConfig } from '@vaadin/hilla-file-router/types.js';


export const config: ViewConfig = {
  title: 'Register Client',
  
};

export default function Register() {
  const [name, setName] = useState('');

  return (
    <>
      <HorizontalLayout className="h-full items-baseline gap-m">
        
        
      </HorizontalLayout>
    </>
  );
}

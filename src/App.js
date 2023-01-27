/**
 * @format
 * @flow strict-local
 */

import React from 'react';

import Navigator from './navigation';
import { BluetoothProvider } from './context/BluetoothContext';

export default function App() {
  return (
    <BluetoothProvider>
      <Navigator />
    </BluetoothProvider>
  );
}



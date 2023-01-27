import { useFocusEffect } from '@react-navigation/native';
import { useState, useCallback, useEffect } from 'react';
import { View, Text, Button, StyleSheet, StatusBar } from 'react-native';

import RTNBluetooth from '../../RTNBluetooth';

import DeviceList from '../components/DeviceList';

const styles = StyleSheet.create({
    contaier: {
        flex: 1,
        marginTop: StatusBar.currentHeight - 8,
    }
})

const enabledOrDisabled = async () => {

    try {
        const enabled = await RTNBluetooth.bluetoothEnabledOrDisabled()
        console.log(enabled)

    } catch (error) {
        console.log(error)
    }
}

import { bluetoothContext } from '../context/BluetoothContext';

export default function SettingsScreen({ navigation }) {
    const {
        isEnabled,
        isDiscovering,
        pairToDevice,
        unpairToDevice,
        blutoothState,
    } = bluetoothContext()

    const [bondedDevices, setBondedDevices] = useState([])
    const [discoveredDevices, setDiscoveredDevices] = useState([])

    const startDiscovery = useCallback(async () => {
        try {
            const devices = await RTNBluetooth.startDiscovery()
            setDiscoveredDevices(devices)
        } catch (error) {
            console.log(error)
        }
    }, []);

    useEffect(() => {
        console.log(blutoothState)
    }, [])

    useFocusEffect
        (useCallback(() => {
            const getBondedDevices = async () => {
                try {
                    const devices = await RTNBluetooth.getBondedDevices()
                    setBondedDevices(devices)
                } catch (error) {
                    console.log(error)
                }
            }

            getBondedDevices()
        }, [])
        );

    return (
        <View style={styles.contaier}>
            <Text>{isEnabled.toString()}</Text>
            <Button title='press me' onPress={() => enabledOrDisabled() } />
            <Text>{"Bonded"}</Text>
            <DeviceList DATA={bondedDevices} />
            {isDiscovering ? <Text>Loading...</Text> :
                <View>
                    <Text>{"Discovered"}</Text>
                    <DeviceList DATA={discoveredDevices} onPress={pairToDevice} />

                </View>
            }

            <Button title='Press me' onPress={() => startDiscovery()} />
        </View>
    );
}
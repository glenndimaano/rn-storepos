import { useFocusEffect } from "@react-navigation/native";
import { createContext, useContext, useState, useCallback, useEffect } from "react";
import { NativeEventEmitter } from "react-native";

import RTNBluetooth from '../../RTNBluetooth'

const bluetoothEmitter = new NativeEventEmitter(RTNBluetooth);
const BluetoothContext = createContext({});

export const bluetoothContext = () => useContext(BluetoothContext)

async function pairToDevice(address) {
    try {
        const device = await RTNBluetooth.pairToDevice(address)
        console.log(device)
    } catch (error) {
        console.log(error)
    }
}

async function unpairToDevice(address) {
    try {
        const device = await RTNBluetooth.unpairToDevice(address)
        console.log(device)
    } catch (error) {
        console.log(error)
    }
}

export function BluetoothProvider({ children }) {
    const [isEnabled, setIsEnabled] = useState(false)
    const [bondedDevices, setBondedDevices] = useState([])
    const [discoveredDevices, setDiscoveredDevices] = useState([])
    const [isDiscovering, setIsDiscovering] = useState(false)
    const [bluetoothState, setBluetoothState] = useState(false)


    const isBluetoothEnabled = useCallback(async () => {
        try {
            const state = await RTNBluetooth.isBluetoothEnabled()
            setIsEnabled(state)
        } catch (error) {
            setIsEnabled(error)
        }
    }, [])

    const getBondedDevices = useCallback(async () => {
        try {
            const devices = await RTNBluetooth.getBondedDevices()
            setBondedDevices(devices)
        } catch (error) {
            console.log(error)
        }
    }, [])

    const startDiscovery = useCallback(async () => {
        try {
            const devices = await RTNBluetooth.startDiscovery()
            setDiscoveredDevices(devices)
        } catch (error) {
            console.log(error)
        }
    }, [])

    useEffect(() => {
        isBluetoothEnabled()

        getBondedDevices()

        const onDeviceFound = bluetoothEmitter.addListener("onDeviceFound", (device) => {
            setDiscoveredDevices(device)
        })

        const onDiscovering = bluetoothEmitter.addListener("onDiscovering", ({ state }) => {
            setIsDiscovering(state)
        })

        const getBluetoothState = bluetoothEmitter.addListener("onStateChange", ({state}) => {
            console.log(state.toString())
            setBluetoothState(state)
        })

        return () => {
            onDeviceFound.remove()
            onDiscovering.remove()
            getBluetoothState.remove()
        }

    }, [])

    let title = "Bluetooth"

    return (
        <BluetoothContext.Provider value={{
            title,
            isEnabled,
            bondedDevices,
            startDiscovery,
            isDiscovering,
            discoveredDevices,
            pairToDevice,
            unpairToDevice,
            bluetoothState
        }}>
            {children}
        </BluetoothContext.Provider>
    )
}

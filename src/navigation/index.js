import { NavigationContainer } from '@react-navigation/native';

import AppTab from './tab/AppTab';

export default function Navigator() {
    return (
        <NavigationContainer>
            <AppTab />
        </NavigationContainer>
    )
}
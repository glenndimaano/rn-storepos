import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import HomeStackScreen from '../stack/HomeStackScreen';

import { View, Text, Button } from 'react-native';

function SampleScreen({ navigation }) {
    return (
        <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
            <Text>Sample Screen</Text>
            <Button title='Press me' onPress={() => navigation.navigate("B")} />
        </View>
    );
}

const Tab = createBottomTabNavigator();

export default function AppTab() {
    return (
        <Tab.Navigator>
        <Tab.Screen name="Home" component={HomeStackScreen} options={{ headerShown: false}} />
        <Tab.Screen name="Sample" component={SampleScreen} />
      </Tab.Navigator>
    )
}
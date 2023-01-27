import { createNativeStackNavigator } from '@react-navigation/native-stack';

import HomeScreen from '../../screen/HomeScreen';
import SettingsScreen from '../../screen/SettingsScreen';

const HomeStack = createNativeStackNavigator();

export default function HomeStackScreen() {
    return (
      <HomeStack.Navigator>
        <HomeStack.Screen
          name="A"
          component={HomeScreen}
          options={{ tabBarLabel: 'Home!' }}
        />
        <HomeStack.Screen
          name="B"
          component={SettingsScreen}
          options={{ tabBarLabel: 'Home!' }}
        />
      </HomeStack.Navigator>
    );
  }
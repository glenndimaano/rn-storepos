import { View, Text, Button } from 'react-native';

import { AFLrequestPermission } from '../util/permissions';

export default function HomeScreen({ navigation }) {

    AFLrequestPermission()

    return (
        <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
            <Text>Home Screen</Text>
            <Button title='Press me' onPress={() => navigation.navigate("B")} />
        </View>
    );
}
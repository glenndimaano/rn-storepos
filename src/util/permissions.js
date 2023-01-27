import { PermissionsAndroid, Platform } from "react-native"

export async function AFLrequestPermission() {
    try { 
      if (Platform.OS === 'android') {
        const available = await PermissionsAndroid.check('android.permission.ACCESS_FINE_LOCATION')
        if (!available) {
          const granted = await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION)
          if  (granted == PermissionsAndroid.RESULTS.GRANTED) {
            console.log("user granted")
          } else {
            console.log("user denied")
          }
        }
      }
    } catch (error) {
      if (error) {
        console.log(error)
      }
    }
}
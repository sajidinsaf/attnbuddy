import * as Notifications from 'expo-notifications';
import * as Device from 'expo-device';
import Constants from 'expo-constants';
import { Platform } from 'react-native';
import { apiRequest } from './api';

// Configure how notifications appear when app is in foreground
Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: true,
    shouldPlaySound: true,
    shouldSetBadge: false,
    shouldShowBanner: true,
    shouldShowList: true,
  }),
});

export async function registerForPushNotifications(token: string): Promise<string | null> {
  if (!Device.isDevice) {
    console.log('Push notifications require a physical device');
    return null;
  }

  // Request permission
  const { status: existingStatus } = await Notifications.getPermissionsAsync();
  let finalStatus = existingStatus;

  if (existingStatus !== 'granted') {
    const { status } = await Notifications.requestPermissionsAsync();
    finalStatus = status;
  }

  if (finalStatus !== 'granted') {
    console.log('Push notification permission not granted');
    return null;
  }

  // Get Expo push token
  let pushToken;
  try {
    const projectId = Constants.expoConfig?.extra?.eas?.projectId || undefined;
    pushToken = await Notifications.getExpoPushTokenAsync({ projectId });
  } catch (e) {
    console.log('Could not get push token (EAS project ID may not be configured):', e);
    return null;
  }

  // Register with backend
  try {
    await apiRequest('/api/push-token', {
      method: 'POST',
      token,
      body: {
        token: pushToken.data,
        deviceName: `${Device.modelName || 'Unknown'} (${Platform.OS})`,
      },
    });
  } catch (e) {
    console.error('Failed to register push token:', e);
  }

  return pushToken.data;
}

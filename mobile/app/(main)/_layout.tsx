import { Tabs } from 'expo-router';
import { Text } from 'react-native';

function TabIcon({ name, focused }: { name: string; focused: boolean }) {
  const icons: Record<string, string> = {
    now: '◉',
    capture: '+',
    settings: '⚙',
  };
  return (
    <Text style={{ fontSize: name === 'capture' ? 28 : 22, color: focused ? '#6366F1' : '#64748B' }}>
      {icons[name] || '•'}
    </Text>
  );
}

export default function MainLayout() {
  return (
    <Tabs
      screenOptions={{
        headerStyle: { backgroundColor: '#0F172A' },
        headerTintColor: '#F8FAFC',
        tabBarStyle: {
          backgroundColor: '#0F172A',
          borderTopColor: '#1E293B',
          borderTopWidth: 1,
          paddingTop: 8,
          height: 88,
        },
        tabBarActiveTintColor: '#6366F1',
        tabBarInactiveTintColor: '#64748B',
        tabBarLabelStyle: {
          fontSize: 12,
          fontWeight: '500',
        },
      }}
    >
      <Tabs.Screen
        name="now"
        options={{
          title: 'Right Now',
          headerTitle: 'Right Now',
          tabBarIcon: ({ focused }) => <TabIcon name="now" focused={focused} />,
        }}
      />
      <Tabs.Screen
        name="capture"
        options={{
          title: 'Capture',
          headerTitle: 'Quick Capture',
          tabBarIcon: ({ focused }) => <TabIcon name="capture" focused={focused} />,
        }}
      />
      <Tabs.Screen
        name="settings"
        options={{
          title: 'Settings',
          headerTitle: 'Settings',
          tabBarIcon: ({ focused }) => <TabIcon name="settings" focused={focused} />,
        }}
      />
    </Tabs>
  );
}

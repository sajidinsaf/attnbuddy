import { View, Text, TouchableOpacity, StyleSheet, ScrollView, Alert } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useCallback, useState } from 'react';
import { useFocusEffect, router } from 'expo-router';
import { useAuth } from '../../contexts/AuthContext';
import { apiRequest } from '../../services/api';
import { Domain } from '../../types/api';

export default function SettingsScreen() {
  const { token, logout } = useAuth();
  const [domains, setDomains] = useState<Domain[]>([]);

  useFocusEffect(useCallback(() => {
    if (!token) return;
    apiRequest<Domain[]>('/api/domains', { token })
      .then(setDomains)
      .catch(() => {});
  }, [token]));

  const handleLogout = () => {
    Alert.alert('Sign Out', 'Are you sure?', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Sign Out',
        style: 'destructive',
        onPress: async () => {
          await logout();
          router.replace('/(auth)/login');
        },
      },
    ]);
  };

  return (
    <SafeAreaView style={styles.container} edges={['bottom']}>
      <ScrollView style={styles.content}>
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>LIFE DOMAINS</Text>
          {domains.map((d) => (
            <View key={d.id} style={styles.domainCard}>
              <View style={styles.domainHeader}>
                <View style={[styles.domainDot, { backgroundColor: d.color }]} />
                <Text style={styles.domainName}>{d.name}</Text>
              </View>
              <View style={styles.domainMeta}>
                <Text style={styles.domainWeight}>Weight: {d.weight}</Text>
                {d.activeStart && d.activeEnd && (
                  <Text style={styles.domainHours}>{d.activeStart} - {d.activeEnd}</Text>
                )}
              </View>
            </View>
          ))}
          {domains.length === 0 && (
            <Text style={styles.emptyText}>No domains loaded</Text>
          )}
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>ABOUT</Text>
          <View style={styles.aboutCard}>
            <Text style={styles.aboutText}>Brasstacks v1.0.0</Text>
            <Text style={styles.aboutSubtext}>Your focus companion</Text>
          </View>
        </View>

        <TouchableOpacity style={styles.logoutButton} onPress={handleLogout}>
          <Text style={styles.logoutText}>Sign Out</Text>
        </TouchableOpacity>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#0F172A' },
  content: { flex: 1, paddingHorizontal: 24, paddingTop: 16 },
  section: { marginBottom: 32 },
  sectionTitle: {
    color: '#6366F1', fontSize: 12, fontWeight: '700', letterSpacing: 2, marginBottom: 12,
  },
  domainCard: {
    backgroundColor: '#1E293B', borderRadius: 12, padding: 16, marginBottom: 8,
    borderWidth: 1, borderColor: '#334155',
  },
  domainHeader: { flexDirection: 'row', alignItems: 'center' },
  domainDot: { width: 12, height: 12, borderRadius: 6, marginRight: 10 },
  domainName: { color: '#F8FAFC', fontSize: 16, fontWeight: '600' },
  domainMeta: { flexDirection: 'row', justifyContent: 'space-between', marginTop: 8 },
  domainWeight: { color: '#64748B', fontSize: 13 },
  domainHours: { color: '#64748B', fontSize: 13 },
  emptyText: { color: '#475569', fontSize: 14 },
  aboutCard: {
    backgroundColor: '#1E293B', borderRadius: 12, padding: 16,
    borderWidth: 1, borderColor: '#334155',
  },
  aboutText: { color: '#F8FAFC', fontSize: 16, fontWeight: '500' },
  aboutSubtext: { color: '#64748B', fontSize: 13, marginTop: 4 },
  logoutButton: {
    backgroundColor: '#1E293B', borderRadius: 12, paddingVertical: 16,
    alignItems: 'center', borderWidth: 1, borderColor: '#EF4444', marginBottom: 32,
  },
  logoutText: { color: '#EF4444', fontSize: 16, fontWeight: '600' },
});

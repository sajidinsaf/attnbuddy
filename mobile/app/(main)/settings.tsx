import { View, Text, TouchableOpacity, StyleSheet, ScrollView, Alert } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useCallback, useState } from 'react';
import { useFocusEffect, router } from 'expo-router';
import { useAuth } from '../../contexts/AuthContext';
import { apiRequest } from '../../services/api';
import { Domain } from '../../types/api';

type NudgeFrequency = 'EAGER' | 'MODERATE' | 'MINIMAL';
type Tone = 'SUPPORTIVE' | 'PEER' | 'PLAYFUL';
type NudgeSettings = { nudgeFrequency: string; quietStart: string | null; quietEnd: string | null };

const QUIET_PRESETS = [
  { label: 'Off', start: null, end: null },
  { label: '10pm–7am', start: '22:00', end: '07:00' },
  { label: '11pm–8am', start: '23:00', end: '08:00' },
  { label: '9pm–6am', start: '21:00', end: '06:00' },
];

export default function SettingsScreen() {
  const { token, logout } = useAuth();
  const [domains, setDomains] = useState<Domain[]>([]);
  const [nudgeFrequency, setNudgeFrequency] = useState<NudgeFrequency>('MODERATE');
  const [quietStart, setQuietStart] = useState<string | null>(null);
  const [quietEnd, setQuietEnd] = useState<string | null>(null);
  const [tone, setTone] = useState<Tone>('SUPPORTIVE');

  useFocusEffect(useCallback(() => {
    if (!token) return;
    apiRequest<Domain[]>('/api/domains', { token })
      .then(setDomains)
      .catch(() => {});
    apiRequest<NudgeSettings>('/api/settings/nudges', { token })
      .then(s => {
        setNudgeFrequency(s.nudgeFrequency as NudgeFrequency);
        setQuietStart(s.quietStart);
        setQuietEnd(s.quietEnd);
      })
      .catch(() => {});
    apiRequest<{ tone: string }>('/api/settings/tone', { token })
      .then(s => setTone(s.tone as Tone))
      .catch(() => {});
  }, [token]));

  const saveNudgeSettings = async (freq: NudgeFrequency, qStart: string | null, qEnd: string | null) => {
    if (!token) return;
    try {
      await apiRequest('/api/settings/nudges', {
        method: 'PUT', token,
        body: { nudgeFrequency: freq, quietStart: qStart, quietEnd: qEnd },
      });
    } catch (e: any) {
      Alert.alert('Error', e.message);
    }
  };

  const handleFrequencyChange = (freq: NudgeFrequency) => {
    setNudgeFrequency(freq);
    saveNudgeSettings(freq, quietStart, quietEnd);
  };

  const handleQuietChange = (start: string | null, end: string | null) => {
    setQuietStart(start);
    setQuietEnd(end);
    saveNudgeSettings(nudgeFrequency, start, end);
  };

  const handleToneChange = async (t: Tone) => {
    setTone(t);
    if (!token) return;
    try {
      await apiRequest('/api/settings/tone', { method: 'PUT', token, body: { tone: t } });
    } catch (e: any) {
      Alert.alert('Error', e.message);
    }
  };

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
          <Text style={styles.sectionTitle}>NUDGE FREQUENCY</Text>
          <View style={styles.chipRow}>
            {(['EAGER', 'MODERATE', 'MINIMAL'] as NudgeFrequency[]).map(f => (
              <TouchableOpacity
                key={f}
                style={[styles.chip, nudgeFrequency === f && styles.chipActive]}
                onPress={() => handleFrequencyChange(f)}
              >
                <Text style={[styles.chipText, nudgeFrequency === f && styles.chipTextActive]}>
                  {f.charAt(0) + f.slice(1).toLowerCase()}
                </Text>
              </TouchableOpacity>
            ))}
          </View>
          <Text style={styles.chipHint}>
            {nudgeFrequency === 'EAGER' ? 'More frequent nudges and reminders' :
             nudgeFrequency === 'MINIMAL' ? 'Only urgent deadline reminders' :
             'Balanced nudges for deadlines and stale tasks'}
          </Text>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>QUIET HOURS</Text>
          <View style={styles.chipRow}>
            {QUIET_PRESETS.map(p => {
              const isActive = p.start === quietStart && p.end === quietEnd;
              return (
                <TouchableOpacity
                  key={p.label}
                  style={[styles.chip, isActive && styles.chipActive]}
                  onPress={() => handleQuietChange(p.start, p.end)}
                >
                  <Text style={[styles.chipText, isActive && styles.chipTextActive]}>{p.label}</Text>
                </TouchableOpacity>
              );
            })}
          </View>
          <Text style={styles.chipHint}>No nudges during quiet hours</Text>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>APP TONE</Text>
          <View style={styles.chipRow}>
            {(['SUPPORTIVE', 'PEER', 'PLAYFUL'] as Tone[]).map(t => (
              <TouchableOpacity
                key={t}
                style={[styles.chip, tone === t && styles.chipActive]}
                onPress={() => handleToneChange(t)}
              >
                <Text style={[styles.chipText, tone === t && styles.chipTextActive]}>
                  {t.charAt(0) + t.slice(1).toLowerCase()}
                </Text>
              </TouchableOpacity>
            ))}
          </View>
          <Text style={styles.chipHint}>
            {tone === 'SUPPORTIVE' ? 'Warm and encouraging' :
             tone === 'PLAYFUL' ? 'Light and fun' :
             'Direct and matter-of-fact'}
          </Text>
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
  chipRow: { flexDirection: 'row', gap: 8, flexWrap: 'wrap' },
  chip: {
    paddingVertical: 8, paddingHorizontal: 16, borderRadius: 16,
    backgroundColor: '#1E293B', borderWidth: 1, borderColor: '#334155',
  },
  chipActive: { backgroundColor: '#6366F1', borderColor: '#6366F1' },
  chipText: { color: '#94A3B8', fontSize: 14, fontWeight: '600' },
  chipTextActive: { color: '#FFFFFF' },
  chipHint: { color: '#475569', fontSize: 12, marginTop: 8 },
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

import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert, ActivityIndicator } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useCallback, useState } from 'react';
import { useFocusEffect, router } from 'expo-router';
import { useAuth } from '../../contexts/AuthContext';
import { apiRequest } from '../../services/api';
import { Domain, Urgency, Importance } from '../../types/api';

type Quadrant = { urgency: Urgency; importance: Importance };

const quadrants: { label: string; q: Quadrant; color: string }[] = [
  { label: 'Urgent &\nImportant', q: { urgency: 'URGENT', importance: 'IMPORTANT' }, color: '#EF4444' },
  { label: 'Not Urgent &\nImportant', q: { urgency: 'NOT_URGENT', importance: 'IMPORTANT' }, color: '#6366F1' },
  { label: 'Urgent &\nNot Important', q: { urgency: 'URGENT', importance: 'NOT_IMPORTANT' }, color: '#F59E0B' },
  { label: 'Not Urgent &\nNot Important', q: { urgency: 'NOT_URGENT', importance: 'NOT_IMPORTANT' }, color: '#334155' },
];

export default function CaptureScreen() {
  const { token } = useAuth();
  const [title, setTitle] = useState('');
  const [selected, setSelected] = useState<Quadrant>({ urgency: 'NOT_URGENT', importance: 'IMPORTANT' });
  const [domains, setDomains] = useState<Domain[]>([]);
  const [selectedDomain, setSelectedDomain] = useState<number | null>(null);
  const [saving, setSaving] = useState(false);

  useFocusEffect(useCallback(() => {
    if (!token) return;
    apiRequest<Domain[]>('/api/domains', { token })
      .then(setDomains)
      .catch(() => {});
  }, [token]));

  const handleCapture = async () => {
    if (!title.trim() || !token) return;
    setSaving(true);
    try {
      await apiRequest('/api/tasks', {
        method: 'POST', token,
        body: {
          title: title.trim(),
          urgency: selected.urgency,
          importance: selected.importance,
          domainId: selectedDomain,
        },
      });
      setTitle('');
      Alert.alert('Captured!', 'Task added. It\'ll show up in Right Now when it\'s the priority.', [
        { text: 'Add Another' },
        { text: 'Go to Now', onPress: () => router.push('/(main)/now') },
      ]);
    } catch (e: any) {
      Alert.alert('Error', e.message);
    } finally {
      setSaving(false);
    }
  };

  const isQ = (q: Quadrant) => selected.urgency === q.urgency && selected.importance === q.importance;

  return (
    <SafeAreaView style={styles.container} edges={['bottom']}>
      <View style={styles.content}>
        <Text style={styles.heading}>What needs doing?</Text>

        <TextInput
          style={styles.input}
          placeholder="e.g., Review Q3 financials"
          placeholderTextColor="#64748B"
          value={title}
          onChangeText={setTitle}
          autoFocus
          returnKeyType="done"
          onSubmitEditing={handleCapture}
        />

        {domains.length > 0 && (
          <>
            <Text style={styles.sectionLabel}>Life domain</Text>
            <View style={styles.domainChips}>
              {domains.map((d) => (
                <TouchableOpacity
                  key={d.id}
                  style={[styles.chip, selectedDomain === d.id && { borderColor: d.color, backgroundColor: d.color + '20' }]}
                  onPress={() => setSelectedDomain(selectedDomain === d.id ? null : d.id)}
                >
                  <View style={[styles.chipDot, { backgroundColor: d.color }]} />
                  <Text style={[styles.chipText, selectedDomain === d.id && { color: d.color }]}>
                    {d.name}
                  </Text>
                </TouchableOpacity>
              ))}
            </View>
          </>
        )}

        <Text style={styles.sectionLabel}>Priority</Text>
        <View style={styles.grid}>
          {[0, 2].map((row) => (
            <View key={row} style={styles.gridRow}>
              {quadrants.slice(row, row + 2).map((q) => (
                <TouchableOpacity
                  key={q.label}
                  style={[styles.gridCell, { borderColor: q.color }, isQ(q.q) && { backgroundColor: q.color + '25' }]}
                  onPress={() => setSelected(q.q)}
                >
                  <Text style={[styles.cellText, isQ(q.q) && { color: q.color, fontWeight: '700' }]}>
                    {q.label}
                  </Text>
                </TouchableOpacity>
              ))}
            </View>
          ))}
        </View>

        <TouchableOpacity
          style={[styles.captureButton, !title.trim() && styles.disabled]}
          onPress={handleCapture}
          disabled={!title.trim() || saving}
        >
          {saving ? (
            <ActivityIndicator color="#FFFFFF" />
          ) : (
            <Text style={styles.captureButtonText}>Capture Task</Text>
          )}
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#0F172A' },
  content: { flex: 1, paddingHorizontal: 24, paddingTop: 16 },
  heading: { color: '#F8FAFC', fontSize: 24, fontWeight: '700' },
  input: {
    backgroundColor: '#1E293B', borderRadius: 12, paddingHorizontal: 16, paddingVertical: 16,
    fontSize: 18, color: '#F8FAFC', borderWidth: 1, borderColor: '#334155', marginTop: 16,
  },
  sectionLabel: { color: '#CBD5E1', fontSize: 14, fontWeight: '500', marginTop: 20, marginBottom: 10 },
  domainChips: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  chip: {
    flexDirection: 'row', alignItems: 'center', paddingHorizontal: 12, paddingVertical: 8,
    borderRadius: 20, borderWidth: 1.5, borderColor: '#334155', backgroundColor: '#1E293B',
  },
  chipDot: { width: 8, height: 8, borderRadius: 4, marginRight: 6 },
  chipText: { color: '#94A3B8', fontSize: 13, fontWeight: '500' },
  grid: { gap: 8 },
  gridRow: { flexDirection: 'row', gap: 8 },
  gridCell: {
    flex: 1, borderRadius: 12, padding: 14, alignItems: 'center', justifyContent: 'center',
    borderWidth: 2, backgroundColor: '#1E293B', minHeight: 70,
  },
  cellText: { color: '#94A3B8', fontSize: 12, textAlign: 'center', lineHeight: 16 },
  captureButton: {
    backgroundColor: '#6366F1', borderRadius: 12, paddingVertical: 16,
    alignItems: 'center', marginTop: 28,
  },
  disabled: { opacity: 0.4 },
  captureButtonText: { color: '#FFFFFF', fontSize: 16, fontWeight: '600' },
});

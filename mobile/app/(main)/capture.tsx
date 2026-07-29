import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert, ActivityIndicator, ScrollView } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useCallback, useState } from 'react';
import { useFocusEffect, router } from 'expo-router';
import { useAuth } from '../../contexts/AuthContext';
import { apiRequest } from '../../services/api';
import { Domain, Urgency, Importance, TaskType } from '../../types/api';

type Quadrant = { urgency: Urgency; importance: Importance };

const quadrants: { label: string; q: Quadrant; color: string }[] = [
  { label: 'Urgent &\nImportant', q: { urgency: 'URGENT', importance: 'IMPORTANT' }, color: '#EF4444' },
  { label: 'Not Urgent &\nImportant', q: { urgency: 'NOT_URGENT', importance: 'IMPORTANT' }, color: '#6366F1' },
  { label: 'Urgent &\nNot Important', q: { urgency: 'URGENT', importance: 'NOT_IMPORTANT' }, color: '#F59E0B' },
  { label: 'Not Urgent &\nNot Important', q: { urgency: 'NOT_URGENT', importance: 'NOT_IMPORTANT' }, color: '#334155' },
];

const effortOptions = [15, 30, 45, 60, 90, 120];

export default function CaptureScreen() {
  const { token } = useAuth();
  const [title, setTitle] = useState('');
  const [selected, setSelected] = useState<Quadrant>({ urgency: 'NOT_URGENT', importance: 'IMPORTANT' });
  const [domains, setDomains] = useState<Domain[]>([]);
  const [selectedDomain, setSelectedDomain] = useState<number | null>(null);
  const [taskType, setTaskType] = useState<TaskType>('ONE_TIME');
  const [dailyEffort, setDailyEffort] = useState(60);
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
      const body: Record<string, unknown> = {
        title: title.trim(),
        urgency: selected.urgency,
        importance: selected.importance,
        domainId: selectedDomain,
        taskType,
      };
      if (taskType === 'SUSTAINED') {
        body.dailyEffortMinutes = dailyEffort;
        // Default target: 2 weeks from now
        body.targetDate = new Date(Date.now() + 14 * 24 * 60 * 60 * 1000).toISOString();
      }
      await apiRequest('/api/tasks', { method: 'POST', token, body });
      setTitle('');
      const typeLabel = taskType === 'SUSTAINED'
        ? `Sustained task added — ${dailyEffort}min daily sessions will appear in Right Now.`
        : "Task added. It'll show up in Right Now when it's the priority.";
      Alert.alert('Captured!', typeLabel, [
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
      <ScrollView style={styles.content} keyboardShouldPersistTaps="handled">
        <Text style={styles.heading}>What needs doing?</Text>

        <TextInput
          style={styles.input}
          placeholder="e.g., Complete AWS certification"
          placeholderTextColor="#64748B"
          value={title}
          onChangeText={setTitle}
          autoFocus
        />

        {/* Task type toggle */}
        <Text style={styles.sectionLabel}>Type of work</Text>
        <View style={styles.typeToggle}>
          <TouchableOpacity
            style={[styles.typeBtn, taskType === 'ONE_TIME' && styles.typeBtnActive]}
            onPress={() => setTaskType('ONE_TIME')}
          >
            <Text style={[styles.typeText, taskType === 'ONE_TIME' && styles.typeTextActive]}>One-time</Text>
            <Text style={styles.typeDesc}>Do it once, done</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.typeBtn, taskType === 'SUSTAINED' && styles.typeBtnActive]}
            onPress={() => setTaskType('SUSTAINED')}
          >
            <Text style={[styles.typeText, taskType === 'SUSTAINED' && styles.typeTextActive]}>Sustained</Text>
            <Text style={styles.typeDesc}>Daily effort over time</Text>
          </TouchableOpacity>
        </View>

        {/* Sustained task options */}
        {taskType === 'SUSTAINED' && (
          <>
            <Text style={styles.sectionLabel}>Daily effort</Text>
            <View style={styles.effortChips}>
              {effortOptions.map((mins) => (
                <TouchableOpacity
                  key={mins}
                  style={[styles.effortChip, dailyEffort === mins && styles.effortChipActive]}
                  onPress={() => setDailyEffort(mins)}
                >
                  <Text style={[styles.effortText, dailyEffort === mins && styles.effortTextActive]}>
                    {mins >= 60 ? `${mins / 60}h` : `${mins}m`}
                  </Text>
                </TouchableOpacity>
              ))}
            </View>
          </>
        )}

        {/* Domain chips */}
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
                  <Text style={[styles.chipText, selectedDomain === d.id && { color: d.color }]}>{d.name}</Text>
                </TouchableOpacity>
              ))}
            </View>
          </>
        )}

        {/* Eisenhower grid */}
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
                  <Text style={[styles.cellText, isQ(q.q) && { color: q.color, fontWeight: '700' }]}>{q.label}</Text>
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

        <View style={{ height: 40 }} />
      </ScrollView>
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
  typeToggle: { flexDirection: 'row', gap: 10 },
  typeBtn: {
    flex: 1, backgroundColor: '#1E293B', borderRadius: 12, padding: 14,
    borderWidth: 2, borderColor: '#334155', alignItems: 'center',
  },
  typeBtnActive: { borderColor: '#6366F1', backgroundColor: '#1E1B4B' },
  typeText: { color: '#94A3B8', fontSize: 15, fontWeight: '600' },
  typeTextActive: { color: '#A5B4FC' },
  typeDesc: { color: '#64748B', fontSize: 11, marginTop: 4 },
  effortChips: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  effortChip: {
    paddingHorizontal: 16, paddingVertical: 10, borderRadius: 20,
    borderWidth: 1.5, borderColor: '#334155', backgroundColor: '#1E293B',
  },
  effortChipActive: { borderColor: '#6366F1', backgroundColor: '#1E1B4B' },
  effortText: { color: '#94A3B8', fontSize: 14, fontWeight: '600' },
  effortTextActive: { color: '#A5B4FC' },
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

import { View, Text, TouchableOpacity, StyleSheet, ActivityIndicator, Alert } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useCallback, useState } from 'react';
import { useFocusEffect, router } from 'expo-router';
import { useAuth } from '../../contexts/AuthContext';
import { apiRequest } from '../../services/api';
import { NowResponse } from '../../types/api';

export default function NowScreen() {
  const { token } = useAuth();
  const [data, setData] = useState<NowResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [acting, setActing] = useState(false);

  const fetchNext = useCallback(async () => {
    if (!token) return;
    try {
      setLoading(true);
      const res = await apiRequest<NowResponse>('/api/tasks/now', { token });
      setData(res);
    } catch {
      // Silently fail — will show empty state
    } finally {
      setLoading(false);
    }
  }, [token]);

  useFocusEffect(useCallback(() => { fetchNext(); }, [fetchNext]));

  const handleAction = async (action: 'done' | 'skip' | 'snooze') => {
    if (!data?.task || !token) return;
    setActing(true);
    try {
      if (action === 'snooze') {
        const until = new Date(Date.now() + 60 * 60 * 1000).toISOString(); // 1 hour
        await apiRequest(`/api/tasks/${data.task.id}/snooze`, {
          method: 'POST', token, body: { until },
        });
      } else {
        await apiRequest(`/api/tasks/${data.task.id}/${action}`, {
          method: 'POST', token,
        });
      }
      await fetchNext();
    } catch (e: any) {
      Alert.alert('Error', e.message);
    } finally {
      setActing(false);
    }
  };

  if (loading) {
    return (
      <SafeAreaView style={styles.container} edges={['bottom']}>
        <View style={styles.centered}>
          <ActivityIndicator size="large" color="#6366F1" />
        </View>
      </SafeAreaView>
    );
  }

  const task = data?.task;

  return (
    <SafeAreaView style={styles.container} edges={['bottom']}>
      <View style={styles.content}>
        {task ? (
          <>
            <View style={[styles.card, task.domainColor ? { borderLeftColor: task.domainColor, borderLeftWidth: 4 } : {}]}>
              {task.domainName && (
                <Text style={[styles.domainLabel, task.domainColor ? { color: task.domainColor } : {}]}>
                  {task.domainName.toUpperCase()}
                </Text>
              )}
              <Text style={styles.cardTitle}>{task.title}</Text>
              {task.taskType === 'SUSTAINED' && task.dailyEffortMinutes && (
                <Text style={styles.sessionLabel}>
                  Today's session — {task.dailyEffortMinutes >= 60 ? `${task.dailyEffortMinutes / 60}h` : `${task.dailyEffortMinutes}m`}
                </Text>
              )}
              {task.notes && <Text style={styles.cardNotes}>{task.notes}</Text>}
              {task.dueDate && (
                <Text style={styles.deadline}>
                  Due {formatDeadline(task.dueDate)}
                </Text>
              )}
              {task.progress && (
                <View style={styles.progressContainer}>
                  <View style={styles.progressBar}>
                    <View style={[styles.progressFill, { width: `${Math.min(100, task.progress.percentComplete)}%` }]} />
                  </View>
                  <Text style={styles.progressText}>
                    {task.progress.completedSessions} sessions · {formatMinutes(task.progress.totalMinutesLogged)} of ~{formatMinutes(task.progress.estimatedTotalMinutes)}
                  </Text>
                </View>
              )}
              <View style={styles.meta}>
                <Text style={styles.quadrant}>
                  {task.urgency === 'URGENT' ? '⚡' : '○'}{' '}
                  {task.importance === 'IMPORTANT' ? 'Important' : 'Not important'}
                </Text>
                {task.score !== null && (
                  <Text style={styles.score}>Score: {task.score}</Text>
                )}
              </View>
            </View>

            <View style={styles.actions}>
              <TouchableOpacity
                style={[styles.actionBtn, styles.doneBtn]}
                onPress={() => handleAction('done')}
                disabled={acting}
              >
                <Text style={styles.actionText}>
                  {task.taskType === 'SUSTAINED' ? "Today's Done ✓" : 'Done ✓'}
                </Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.actionBtn, styles.skipBtn]}
                onPress={() => handleAction('skip')}
                disabled={acting}
              >
                <Text style={styles.actionText}>Skip →</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.actionBtn, styles.snoozeBtn]}
                onPress={() => handleAction('snooze')}
                disabled={acting}
              >
                <Text style={styles.actionText}>Snooze 1h</Text>
              </TouchableOpacity>
            </View>

            {data && data.pendingCount > 1 && (
              <Text style={styles.pending}>{data.pendingCount - 1} more tasks waiting</Text>
            )}
          </>
        ) : (
          <View style={styles.emptyCard}>
            <Text style={styles.emptyTitle}>All clear!</Text>
            <Text style={styles.emptySubtitle}>
              No tasks right now.{'\n'}Tap the + tab to capture something.
            </Text>
            <TouchableOpacity style={styles.captureBtn} onPress={() => router.push('/(main)/capture')}>
              <Text style={styles.captureBtnText}>Capture a Task</Text>
            </TouchableOpacity>
          </View>
        )}
      </View>
    </SafeAreaView>
  );
}

function formatMinutes(mins: number): string {
  if (mins < 60) return `${mins}m`;
  const h = Math.floor(mins / 60);
  const m = mins % 60;
  return m > 0 ? `${h}h ${m}m` : `${h}h`;
}

function formatDeadline(iso: string): string {
  const due = new Date(iso);
  const now = new Date();
  const hours = Math.round((due.getTime() - now.getTime()) / (1000 * 60 * 60));
  if (hours < 0) return 'overdue';
  if (hours < 1) return 'in less than an hour';
  if (hours < 24) return `in ${hours}h`;
  const days = Math.round(hours / 24);
  return `in ${days} day${days === 1 ? '' : 's'}`;
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#0F172A' },
  content: { flex: 1, paddingHorizontal: 20, justifyContent: 'center' },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  card: {
    backgroundColor: '#1E293B', borderRadius: 20, padding: 28,
    borderWidth: 1, borderColor: '#334155',
  },
  domainLabel: {
    fontSize: 11, fontWeight: '700', letterSpacing: 2, color: '#6366F1', marginBottom: 12,
  },
  cardTitle: { color: '#F8FAFC', fontSize: 26, fontWeight: '700', lineHeight: 34 },
  cardNotes: { color: '#94A3B8', fontSize: 15, marginTop: 10, lineHeight: 22 },
  deadline: { color: '#F59E0B', fontSize: 13, fontWeight: '600', marginTop: 12 },
  meta: { flexDirection: 'row', justifyContent: 'space-between', marginTop: 16 },
  sessionLabel: { color: '#A5B4FC', fontSize: 13, fontWeight: '600', marginTop: 6 },
  progressContainer: { marginTop: 14 },
  progressBar: { height: 6, backgroundColor: '#334155', borderRadius: 3, overflow: 'hidden' as const },
  progressFill: { height: '100%' as const, backgroundColor: '#6366F1', borderRadius: 3 },
  progressText: { color: '#64748B', fontSize: 12, marginTop: 6 },
  quadrant: { color: '#64748B', fontSize: 13 },
  score: { color: '#475569', fontSize: 12 },
  actions: { flexDirection: 'row', gap: 10, marginTop: 24 },
  actionBtn: { flex: 1, borderRadius: 14, paddingVertical: 16, alignItems: 'center' },
  doneBtn: { backgroundColor: '#22C55E' },
  skipBtn: { backgroundColor: '#334155' },
  snoozeBtn: { backgroundColor: '#1E293B', borderWidth: 1, borderColor: '#334155' },
  actionText: { color: '#FFFFFF', fontSize: 15, fontWeight: '600' },
  pending: { color: '#475569', fontSize: 13, textAlign: 'center', marginTop: 20 },
  emptyCard: {
    backgroundColor: '#1E293B', borderRadius: 20, padding: 32,
    borderWidth: 1, borderColor: '#334155', alignItems: 'center',
  },
  emptyTitle: { color: '#F8FAFC', fontSize: 28, fontWeight: '700' },
  emptySubtitle: { color: '#94A3B8', fontSize: 15, textAlign: 'center', marginTop: 12, lineHeight: 22 },
  captureBtn: {
    backgroundColor: '#6366F1', borderRadius: 12, paddingVertical: 14, paddingHorizontal: 28, marginTop: 24,
  },
  captureBtnText: { color: '#FFFFFF', fontSize: 15, fontWeight: '600' },
});

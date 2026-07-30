import { View, Text, TouchableOpacity, StyleSheet, ActivityIndicator, Alert, TextInput, ScrollView } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useCallback, useState } from 'react';
import { useFocusEffect, router } from 'expo-router';
import { useAuth } from '../../contexts/AuthContext';
import { apiRequest } from '../../services/api';
import { NowResponse, MicroStep, Template } from '../../types/api';

export default function NowScreen() {
  const { token } = useAuth();
  const [data, setData] = useState<NowResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [acting, setActing] = useState(false);
  const [newStepTitle, setNewStepTitle] = useState('');
  const [addingStep, setAddingStep] = useState(false);
  const [showAddStep, setShowAddStep] = useState(false);
  const [templates, setTemplates] = useState<Template[]>([]);
  const [showTemplates, setShowTemplates] = useState(false);
  const [applyingTemplate, setApplyingTemplate] = useState(false);

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

  const handleAddMicroStep = async () => {
    if (!data?.task || !token || !newStepTitle.trim()) return;
    setAddingStep(true);
    try {
      await apiRequest('/api/tasks', {
        method: 'POST', token,
        body: { title: newStepTitle.trim(), parentTaskId: data.task.id },
      });
      setNewStepTitle('');
      setShowAddStep(false);
      await fetchNext();
    } catch (e: any) {
      Alert.alert('Error', e.message);
    } finally {
      setAddingStep(false);
    }
  };

  const handleShowTemplates = async () => {
    if (!token) return;
    try {
      const res = await apiRequest<Template[]>('/api/tasks/templates', { token });
      setTemplates(res);
      setShowTemplates(true);
    } catch (e: any) {
      Alert.alert('Error', e.message);
    }
  };

  const handleApplyTemplate = async (templateId: string) => {
    if (!data?.task || !token) return;
    setApplyingTemplate(true);
    try {
      await apiRequest(`/api/tasks/${data.task.id}/apply-template?templateId=${templateId}`, {
        method: 'POST', token,
      });
      setShowTemplates(false);
      await fetchNext();
    } catch (e: any) {
      Alert.alert('Error', e.message);
    } finally {
      setApplyingTemplate(false);
    }
  };

  const handleStepDone = async (stepId: number) => {
    if (!token) return;
    setActing(true);
    try {
      await apiRequest(`/api/tasks/${stepId}/done`, { method: 'POST', token });
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

            {/* Micro-steps */}
            {task.microSteps && task.microSteps.length > 0 && (
              <View style={styles.stepsContainer}>
                <Text style={styles.stepsHeader}>
                  Steps ({task.microSteps.filter(s => s.status === 'DONE').length}/{task.microSteps.length})
                </Text>
                {task.microSteps.map((step) => (
                  <TouchableOpacity
                    key={step.id}
                    style={styles.stepRow}
                    onPress={() => step.status !== 'DONE' && handleStepDone(step.id)}
                    disabled={acting || step.status === 'DONE'}
                  >
                    <View style={[styles.stepCheck, step.status === 'DONE' && styles.stepCheckDone]}>
                      {step.status === 'DONE' && <Text style={styles.stepCheckMark}>✓</Text>}
                    </View>
                    <Text style={[styles.stepTitle, step.status === 'DONE' && styles.stepTitleDone]}>
                      {step.title}
                    </Text>
                  </TouchableOpacity>
                ))}
              </View>
            )}

            {/* Add micro-step */}
            {!showAddStep && !showTemplates ? (
              <View style={styles.breakdownRow}>
                <TouchableOpacity style={styles.addStepBtn} onPress={() => setShowAddStep(true)}>
                  <Text style={styles.addStepText}>+ Add a step</Text>
                </TouchableOpacity>
                {!(task.microSteps && task.microSteps.length > 0) && (
                  <TouchableOpacity style={styles.addStepBtn} onPress={handleShowTemplates}>
                    <Text style={styles.templateBtnText}>Use a template</Text>
                  </TouchableOpacity>
                )}
              </View>
            ) : showTemplates ? (
              <View style={styles.templatePicker}>
                <Text style={styles.templatePickerTitle}>Choose a template</Text>
                <ScrollView style={styles.templateList} nestedScrollEnabled>
                  {['academic', 'executive', 'professional'].map(category => {
                    const catTemplates = templates.filter(t => t.category === category);
                    if (catTemplates.length === 0) return null;
                    return (
                      <View key={category}>
                        <Text style={styles.templateCategory}>{category.toUpperCase()}</Text>
                        {catTemplates.map(t => (
                          <TouchableOpacity
                            key={t.id}
                            style={styles.templateItem}
                            onPress={() => handleApplyTemplate(t.id)}
                            disabled={applyingTemplate}
                          >
                            <Text style={styles.templateName}>{t.name}</Text>
                            <Text style={styles.templateStepCount}>{t.steps.length} steps</Text>
                          </TouchableOpacity>
                        ))}
                      </View>
                    );
                  })}
                </ScrollView>
                <TouchableOpacity onPress={() => setShowTemplates(false)}>
                  <Text style={styles.addStepCancel}>Cancel</Text>
                </TouchableOpacity>
              </View>
            ) : (
              <View style={styles.addStepForm}>
                <TextInput
                  style={styles.addStepInput}
                  placeholder="Quick step (< 2 min)..."
                  placeholderTextColor="#64748B"
                  value={newStepTitle}
                  onChangeText={setNewStepTitle}
                  onSubmitEditing={handleAddMicroStep}
                  autoFocus
                />
                <View style={styles.addStepActions}>
                  <TouchableOpacity
                    style={styles.addStepConfirm}
                    onPress={handleAddMicroStep}
                    disabled={addingStep || !newStepTitle.trim()}
                  >
                    <Text style={styles.addStepConfirmText}>{addingStep ? '...' : 'Add'}</Text>
                  </TouchableOpacity>
                  <TouchableOpacity onPress={() => { setShowAddStep(false); setNewStepTitle(''); }}>
                    <Text style={styles.addStepCancel}>Cancel</Text>
                  </TouchableOpacity>
                </View>
              </View>
            )}

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
  stepsContainer: { marginTop: 16, backgroundColor: '#0F172A', borderRadius: 12, padding: 14 },
  stepsHeader: { color: '#94A3B8', fontSize: 12, fontWeight: '600', letterSpacing: 1, marginBottom: 10 },
  stepRow: { flexDirection: 'row', alignItems: 'center', paddingVertical: 8 },
  stepCheck: {
    width: 22, height: 22, borderRadius: 11, borderWidth: 2, borderColor: '#475569',
    alignItems: 'center', justifyContent: 'center', marginRight: 12,
  },
  stepCheckDone: { backgroundColor: '#22C55E', borderColor: '#22C55E' },
  stepCheckMark: { color: '#FFFFFF', fontSize: 12, fontWeight: '700' },
  stepTitle: { color: '#E2E8F0', fontSize: 15, flex: 1 },
  stepTitleDone: { color: '#64748B', textDecorationLine: 'line-through' },
  breakdownRow: { flexDirection: 'row', justifyContent: 'space-between', marginTop: 12 },
  addStepBtn: { marginTop: 0, paddingVertical: 10 },
  addStepText: { color: '#6366F1', fontSize: 14, fontWeight: '600' },
  templateBtnText: { color: '#A78BFA', fontSize: 14, fontWeight: '600' },
  templatePicker: { marginTop: 12, backgroundColor: '#0F172A', borderRadius: 12, padding: 14 },
  templatePickerTitle: { color: '#F8FAFC', fontSize: 15, fontWeight: '600', marginBottom: 10 },
  templateList: { maxHeight: 200 },
  templateCategory: { color: '#64748B', fontSize: 11, fontWeight: '700', letterSpacing: 1.5, marginTop: 10, marginBottom: 6 },
  templateItem: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', paddingVertical: 10, borderBottomWidth: 1, borderBottomColor: '#1E293B' },
  templateName: { color: '#E2E8F0', fontSize: 15 },
  templateStepCount: { color: '#64748B', fontSize: 12 },
  addStepForm: { marginTop: 12 },
  addStepInput: {
    backgroundColor: '#1E293B', borderRadius: 10, padding: 12, color: '#F8FAFC', fontSize: 15,
    borderWidth: 1, borderColor: '#334155',
  },
  addStepActions: { flexDirection: 'row', alignItems: 'center', gap: 12, marginTop: 10 },
  addStepConfirm: { backgroundColor: '#6366F1', borderRadius: 8, paddingVertical: 8, paddingHorizontal: 16 },
  addStepConfirmText: { color: '#FFFFFF', fontSize: 14, fontWeight: '600' },
  addStepCancel: { color: '#64748B', fontSize: 14 },
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

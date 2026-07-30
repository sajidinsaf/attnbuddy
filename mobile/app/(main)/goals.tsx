import { View, Text, TouchableOpacity, FlatList, TextInput, StyleSheet, ActivityIndicator, Alert } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useCallback, useState } from 'react';
import { useFocusEffect } from 'expo-router';
import { useAuth } from '../../contexts/AuthContext';
import { apiRequest } from '../../services/api';

type Goal = {
  id: number;
  title: string;
  targetDate: string | null;
  status: string;
  createdAt: string;
  completedAt: string | null;
};

export default function GoalsScreen() {
  const { token } = useAuth();
  const [goals, setGoals] = useState<Goal[]>([]);
  const [loading, setLoading] = useState(true);
  const [acting, setActing] = useState(false);
  const [showAdd, setShowAdd] = useState(false);
  const [newTitle, setNewTitle] = useState('');

  const fetchGoals = useCallback(async () => {
    if (!token) return;
    try {
      const res = await apiRequest<Goal[]>('/api/goals', { token });
      setGoals(res || []);
    } catch {
      // Silently fail
    }
  }, [token]);

  useFocusEffect(useCallback(() => {
    setLoading(true);
    fetchGoals().finally(() => setLoading(false));
  }, [fetchGoals]));

  const handleAdd = async () => {
    if (!token || !newTitle.trim()) return;
    setActing(true);
    try {
      await apiRequest('/api/goals', {
        method: 'POST', token,
        body: { title: newTitle.trim() },
      });
      setNewTitle('');
      setShowAdd(false);
      await fetchGoals();
    } catch (e: any) {
      Alert.alert('Error', e.message);
    } finally {
      setActing(false);
    }
  };

  const handleComplete = async (id: number) => {
    if (!token) return;
    setActing(true);
    try {
      await apiRequest(`/api/goals/${id}/complete`, { method: 'POST', token });
      await fetchGoals();
    } catch (e: any) {
      Alert.alert('Error', e.message);
    } finally {
      setActing(false);
    }
  };

  const activeGoals = goals.filter(g => g.status === 'ACTIVE');
  const completedGoals = goals.filter(g => g.status === 'COMPLETED');

  if (loading) {
    return (
      <SafeAreaView style={styles.container} edges={['bottom']}>
        <View style={styles.centered}>
          <ActivityIndicator size="large" color="#6366F1" />
        </View>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.container} edges={['bottom']}>
      <FlatList
        data={[...activeGoals, ...completedGoals]}
        keyExtractor={item => item.id.toString()}
        contentContainerStyle={styles.list}
        ListHeaderComponent={
          <>
            {!showAdd ? (
              <TouchableOpacity style={styles.addBtn} onPress={() => setShowAdd(true)}>
                <Text style={styles.addBtnText}>+ Add a goal</Text>
              </TouchableOpacity>
            ) : (
              <View style={styles.addForm}>
                <TextInput
                  style={styles.addInput}
                  placeholder="What do you want to achieve?"
                  placeholderTextColor="#64748B"
                  value={newTitle}
                  onChangeText={setNewTitle}
                  onSubmitEditing={handleAdd}
                  autoFocus
                />
                <View style={styles.addActions}>
                  <TouchableOpacity style={styles.addConfirm} onPress={handleAdd} disabled={acting || !newTitle.trim()}>
                    <Text style={styles.addConfirmText}>{acting ? '...' : 'Add'}</Text>
                  </TouchableOpacity>
                  <TouchableOpacity onPress={() => { setShowAdd(false); setNewTitle(''); }}>
                    <Text style={styles.cancelText}>Cancel</Text>
                  </TouchableOpacity>
                </View>
              </View>
            )}
            {activeGoals.length === 0 && completedGoals.length === 0 && (
              <View style={styles.emptyState}>
                <Text style={styles.emptyTitle}>No goals yet</Text>
                <Text style={styles.emptySubtitle}>Set a long-term goal to track your progress</Text>
              </View>
            )}
          </>
        }
        renderItem={({ item }) => (
          <View style={styles.row}>
            {item.status === 'ACTIVE' ? (
              <TouchableOpacity
                style={styles.goalCheck}
                onPress={() => handleComplete(item.id)}
                disabled={acting}
              >
                <View style={styles.goalCheckInner} />
              </TouchableOpacity>
            ) : (
              <View style={[styles.goalCheck, styles.goalCheckDone]}>
                <Text style={styles.goalCheckMark}>✓</Text>
              </View>
            )}
            <View style={styles.goalContent}>
              <Text style={[styles.goalTitle, item.status === 'COMPLETED' && styles.goalTitleDone]}>
                {item.title}
              </Text>
              <Text style={styles.goalDate}>
                {item.status === 'COMPLETED'
                  ? `Completed ${formatRelative(item.completedAt)}`
                  : `Added ${formatRelative(item.createdAt)}`}
              </Text>
            </View>
          </View>
        )}
      />
    </SafeAreaView>
  );
}

function formatRelative(iso: string | null): string {
  if (!iso) return '';
  const d = new Date(iso);
  const days = Math.floor((Date.now() - d.getTime()) / 86400000);
  if (days === 0) return 'today';
  if (days === 1) return 'yesterday';
  if (days < 7) return `${days} days ago`;
  return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#0F172A' },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center' },
  list: { paddingHorizontal: 16, paddingTop: 8 },
  addBtn: { paddingVertical: 14, marginBottom: 16 },
  addBtnText: { color: '#6366F1', fontSize: 15, fontWeight: '600' },
  addForm: { marginBottom: 16 },
  addInput: {
    backgroundColor: '#1E293B', borderRadius: 10, padding: 12, color: '#F8FAFC', fontSize: 15,
    borderWidth: 1, borderColor: '#334155',
  },
  addActions: { flexDirection: 'row', alignItems: 'center', gap: 12, marginTop: 10 },
  addConfirm: { backgroundColor: '#6366F1', borderRadius: 8, paddingVertical: 8, paddingHorizontal: 16 },
  addConfirmText: { color: '#FFFFFF', fontSize: 14, fontWeight: '600' },
  cancelText: { color: '#64748B', fontSize: 14 },
  emptyState: { alignItems: 'center', paddingVertical: 40 },
  emptyTitle: { color: '#F8FAFC', fontSize: 22, fontWeight: '700' },
  emptySubtitle: { color: '#94A3B8', fontSize: 15, marginTop: 8 },
  row: {
    backgroundColor: '#1E293B', borderRadius: 12, padding: 16, marginBottom: 8,
    flexDirection: 'row', alignItems: 'center',
  },
  goalCheck: {
    width: 26, height: 26, borderRadius: 13, borderWidth: 2, borderColor: '#475569',
    alignItems: 'center', justifyContent: 'center', marginRight: 14,
  },
  goalCheckInner: { width: 12, height: 12, borderRadius: 6 },
  goalCheckDone: { backgroundColor: '#22C55E', borderColor: '#22C55E' },
  goalCheckMark: { color: '#FFFFFF', fontSize: 14, fontWeight: '700' },
  goalContent: { flex: 1 },
  goalTitle: { color: '#E2E8F0', fontSize: 16, fontWeight: '500' },
  goalTitleDone: { color: '#64748B', textDecorationLine: 'line-through' },
  goalDate: { color: '#64748B', fontSize: 12, marginTop: 4 },
});

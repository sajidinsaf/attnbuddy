import { View, Text, FlatList, TouchableOpacity, StyleSheet, ActivityIndicator, Alert } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useCallback, useState } from 'react';
import { useFocusEffect } from 'expo-router';
import { useAuth } from '../../contexts/AuthContext';
import { apiRequest } from '../../services/api';
import { TaskResponse } from '../../types/api';

type PageResponse = {
  content: TaskResponse[];
  last: boolean;
  number: number;
};

type Filter = 'ALL' | 'PENDING' | 'SNOOZED' | 'DONE';

export default function TasksScreen() {
  const { token } = useAuth();
  const [tasks, setTasks] = useState<TaskResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [acting, setActing] = useState(false);
  const [page, setPage] = useState(0);
  const [isLast, setIsLast] = useState(false);
  const [filter, setFilter] = useState<Filter>('ALL');

  const fetchPage = useCallback(async (pageNum: number, append: boolean, status: Filter) => {
    if (!token) return;
    try {
      const statusParam = status === 'ALL' ? '' : `&status=${status}`;
      const res = await apiRequest<PageResponse>(
        `/api/tasks?page=${pageNum}&size=20&sort=createdAt,desc${statusParam}`,
        { token },
      );
      setTasks(prev => append ? [...prev, ...res.content] : res.content);
      setIsLast(res.last);
      setPage(res.number);
    } catch (e: any) {
      console.error('Tasks fetch failed:', e.message);
    }
  }, [token]);

  useFocusEffect(useCallback(() => {
    setLoading(true);
    fetchPage(0, false, filter).finally(() => setLoading(false));
  }, [fetchPage, filter]));

  const changeFilter = (f: Filter) => {
    setFilter(f);
    setLoading(true);
    fetchPage(0, false, f).finally(() => setLoading(false));
  };

  const loadMore = async () => {
    if (isLast || loadingMore) return;
    setLoadingMore(true);
    await fetchPage(page + 1, true, filter);
    setLoadingMore(false);
  };

  const [undoTask, setUndoTask] = useState<{ id: number; title: string } | null>(null);

  const handleUnsnooze = async (taskId: number) => {
    if (!token) return;
    setActing(true);
    try {
      await apiRequest(`/api/tasks/${taskId}/unsnooze`, { method: 'POST', token });
      await fetchPage(0, false, filter);
    } catch (e: any) {
      Alert.alert('Error', e.message);
    } finally {
      setActing(false);
    }
  };

  const handleDelete = async (taskId: number, title: string) => {
    if (!token) return;
    setActing(true);
    try {
      await apiRequest(`/api/tasks/${taskId}`, { method: 'DELETE', token });
      setUndoTask({ id: taskId, title });
      await fetchPage(0, false, filter);
      // Auto-dismiss undo after 8 seconds
      setTimeout(() => setUndoTask(prev => prev?.id === taskId ? null : prev), 8000);
    } catch (e: any) {
      Alert.alert('Error', e.message);
    } finally {
      setActing(false);
    }
  };

  const handleUndo = async () => {
    if (!token || !undoTask) return;
    try {
      await apiRequest(`/api/tasks/${undoTask.id}/restore`, { method: 'POST', token });
      setUndoTask(null);
      await fetchPage(0, false, filter);
    } catch (e: any) {
      Alert.alert('Error', e.message);
    }
  };

  const confirmDelete = (taskId: number, title: string) => {
    Alert.alert('Delete task?', title, [
      { text: 'Cancel', style: 'cancel' },
      { text: 'Delete', style: 'destructive', onPress: () => handleDelete(taskId, title) },
    ]);
  };

  const filters: Filter[] = ['ALL', 'PENDING', 'SNOOZED', 'DONE'];

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
      <View style={styles.filterRow}>
        {filters.map(f => (
          <TouchableOpacity
            key={f}
            style={[styles.filterChip, filter === f && styles.filterChipActive]}
            onPress={() => changeFilter(f)}
          >
            <Text style={[styles.filterText, filter === f && styles.filterTextActive]}>
              {f === 'ALL' ? 'All' : f.charAt(0) + f.slice(1).toLowerCase()}
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      {tasks.length === 0 ? (
        <View style={styles.centered}>
          <Text style={styles.emptyTitle}>No tasks</Text>
          <Text style={styles.emptySubtitle}>
            {filter === 'ALL' ? 'Capture a task to get started' : `No ${filter.toLowerCase()} tasks`}
          </Text>
        </View>
      ) : (
        <FlatList
          data={tasks}
          keyExtractor={item => item.id.toString()}
          contentContainerStyle={styles.list}
          onEndReached={loadMore}
          onEndReachedThreshold={0.3}
          ListFooterComponent={loadingMore ? <ActivityIndicator color="#6366F1" style={{ padding: 16 }} /> : null}
          renderItem={({ item }) => (
            <View style={[styles.row, item.domainColor ? { borderLeftColor: item.domainColor, borderLeftWidth: 3 } : {}]}>
              <View style={styles.rowContent}>
                <Text style={[styles.rowTitle, item.status === 'DONE' && styles.rowTitleDone]}>
                  {item.title}
                </Text>
                <View style={styles.rowMeta}>
                  {item.domainName && (
                    <Text style={[styles.rowDomain, item.domainColor ? { color: item.domainColor } : {}]}>
                      {item.domainName}
                    </Text>
                  )}
                  <Text style={styles.rowStatus}>{statusLabel(item)}</Text>
                </View>
              </View>
              {item.status === 'SNOOZED' && (
                <TouchableOpacity
                  style={styles.unsnoozeBtn}
                  onPress={() => handleUnsnooze(item.id)}
                  disabled={acting}
                >
                  <Text style={styles.unsnoozeBtnText}>Wake</Text>
                </TouchableOpacity>
              )}
              <TouchableOpacity
                onPress={() => confirmDelete(item.id, item.title)}
                disabled={acting}
                style={styles.deleteBtn}
              >
                <Text style={styles.deleteBtnText}>x</Text>
              </TouchableOpacity>
            </View>
          )}
        />
      )}

      {undoTask && (
        <View style={styles.undoBanner}>
          <Text style={styles.undoText} numberOfLines={1}>Deleted "{undoTask.title}"</Text>
          <TouchableOpacity onPress={handleUndo}>
            <Text style={styles.undoAction}>Undo</Text>
          </TouchableOpacity>
        </View>
      )}
    </SafeAreaView>
  );
}

function statusLabel(task: TaskResponse): string {
  if (task.status === 'DONE') return 'Done';
  if (task.status === 'SNOOZED' && task.snoozedUntil) {
    const until = new Date(task.snoozedUntil);
    const now = new Date();
    if (until > now) {
      const mins = Math.round((until.getTime() - now.getTime()) / 60000);
      if (mins < 60) return `Snoozed ${mins}m`;
      return `Snoozed ${Math.round(mins / 60)}h`;
    }
    return 'Snooze expired';
  }
  if (task.status === 'SNOOZED') return 'Snoozed';
  if (task.taskType === 'SUSTAINED') return 'Sustained';
  return '';
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#0F172A' },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 20 },
  filterRow: { flexDirection: 'row', gap: 8, paddingHorizontal: 16, paddingVertical: 12 },
  filterChip: {
    paddingVertical: 6, paddingHorizontal: 14, borderRadius: 16,
    backgroundColor: '#1E293B', borderWidth: 1, borderColor: '#334155',
  },
  filterChipActive: { backgroundColor: '#6366F1', borderColor: '#6366F1' },
  filterText: { color: '#94A3B8', fontSize: 13, fontWeight: '600' },
  filterTextActive: { color: '#FFFFFF' },
  emptyTitle: { color: '#F8FAFC', fontSize: 22, fontWeight: '700' },
  emptySubtitle: { color: '#94A3B8', fontSize: 15, textAlign: 'center', marginTop: 8 },
  list: { paddingHorizontal: 16 },
  row: {
    backgroundColor: '#1E293B', borderRadius: 12, padding: 16, marginBottom: 8,
    flexDirection: 'row', alignItems: 'center',
  },
  rowContent: { flex: 1, marginRight: 12 },
  rowTitle: { color: '#E2E8F0', fontSize: 15, fontWeight: '500' },
  rowTitleDone: { color: '#64748B', textDecorationLine: 'line-through' },
  rowMeta: { flexDirection: 'row', gap: 8, marginTop: 4 },
  rowDomain: { color: '#64748B', fontSize: 12 },
  rowStatus: { color: '#64748B', fontSize: 12 },
  unsnoozeBtn: {
    backgroundColor: '#4F46E5', borderRadius: 8, paddingVertical: 6, paddingHorizontal: 12,
  },
  unsnoozeBtnText: { color: '#FFFFFF', fontSize: 13, fontWeight: '600' },
  deleteBtn: { padding: 8, marginLeft: 4 },
  deleteBtnText: { color: '#64748B', fontSize: 16, fontWeight: '600' },
  undoBanner: {
    position: 'absolute', bottom: 0, left: 0, right: 0,
    backgroundColor: '#334155', flexDirection: 'row', alignItems: 'center',
    justifyContent: 'space-between', paddingHorizontal: 20, paddingVertical: 14,
  },
  undoText: { color: '#E2E8F0', fontSize: 14, flex: 1, marginRight: 12 },
  undoAction: { color: '#6366F1', fontSize: 15, fontWeight: '700' },
});

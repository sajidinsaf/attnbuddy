import { View, Text, FlatList, StyleSheet, ActivityIndicator } from 'react-native';
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

export default function HistoryScreen() {
  const { token } = useAuth();
  const [tasks, setTasks] = useState<TaskResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [page, setPage] = useState(0);
  const [isLast, setIsLast] = useState(false);

  const fetchPage = useCallback(async (pageNum: number, append: boolean) => {
    if (!token) return;
    try {
      const res = await apiRequest<PageResponse>(
        `/api/tasks?status=DONE&page=${pageNum}&size=20&sort=completedAt,desc`,
        { token },
      );
      setTasks(prev => append ? [...prev, ...res.content] : res.content);
      setIsLast(res.last);
      setPage(res.number);
    } catch {
      // Silently fail
    }
  }, [token]);

  useFocusEffect(useCallback(() => {
    setLoading(true);
    fetchPage(0, false).finally(() => setLoading(false));
  }, [fetchPage]));

  const loadMore = async () => {
    if (isLast || loadingMore) return;
    setLoadingMore(true);
    await fetchPage(page + 1, true);
    setLoadingMore(false);
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

  return (
    <SafeAreaView style={styles.container} edges={['bottom']}>
      {tasks.length === 0 ? (
        <View style={styles.centered}>
          <Text style={styles.emptyTitle}>No completed tasks yet</Text>
          <Text style={styles.emptySubtitle}>
            Tasks you complete will show up here
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
                <Text style={styles.rowTitle}>{item.title}</Text>
                {item.domainName && (
                  <Text style={[styles.rowDomain, item.domainColor ? { color: item.domainColor } : {}]}>
                    {item.domainName}
                  </Text>
                )}
              </View>
              <Text style={styles.rowDate}>{formatCompletedDate(item.completedAt)}</Text>
            </View>
          )}
        />
      )}
    </SafeAreaView>
  );
}

function formatCompletedDate(iso: string | null): string {
  if (!iso) return '';
  const date = new Date(iso);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

  if (diffDays === 0) return 'Today';
  if (diffDays === 1) return 'Yesterday';
  if (diffDays < 7) return `${diffDays} days ago`;

  return date.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#0F172A' },
  centered: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 20 },
  emptyTitle: { color: '#F8FAFC', fontSize: 22, fontWeight: '700' },
  emptySubtitle: { color: '#94A3B8', fontSize: 15, textAlign: 'center', marginTop: 8, lineHeight: 22 },
  list: { paddingHorizontal: 16, paddingTop: 8 },
  row: {
    backgroundColor: '#1E293B', borderRadius: 12, padding: 16, marginBottom: 8,
    flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between',
  },
  rowContent: { flex: 1, marginRight: 12 },
  rowTitle: { color: '#E2E8F0', fontSize: 15, fontWeight: '500' },
  rowDomain: { color: '#64748B', fontSize: 12, marginTop: 4 },
  rowDate: { color: '#64748B', fontSize: 12 },
});

import { View, Text, StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

export default function NowScreen() {
  return (
    <SafeAreaView style={styles.container} edges={['bottom']}>
      <View style={styles.content}>
        <View style={styles.card}>
          <Text style={styles.cardLabel}>YOUR NEXT TASK</Text>
          <Text style={styles.cardTitle}>No tasks yet</Text>
          <Text style={styles.cardSubtitle}>
            Tap the + tab to capture your first task.{'\n'}
            AttnBuddy will pick what to work on next.
          </Text>
        </View>
        <Text style={styles.hint}>
          This is the Right Now screen — it shows one task at a time.{'\n'}
          No lists. No overwhelm. Just the next thing.
        </Text>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0F172A',
  },
  content: {
    flex: 1,
    paddingHorizontal: 24,
    justifyContent: 'center',
    alignItems: 'center',
  },
  card: {
    backgroundColor: '#1E293B',
    borderRadius: 20,
    padding: 32,
    width: '100%',
    borderWidth: 1,
    borderColor: '#334155',
    alignItems: 'center',
  },
  cardLabel: {
    color: '#6366F1',
    fontSize: 12,
    fontWeight: '700',
    letterSpacing: 2,
    marginBottom: 16,
  },
  cardTitle: {
    color: '#F8FAFC',
    fontSize: 28,
    fontWeight: '700',
    textAlign: 'center',
  },
  cardSubtitle: {
    color: '#94A3B8',
    fontSize: 15,
    textAlign: 'center',
    marginTop: 12,
    lineHeight: 22,
  },
  hint: {
    color: '#475569',
    fontSize: 13,
    textAlign: 'center',
    marginTop: 32,
    lineHeight: 20,
  },
});

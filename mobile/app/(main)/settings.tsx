import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

export default function SettingsScreen() {
  const handleLogout = () => {
    // TODO: Clear tokens and redirect to login
  };

  return (
    <SafeAreaView style={styles.container} edges={['bottom']}>
      <View style={styles.content}>
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Profile</Text>
          <View style={styles.card}>
            <Text style={styles.cardLabel}>Name</Text>
            <Text style={styles.cardValue}>Not configured yet</Text>
          </View>
          <View style={styles.card}>
            <Text style={styles.cardLabel}>Profile</Text>
            <Text style={styles.cardValue}>—</Text>
          </View>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Life Domains</Text>
          <View style={styles.card}>
            <Text style={styles.cardValue}>Domain management coming soon</Text>
            <Text style={styles.cardHint}>
              Organize tasks by life area: Work, Family, Personal, Health, and more
            </Text>
          </View>
        </View>

        <View style={{ flex: 1 }} />

        <TouchableOpacity style={styles.logoutButton} onPress={handleLogout}>
          <Text style={styles.logoutText}>Sign Out</Text>
        </TouchableOpacity>
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
    paddingTop: 16,
  },
  section: {
    marginBottom: 32,
  },
  sectionTitle: {
    color: '#6366F1',
    fontSize: 12,
    fontWeight: '700',
    letterSpacing: 2,
    textTransform: 'uppercase',
    marginBottom: 12,
  },
  card: {
    backgroundColor: '#1E293B',
    borderRadius: 12,
    padding: 16,
    marginBottom: 8,
    borderWidth: 1,
    borderColor: '#334155',
  },
  cardLabel: {
    color: '#94A3B8',
    fontSize: 12,
    fontWeight: '500',
    marginBottom: 4,
  },
  cardValue: {
    color: '#F8FAFC',
    fontSize: 16,
  },
  cardHint: {
    color: '#64748B',
    fontSize: 13,
    marginTop: 8,
  },
  logoutButton: {
    backgroundColor: '#1E293B',
    borderRadius: 12,
    paddingVertical: 16,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#EF4444',
    marginBottom: 16,
  },
  logoutText: {
    color: '#EF4444',
    fontSize: 16,
    fontWeight: '600',
  },
});

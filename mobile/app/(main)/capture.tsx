import { View, Text, TextInput, TouchableOpacity, StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useState } from 'react';

export default function CaptureScreen() {
  const [title, setTitle] = useState('');

  const handleCapture = () => {
    // TODO: Implement task creation API call
    setTitle('');
  };

  return (
    <SafeAreaView style={styles.container} edges={['bottom']}>
      <View style={styles.content}>
        <Text style={styles.heading}>What needs doing?</Text>
        <Text style={styles.subheading}>Just the title is enough. You can add details later.</Text>

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

        <Text style={styles.sectionLabel}>How urgent and important is this?</Text>
        <View style={styles.eisenhowerGrid}>
          <View style={styles.eisenhowerRow}>
            <TouchableOpacity style={[styles.eisenhowerCell, styles.q1]}>
              <Text style={styles.cellLabel}>Urgent &</Text>
              <Text style={styles.cellLabel}>Important</Text>
            </TouchableOpacity>
            <TouchableOpacity style={[styles.eisenhowerCell, styles.q2]}>
              <Text style={styles.cellLabel}>Not Urgent &</Text>
              <Text style={styles.cellLabel}>Important</Text>
            </TouchableOpacity>
          </View>
          <View style={styles.eisenhowerRow}>
            <TouchableOpacity style={[styles.eisenhowerCell, styles.q3]}>
              <Text style={styles.cellLabel}>Urgent &</Text>
              <Text style={styles.cellLabel}>Not Important</Text>
            </TouchableOpacity>
            <TouchableOpacity style={[styles.eisenhowerCell, styles.q4]}>
              <Text style={styles.cellLabel}>Not Urgent &</Text>
              <Text style={styles.cellLabel}>Not Important</Text>
            </TouchableOpacity>
          </View>
        </View>

        <TouchableOpacity
          style={[styles.captureButton, !title.trim() && styles.captureButtonDisabled]}
          onPress={handleCapture}
          disabled={!title.trim()}
        >
          <Text style={styles.captureButtonText}>Capture Task</Text>
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
  heading: {
    color: '#F8FAFC',
    fontSize: 24,
    fontWeight: '700',
  },
  subheading: {
    color: '#94A3B8',
    fontSize: 14,
    marginTop: 8,
    marginBottom: 24,
  },
  input: {
    backgroundColor: '#1E293B',
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 16,
    fontSize: 18,
    color: '#F8FAFC',
    borderWidth: 1,
    borderColor: '#334155',
  },
  sectionLabel: {
    color: '#CBD5E1',
    fontSize: 14,
    fontWeight: '500',
    marginTop: 24,
    marginBottom: 12,
  },
  eisenhowerGrid: {
    gap: 8,
  },
  eisenhowerRow: {
    flexDirection: 'row',
    gap: 8,
  },
  eisenhowerCell: {
    flex: 1,
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 2,
  },
  q1: {
    backgroundColor: '#1E293B',
    borderColor: '#EF4444',
  },
  q2: {
    backgroundColor: '#1E293B',
    borderColor: '#6366F1',
  },
  q3: {
    backgroundColor: '#1E293B',
    borderColor: '#F59E0B',
  },
  q4: {
    backgroundColor: '#1E293B',
    borderColor: '#334155',
  },
  cellLabel: {
    color: '#94A3B8',
    fontSize: 12,
    textAlign: 'center',
  },
  captureButton: {
    backgroundColor: '#6366F1',
    borderRadius: 12,
    paddingVertical: 16,
    alignItems: 'center',
    marginTop: 32,
  },
  captureButtonDisabled: {
    opacity: 0.4,
  },
  captureButtonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '600',
  },
});

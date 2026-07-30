import { View, Text, TouchableOpacity, StyleSheet, Vibration } from 'react-native';
import { useState, useEffect, useRef, useCallback } from 'react';
import { apiRequest } from '../services/api';

const DURATION_OPTIONS = [5, 10, 15, 25, 45];
const WARNING_THRESHOLD = 0.8;

type Props = {
  taskId: number;
  token: string;
  onComplete: () => void;
  onCancel: () => void;
};

type Phase = 'pick' | 'running' | 'warning' | 'done';

export default function FocusTimer({ taskId, token, onComplete, onCancel }: Props) {
  const [phase, setPhase] = useState<Phase>('pick');
  const [durationMin, setDurationMin] = useState(25);
  const [remainingSec, setRemainingSec] = useState(0);
  const [totalSec, setTotalSec] = useState(0);
  const startedAtRef = useRef<string | null>(null);
  const warningFiredRef = useRef(false);

  const start = useCallback((minutes: number) => {
    const secs = minutes * 60;
    setDurationMin(minutes);
    setTotalSec(secs);
    setRemainingSec(secs);
    setPhase('running');
    startedAtRef.current = new Date().toISOString();
    warningFiredRef.current = false;
  }, []);

  useEffect(() => {
    if (phase !== 'running' && phase !== 'warning') return;

    const interval = setInterval(() => {
      setRemainingSec(prev => {
        const next = prev - 1;

        // 80% warning
        if (!warningFiredRef.current && next <= totalSec * (1 - WARNING_THRESHOLD)) {
          warningFiredRef.current = true;
          setPhase('warning');
          Vibration.vibrate([0, 200, 100, 200]);
        }

        // Done
        if (next <= 0) {
          clearInterval(interval);
          setPhase('done');
          Vibration.vibrate([0, 300, 150, 300, 150, 300]);
          logSession(true);
          return 0;
        }
        return next;
      });
    }, 1000);

    return () => clearInterval(interval);
  }, [phase, totalSec]);

  const logSession = async (completed: boolean) => {
    if (!startedAtRef.current) return;
    const actualMinutes = Math.round((totalSec - remainingSec) / 60);
    try {
      await apiRequest(`/api/tasks/${taskId}/focus`, {
        method: 'POST', token,
        body: {
          durationMinutes: durationMin,
          actualMinutes: completed ? durationMin : actualMinutes,
          completed,
          startedAt: startedAtRef.current,
        },
      });
    } catch {
      // Non-critical — don't block UX
    }
  };

  const handleStop = async () => {
    await logSession(false);
    onCancel();
  };

  const elapsed = totalSec - remainingSec;
  const progress = totalSec > 0 ? (elapsed / totalSec) * 100 : 0;
  const mins = Math.floor(remainingSec / 60);
  const secs = remainingSec % 60;

  if (phase === 'pick') {
    return (
      <View style={styles.container}>
        <Text style={styles.pickTitle}>Focus session</Text>
        <Text style={styles.pickSubtitle}>How long do you want to focus?</Text>
        <View style={styles.durationRow}>
          {DURATION_OPTIONS.map(d => (
            <TouchableOpacity key={d} style={styles.durationBtn} onPress={() => start(d)}>
              <Text style={styles.durationText}>{d}m</Text>
            </TouchableOpacity>
          ))}
        </View>
        <TouchableOpacity onPress={onCancel}>
          <Text style={styles.cancelText}>Cancel</Text>
        </TouchableOpacity>
      </View>
    );
  }

  if (phase === 'done') {
    return (
      <View style={styles.container}>
        <Text style={styles.doneTitle}>Session complete</Text>
        <Text style={styles.doneSubtitle}>{durationMin} minutes of focus</Text>
        <View style={styles.doneActions}>
          <TouchableOpacity style={styles.doneBtn} onPress={onComplete}>
            <Text style={styles.doneBtnText}>Continue</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.extendBtn} onPress={() => start(durationMin)}>
            <Text style={styles.extendBtnText}>Another {durationMin}m</Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  }

  // running or warning
  const isWarning = phase === 'warning';

  return (
    <View style={styles.container}>
      {isWarning && (
        <Text style={styles.warningLabel}>Wrapping up soon</Text>
      )}
      <Text style={[styles.timer, isWarning && styles.timerWarning]}>
        {mins}:{secs.toString().padStart(2, '0')}
      </Text>
      <View style={styles.timerBar}>
        <View style={[
          styles.timerFill,
          { width: `${Math.min(100, progress)}%` },
          isWarning && styles.timerFillWarning,
        ]} />
      </View>
      <Text style={styles.timerMeta}>{durationMin}m session</Text>
      <TouchableOpacity style={styles.stopBtn} onPress={handleStop}>
        <Text style={styles.stopBtnText}>Stop</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#1E293B', borderRadius: 20, padding: 28,
    borderWidth: 1, borderColor: '#334155', alignItems: 'center',
  },
  // Pick phase
  pickTitle: { color: '#F8FAFC', fontSize: 22, fontWeight: '700' },
  pickSubtitle: { color: '#94A3B8', fontSize: 14, marginTop: 8, marginBottom: 20 },
  durationRow: { flexDirection: 'row', gap: 10, marginBottom: 20 },
  durationBtn: {
    backgroundColor: '#6366F1', borderRadius: 12, paddingVertical: 14, paddingHorizontal: 18,
  },
  durationText: { color: '#FFFFFF', fontSize: 16, fontWeight: '700' },
  cancelText: { color: '#64748B', fontSize: 14 },
  // Running phase
  timer: { color: '#F8FAFC', fontSize: 64, fontWeight: '200', fontVariant: ['tabular-nums'] },
  timerWarning: { color: '#F59E0B' },
  warningLabel: {
    color: '#F59E0B', fontSize: 13, fontWeight: '600', letterSpacing: 1, marginBottom: 4,
  },
  timerBar: {
    width: '100%', height: 6, backgroundColor: '#334155', borderRadius: 3,
    overflow: 'hidden' as const, marginTop: 16,
  },
  timerFill: { height: '100%' as const, backgroundColor: '#6366F1', borderRadius: 3 },
  timerFillWarning: { backgroundColor: '#F59E0B' },
  timerMeta: { color: '#64748B', fontSize: 12, marginTop: 8 },
  stopBtn: {
    marginTop: 20, borderWidth: 1, borderColor: '#475569', borderRadius: 12,
    paddingVertical: 10, paddingHorizontal: 24,
  },
  stopBtnText: { color: '#94A3B8', fontSize: 14, fontWeight: '600' },
  // Done phase
  doneTitle: { color: '#22C55E', fontSize: 22, fontWeight: '700' },
  doneSubtitle: { color: '#94A3B8', fontSize: 14, marginTop: 6, marginBottom: 20 },
  doneActions: { flexDirection: 'row', gap: 12 },
  doneBtn: { backgroundColor: '#6366F1', borderRadius: 12, paddingVertical: 14, paddingHorizontal: 24 },
  doneBtnText: { color: '#FFFFFF', fontSize: 15, fontWeight: '600' },
  extendBtn: {
    borderWidth: 1, borderColor: '#475569', borderRadius: 12,
    paddingVertical: 14, paddingHorizontal: 24,
  },
  extendBtnText: { color: '#94A3B8', fontSize: 15, fontWeight: '600' },
});

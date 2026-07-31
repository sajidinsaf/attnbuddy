import { View, Text, TouchableOpacity, StyleSheet, Vibration } from 'react-native';
import { useState, useEffect, useRef, useCallback } from 'react';
import { apiRequest } from '../services/api';

const DURATION_OPTIONS = [5, 10, 15, 25, 45];
const WARNING_THRESHOLD = 0.8;
const CHECK_IN_INTERVAL = 300; // 5 minutes in seconds

const CHECK_IN_MESSAGES = [
  "Still with me? You're doing great.",
  "How's it going? Keep at it.",
  "Checking in — you've got this.",
  "Still working? Awesome focus.",
  "Quick check — need anything?",
  "You're making progress. Keep going.",
  "Just checking in. You're doing well.",
  "Still here with you. How's it feel?",
];

type Props = {
  taskId: number;
  token: string;
  onComplete: () => void;
  onCancel: () => void;
};

type Phase = 'pick' | 'running' | 'warning' | 'done' | 'checkin';
type Mode = 'focus' | 'bodydouble';

export default function FocusTimer({ taskId, token, onComplete, onCancel }: Props) {
  const [mode, setMode] = useState<Mode | null>(null);
  const [phase, setPhase] = useState<Phase>('pick');
  const [durationMin, setDurationMin] = useState(25);
  const [remainingSec, setRemainingSec] = useState(0);
  const [totalSec, setTotalSec] = useState(0);
  const [checkInCount, setCheckInCount] = useState(0);
  const startedAtRef = useRef<string | null>(null);
  const warningFiredRef = useRef(false);
  const lastCheckInRef = useRef(0);
  const preCheckInPhaseRef = useRef<Phase>('running');

  const start = useCallback((minutes: number, selectedMode: Mode) => {
    const secs = minutes * 60;
    setMode(selectedMode);
    setDurationMin(minutes);
    setTotalSec(secs);
    setRemainingSec(secs);
    setPhase('running');
    setCheckInCount(0);
    startedAtRef.current = new Date().toISOString();
    warningFiredRef.current = false;
    lastCheckInRef.current = 0;
  }, []);

  useEffect(() => {
    if (phase !== 'running' && phase !== 'warning') return;

    const interval = setInterval(() => {
      setRemainingSec(prev => {
        const next = prev - 1;
        const elapsed = totalSec - next;

        // Body double check-in every 5 minutes
        if (mode === 'bodydouble' && elapsed > 0
            && elapsed - lastCheckInRef.current >= CHECK_IN_INTERVAL
            && next > 30) { // Don't check in during last 30 seconds
          lastCheckInRef.current = elapsed;
          preCheckInPhaseRef.current = warningFiredRef.current ? 'warning' : 'running';
          setPhase('checkin');
          setCheckInCount(c => c + 1);
          Vibration.vibrate([0, 150, 100, 150]);
        }

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
  }, [phase, totalSec, mode]);

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
      // Non-critical
    }
  };

  const handleStop = async () => {
    await logSession(false);
    onCancel();
  };

  const handleCheckInResponse = (response: 'continue' | 'break' | 'done') => {
    if (response === 'done') {
      logSession(false);
      onComplete();
    } else if (response === 'break') {
      handleStop();
    } else {
      setPhase(preCheckInPhaseRef.current);
    }
  };

  const elapsed = totalSec - remainingSec;
  const progress = totalSec > 0 ? (elapsed / totalSec) * 100 : 0;
  const mins = Math.floor(remainingSec / 60);
  const secs = remainingSec % 60;

  // Mode picker
  if (phase === 'pick' && mode === null) {
    return (
      <View style={styles.container}>
        <Text style={styles.pickTitle}>Focus session</Text>
        <Text style={styles.pickSubtitle}>Choose your mode</Text>
        <TouchableOpacity style={styles.modeBtn} onPress={() => setMode('focus')}>
          <Text style={styles.modeBtnTitle}>Solo Focus</Text>
          <Text style={styles.modeBtnDesc}>Countdown timer with 80% warning</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.modeBtn} onPress={() => setMode('bodydouble')}>
          <Text style={styles.modeBtnTitle}>Focus Companion</Text>
          <Text style={styles.modeBtnDesc}>Gentle check-ins every 5 min — like having someone working alongside you</Text>
        </TouchableOpacity>
        <TouchableOpacity onPress={onCancel} style={{ marginTop: 16 }}>
          <Text style={styles.cancelText}>Cancel</Text>
        </TouchableOpacity>
      </View>
    );
  }

  // Duration picker
  if (phase === 'pick') {
    return (
      <View style={styles.container}>
        <Text style={styles.pickTitle}>
          {mode === 'bodydouble' ? 'Focus Companion' : 'Focus session'}
        </Text>
        <Text style={styles.pickSubtitle}>How long?</Text>
        <View style={styles.durationRow}>
          {DURATION_OPTIONS.map(d => (
            <TouchableOpacity key={d} style={styles.durationBtn} onPress={() => start(d, mode!)}>
              <Text style={styles.durationText}>{d}m</Text>
            </TouchableOpacity>
          ))}
        </View>
        <TouchableOpacity onPress={() => setMode(null)}>
          <Text style={styles.cancelText}>Back</Text>
        </TouchableOpacity>
      </View>
    );
  }

  // Check-in (focus companion only)
  if (phase === 'checkin') {
    const message = CHECK_IN_MESSAGES[(checkInCount - 1) % CHECK_IN_MESSAGES.length];
    return (
      <View style={styles.container}>
        <Text style={styles.checkInLabel}>CHECK-IN</Text>
        <Text style={styles.checkInMessage}>{message}</Text>
        <Text style={styles.checkInTimer}>
          {mins}:{secs.toString().padStart(2, '0')} remaining
        </Text>
        <View style={styles.checkInActions}>
          <TouchableOpacity style={styles.checkInContinue} onPress={() => handleCheckInResponse('continue')}>
            <Text style={styles.checkInContinueText}>Still going</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.checkInBreak} onPress={() => handleCheckInResponse('break')}>
            <Text style={styles.checkInBreakText}>Need a break</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.checkInDone} onPress={() => handleCheckInResponse('done')}>
            <Text style={styles.checkInDoneText}>I'm done</Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  }

  // Done
  if (phase === 'done') {
    return (
      <View style={styles.container}>
        <Text style={styles.doneTitle}>Session complete</Text>
        <Text style={styles.doneSubtitle}>
          {durationMin} minutes of {mode === 'bodydouble' ? 'focus companion' : 'focus'}
          {mode === 'bodydouble' && checkInCount > 0 ? ` · ${checkInCount} check-ins` : ''}
        </Text>
        <View style={styles.doneActions}>
          <TouchableOpacity style={styles.doneBtn} onPress={onComplete}>
            <Text style={styles.doneBtnText}>Continue</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.extendBtn} onPress={() => start(durationMin, mode!)}>
            <Text style={styles.extendBtnText}>Another {durationMin}m</Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  }

  // Running or warning
  const isWarning = phase === 'warning';

  return (
    <View style={styles.container}>
      {mode === 'bodydouble' && (
        <Text style={styles.focusCompanionLabel}>FOCUS COMPANION</Text>
      )}
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
      <Text style={styles.timerMeta}>
        {durationMin}m {mode === 'bodydouble' ? 'focus companion' : 'session'}
      </Text>
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
  // Mode picker
  modeBtn: {
    backgroundColor: '#0F172A', borderRadius: 14, padding: 18, width: '100%',
    marginBottom: 10, borderWidth: 1, borderColor: '#334155',
  },
  modeBtnTitle: { color: '#F8FAFC', fontSize: 17, fontWeight: '700' },
  modeBtnDesc: { color: '#94A3B8', fontSize: 13, marginTop: 4 },
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
  focusCompanionLabel: {
    color: '#A78BFA', fontSize: 11, fontWeight: '700', letterSpacing: 2, marginBottom: 4,
  },
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
  // Check-in phase
  checkInLabel: { color: '#A78BFA', fontSize: 11, fontWeight: '700', letterSpacing: 2, marginBottom: 8 },
  checkInMessage: { color: '#F8FAFC', fontSize: 20, fontWeight: '600', textAlign: 'center', lineHeight: 28 },
  checkInTimer: { color: '#64748B', fontSize: 13, marginTop: 12, marginBottom: 20 },
  checkInActions: { width: '100%', gap: 8 },
  checkInContinue: { backgroundColor: '#6366F1', borderRadius: 12, paddingVertical: 14, alignItems: 'center' },
  checkInContinueText: { color: '#FFFFFF', fontSize: 15, fontWeight: '600' },
  checkInBreak: { borderWidth: 1, borderColor: '#475569', borderRadius: 12, paddingVertical: 14, alignItems: 'center' },
  checkInBreakText: { color: '#94A3B8', fontSize: 15, fontWeight: '600' },
  checkInDone: { paddingVertical: 10, alignItems: 'center' },
  checkInDoneText: { color: '#64748B', fontSize: 14 },
  // Done phase
  doneTitle: { color: '#22C55E', fontSize: 22, fontWeight: '700' },
  doneSubtitle: { color: '#94A3B8', fontSize: 14, marginTop: 6, marginBottom: 20, textAlign: 'center' },
  doneActions: { flexDirection: 'row', gap: 12 },
  doneBtn: { backgroundColor: '#6366F1', borderRadius: 12, paddingVertical: 14, paddingHorizontal: 24 },
  doneBtnText: { color: '#FFFFFF', fontSize: 15, fontWeight: '600' },
  extendBtn: {
    borderWidth: 1, borderColor: '#475569', borderRadius: 12,
    paddingVertical: 14, paddingHorizontal: 24,
  },
  extendBtnText: { color: '#94A3B8', fontSize: 15, fontWeight: '600' },
});

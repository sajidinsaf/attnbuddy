import { View, Text, TouchableOpacity, StyleSheet, Vibration } from 'react-native';
import { useState, useEffect, useRef } from 'react';

const GROUNDING_STEPS = [
  { sense: 'SEE', prompt: 'Name 5 things you can see right now.' },
  { sense: 'TOUCH', prompt: 'Name 4 things you can physically feel.' },
  { sense: 'HEAR', prompt: 'Name 3 things you can hear.' },
  { sense: 'SMELL', prompt: 'Name 2 things you can smell.' },
  { sense: 'TASTE', prompt: 'Name 1 thing you can taste.' },
];

type Props = {
  onMicroSession: () => void; // Start a 5-min focus session
  onPark: () => void;         // Snooze with breadcrumb
  onClose: () => void;
};

type Phase = 'menu' | 'grounding' | 'breathing' | 'groundingDone';

export default function RescueMode({ onMicroSession, onPark, onClose }: Props) {
  const [phase, setPhase] = useState<Phase>('menu');
  const [groundingStep, setGroundingStep] = useState(0);
  const [breathPhase, setBreathPhase] = useState<'in' | 'hold' | 'out'>('in');
  const [breathCount, setBreathCount] = useState(0);
  const [breathTimer, setBreathTimer] = useState(4);

  // Breathing exercise: 4-4-4 box breathing
  useEffect(() => {
    if (phase !== 'breathing') return;

    const interval = setInterval(() => {
      setBreathTimer(prev => {
        if (prev <= 1) {
          setBreathPhase(current => {
            if (current === 'in') return 'hold';
            if (current === 'hold') return 'out';
            // 'out' → next cycle
            setBreathCount(c => c + 1);
            return 'in';
          });
          Vibration.vibrate(100);
          return 4;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(interval);
  }, [phase]);

  // Menu
  if (phase === 'menu') {
    return (
      <View style={styles.container}>
        <Text style={styles.title}>It's okay.</Text>
        <Text style={styles.subtitle}>
          Take a breath. You don't have to do everything right now.
        </Text>

        <TouchableOpacity style={styles.option} onPress={onMicroSession}>
          <Text style={styles.optionTitle}>Just 5 minutes</Text>
          <Text style={styles.optionDesc}>Work on one small thing. That's enough.</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.option} onPress={onPark}>
          <Text style={styles.optionTitle}>Park it with a plan</Text>
          <Text style={styles.optionDesc}>Leave a note for future you and step away.</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.option} onPress={() => setPhase('breathing')}>
          <Text style={styles.optionTitle}>Breathe first</Text>
          <Text style={styles.optionDesc}>Box breathing — 1 minute to reset.</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.option} onPress={() => setPhase('grounding')}>
          <Text style={styles.optionTitle}>Ground myself</Text>
          <Text style={styles.optionDesc}>5-4-3-2-1 senses exercise.</Text>
        </TouchableOpacity>

        <TouchableOpacity onPress={onClose} style={{ marginTop: 16 }}>
          <Text style={styles.cancelText}>I'm okay, go back</Text>
        </TouchableOpacity>
      </View>
    );
  }

  // Breathing exercise
  if (phase === 'breathing') {
    const label = breathPhase === 'in' ? 'Breathe in...' :
                  breathPhase === 'hold' ? 'Hold...' : 'Breathe out...';

    if (breathCount >= 4) {
      return (
        <View style={styles.container}>
          <Text style={styles.doneTitle}>Well done</Text>
          <Text style={styles.subtitle}>4 breaths completed. How do you feel?</Text>
          <TouchableOpacity style={styles.doneBtn} onPress={() => { setPhase('menu'); setBreathCount(0); setBreathTimer(4); setBreathPhase('in'); }}>
            <Text style={styles.doneBtnText}>Back to options</Text>
          </TouchableOpacity>
          <TouchableOpacity onPress={onClose} style={{ marginTop: 12 }}>
            <Text style={styles.cancelText}>I'm ready to go back</Text>
          </TouchableOpacity>
        </View>
      );
    }

    return (
      <View style={styles.container}>
        <Text style={styles.breathLabel}>BOX BREATHING</Text>
        <Text style={styles.breathPhase}>{label}</Text>
        <Text style={styles.breathTimer}>{breathTimer}</Text>
        <View style={styles.breathDots}>
          {[0, 1, 2, 3].map(i => (
            <View key={i} style={[styles.breathDot, i < breathCount && styles.breathDotFilled]} />
          ))}
        </View>
        <TouchableOpacity onPress={() => { setPhase('menu'); setBreathCount(0); setBreathTimer(4); setBreathPhase('in'); }} style={{ marginTop: 20 }}>
          <Text style={styles.cancelText}>Stop</Text>
        </TouchableOpacity>
      </View>
    );
  }

  // Grounding exercise
  if (phase === 'grounding') {
    const step = GROUNDING_STEPS[groundingStep];

    return (
      <View style={styles.container}>
        <Text style={styles.breathLabel}>{step.sense}</Text>
        <Text style={styles.groundingPrompt}>{step.prompt}</Text>
        <Text style={styles.groundingProgress}>{groundingStep + 1} of 5</Text>

        {groundingStep < 4 ? (
          <TouchableOpacity style={styles.doneBtn} onPress={() => { setGroundingStep(s => s + 1); Vibration.vibrate(50); }}>
            <Text style={styles.doneBtnText}>Next</Text>
          </TouchableOpacity>
        ) : (
          <TouchableOpacity style={styles.doneBtn} onPress={() => { setPhase('groundingDone'); setGroundingStep(0); }}>
            <Text style={styles.doneBtnText}>Done</Text>
          </TouchableOpacity>
        )}
        <TouchableOpacity onPress={() => { setPhase('menu'); setGroundingStep(0); }} style={{ marginTop: 12 }}>
          <Text style={styles.cancelText}>Stop</Text>
        </TouchableOpacity>
      </View>
    );
  }

  // Grounding done
  return (
    <View style={styles.container}>
      <Text style={styles.doneTitle}>You're here.</Text>
      <Text style={styles.subtitle}>Take a moment. There's no rush.</Text>
      <TouchableOpacity style={styles.doneBtn} onPress={() => setPhase('menu')}>
        <Text style={styles.doneBtnText}>Back to options</Text>
      </TouchableOpacity>
      <TouchableOpacity onPress={onClose} style={{ marginTop: 12 }}>
        <Text style={styles.cancelText}>I'm ready to go back</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#1E293B', borderRadius: 20, padding: 28,
    borderWidth: 1, borderColor: '#334155', alignItems: 'center',
  },
  title: { color: '#F8FAFC', fontSize: 28, fontWeight: '700' },
  subtitle: { color: '#94A3B8', fontSize: 15, textAlign: 'center', marginTop: 8, marginBottom: 24, lineHeight: 22 },
  option: {
    backgroundColor: '#0F172A', borderRadius: 14, padding: 18, width: '100%',
    marginBottom: 10, borderWidth: 1, borderColor: '#334155',
  },
  optionTitle: { color: '#F8FAFC', fontSize: 17, fontWeight: '600' },
  optionDesc: { color: '#94A3B8', fontSize: 13, marginTop: 4 },
  cancelText: { color: '#64748B', fontSize: 14 },
  // Breathing
  breathLabel: { color: '#A78BFA', fontSize: 11, fontWeight: '700', letterSpacing: 2, marginBottom: 16 },
  breathPhase: { color: '#F8FAFC', fontSize: 28, fontWeight: '300' },
  breathTimer: { color: '#6366F1', fontSize: 72, fontWeight: '200', fontVariant: ['tabular-nums'], marginVertical: 8 },
  breathDots: { flexDirection: 'row', gap: 8, marginTop: 8 },
  breathDot: { width: 10, height: 10, borderRadius: 5, backgroundColor: '#334155' },
  breathDotFilled: { backgroundColor: '#6366F1' },
  // Grounding
  groundingPrompt: { color: '#F8FAFC', fontSize: 22, fontWeight: '500', textAlign: 'center', marginTop: 8, lineHeight: 30 },
  groundingProgress: { color: '#64748B', fontSize: 13, marginTop: 16, marginBottom: 20 },
  // Done
  doneTitle: { color: '#22C55E', fontSize: 24, fontWeight: '700' },
  doneBtn: { backgroundColor: '#6366F1', borderRadius: 12, paddingVertical: 14, paddingHorizontal: 24, marginTop: 8 },
  doneBtnText: { color: '#FFFFFF', fontSize: 15, fontWeight: '600' },
});

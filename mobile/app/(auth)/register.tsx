import { View, Text, TextInput, TouchableOpacity, StyleSheet } from 'react-native';
import { Link } from 'expo-router';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useState } from 'react';

type Profile = 'EXECUTIVE' | 'PROFESSIONAL' | 'STUDENT';

const profiles: { value: Profile; label: string; description: string }[] = [
  { value: 'EXECUTIVE', label: 'Executive', description: 'Business leader managing strategic priorities' },
  { value: 'PROFESSIONAL', label: 'Professional', description: 'Knowledge worker with competing responsibilities' },
  { value: 'STUDENT', label: 'Student', description: 'Managing academic workload and deadlines' },
];

export default function RegisterScreen() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [profile, setProfile] = useState<Profile>('PROFESSIONAL');

  const handleRegister = () => {
    // TODO: Implement API register call
  };

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.content}>
        <Text style={styles.title}>Get Started</Text>
        <Text style={styles.subtitle}>Tell us a bit about yourself</Text>

        <View style={styles.form}>
          <TextInput
            style={styles.input}
            placeholder="Your name"
            placeholderTextColor="#64748B"
            value={name}
            onChangeText={setName}
            autoComplete="name"
          />
          <TextInput
            style={styles.input}
            placeholder="Email"
            placeholderTextColor="#64748B"
            value={email}
            onChangeText={setEmail}
            keyboardType="email-address"
            autoCapitalize="none"
            autoComplete="email"
          />
          <TextInput
            style={styles.input}
            placeholder="Password"
            placeholderTextColor="#64748B"
            value={password}
            onChangeText={setPassword}
            secureTextEntry
          />

          <Text style={styles.sectionLabel}>What best describes you?</Text>
          <View style={styles.profilePicker}>
            {profiles.map((p) => (
              <TouchableOpacity
                key={p.value}
                style={[styles.profileOption, profile === p.value && styles.profileSelected]}
                onPress={() => setProfile(p.value)}
              >
                <Text style={[styles.profileLabel, profile === p.value && styles.profileLabelSelected]}>
                  {p.label}
                </Text>
                <Text style={styles.profileDesc}>{p.description}</Text>
              </TouchableOpacity>
            ))}
          </View>

          <TouchableOpacity style={styles.button} onPress={handleRegister}>
            <Text style={styles.buttonText}>Create Account</Text>
          </TouchableOpacity>
        </View>

        <Link href="/(auth)/login" asChild>
          <TouchableOpacity style={styles.linkButton}>
            <Text style={styles.linkText}>
              Already have an account? <Text style={styles.linkBold}>Sign in</Text>
            </Text>
          </TouchableOpacity>
        </Link>
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
  },
  title: {
    fontSize: 32,
    fontWeight: '700',
    color: '#F8FAFC',
    textAlign: 'center',
  },
  subtitle: {
    fontSize: 16,
    color: '#94A3B8',
    textAlign: 'center',
    marginTop: 8,
    marginBottom: 32,
  },
  form: {
    gap: 16,
  },
  input: {
    backgroundColor: '#1E293B',
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 14,
    fontSize: 16,
    color: '#F8FAFC',
    borderWidth: 1,
    borderColor: '#334155',
  },
  sectionLabel: {
    color: '#CBD5E1',
    fontSize: 14,
    fontWeight: '500',
    marginTop: 8,
  },
  profilePicker: {
    gap: 10,
  },
  profileOption: {
    backgroundColor: '#1E293B',
    borderRadius: 12,
    padding: 14,
    borderWidth: 2,
    borderColor: '#334155',
  },
  profileSelected: {
    borderColor: '#6366F1',
    backgroundColor: '#1E1B4B',
  },
  profileLabel: {
    color: '#F8FAFC',
    fontSize: 16,
    fontWeight: '600',
  },
  profileLabelSelected: {
    color: '#A5B4FC',
  },
  profileDesc: {
    color: '#94A3B8',
    fontSize: 13,
    marginTop: 4,
  },
  button: {
    backgroundColor: '#6366F1',
    borderRadius: 12,
    paddingVertical: 16,
    alignItems: 'center',
    marginTop: 8,
  },
  buttonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '600',
  },
  linkButton: {
    marginTop: 24,
    alignItems: 'center',
  },
  linkText: {
    color: '#94A3B8',
    fontSize: 14,
  },
  linkBold: {
    color: '#6366F1',
    fontWeight: '600',
  },
});

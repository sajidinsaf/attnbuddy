import { View, Text, TextInput, TouchableOpacity, FlatList, StyleSheet, ActivityIndicator, KeyboardAvoidingView, Platform } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useState, useRef } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { apiRequest } from '../../services/api';

type Message = {
  id: string;
  role: 'user' | 'assistant';
  content: string;
};

const WELCOME_MESSAGE: Message = {
  id: 'welcome',
  role: 'assistant',
  content: "Hi! I'm your Brasstacks assistant. Ask me anything — about your tasks, how features work, or what to focus on. For example:\n\n" +
    "\"What tasks do I have?\"\n" +
    "\"What did I finish this week?\"\n" +
    "\"How does body doubling work?\"\n" +
    "\"What should I focus on?\"",
};

export default function ChatScreen() {
  const { token } = useAuth();
  const [messages, setMessages] = useState<Message[]>([WELCOME_MESSAGE]);
  const [input, setInput] = useState('');
  const [sending, setSending] = useState(false);
  const flatListRef = useRef<FlatList>(null);

  const sendMessage = async () => {
    if (!token || !input.trim() || sending) return;

    const userMsg: Message = {
      id: Date.now().toString(),
      role: 'user',
      content: input.trim(),
    };

    const updatedMessages = [...messages, userMsg];
    setMessages(updatedMessages);
    setInput('');
    setSending(true);

    try {
      // Build history (exclude welcome message)
      const history = updatedMessages
        .filter(m => m.id !== 'welcome')
        .slice(-10) // Last 10 messages for context
        .map(m => ({ role: m.role, content: m.content }));

      // Remove the last user message from history since we send it separately
      history.pop();

      const res = await apiRequest<{ reply: string }>('/api/chat', {
        method: 'POST',
        token,
        body: {
          message: userMsg.content,
          history: history.length > 0 ? history : null,
        },
      });

      const assistantMsg: Message = {
        id: (Date.now() + 1).toString(),
        role: 'assistant',
        content: res?.reply || "Sorry, I couldn't process that. Please try again.",
      };

      setMessages(prev => [...prev, assistantMsg]);
    } catch (e: any) {
      const errorMsg: Message = {
        id: (Date.now() + 1).toString(),
        role: 'assistant',
        content: "Something went wrong. Make sure AI is enabled in Settings with a valid API key.",
      };
      setMessages(prev => [...prev, errorMsg]);
    } finally {
      setSending(false);
    }
  };

  return (
    <SafeAreaView style={styles.container} edges={['bottom']}>
      <KeyboardAvoidingView
        style={{ flex: 1 }}
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        keyboardVerticalOffset={90}
      >
        <FlatList
          ref={flatListRef}
          data={messages}
          keyExtractor={item => item.id}
          contentContainerStyle={styles.messageList}
          onContentSizeChange={() => flatListRef.current?.scrollToEnd({ animated: true })}
          renderItem={({ item }) => (
            <View style={[
              styles.messageBubble,
              item.role === 'user' ? styles.userBubble : styles.assistantBubble,
            ]}>
              <Text style={[
                styles.messageText,
                item.role === 'user' && styles.userText,
              ]}>
                {item.content}
              </Text>
            </View>
          )}
          ListFooterComponent={sending ? (
            <View style={[styles.messageBubble, styles.assistantBubble]}>
              <ActivityIndicator size="small" color="#6366F1" />
            </View>
          ) : null}
        />

        <View style={styles.inputRow}>
          <TextInput
            style={styles.textInput}
            placeholder="Ask me anything..."
            placeholderTextColor="#64748B"
            value={input}
            onChangeText={setInput}
            onSubmitEditing={sendMessage}
            returnKeyType="send"
            multiline
            maxLength={500}
            editable={!sending}
          />
          <TouchableOpacity
            style={[styles.sendBtn, (!input.trim() || sending) && styles.sendBtnDisabled]}
            onPress={sendMessage}
            disabled={!input.trim() || sending}
          >
            <Text style={styles.sendBtnText}>Send</Text>
          </TouchableOpacity>
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#0F172A' },
  messageList: { paddingHorizontal: 16, paddingTop: 8, paddingBottom: 8 },
  messageBubble: {
    maxWidth: '85%', borderRadius: 16, padding: 14, marginBottom: 8,
  },
  userBubble: {
    backgroundColor: '#6366F1', alignSelf: 'flex-end',
    borderBottomRightRadius: 4,
  },
  assistantBubble: {
    backgroundColor: '#1E293B', alignSelf: 'flex-start',
    borderBottomLeftRadius: 4, borderWidth: 1, borderColor: '#334155',
  },
  messageText: { color: '#E2E8F0', fontSize: 15, lineHeight: 22 },
  userText: { color: '#FFFFFF' },
  inputRow: {
    flexDirection: 'row', alignItems: 'flex-end', paddingHorizontal: 12,
    paddingVertical: 8, borderTopWidth: 1, borderTopColor: '#1E293B',
    backgroundColor: '#0F172A',
  },
  textInput: {
    flex: 1, backgroundColor: '#1E293B', borderRadius: 20, paddingHorizontal: 16,
    paddingVertical: 10, color: '#F8FAFC', fontSize: 15, maxHeight: 100,
    borderWidth: 1, borderColor: '#334155',
  },
  sendBtn: {
    backgroundColor: '#6366F1', borderRadius: 20, paddingVertical: 10,
    paddingHorizontal: 18, marginLeft: 8,
  },
  sendBtnDisabled: { backgroundColor: '#334155' },
  sendBtnText: { color: '#FFFFFF', fontSize: 15, fontWeight: '600' },
});

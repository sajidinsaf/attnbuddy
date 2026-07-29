import { Redirect } from 'expo-router';

export default function Index() {
  // TODO: Check auth state and redirect accordingly
  // For now, always go to the main screen
  return <Redirect href="/(main)/now" />;
}

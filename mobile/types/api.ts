// Auth
export type Profile = 'EXECUTIVE' | 'PROFESSIONAL' | 'STUDENT';

export type AuthResponse = {
  userId: number;
  accessToken: string;
  refreshToken: string | null;
  expiresIn: number;
};

// Domains
export type Domain = {
  id: number;
  name: string;
  color: string;
  weight: number;
  activeStart: string | null;
  activeEnd: string | null;
  position: number;
};

// Tasks
export type Urgency = 'URGENT' | 'NOT_URGENT';
export type Importance = 'IMPORTANT' | 'NOT_IMPORTANT';
export type TaskStatus = 'PENDING' | 'DONE' | 'SKIPPED' | 'SNOOZED';
export type TaskType = 'ONE_TIME' | 'SUSTAINED';

export type SustainedProgress = {
  completedSessions: number;
  totalMinutesLogged: number;
  estimatedTotalMinutes: number;
  percentComplete: number;
};

export type MicroStep = {
  id: number;
  title: string;
  status: TaskStatus;
  position: number;
};

export type TaskResponse = {
  id: number;
  title: string;
  notes: string | null;
  urgency: Urgency;
  importance: Importance;
  status: TaskStatus;
  dueDate: string | null;
  snoozedUntil: string | null;
  createdAt: string;
  completedAt: string | null;
  domainId: number | null;
  domainName: string | null;
  domainColor: string | null;
  score: number | null;
  taskType: TaskType;
  dailyEffortMinutes: number | null;
  targetDate: string | null;
  progress: SustainedProgress | null;
  parentTaskId: number | null;
  microSteps: MicroStep[] | null;
};

export type NowResponse = {
  task: TaskResponse | null;
  pendingCount: number;
};

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { useAuth } from './AuthContext';
import type { Department, Title, Person, Task, Meeting } from '../types/models';

type DataState<T> = {
  data: T;
  loading: boolean;
  lastFetched: number | null;
};

type DataContextValue = {
  departments: DataState<Department[]>;
  titles: DataState<Title[]>;
  people: DataState<Person[]>;
  tasks: DataState<Task[]>;
  meetings: DataState<Meeting[]>;
  refreshDepartments: (force?: boolean) => Promise<void>;
  refreshTitles: (force?: boolean) => Promise<void>;
  refreshPeople: (force?: boolean) => Promise<void>;
  refreshTasks: (force?: boolean) => Promise<void>;
  refreshMeetings: (force?: boolean) => Promise<void>;
  refreshAll: (force?: boolean) => Promise<void>;
};

const DataContext = createContext<DataContextValue | undefined>(undefined);

const TTL_MS = 5 * 60 * 1000;

export function DataProvider({ children }: { children: React.ReactNode }) {
  const { token, isAuthenticated } = useAuth();

  const [departments, setDepartments] = useState<DataState<Department[]>>({ data: [], loading: false, lastFetched: null });
  const [titles, setTitles] = useState<DataState<Title[]>>({ data: [], loading: false, lastFetched: null });
  const [people, setPeople] = useState<DataState<Person[]>>({ data: [], loading: false, lastFetched: null });
  const [tasks, setTasks] = useState<DataState<Task[]>>({ data: [], loading: false, lastFetched: null });
  const [meetings, setMeetings] = useState<DataState<Meeting[]>>({ data: [], loading: false, lastFetched: null });

  const shouldFetch = (lastFetched: number | null, force?: boolean) => {
    if (force) return true;
    if (!lastFetched) return true;
    return Date.now() - lastFetched > TTL_MS;
  };

  const authHeader = useMemo(() => (token ? { Authorization: `Bearer ${token}` } : {}), [token]);

  const refreshDepartments = useCallback(async (force?: boolean) => {
    if (!isAuthenticated || !token) return;
    if (!shouldFetch(departments.lastFetched, force)) return;
    setDepartments((s) => ({ ...s, loading: true }));
    try {
      const res = await fetch('http://localhost:8081/api/departments', { headers: authHeader as any });
      if (res.ok) {
        const data: Department[] = await res.json();
        const ordered = [...data].sort((a, b) => a.name.localeCompare(b.name));
        setDepartments({ data: ordered, loading: false, lastFetched: Date.now() });
      } else {
        setDepartments((s) => ({ ...s, loading: false }));
      }
    } catch {
      setDepartments((s) => ({ ...s, loading: false }));
    }
  }, [isAuthenticated, token, departments.lastFetched, authHeader]);

  const refreshTitles = useCallback(async (force?: boolean) => {
    if (!isAuthenticated || !token) return;
    if (!shouldFetch(titles.lastFetched, force)) return;
    setTitles((s) => ({ ...s, loading: true }));
    try {
      const res = await fetch('http://localhost:8081/api/titles', { headers: authHeader as any });
      if (res.ok) {
        const data: Title[] = await res.json();
        const ordered = [...data].sort((a, b) => a.name.localeCompare(b.name));
        setTitles({ data: ordered, loading: false, lastFetched: Date.now() });
      } else {
        setTitles((s) => ({ ...s, loading: false }));
      }
    } catch {
      setTitles((s) => ({ ...s, loading: false }));
    }
  }, [isAuthenticated, token, titles.lastFetched, authHeader]);

  const refreshPeople = useCallback(async (force?: boolean) => {
    if (!isAuthenticated || !token) return;
    if (!shouldFetch(people.lastFetched, force)) return;
    setPeople((s) => ({ ...s, loading: true }));
    try {
      const res = await fetch('http://localhost:8081/api/people?page=0&size=1000', { headers: authHeader as any });
      if (res.ok) {
        const raw = await res.json();
        const content: Person[] = Array.isArray(raw) ? raw : (raw?.content || []);
        setPeople({ data: content, loading: false, lastFetched: Date.now() });
      } else {
        setPeople((s) => ({ ...s, loading: false }));
      }
    } catch {
      setPeople((s) => ({ ...s, loading: false }));
    }
  }, [isAuthenticated, token, people.lastFetched, authHeader]);

  const refreshTasks = useCallback(async (force?: boolean) => {
    if (!isAuthenticated || !token) return;
    if (!shouldFetch(tasks.lastFetched, force)) return;
    setTasks((s) => ({ ...s, loading: true }));
    try {
      const res = await fetch('http://localhost:8081/api/tasks', { headers: authHeader as any });
      if (res.ok) {
        const raw = await res.json();
        const list: Task[] = Array.isArray(raw) ? raw : (raw?.content || []);
        setTasks({ data: list, loading: false, lastFetched: Date.now() });
      } else {
        setTasks((s) => ({ ...s, loading: false }));
      }
    } catch {
      setTasks((s) => ({ ...s, loading: false }));
    }
  }, [isAuthenticated, token, tasks.lastFetched, authHeader]);

  const refreshMeetings = useCallback(async (force?: boolean) => {
    if (!isAuthenticated || !token) return;
    if (!shouldFetch(meetings.lastFetched, force)) return;
    setMeetings((s) => ({ ...s, loading: true }));
    try {
      const res = await fetch('http://localhost:8081/api/meetings', { headers: authHeader as any });
      if (res.ok) {
        const raw = await res.json();
        const list: Meeting[] = Array.isArray(raw) ? raw : (raw?.content || []);
        setMeetings({ data: list, loading: false, lastFetched: Date.now() });
      } else {
        setMeetings((s) => ({ ...s, loading: false }));
      }
    } catch {
      setMeetings((s) => ({ ...s, loading: false }));
    }
  }, [isAuthenticated, token, meetings.lastFetched, authHeader]);

  const refreshAll = useCallback(async (force?: boolean) => {
    await Promise.all([
      refreshDepartments(force),
      refreshTitles(force),
      refreshPeople(force),
      refreshTasks(force),
      refreshMeetings(force),
    ]);
  }, [refreshDepartments, refreshTitles, refreshPeople, refreshTasks, refreshMeetings]);

  useEffect(() => {
    if (!isAuthenticated || !token) return;
    refreshAll();
  }, [isAuthenticated, token, refreshAll]);

  const value: DataContextValue = {
    departments,
    titles,
    people,
    tasks,
    meetings,
    refreshDepartments,
    refreshTitles,
    refreshPeople,
    refreshTasks,
    refreshMeetings,
    refreshAll,
  };

  return <DataContext.Provider value={value}>{children}</DataContext.Provider>;
}

export function useData() {
  const ctx = useContext(DataContext);
  if (!ctx) throw new Error('useData must be used within DataProvider');
  return ctx;
}



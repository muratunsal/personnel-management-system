import { createContext, useContext, useState, ReactNode } from 'react';

export type TabKey = 'Dashboard' | 'People' | 'Organization' | 'Messages' | 'Calendar' | 'Settings';

type TabsContextValue = {
  activeTab: TabKey;
  setActiveTab: (key: TabKey) => void;
};

const defaultTabsContext: TabsContextValue = {
  activeTab: 'Dashboard',
  setActiveTab: () => {}
};

const TabsContext = createContext<TabsContextValue>(defaultTabsContext);

export function TabsProvider(props: { children: ReactNode }) {
  const [activeTab, setActiveTab] = useState<TabKey>('Dashboard');

  return (
    <TabsContext.Provider value={{ activeTab, setActiveTab }}>
      {props.children}
    </TabsContext.Provider>
  );
}

export function useTabs() {
  return useContext(TabsContext);
} 
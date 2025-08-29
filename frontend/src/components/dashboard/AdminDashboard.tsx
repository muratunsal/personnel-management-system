import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import DashCard from './DashCard';
import ActionButton from './ActionButton';
import type { Task, Meeting } from '../../types/models';
import { ReactComponent as UsersIcon } from '../../icons/users.svg';
import { ReactComponent as OrganizationIcon } from '../../icons/organization.svg';
import { ReactComponent as DashboardIcon } from '../../icons/dashboard.svg';
import { ReactComponent as CalendarIcon } from '../../icons/calendar.svg';
import DashMiddleSection from './DashMiddleSection';
import DashTaskModal from './DashTaskModal';
import DashMeetingModal from './DashMeetingModal';

interface AdminDashboardProps {
  onShowAddEmployee: () => void;
  onShowAddDepartment: () => void;
  onShowAddTitle: () => void;
  onShowCreateTask: () => void;
  onShowCreateMeeting: () => void;
  onViewAllTasks: (tasks: Task[], title: string) => void;
  onViewAllMeetings: (meetings: Meeting[], title: string) => void;
}

export default function AdminDashboard({
  onShowAddEmployee,
  onShowAddDepartment,
  onShowAddTitle,
  onShowCreateTask,
  onShowCreateMeeting,
  onViewAllTasks,
  onViewAllMeetings
}: AdminDashboardProps) {
  const { token } = useAuth();
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    totalEmployees: 0,
    activeDepartments: 0,
    totalTasks: 0,
    closedTasks: 0,
    systemEfficiency: 0,
    activeMeetings: 0,
    averageTaskCompletion: 0
  });
  
  const [dashboardTasks, setDashboardTasks] = useState<Task[]>([]);
  const [dashboardMeetings, setDashboardMeetings] = useState<Meeting[]>([]);

  const [selectedTask, setSelectedTask] = useState<Task | null>(null);
  const [selectedMeeting, setSelectedMeeting] = useState<Meeting | null>(null);

  useEffect(() => {
    if (!token) return;
    
    const loadDashboardData = async () => {
      setLoading(true);
      try {
        const [peopleRes, deptRes, tasksRes, meetingsRes] = await Promise.all([
          fetch('http://localhost:8081/api/people', {
            headers: { Authorization: `Bearer ${token}` }
          }),
          fetch('http://localhost:8081/api/departments', {
            headers: { Authorization: `Bearer ${token}` }
          }),
          fetch('http://localhost:8081/api/tasks', {
            headers: { Authorization: `Bearer ${token}` }
          }),
          fetch('http://localhost:8081/api/meetings', {
            headers: { Authorization: `Bearer ${token}` }
          })
        ]);

        const peopleData = await peopleRes.json();
        const deptData = await deptRes.json();
        const tasksData = await tasksRes.json();
        const meetingsData = await meetingsRes.json();

        const totalEmployees = Array.isArray(peopleData) ? peopleData.length : (peopleData.totalElements || 0);
        const activeDepartments = Array.isArray(deptData) ? deptData.length : 0;
        const totalTasks = Array.isArray(tasksData) ? tasksData.length : 0;
        const closedTasks = Array.isArray(tasksData) ? tasksData.filter((task: Task) => task.status === 'CLOSED').length : 0;

        const tasks: Task[] = Array.isArray(tasksData) ? tasksData : [];
        const meetings: Meeting[] = Array.isArray(meetingsData) ? meetingsData : [];
        
        const completedTasks = tasks.filter((task: Task) => task.status === 'COMPLETED' || task.status === 'CLOSED').length;
        const averageTaskCompletion = totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : 0;
        
        const today = new Date();
        const activeMeetings = meetings.filter((meeting: Meeting) => {
          const meetingDate = new Date(meeting.day);
          return meetingDate >= today;
        }).length;

        const systemEfficiency = Math.round((activeDepartments / Math.max(totalEmployees, 1)) * 100);

        setStats({
          totalEmployees,
          activeDepartments,
          totalTasks,
          closedTasks,
          systemEfficiency,
          activeMeetings,
          averageTaskCompletion
        });

        if (Array.isArray(tasksData)) {
          const sortedTasks = tasksData.sort((a: Task, b: Task) => 
            new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
          );
          setDashboardTasks(sortedTasks);
        }

        if (Array.isArray(meetingsData)) {
          const sortedMeetings = meetingsData.sort((a: Meeting, b: Meeting) => 
            new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
          );
          setDashboardMeetings(sortedMeetings);
        }
      } catch (error) {
        console.error('Error loading dashboard data:', error);
      } finally {
        setLoading(false);
      }
    };

    loadDashboardData();
  }, [token]);

  const updateTaskStatus = async (taskId: number, status: string) => {
    try {
      const response = await fetch(`http://localhost:8081/api/tasks/${taskId}/status`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ status })
      });

      if (response.ok) {
        const updatedTask = await response.json();
        setDashboardTasks(prev => prev.map(task => 
          task.id === taskId ? updatedTask : updatedTask
        ));
        if (selectedTask?.id === taskId) {
          setSelectedTask(updatedTask);
        }
      }
    } catch (error) {
      console.error('Error updating task status:', error);
    }
  };

  const closeTask = async (taskId: number) => {
    try {
      const response = await fetch(`http://localhost:8081/api/tasks/${taskId}/close`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (response.ok) {
        const updatedTask = await response.json();
        setDashboardTasks(prev => prev.map(task => 
          task.id === taskId ? updatedTask : updatedTask
        ));
        if (selectedTask?.id === taskId) {
          setSelectedTask(updatedTask);
        }
      }
    } catch (error) {
      console.error('Error closing task:', error);
    }
  };

  if (loading) {
    return (
      <div className="org-chart-loading">
        <div className="org-chart-spinner"></div>
        <p>Loading dashboard...</p>
      </div>
    );
  }

  return (
    <div className="dash-container">
      <div className="dash-section">
        <div className="dash-stats-grid">
          <DashCard
            title="Total Employees"
            value={stats.totalEmployees}
            icon={<UsersIcon width={24} height={24} />}
            subtitle="Active personnel"
            color="#3B82F6"
          />
          <DashCard
            title="Active Departments"
            value={stats.activeDepartments}
            icon={<OrganizationIcon width={24} height={24} />}
            subtitle="Organizational units"
            color="#10B981"
          />
          <DashCard
            title="Total Tasks"
            value={stats.totalTasks}
            icon={<DashboardIcon width={24} height={24} />}
            subtitle="All assigned tasks"
            color="#F59E0B"
          />
          <DashCard
            title="Task Completion Rate"
            value={`${stats.averageTaskCompletion}%`}
            icon={<DashboardIcon width={24} height={24} />}
            subtitle="System performance"
            color="#8B5CF6"
          />
        </div>
      </div>

      <DashMiddleSection 
        tasks={dashboardTasks}
        meetings={dashboardMeetings}
        onTaskClick={setSelectedTask}
        onMeetingClick={setSelectedMeeting}
        taskTitle="Tasks"
        meetingTitle="Meetings"
        onViewAllTasks={onViewAllTasks}
        onViewAllMeetings={onViewAllMeetings}
      />

      <div className="dash-section dash-section--footer">
        <h2 className="dash-section-title">Quick Actions</h2>
        <div className="dash-actions-grid">
          <ActionButton label="Add Employee" onClick={onShowAddEmployee} icon={<UsersIcon width={24} height={24} />} />
          <ActionButton label="Add Department" onClick={onShowAddDepartment} icon={<OrganizationIcon width={24} height={24} />} />
          <ActionButton label="Add Title" onClick={onShowAddTitle} icon={<DashboardIcon width={24} height={24} />} />
          <ActionButton label="Create Task" onClick={onShowCreateTask} icon={<DashboardIcon width={24} height={24} />} />
          <ActionButton label="Create Meeting" onClick={onShowCreateMeeting} icon={<CalendarIcon width={24} height={24} />} />
        </div>
      </div>

      <DashTaskModal 
        task={selectedTask}
        onClose={() => setSelectedTask(null)}
        onUpdateStatus={updateTaskStatus}
        onCloseTask={closeTask}
        showCloseButton={true}
      />
      
      <DashMeetingModal 
        meeting={selectedMeeting}
        onClose={() => setSelectedMeeting(null)}
      />
    </div>
  );
}

import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import DashCard from './DashCard';
import ActionButton from './ActionButton';
import type { Task, Meeting, Person } from '../../types/models';
import { ReactComponent as UsersIcon } from '../../icons/users.svg';
import { ReactComponent as OrganizationIcon } from '../../icons/organization.svg';
import { ReactComponent as DashboardIcon } from '../../icons/dashboard.svg';
import { ReactComponent as CalendarIcon } from '../../icons/calendar.svg';
import DashMiddleSection from './DashMiddleSection';
import DashTaskModal from './DashTaskModal';
import DashMeetingModal from './DashMeetingModal';

interface HRDashboardProps {
  onShowAddEmployee: () => void;
  onShowCreateTask: () => void;
  onShowCreateMeeting: () => void;
  onViewAllTasks: (tasks: Task[], title: string) => void;
  onViewAllMeetings: (meetings: Meeting[], title: string) => void;
}

export default function HRDashboard({ 
  onShowAddEmployee, 
  onShowCreateTask,
  onShowCreateMeeting,
  onViewAllTasks, 
  onViewAllMeetings 
}: HRDashboardProps) {
  const { user, token } = useAuth();
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    totalEmployees: 0,
    activeDepartments: 0,
    openTasks: 0,
    newHiresThisMonth: 0,
    averageSalary: 0,
    employeeRetention: 0
  });
  const [myTasks, setMyTasks] = useState<Task[]>([]);
  const [myMeetings, setMyMeetings] = useState<Meeting[]>([]);
  
  const [selectedTask, setSelectedTask] = useState<Task | null>(null);
  const [selectedMeeting, setSelectedMeeting] = useState<Meeting | null>(null);

  useEffect(() => {
    if (!token || !user?.email) return;
    
    const loadData = async () => {
      setLoading(true);
      try {
        const [userRes, myTasksRes, myMeetingsRes, peopleRes, deptRes] = await Promise.all([
          fetch(`http://localhost:8081/api/people?size=1&email=${encodeURIComponent(user.email)}`, {
            headers: { Authorization: `Bearer ${token}` }
          }),
          fetch('http://localhost:8081/api/tasks/user', {
            headers: { Authorization: `Bearer ${token}` }
          }),
          fetch('http://localhost:8081/api/meetings/user', {
            headers: { Authorization: `Bearer ${token}` }
          }),
          fetch('http://localhost:8081/api/people', {
            headers: { Authorization: `Bearer ${token}` }
          }),
          fetch('http://localhost:8081/api/departments', {
            headers: { Authorization: `Bearer ${token}` }
          })
        ]);

        const userData = await userRes.json();
        const myTasksData = await myTasksRes.json();
        const myMeetingsData = await myMeetingsRes.json();
        const peopleData = await peopleRes.json();
        const deptData = await deptRes.json();

        const userContent = Array.isArray(userData) ? userData : (userData?.content ?? []);
        if (userContent.length > 0) {
        }

        const totalEmployees = Array.isArray(peopleData) ? peopleData.length : (peopleData.totalElements || 0);
        const activeDepartments = Array.isArray(deptData) ? deptData.length : 0;

        const tasks: Task[] = Array.isArray(myTasksData) ? myTasksData : [];
        const meetings: Meeting[] = Array.isArray(myMeetingsData) ? myMeetingsData : [];
        
        const sortedTasks = tasks.sort((a: Task, b: Task) => 
          new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        );
        const sortedMeetings = meetings.sort((a: Meeting, b: Meeting) => 
          new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        );
        
        setMyTasks(sortedTasks);
        setMyMeetings(sortedMeetings);

        const openTasks = tasks.filter(t => 
          t.status === 'ASSIGNED' || t.status === 'IN_PROGRESS'
        ).length;

        const people: Person[] = Array.isArray(peopleData) ? peopleData : (peopleData.content || []);
        
        const currentMonth = new Date().getMonth();
        const currentYear = new Date().getFullYear();
        const newHiresThisMonth = people.filter((person: Person) => {
          if (!person.contractStartDate) return false;
          const startDate = new Date(person.contractStartDate);
          return startDate.getMonth() === currentMonth && startDate.getFullYear() === currentYear;
        }).length;

        const salaries = people
          .filter((person: Person) => person.salary && person.salary > 0)
          .map((person: Person) => person.salary || 0);
        
        const averageSalary = salaries.length > 0 
          ? Math.round(salaries.reduce((sum, salary) => sum + salary, 0) / salaries.length)
          : 0;

        const oneYearAgo = new Date();
        oneYearAgo.setFullYear(oneYearAgo.getFullYear() - 1);
        
        const longTermEmployees = people.filter((person: Person) => {
          if (!person.contractStartDate) return false;
          const startDate = new Date(person.contractStartDate);
          return startDate < oneYearAgo;
        }).length;
        
        const employeeRetention = totalEmployees > 0 ? Math.round((longTermEmployees / totalEmployees) * 100) : 0;

        setStats({
          totalEmployees,
          activeDepartments,
          openTasks,
          newHiresThisMonth,
          averageSalary,
          employeeRetention
        });

      } catch (error) {
        console.error('Error loading HR dashboard data:', error);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [token, user?.email]);

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
        setMyTasks(prev => prev.map(task => 
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

  const getTaskColor = (task: Task) => {
    if (task.createdBy?.email === user?.email) {
      return '#10B981';
    } else {
      return '#3B82F6';
    }
  };

  const getMeetingColor = (meeting: Meeting) => {
    if (meeting.organizer?.email === user?.email) {
      return '#F59E0B';
    } else {
      return '#8B5CF6';
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
            title="My Open Tasks"
            value={stats.openTasks}
            icon={<DashboardIcon width={24} height={24} />}
            subtitle="HR responsibilities"
            color="#F59E0B"
          />
          <DashCard
            title="Average Salary"
            value={`$${stats.averageSalary.toLocaleString()}`}
            icon={<OrganizationIcon width={24} height={24} />}
            subtitle="Compensation insights"
            color="#EF4444"
          />
        </div>
      </div>

      <DashMiddleSection 
        tasks={myTasks}
        meetings={myMeetings}
        onTaskClick={setSelectedTask}
        onMeetingClick={setSelectedMeeting}
        taskTitle="My Tasks & Created Tasks"
        meetingTitle="My Meetings & Created Meetings"
        onViewAllTasks={onViewAllTasks}
        onViewAllMeetings={onViewAllMeetings}
        getTaskColor={getTaskColor}
        getMeetingColor={getMeetingColor}
      />

      <div className="dash-section dash-section--footer">
        <h2 className="dash-section-title">HR Operations</h2>
        <div className="dash-actions-grid">
          <ActionButton label="Add Employee" onClick={onShowAddEmployee} icon={<UsersIcon width={24} height={24} />} />
          <ActionButton label="Create Task" onClick={onShowCreateTask} icon={<DashboardIcon width={24} height={24} />} />
          <ActionButton label="Create Meeting" onClick={onShowCreateMeeting} icon={<CalendarIcon width={24} height={24} />} />
        </div>
      </div>

      <DashTaskModal 
        task={selectedTask}
        onClose={() => setSelectedTask(null)}
        onUpdateStatus={updateTaskStatus}
        showCloseButton={false}
      />
      
      <DashMeetingModal 
        meeting={selectedMeeting}
        onClose={() => setSelectedMeeting(null)}
      />
    </div>
  );
}
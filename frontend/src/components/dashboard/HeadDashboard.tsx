import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import DashCard from './DashCard';
import ActionButton from './ActionButton';
import type { Task, Meeting, Department } from '../../types/models';
import { ReactComponent as UsersIcon } from '../../icons/users.svg';
import { ReactComponent as OrganizationIcon } from '../../icons/organization.svg';
import { ReactComponent as DashboardIcon } from '../../icons/dashboard.svg';
import { ReactComponent as CalendarIcon } from '../../icons/calendar.svg';
import DashMiddleSection from './DashMiddleSection';
import DashTaskModal from './DashTaskModal';
import DashMeetingModal from './DashMeetingModal';

interface HeadDashboardProps {
  onShowCreateTask: () => void;
  onShowCreateMeeting: () => void;
  onViewAllTasks: (tasks: Task[], title: string) => void;
  onViewAllMeetings: (meetings: Meeting[], title: string) => void;
}

export default function HeadDashboard({ 
  onShowCreateTask, 
  onShowCreateMeeting, 
  onViewAllTasks, 
  onViewAllMeetings 
}: HeadDashboardProps) {
  const { user, token, getUserDetails } = useAuth();
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    departmentEmployees: 0,
    departmentOpenTasks: 0,
    completedTasksThisMonth: 0,
    averageTaskCompletion: 0,
    teamWorkload: 0,
    upcomingDepartmentMeetings: 0
  });
  const [myTasks, setMyTasks] = useState<Task[]>([]);
  const [myMeetings, setMyMeetings] = useState<Meeting[]>([]);
  const [userDepartment, setUserDepartment] = useState<Department | null>(null);
  const [selectedTask, setSelectedTask] = useState<Task | null>(null);
  const [selectedMeeting, setSelectedMeeting] = useState<Meeting | null>(null);

  useEffect(() => {
    if (!token || !user?.email) return;
    
    if (!user.departmentId) {
      getUserDetails();
    }
    
    const loadData = async () => {
      setLoading(true);
      try {
        const [userRes, myTasksRes, myMeetingsRes] = await Promise.all([
          fetch(`http://localhost:8081/api/people?size=1&email=${encodeURIComponent(user.email)}`, {
            headers: { Authorization: `Bearer ${token}` }
          }),
          fetch('http://localhost:8081/api/tasks/user', {
            headers: { Authorization: `Bearer ${token}` }
          }),
          fetch('http://localhost:8081/api/meetings/user', {
            headers: { Authorization: `Bearer ${token}` }
          })
        ]);

        const userData = await userRes.json();
        const myTasksData = await myTasksRes.json();
        const myMeetingsData = await myMeetingsRes.json();

        const userContent = Array.isArray(userData) ? userData : (userData?.content ?? []);
        if (userContent.length > 0) {
          const userPerson = userContent[0];
          const department = userPerson.department;
          setUserDepartment(department);

          if (!department) return;

          const [deptPeopleRes, allTasksRes, deptMeetingsRes] = await Promise.all([
            fetch(`http://localhost:8081/api/people?departmentId=${department.id}&size=1000`, {
              headers: { Authorization: `Bearer ${token}` }
            }),
            fetch('http://localhost:8081/api/tasks', {
              headers: { Authorization: `Bearer ${token}` }
            }),
            fetch(`http://localhost:8081/api/meetings/department/${department.id}`, {
              headers: { Authorization: `Bearer ${token}` }
            })
          ]);

          const deptPeopleData = await deptPeopleRes.json();
          const allTasksData = await allTasksRes.json();
          const deptMeetingsData = await deptMeetingsRes.json();

          const deptPeople = Array.isArray(deptPeopleData) ? deptPeopleData : (deptPeopleData.content || []);
          const departmentEmployees = deptPeople.length;

          const deptTasks: Task[] = (Array.isArray(allTasksData) ? allTasksData : [])
            .filter((task: Task) => task.department?.id === department.id);
          
          const openDeptTasks = deptTasks.filter(task => task.status !== 'CLOSED');
          
          const currentMonth = new Date().getMonth();
          const currentYear = new Date().getFullYear();
          const completedTasksThisMonth = deptTasks.filter((task: Task) => {
            if (task.status !== 'COMPLETED') return false;
            const dt = new Date(task.createdAt);
            return dt.getMonth() === currentMonth && dt.getFullYear() === currentYear;
          }).length;

          const totalDeptTasks = deptTasks.length;
          const completedDeptTasks = deptTasks.filter(task => task.status === 'COMPLETED' || task.status === 'CLOSED').length;
          const averageTaskCompletion = totalDeptTasks > 0 ? Math.round((completedDeptTasks / totalDeptTasks) * 100) : 0;

          const today = new Date();
          const upcomingDepartmentMeetings = (Array.isArray(deptMeetingsData) ? deptMeetingsData : [])
            .filter((meeting: Meeting) => {
              const meetingDate = new Date(meeting.day);
              return meetingDate >= today && meeting.department?.id === department.id;
            }).length;

          const teamWorkload = Math.round((openDeptTasks.length / Math.max(departmentEmployees, 1)) * 10) / 10;

          setStats({
            departmentEmployees,
            departmentOpenTasks: openDeptTasks.length,
            completedTasksThisMonth,
            averageTaskCompletion,
            teamWorkload,
            upcomingDepartmentMeetings
          });
        }

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
      } catch (error) {
        console.error('Error loading head dashboard data:', error);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [token, user?.email, user?.departmentId, getUserDetails]);

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
        setMyTasks(prev => prev.map(task => 
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
            title="Department Staff"
            value={stats.departmentEmployees}
            icon={<UsersIcon width={24} height={24} />}
            subtitle={userDepartment?.name || 'Your department'}
            color="#3B82F6"
          />
          <DashCard
            title="Open Department Tasks"
            value={stats.departmentOpenTasks}
            icon={<DashboardIcon width={24} height={24} />}
            subtitle="Pending completion"
            color="#F59E0B"
          />
          <DashCard
            title="Task Completion Rate"
            value={`${stats.averageTaskCompletion}%`}
            icon={<OrganizationIcon width={24} height={24} />}
            subtitle="Department efficiency"
            color="#10B981"
          />
          <DashCard
            title="Upcoming Meetings"
            value={stats.upcomingDepartmentMeetings}
            icon={<CalendarIcon width={24} height={24} />}
            subtitle="Department schedule"
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
        <h2 className="dash-section-title">Department Management</h2>
        <div className="dash-actions-grid">
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
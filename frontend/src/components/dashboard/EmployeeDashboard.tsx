import { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import DashCard from './DashCard';
import type { Task, Department, Meeting, Person } from '../../types/models';
import { ReactComponent as DashboardIcon } from '../../icons/dashboard.svg';
import { ReactComponent as UsersIcon } from '../../icons/users.svg';
import { ReactComponent as CalendarIcon } from '../../icons/calendar.svg';
import DashMiddleSection from './DashMiddleSection';
import DashTaskModal from './DashTaskModal';
import DashMeetingModal from './DashMeetingModal';

interface EmployeeDashboardProps {
  onViewAllTasks: (tasks: Task[], title: string) => void;
  onViewAllMeetings: (meetings: Meeting[], title: string) => void;
}

export default function EmployeeDashboard({ onViewAllTasks, onViewAllMeetings }: EmployeeDashboardProps) {
  const { user, token } = useAuth();
  const [stats, setStats] = useState({
    openTasks: 0,
    completedTasks: 0,
    departmentColleagues: 0,
    upcomingMeetings: 0,
    taskCompletionRate: 0,
    workAnniversary: 0
  });
  const [myTasks, setMyTasks] = useState<Task[]>([]);
  const [myMeetings, setMyMeetings] = useState<Meeting[]>([]);
  const [userDepartment, setUserDepartment] = useState<Department | null>(null);
  const [currentPerson, setCurrentPerson] = useState<Person | null>(null);
  const [selectedTask, setSelectedTask] = useState<Task | null>(null);
  const [selectedMeeting, setSelectedMeeting] = useState<Meeting | null>(null);

  useEffect(() => {
    if (!token || !user?.email) return;
    
    const loadData = async () => {
      try {
        const [userRes, myTasksRes, myMeetingsRes] = await Promise.all([
          fetch(`http://localhost:8081/api/people?size=1&email=${encodeURIComponent(user.email)}`, {
            headers: { Authorization: `Bearer ${token}` }
          }),
          fetch('http://localhost:8081/api/tasks/me', {
            headers: { Authorization: `Bearer ${token}` }
          }),
          fetch('http://localhost:8081/api/meetings/me', {
            headers: { Authorization: `Bearer ${token}` }
          })
        ]);

        const userData = await userRes.json();
        const myTasksData = await myTasksRes.json();
        const myMeetingsData = await myMeetingsRes.json();

        const userContent = Array.isArray(userData) ? userData : (userData?.content ?? []);
        if (userContent.length > 0) {
          const userPerson = userContent[0];
          setCurrentPerson(userPerson);
          const department = userPerson.department;
          setUserDepartment(department);

          if (department) {
            const deptPeopleRes = await fetch(`http://localhost:8081/api/people?departmentId=${department.id}&size=1000`, {
              headers: { Authorization: `Bearer ${token}` }
            });
            const deptPeopleData = await deptPeopleRes.json();
            const deptPeople = Array.isArray(deptPeopleData) ? deptPeopleData : (deptPeopleData.content || []);
            
            setStats(prev => ({
              ...prev,
              departmentColleagues: Math.max(0, deptPeople.length - 1)
            }));
          }
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

        const openTasks = tasks.filter(t => 
          t.status === 'ASSIGNED' || t.status === 'IN_PROGRESS'
        ).length;
        
        const completedTasks = tasks.filter(t => 
          t.status === 'COMPLETED' || t.status === 'CLOSED'
        ).length;

        const totalTasks = tasks.length;
        const taskCompletionRate = totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : 0;

        const today = new Date();
        const upcomingMeetings = meetings.filter(m => {
          const meetingDate = new Date(m.day);
          return meetingDate >= today;
        }).length;

        let workAnniversary = 0;
        if (currentPerson?.contractStartDate) {
          const contractStart = new Date(currentPerson.contractStartDate);
          const yearsWorked = today.getFullYear() - contractStart.getFullYear();
          const monthDiff = today.getMonth() - contractStart.getMonth();
          if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < contractStart.getDate())) {
            workAnniversary = yearsWorked - 1;
          } else {
            workAnniversary = yearsWorked;
          }
        }

        setStats(prev => ({
          ...prev,
          openTasks,
          completedTasks,
          taskCompletionRate,
          upcomingMeetings,
          workAnniversary
        }));

      } catch (error) {
        console.error('Error loading employee dashboard data:', error);
      }
    };

    loadData();
  }, [token, user?.email, currentPerson?.contractStartDate]);

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

  return (
    <div className="dash-container">
      <div className="dash-section">
        <div className="dash-stats-grid">
          <DashCard
            title="Open Tasks"
            value={stats.openTasks}
            icon={<DashboardIcon width={24} height={24} />}
            subtitle="Assigned to you"
            color="#F59E0B"
          />
          <DashCard
            title="Task Completion"
            value={`${stats.taskCompletionRate}%`}
            icon={<DashboardIcon width={24} height={24} />}
            subtitle="Your success rate"
            color="#10B981"
          />
          <DashCard
            title="Upcoming Meetings"
            value={stats.upcomingMeetings}
            icon={<CalendarIcon width={24} height={24} />}
            subtitle="This week/month"
            color="#8B5CF6"
          />
          <DashCard
            title="Department Colleagues"
            value={stats.departmentColleagues}
            icon={<UsersIcon width={24} height={24} />}
            subtitle={userDepartment?.name || 'Your department'}
            color="#3B82F6"
          />
        </div>
      </div>

      <DashMiddleSection 
        tasks={myTasks}
        meetings={myMeetings}
        onTaskClick={setSelectedTask}
        onMeetingClick={setSelectedMeeting}
        taskTitle="My Tasks"
        meetingTitle="My Meetings"
        onViewAllTasks={onViewAllTasks}
        onViewAllMeetings={onViewAllMeetings}
      />

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
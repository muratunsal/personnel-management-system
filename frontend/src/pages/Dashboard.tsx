import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useData } from '../context/DataContext';
import AdminDashboard from '../components/dashboard/AdminDashboard';
import HRDashboard from '../components/dashboard/HRDashboard';
import HeadDashboard from '../components/dashboard/HeadDashboard';
import EmployeeDashboard from '../components/dashboard/EmployeeDashboard';
import DashTaskModal from '../components/dashboard/DashTaskModal';
import DashMeetingModal from '../components/dashboard/DashMeetingModal';
import AddEmployeeModal from '../components/modals/AddEmployeeModal';
import AddDepartmentModal from '../components/modals/AddDepartmentModal';
import AddTitleModal from '../components/modals/AddTitleModal';
import CreateTaskModal from '../components/modals/CreateTaskModal';
import CreateMeetingModal from '../components/modals/CreateMeetingModal';
import { ReactComponent as ChevronDownIcon } from '../icons/chevron-down.svg';
import { ReactComponent as ChevronUpIcon } from '../icons/chevron-up.svg';
import { ReactComponent as DashboardIcon } from '../icons/dashboard.svg';
import { ReactComponent as CalendarIcon } from '../icons/calendar.svg';
import { ReactComponent as SearchIcon } from '../icons/search.svg';
 
import type { Department, Title, Task, Person, Meeting } from '../types/models';
import '../styles/dashboard.css';
import '../styles/people.css';



export default function Dashboard() {
  const { user, token } = useAuth();
  const [showAddEmployeeForm, setShowAddEmployeeForm] = useState(false);
  const [showAddDepartmentForm, setShowAddDepartmentForm] = useState(false);
  const [showAddTitleForm, setShowAddTitleForm] = useState(false);
  const [showCreateTaskForm, setShowCreateTaskForm] = useState(false);
  const [showCreateMeetingForm, setShowCreateMeetingForm] = useState(false);
  const [dashVersion, setDashVersion] = useState(0);
  const bumpDashboard = () => setDashVersion(v => v + 1);
  const [viewAllMode, setViewAllMode] = useState<'none' | 'tasks' | 'meetings'>('none');
  const [viewAllData, setViewAllData] = useState<{ tasks: Task[], meetings: Meeting[] }>({ tasks: [], meetings: [] });
  const [viewAllTitle, setViewAllTitle] = useState('');
  const [viewAllSearch, setViewAllSearch] = useState('');
  const [viewAllFiltersOpen, setViewAllFiltersOpen] = useState(false);
  const [viewAllStatusFilter, setViewAllStatusFilter] = useState('');
  const [viewAllDateFilter, setViewAllDateFilter] = useState('');
 
  const [viewAllTaskPriorityFilter, setViewAllTaskPriorityFilter] = useState('');
  const [viewAllTaskDepartmentFilter, setViewAllTaskDepartmentFilter] = useState('');
  const [viewAllTaskCreatedByFilter, setViewAllTaskCreatedByFilter] = useState('');
  const [viewAllTaskAssigneeFilter, setViewAllTaskAssigneeFilter] = useState('');
  
  const [viewAllMeetingDepartmentFilter, setViewAllMeetingDepartmentFilter] = useState('');
  const [viewAllMeetingOrganizerFilter, setViewAllMeetingOrganizerFilter] = useState('');
  const [viewAllMeetingParticipantFilter, setViewAllMeetingParticipantFilter] = useState('');
  
  const { departments: depState, titles: titleState, people: peopleState, refreshAll } = useData();
  const [taskCreators, setTaskCreators] = useState<Person[]>([]);
  const [allPeople, setAllPeople] = useState<Person[]>([]);
  
  const [selectedTask, setSelectedTask] = useState<Task | null>(null);
  const [selectedMeeting, setSelectedMeeting] = useState<Meeting | null>(null);
 
  
  const [departments, setDepartments] = useState<Department[]>([]);
  const [titles, setTitles] = useState<Title[]>([]);


  const refreshMetadata = async () => {
    if (!token) return;
    try {
      const [depRes, titleRes] = await Promise.all([
        fetch('http://localhost:8081/api/departments', {
          headers: { Authorization: `Bearer ${token}` }
        }),
        fetch('http://localhost:8081/api/titles', {
          headers: { Authorization: `Bearer ${token}` }
        })
      ]);
      if (depRes.ok) {
        const deps = await depRes.json();
        const ordered = [...deps].sort((a: Department, b: Department) => a.name.localeCompare(b.name));
        setDepartments(ordered);
      }
      if (titleRes.ok) {
        const tits = await titleRes.json();
        const ordered = [...tits].sort((a: Title, b: Title) => a.name.localeCompare(b.name));
        setTitles(ordered);
      }
    } catch (err) {
      console.error('Error loading metadata:', err);
    }
  };

  useEffect(() => {
    setDepartments(depState.data);
    setTitles(titleState.data);
  }, [depState.data, titleState.data]);


  useEffect(() => {
    const people = peopleState.data;
    setAllPeople(people);
    const creators = people.filter(p => 
      p.title?.name?.includes('HR') || 
      p.title?.name?.includes('Head') ||
      p.title?.name?.includes('Manager') ||
      p.title?.name?.includes('Director')
    );
    setTaskCreators(creators);
  }, [peopleState.data]);



  const getOrderedTitlesForDepartment = (departmentId: number) => {
    const dep = departments.find(d => d.id === departmentId);
    const headName = dep ? `Head of ${dep.name}` : '';
    const hasHead = !!dep?.headOfDepartment;
    const list = titles.filter(t => t.department?.id === departmentId);
    const filtered = hasHead ? list.filter(t => t.name !== headName) : list;
    return filtered.sort((a, b) => a.name.localeCompare(b.name));
  };

  const getActiveTaskFilters = () => {
    const active: Array<{ key: string; label: string }> = [];
    if (viewAllSearch) active.push({ key: 'search', label: `Search: "${viewAllSearch}"` });
    if (viewAllTaskPriorityFilter) active.push({ key: 'priority', label: `Priority: ${viewAllTaskPriorityFilter}` });
    if (viewAllStatusFilter) active.push({ key: 'status', label: `Status: ${viewAllStatusFilter.replace('_', ' ')}` });
    if (viewAllTaskDepartmentFilter) {
      const dept = departments.find(d => String(d.id) === String(viewAllTaskDepartmentFilter));
      active.push({ key: 'department', label: `Department: ${dept?.name ?? viewAllTaskDepartmentFilter}` });
    }
    if (viewAllTaskCreatedByFilter) {
      const creator = taskCreators.find(p => String(p.id) === String(viewAllTaskCreatedByFilter));
      active.push({ key: 'createdBy', label: `Created By: ${creator ? `${creator.firstName} ${creator.lastName}` : viewAllTaskCreatedByFilter}` });
    }
    if (viewAllTaskAssigneeFilter) {
      const assignee = allPeople.find(p => String(p.id) === String(viewAllTaskAssigneeFilter));
      active.push({ key: 'assignee', label: `Assignee: ${assignee ? `${assignee.firstName} ${assignee.lastName}` : viewAllTaskAssigneeFilter}` });
    }
    return active;
  };

  const getActiveMeetingFilters = () => {
    const active: Array<{ key: string; label: string }> = [];
    if (viewAllSearch) active.push({ key: 'search', label: `Search: "${viewAllSearch}"` });
    if (viewAllMeetingDepartmentFilter) {
      const dept = departments.find(d => String(d.id) === String(viewAllMeetingDepartmentFilter));
      active.push({ key: 'department', label: `Department: ${dept?.name ?? viewAllMeetingDepartmentFilter}` });
    }
    if (viewAllMeetingOrganizerFilter) {
      const organizer = taskCreators.find(p => String(p.id) === String(viewAllMeetingOrganizerFilter));
      active.push({ key: 'organizer', label: `Organizer: ${organizer ? `${organizer.firstName} ${organizer.lastName}` : viewAllMeetingOrganizerFilter}` });
    }
    if (viewAllMeetingParticipantFilter) {
      const participant = allPeople.find(p => String(p.id) === String(viewAllMeetingParticipantFilter));
      active.push({ key: 'participant', label: `Participant: ${participant ? `${participant.firstName} ${participant.lastName}` : viewAllMeetingParticipantFilter}` });
    }
    if (viewAllDateFilter) active.push({ key: 'date', label: `Date: ${viewAllDateFilter}` });
    return active;
  };

  const clearAllTaskFilters = () => {
    setViewAllSearch('');
    setViewAllTaskPriorityFilter('');
    setViewAllStatusFilter('');
    setViewAllTaskDepartmentFilter('');
    setViewAllTaskCreatedByFilter('');
    setViewAllTaskAssigneeFilter('');
  };

  const clearAllMeetingFilters = () => {
    setViewAllSearch('');
    setViewAllMeetingDepartmentFilter('');
    setViewAllMeetingOrganizerFilter('');
    setViewAllMeetingParticipantFilter('');
    setViewAllDateFilter('');
  };

  const normalizeHex = (input: string) => {
    if (!input) return { value: '#999999', valid: false };
    let s = input.trim();
    if (!s.startsWith('#')) s = '#' + s;
    if (/^#[0-9a-fA-F]{6}$/.test(s)) return { value: s.toUpperCase(), valid: true };
    if (/^#[0-9a-fA-F]{3}$/.test(s)) {
      const r = s[1];
      const g = s[2];
      const b = s[3];
      return { value: (`#${r}${r}${g}${g}${b}${b}`).toUpperCase(), valid: true };
    }
    return { value: '#999999', valid: false };
  };



  const handleAddEmployee = async (formData: any) => {
    if (!token) throw new Error('No token');
    
      const payload = {
        ...formData,
        departmentId: formData.departmentId ? parseInt(formData.departmentId) : null,
        titleId: formData.titleId ? parseInt(formData.titleId) : null
      };

      const response = await fetch('http://localhost:8081/api/people', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(payload)
      });

    if (!response.ok) {
      throw new Error('Failed to add employee');
    }
    await refreshAll(true);
    setShowAddEmployeeForm(false);
    bumpDashboard();
  };

  const handleAddDepartment = async (formData: any) => {
    if (!token) throw new Error('No token');
    
    const norm = normalizeHex(formData.color);
    if (!norm.valid) {
      console.warn('Invalid hex color. Using fallback.');
    }
    
      const res = await fetch('http://localhost:8081/api/departments', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
      body: JSON.stringify({ name: formData.name, color: norm.value })
    });
    
    if (!res.ok) {
        if (res.status === 409) {
        throw new Error('Department name already exists');
        } else {
        throw new Error('Failed to create department');
      }
    }
    await refreshAll(true);
    setShowAddDepartmentForm(false);
    bumpDashboard();
  };

  const handleAddTitle = async (formData: any) => {
    if (!token) throw new Error('No token');
    
    if (!formData.departmentId) {
      throw new Error('Select department');
    }
    
      const res = await fetch('http://localhost:8081/api/titles', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
      body: JSON.stringify({ name: formData.name, departmentId: parseInt(formData.departmentId) })
    });
    
    if (!res.ok) {
        if (res.status === 409) {
        throw new Error('Title name already exists');
        } else {
        throw new Error('Failed to create title');
      }
    }
    await refreshAll(true);
    setShowAddTitleForm(false);
    bumpDashboard();
  };

  const handleCreateTask = async (formData: any) => {
    if (!token) throw new Error('No token');
    
    const res = await fetch('http://localhost:8081/api/tasks/create', {
      method: 'POST',
      headers: { 
        'Content-Type': 'application/json', 
        Authorization: `Bearer ${token}` 
      },
      body: JSON.stringify({ 
        title: formData.title, 
        description: formData.description, 
        priority: formData.priority, 
        departmentId: formData.departmentId ? parseInt(formData.departmentId) : null, 
        assigneeId: formData.assigneeId ? parseInt(formData.assigneeId) : null 
      })
    });
    
    if (!res.ok) {
      throw new Error('Failed to create task');
    }
    await refreshAll(true);
    setShowCreateTaskForm(false);
    bumpDashboard();
  };

  const handleCreateMeeting = async (formData: any) => {
    if (!token) throw new Error('No token');
    
    const payload = {
      title: formData.title,
      description: formData.description,
      departmentId: formData.departmentId ? parseInt(formData.departmentId) : null,
      day: formData.day,
      startTime: formData.startTime,
      endTime: formData.endTime,
      participantIds: formData.participantIds
    };
    
    const res = await fetch('http://localhost:8081/api/meetings/create', {
      method: 'POST',
      headers: { 
        'Content-Type': 'application/json', 
        Authorization: `Bearer ${token}` 
      },
      body: JSON.stringify(payload)
    });
    
    if (!res.ok) {
      throw new Error('Failed to create meeting');
    }
    await refreshAll(true);
    setShowCreateMeetingForm(false);
    bumpDashboard();
  };

  const renderDashboard = () => {
    switch (user?.role) {
      case 'ADMIN':
        return (
          <AdminDashboard
            key={`dash-${dashVersion}-admin`}
            onShowAddEmployee={async () => { await refreshMetadata(); setShowAddEmployeeForm(true); }}
            onShowAddDepartment={() => setShowAddDepartmentForm(true)}
            onShowAddTitle={() => setShowAddTitleForm(true)}
            onShowCreateTask={() => setShowCreateTaskForm(true)}
            onShowCreateMeeting={() => setShowCreateMeetingForm(true)}
            onViewAllTasks={(tasks: Task[], title: string) => {
              setViewAllMode('tasks');
              setViewAllData({ tasks, meetings: [] });
              setViewAllTitle(title);
              setViewAllSearch('');
              setViewAllFiltersOpen(false);
              setViewAllStatusFilter('');
              setViewAllDateFilter('');
              setViewAllTaskPriorityFilter('');
              setViewAllTaskDepartmentFilter('');
              setViewAllTaskCreatedByFilter('');
              setViewAllTaskAssigneeFilter('');
            }}
            onViewAllMeetings={(meetings: Meeting[], title: string) => {
              setViewAllMode('meetings');
              setViewAllData({ tasks: [], meetings });
              setViewAllTitle(title);
              setViewAllSearch('');
              setViewAllFiltersOpen(false);
              setViewAllStatusFilter('');
              setViewAllDateFilter('');
              setViewAllMeetingDepartmentFilter('');
              setViewAllMeetingOrganizerFilter('');
              setViewAllMeetingParticipantFilter('');
            }}
          />
        );
      case 'HR':
        return (
          <HRDashboard
            key={`dash-${dashVersion}-hr`}
            onShowAddEmployee={async () => { await refreshMetadata(); setShowAddEmployeeForm(true); }}
            onShowCreateTask={() => setShowCreateTaskForm(true)}
            onShowCreateMeeting={() => setShowCreateMeetingForm(true)}
            onViewAllTasks={(tasks: Task[], title: string) => {
              setViewAllMode('tasks');
              setViewAllData({ tasks, meetings: [] });
              setViewAllTitle(title);
              setViewAllSearch('');
              setViewAllFiltersOpen(false);
              setViewAllStatusFilter('');
              setViewAllDateFilter('');
              setViewAllTaskPriorityFilter('');
              setViewAllTaskDepartmentFilter('');
              setViewAllTaskCreatedByFilter('');
              setViewAllTaskAssigneeFilter('');
            }}
            onViewAllMeetings={(meetings: Meeting[], title: string) => {
              setViewAllMode('meetings');
              setViewAllData({ tasks: [], meetings });
              setViewAllTitle(title);
              setViewAllSearch('');
              setViewAllFiltersOpen(false);
              setViewAllStatusFilter('');
              setViewAllDateFilter('');
              setViewAllMeetingDepartmentFilter('');
              setViewAllMeetingOrganizerFilter('');
              setViewAllMeetingParticipantFilter('');
            }}
          />
        );
      case 'HEAD':
        return (
          <HeadDashboard
            key={`dash-${dashVersion}-head`}
            onShowCreateTask={() => setShowCreateTaskForm(true)}
            onShowCreateMeeting={() => setShowCreateMeetingForm(true)}
            onViewAllTasks={(tasks: Task[], title: string) => {
              setViewAllMode('tasks');
              setViewAllData({ tasks, meetings: [] });
              setViewAllTitle(title);
              setViewAllSearch('');
              setViewAllFiltersOpen(false);
              setViewAllStatusFilter('');
              setViewAllDateFilter('');
              setViewAllTaskPriorityFilter('');
              setViewAllTaskDepartmentFilter('');
              setViewAllTaskCreatedByFilter('');
              setViewAllTaskAssigneeFilter('');
            }}
            onViewAllMeetings={(meetings: Meeting[], title: string) => {
              setViewAllMode('meetings');
              setViewAllData({ tasks: [], meetings });
              setViewAllTitle(title);
              setViewAllSearch('');
              setViewAllFiltersOpen(false);
              setViewAllStatusFilter('');
              setViewAllDateFilter('');
              setViewAllMeetingDepartmentFilter('');
              setViewAllMeetingOrganizerFilter('');
              setViewAllMeetingParticipantFilter('');
            }}
          />
        );
      case 'EMPLOYEE':
      default:
        return (
          <EmployeeDashboard
            key={`dash-${dashVersion}-employee`}
            onViewAllTasks={(tasks: Task[], title: string) => {
              setViewAllMode('tasks');
              setViewAllData({ tasks, meetings: [] });
              setViewAllTitle(title);
              setViewAllSearch('');
              setViewAllFiltersOpen(false);
              setViewAllStatusFilter('');
              setViewAllDateFilter('');
              setViewAllTaskPriorityFilter('');
              setViewAllTaskDepartmentFilter('');
              setViewAllTaskCreatedByFilter('');
              setViewAllTaskAssigneeFilter('');
            }}
            onViewAllMeetings={(meetings: Meeting[], title: string) => {
              setViewAllMode('meetings');
              setViewAllData({ tasks: [], meetings });
              setViewAllTitle(title);
              setViewAllSearch('');
              setViewAllFiltersOpen(false);
              setViewAllStatusFilter('');
              setViewAllDateFilter('');
              setViewAllMeetingDepartmentFilter('');
              setViewAllMeetingOrganizerFilter('');
              setViewAllMeetingParticipantFilter('');
            }}
          />
        );
    }
  };

  const renderViewAll = () => {
    if (viewAllMode === 'none') return null;

    if (viewAllMode === 'tasks') {
      return (
        <div className="dash-container">
          <div className="profile-navigation">
            <button className="profile-back-button base-button" onClick={() => {
              setViewAllMode('none');
              setViewAllSearch('');
              setViewAllFiltersOpen(false);
              setViewAllStatusFilter('');
              setViewAllDateFilter('');
              setViewAllTaskPriorityFilter('');
              setViewAllTaskDepartmentFilter('');
              setViewAllTaskCreatedByFilter('');
              setViewAllTaskAssigneeFilter('');
              setViewAllMeetingDepartmentFilter('');
              setViewAllMeetingOrganizerFilter('');
              setViewAllMeetingParticipantFilter('');
            }}>
              <ChevronDownIcon width={16} height={16} />
            </button>
            <div className="profile-nav-preview">
              <div className="profile-nav-photo base-photo">
                <DashboardIcon width={16} height={16} />
              </div>
              <span className="profile-nav-name">{viewAllTitle}</span>
            </div>
          </div>

          <div className="people-header">
            <div className="search-wrapper">
              <SearchIcon className="search-icon" width={18} height={18} />
              <input 
                className="people-search-input base-input" 
                placeholder="Search tasks..." 
                value={viewAllSearch}
                onChange={(e) => setViewAllSearch(e.target.value)}
              />
            </div>
            <div className="header-actions">
              <button 
                className="filter-toggle base-button"
                onClick={() => setViewAllFiltersOpen(!viewAllFiltersOpen)}
              >
                {viewAllFiltersOpen ? <ChevronUpIcon width={16} height={16} /> : <ChevronDownIcon width={16} height={16} />}
                Filters
                {getActiveTaskFilters().length > 0 && <span className="filter-count">{getActiveTaskFilters().length}</span>}
              </button>
              
              {getActiveTaskFilters().length > 0 && (
                <button
                  className="clear-all-button base-button"
                  onClick={clearAllTaskFilters}
                  title="Clear all filters"
                >
                  Clear All
                </button>
              )}
            </div>
          </div>

          {viewAllFiltersOpen && (
            <div className="filters-section">
              <div className="filter-controls">
                <div className="filter-group">
                  <label className="filter-label">Priority</label>
                  <div className="custom-select">
                    <select 
                      className="filter-dropdown base-input" 
                      value={viewAllTaskPriorityFilter} 
                      onChange={(e) => setViewAllTaskPriorityFilter(e.target.value)}
                    >
                      <option value="">All Priorities</option>
                      <option value="LOW">Low</option>
                      <option value="MEDIUM">Medium</option>
                      <option value="HIGH">High</option>
                      <option value="CRITICAL">Critical</option>
                    </select>
                  </div>
                </div>
                
                <div className="filter-group">
                  <label className="filter-label">Status</label>
                  <div className="custom-select">
                    <select 
                      className="filter-dropdown base-input" 
                      value={viewAllStatusFilter} 
                      onChange={(e) => setViewAllStatusFilter(e.target.value)}
                    >
                      <option value="">All Statuses</option>
                      <option value="ASSIGNED">Assigned</option>
                      <option value="IN_PROGRESS">In Progress</option>
                      <option value="COMPLETED">Completed</option>
                      <option value="CLOSED">Closed</option>
                    </select>
                  </div>
                </div>
                
                <div className="filter-group">
                  <label className="filter-label">Department</label>
                  <div className="custom-select">
                    <select 
                      className="filter-dropdown base-input" 
                      value={viewAllTaskDepartmentFilter} 
                      onChange={(e) => setViewAllTaskDepartmentFilter(e.target.value)}
                    >
                      <option value="">All Departments</option>
                      {departments.map((dept) => (
                        <option key={dept.id} value={dept.id}>{dept.name}</option>
                      ))}
                    </select>
                  </div>
                </div>
                
                <div className="filter-group">
                  <label className="filter-label">Created By</label>
                  <div className="custom-select">
                    <select 
                      className="filter-dropdown base-input" 
                      value={viewAllTaskCreatedByFilter} 
                      onChange={(e) => setViewAllTaskCreatedByFilter(e.target.value)}
                    >
                      <option value="">All Creators</option>
                      <option value="ADMIN">Admin</option>
                      {taskCreators.map((creator) => (
                        <option key={creator.id} value={creator.id}>{creator.firstName} {creator.lastName}</option>
                      ))}
                    </select>
                  </div>
                </div>
                
                <div className="filter-group">
                  <label className="filter-label">Assignee</label>
                  <div className="custom-select">
                    <select 
                      className="filter-dropdown base-input" 
                      value={viewAllTaskAssigneeFilter} 
                      onChange={(e) => setViewAllTaskAssigneeFilter(e.target.value)}
                    >
                      <option value="">All Assignees</option>
                      {allPeople.map((person) => (
                        <option key={person.id} value={person.id}>{person.firstName} {person.lastName}</option>
                      ))}
                    </select>
                  </div>
                </div>
              </div>
              
              {getActiveTaskFilters().length > 0 && (
                <div className="active-filters">
                  <span className="filters-label">Active filters:</span>
                  <div className="filter-chips">
                    {getActiveTaskFilters().map((filter) => (
                      <span className="filter-chip" key={filter.key}>
                        <span className="filter-chip-text">{filter.label}</span>
                        <button 
                          className="filter-chip-remove base-button" 
                          onClick={() => {
                            if (filter.key === 'search') setViewAllSearch('');
                            if (filter.key === 'priority') setViewAllTaskPriorityFilter('');
                            if (filter.key === 'status') setViewAllStatusFilter('');
                            if (filter.key === 'department') setViewAllTaskDepartmentFilter('');
                            if (filter.key === 'createdBy') setViewAllTaskCreatedByFilter('');
                            if (filter.key === 'assignee') setViewAllTaskAssigneeFilter('');
                          }} 
                          aria-label="Remove"
                        >
                          ×
                        </button>
                      </span>
                    ))}
              </div>
                </div>
              )}
            </div>
          )}

          <div className="table-wrapper">
            <table className="people-table">
              <thead>
                <tr>
                  <th className="sortable">Title</th>
                  <th className="sortable">Priority</th>
                  <th className="sortable">Status</th>
                  <th className="sortable">Department</th>
                  <th className="sortable">Created By</th>
                  <th className="sortable">Assignee</th>
                  <th className="sortable">Created</th>
                </tr>
              </thead>
              <tbody>
                {viewAllData.tasks
                  .filter(task => (viewAllSearch ? 
                    task.title.toLowerCase().includes(viewAllSearch.toLowerCase()) || 
                    (task.description && task.description.toLowerCase().includes(viewAllSearch.toLowerCase())) : true))
                  .filter(task => (viewAllTaskPriorityFilter ? task.priority === viewAllTaskPriorityFilter : true))
                  .filter(task => (viewAllStatusFilter ? task.status === viewAllStatusFilter : true))
                  .filter(task => (viewAllTaskDepartmentFilter ? task.department?.id === parseInt(viewAllTaskDepartmentFilter) : true))
                  .filter(task => (viewAllTaskCreatedByFilter ? 
                    viewAllTaskCreatedByFilter === 'ADMIN' ? !task.createdBy : 
                    task.createdBy?.id === parseInt(viewAllTaskCreatedByFilter) : true))
                  .filter(task => (viewAllTaskAssigneeFilter ? task.assignee.id === parseInt(viewAllTaskAssigneeFilter) : true))
                  .map(task => (
                    <tr key={task.id} className="table-row clickable-row" onClick={() => setSelectedTask(task)}>
                      <td className="clickable-cell">{task.title}</td>
                      <td className="clickable-cell">
                        <span className={`priority-badge priority-${task.priority.toLowerCase()}`}>
                          {task.priority}
                        </span>
                      </td>
                      <td className="clickable-cell">
                        <span className={`status-badge status-${task.status.toLowerCase().replace('_', '-')}`}>
                          {task.status.replace('_', ' ')}
                        </span>
                      </td>
                      <td className="clickable-cell">{task.department?.name ?? '-'}</td>
                      <td className="clickable-cell">
                        {task.createdBy ? `${task.createdBy.firstName} ${task.createdBy.lastName}` : 'Admin'}
                      </td>
                      <td className="clickable-cell">{task.assignee.firstName} {task.assignee.lastName}</td>
                      <td className="clickable-cell">{new Date(task.createdAt).toLocaleDateString() + ' ' + new Date(task.createdAt).toLocaleTimeString('tr-TR', { hour: '2-digit', minute: '2-digit' })}</td>
                    </tr>
                  ))}
              </tbody>
            </table>
          </div>
        </div>
      );
    }

    if (viewAllMode === 'meetings') {
      return (
        <div className="dash-container">
          <div className="profile-navigation">
            <button className="profile-back-button base-button" onClick={() => {
              setViewAllMode('none');
              setViewAllSearch('');
              setViewAllFiltersOpen(false);
              setViewAllStatusFilter('');
              setViewAllDateFilter('');
              setViewAllTaskPriorityFilter('');
              setViewAllTaskDepartmentFilter('');
              setViewAllTaskCreatedByFilter('');
              setViewAllTaskAssigneeFilter('');
              setViewAllMeetingDepartmentFilter('');
              setViewAllMeetingOrganizerFilter('');
              setViewAllMeetingParticipantFilter('');
            }}>
              <ChevronDownIcon width={16} height={16} />
            </button>
            <div className="profile-nav-preview">
              <div className="profile-nav-photo base-photo">
                <CalendarIcon width={16} height={16} />
              </div>
              <span className="profile-nav-name">{viewAllTitle}</span>
            </div>
          </div>

          <div className="people-header">
            <div className="search-wrapper">
              <SearchIcon className="search-icon" width={18} height={18} />
              <input 
                className="people-search-input base-input" 
                placeholder="Search meetings..." 
                value={viewAllSearch}
                onChange={(e) => setViewAllSearch(e.target.value)}
              />
            </div>
            <div className="header-actions">
              <button 
                className="filter-toggle base-button"
                onClick={() => setViewAllFiltersOpen(!viewAllFiltersOpen)}
              >
                {viewAllFiltersOpen ? <ChevronUpIcon width={16} height={16} /> : <ChevronDownIcon width={16} height={16} />}
                Filters
                {getActiveMeetingFilters().length > 0 && <span className="filter-count">{getActiveMeetingFilters().length}</span>}
              </button>
              
              {getActiveMeetingFilters().length > 0 && (
                <button
                  className="clear-all-button base-button"
                  onClick={clearAllMeetingFilters}
                  title="Clear all filters"
                >
                  Clear All
                </button>
              )}
            </div>
          </div>

          {viewAllFiltersOpen && (
            <div className="filters-section">
              <div className="filter-controls">
                <div className="filter-group">
                  <label className="filter-label">Department</label>
                  <div className="custom-select">
                    <select 
                      className="filter-dropdown base-input" 
                      value={viewAllMeetingDepartmentFilter} 
                      onChange={(e) => setViewAllMeetingDepartmentFilter(e.target.value)}
                    >
                      <option value="">All Departments</option>
                      {departments.map((dept) => (
                        <option key={dept.id} value={dept.id}>{dept.name}</option>
                      ))}
                    </select>
                  </div>
                </div>
                
                <div className="filter-group">
                  <label className="filter-label">Organizer</label>
                  <div className="custom-select">
                    <select 
                      className="filter-dropdown base-input" 
                      value={viewAllMeetingOrganizerFilter} 
                      onChange={(e) => setViewAllMeetingOrganizerFilter(e.target.value)}
                    >
                      <option value="">All Organizers</option>
                      <option value="ADMIN">Admin</option>
                      {taskCreators.map((creator) => (
                        <option key={creator.id} value={creator.id}>{creator.firstName} {creator.lastName}</option>
                      ))}
                    </select>
                  </div>
                </div>
                
                <div className="filter-group">
                  <label className="filter-label">Participant</label>
                  <div className="custom-select">
                    <select 
                      className="filter-dropdown base-input" 
                      value={viewAllMeetingParticipantFilter} 
                      onChange={(e) => setViewAllMeetingParticipantFilter(e.target.value)}
                    >
                      <option value="">All Participants</option>
                      {allPeople.map((person) => (
                        <option key={person.id} value={person.id}>{person.firstName} {person.lastName}</option>
                      ))}
                    </select>
                  </div>
                </div>
                
                <div className="filter-group">
                  <label className="filter-label">Date Range</label>
                  <div className="custom-select">
                    <select 
                      className="filter-dropdown base-input" 
                      value={viewAllDateFilter} 
                      onChange={(e) => setViewAllDateFilter(e.target.value)}
                    >
                      <option value="">All Dates</option>
                      <option value="today">Today</option>
                      <option value="week">This Week</option>
                      <option value="month">This Month</option>
                    </select>
                  </div>
                </div>
              </div>
              
              {getActiveMeetingFilters().length > 0 && (
                <div className="active-filters">
                  <span className="filters-label">Active filters:</span>
                  <div className="filter-chips">
                    {getActiveMeetingFilters().map((filter) => (
                      <span className="filter-chip" key={filter.key}>
                        <span className="filter-chip-text">{filter.label}</span>
                        <button 
                          className="filter-chip-remove base-button" 
                          onClick={() => {
                            if (filter.key === 'search') setViewAllSearch('');
                            if (filter.key === 'department') setViewAllMeetingDepartmentFilter('');
                            if (filter.key === 'organizer') setViewAllMeetingOrganizerFilter('');
                            if (filter.key === 'participant') setViewAllMeetingParticipantFilter('');
                            if (filter.key === 'date') setViewAllDateFilter('');
                          }} 
                          aria-label="Remove"
                        >
                          ×
                        </button>
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          <div className="table-wrapper">
            <table className="people-table">
              <thead>
                <tr>
                  <th className="sortable">Title</th>
                  <th className="sortable">Department</th>
                  <th className="sortable">Organizer</th>
                  <th className="sortable">Day</th>
                  <th className="sortable">Start Time</th>
                  <th className="sortable">End Time</th>
                </tr>
              </thead>
              <tbody>
                {viewAllData.meetings
                  .filter(meeting => (viewAllSearch ? 
                    meeting.title.toLowerCase().includes(viewAllSearch.toLowerCase()) || 
                    (meeting.description && meeting.description.toLowerCase().includes(viewAllSearch.toLowerCase())) : true))
                  .filter(meeting => (viewAllMeetingDepartmentFilter ? meeting.department?.id === parseInt(viewAllMeetingDepartmentFilter) : true))
                  .filter(meeting => (viewAllMeetingOrganizerFilter ? 
                    viewAllMeetingOrganizerFilter === 'ADMIN' ? !meeting.organizer : 
                    meeting.organizer?.id === parseInt(viewAllMeetingOrganizerFilter) : true))
                  .filter(meeting => (viewAllMeetingParticipantFilter ? 
                    meeting.participants.some(p => p.id === parseInt(viewAllMeetingParticipantFilter)) : true))
                  .filter(meeting => {
                    if (!viewAllDateFilter) return true;
                    const meetingDate = new Date(meeting.day);
                    const now = new Date();
                    
                    if (viewAllDateFilter === 'today') {
                      return meetingDate.toDateString() === now.toDateString();
                    } else if (viewAllDateFilter === 'week') {
                      const weekStart = new Date(now.setDate(now.getDate() - now.getDay()));
                      const weekEnd = new Date(weekStart.getTime() + 6 * 24 * 60 * 60 * 1000);
                      return meetingDate >= weekStart && meetingDate <= weekEnd;
                    } else if (viewAllDateFilter === 'month') {
                      return meetingDate.getMonth() === now.getMonth() && meetingDate.getFullYear() === now.getFullYear();
                    }
                    return true;
                  })
                  .map(meeting => (
                    <tr key={meeting.id} className="table-row clickable-row" onClick={() => setSelectedMeeting(meeting)}>
                      <td className="clickable-cell">{meeting.title}</td>
                      <td className="clickable-cell">{meeting.department?.name ?? '-'}</td>
                      <td className="clickable-cell">
                        {meeting.organizer ? `${meeting.organizer.firstName} ${meeting.organizer.lastName}` : 'Admin'}
                      </td>
                      <td className="clickable-cell">{new Date(meeting.day).toLocaleDateString()}</td>
                      <td className="clickable-cell">{meeting.startTime.substring(0, 5)}</td>
                      <td className="clickable-cell">{meeting.endTime.substring(0, 5)}</td>
                    </tr>
                  ))}
              </tbody>
            </table>
          </div>
        </div>
      );
    }

    return null;
  };


  return (
    <>
      {viewAllMode === 'none' ? renderDashboard() : renderViewAll()}

      

      <AddEmployeeModal
        isOpen={showAddEmployeeForm}
        onClose={() => setShowAddEmployeeForm(false)}
        onSubmit={handleAddEmployee}
        departments={departments}
        titles={titles}
        getOrderedTitlesForDepartment={getOrderedTitlesForDepartment}
      />

      <AddDepartmentModal
        isOpen={showAddDepartmentForm}
        onClose={() => setShowAddDepartmentForm(false)}
        onSubmit={handleAddDepartment}
      />

      <AddTitleModal
        isOpen={showAddTitleForm}
        onClose={() => setShowAddTitleForm(false)}
        onSubmit={handleAddTitle}
        departments={departments}
      />

      <CreateTaskModal
        isOpen={showCreateTaskForm}
        onClose={() => setShowCreateTaskForm(false)}
        onSubmit={handleCreateTask}
        departments={departments}
        token={token || ''}
      />



      <CreateMeetingModal
        isOpen={showCreateMeetingForm}
        onClose={() => setShowCreateMeetingForm(false)}
        onSubmit={handleCreateMeeting}
        departments={departments}
        token={token || ''}
      />

      <DashTaskModal
        task={selectedTask}
        onClose={() => setSelectedTask(null)}
        onUpdateStatus={(taskId, status) => {
          console.log(`Updating task ${taskId} to status: ${status}`);
        }}
        onCloseTask={(taskId) => {
          console.log(`Closing task ${taskId}`);
        }}
        showCloseButton={true}
      />

      <DashMeetingModal
        meeting={selectedMeeting}
        onClose={() => setSelectedMeeting(null)}
      />

    </>
  );
} 
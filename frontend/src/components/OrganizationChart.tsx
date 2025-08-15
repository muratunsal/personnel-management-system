import { useEffect, useState, useRef } from 'react';
import type { Person as PersonModel, Department as DepartmentModel } from '../types/models';
import { ReactComponent as UserAvatarIcon } from '../icons/user-avatar.svg';
import '../styles/organization.css';

interface OrgPerson {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  title: {
    name: string;
  };
  profilePictureUrl?: string | null;
}

interface Department {
  id: number;
  name: string;
  color: string;
  headOfDepartment: OrgPerson | null;
  employees: OrgPerson[];
}

interface OrganizationData {
  departments: Department[];
}

function Avatar({ url, color, alt }: { url?: string | null; color: string; alt: string }) {
  const [error, setError] = useState(false);
  const hasImage = !!url && !error;
  return (
    <div
      className="org-chart-person-avatar"
      style={{ backgroundColor: hasImage ? 'transparent' : color, color: hasImage ? undefined : '#ffffff', overflow: 'hidden' }}
    >
      {hasImage ? (
        <img
          src={url as string}
          alt={alt}
          onError={() => setError(true)}
          style={{ width: '100%', height: '100%', objectFit: 'cover', display: 'block' }}
        />
      ) : (
        <UserAvatarIcon width={20} height={20} />
      )}
    </div>
  );
}

export default function OrganizationChart({ onPersonClick, onDepartmentClick }: { onPersonClick?: (p: PersonModel) => void; onDepartmentClick?: (d: DepartmentModel) => void } = {}) {
  const [data, setData] = useState<OrganizationData | null>(null);
  const [loading, setLoading] = useState(true);
  const [scale, setScale] = useState(0.8);
  const [position, setPosition] = useState({ x: 0, y: 0 });
  const [isDragging, setIsDragging] = useState(false);
  const [dragStart, setDragStart] = useState({ x: 0, y: 0 });
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    fetchOrganizationData();
  }, []);

  const fetchOrganizationData = async () => {
    try {
      const response = await fetch('http://localhost:8081/api/departments/organization-structure');
      const orgData = await response.json();
      setData(orgData);
    } catch (error) {
      console.error('Error fetching organization data:', error);
    } finally {
      setLoading(false);
    }
  };

  const DEPT_SPACING = 420;
  const MAIN_LINE_Y = 100;
  const DEPT_Y = 170;

  const CARD_WIDTH = 260;
  const CARD_HEIGHT = 92;
  const DEPT_WIDTH = 320;
  const HEAD_Y = 255;
  const EMPLOYEE_START_Y = 360;
  const EMPLOYEE_SPACING_Y = 105;
  const CONNECTOR_COLOR = '#9ca3af';
  const handleMouseDown = (e: React.MouseEvent) => {
    setIsDragging(true);
    setDragStart({ x: e.clientX - position.x, y: e.clientY - position.y });
  };

  const handleMouseMove = (e: React.MouseEvent) => {
    if (!isDragging) {
      return;
    }
    setPosition({ x: e.clientX - dragStart.x, y: e.clientY - dragStart.y });
  };

  const handleMouseUp = () => {
    setIsDragging(false);
  };

  const handleWheel = (e: React.WheelEvent) => {
    e.preventDefault();
    const zoomIntensity = 0.1;
    let newScale = scale;
    if (e.deltaY < 0) {
      newScale = scale * (1 + zoomIntensity);
    } else {
      newScale = scale * (1 - zoomIntensity);
    }
    if (newScale < 0.1) {
      newScale = 0.1;
    }
    if (newScale > 3) {
      newScale = 3;
    }
    setScale(newScale);
  };

  const zoomIn = () => setScale(prev => {
    const next = prev * 1.2;
    return next > 3 ? 3 : next;
  });
  const zoomOut = () => setScale(prev => {
    const next = prev / 1.2;
    return next < 0.1 ? 0.1 : next;
  });
  const resetView = () => {
    setScale(0.8);
    setPosition({ x: 0, y: 0 });
  };

  if (loading) {
    return (
      <div className="org-chart-loading">
        <div className="org-chart-spinner"></div>
        <p>Loading organization chart...</p>
      </div>
    );
  }

  if (!data || data.departments.length === 0) {
    return (
      <div className="org-chart-empty">
        <p>No organization data available</p>
      </div>
    );
  }

  const startX = 80;
  const deptCount = data.departments.length;
  const firstLeftX = startX;
  const lastRightX = startX + (deptCount - 1) * DEPT_SPACING + DEPT_WIDTH;
  const svgWidth = lastRightX + startX;

  return (
    <div 
      className="org-chart-container"
      ref={containerRef}
      onMouseDown={handleMouseDown}
      onMouseMove={handleMouseMove}
      onMouseUp={handleMouseUp}
      onMouseLeave={handleMouseUp}
      onWheel={handleWheel}
    >

      <div className="org-chart-controls">
        <button className="org-chart-zoom-btn" onClick={zoomIn}>+</button>
        <button className="org-chart-zoom-btn" onClick={zoomOut}>−</button>
        <button className="org-chart-reset-btn" onClick={resetView}>Reset</button>
      </div>


      <div 
        className="org-chart-canvas"
        style={{
          transform: `translate(${position.x}px, ${position.y}px) scale(${scale})`,
        }}
      >

        <svg className="org-chart-svg" width={svgWidth} height={20000}>
          
          <path d={`M ${firstLeftX} ${MAIN_LINE_Y} H ${lastRightX}`} stroke={CONNECTOR_COLOR} strokeWidth={5} fill="none" strokeLinecap="round" filter="drop-shadow(0 2px 4px rgba(156, 163, 175, 0.3))" />

          {data.departments.map((dept, deptIndex) => {
            const deptX = startX + (deptIndex * DEPT_SPACING);
            const deptCenterX = deptX + DEPT_WIDTH / 2;
            const sortedEmployees = [...dept.employees].sort((a, b) => a.id - b.id);
            const bottomY = sortedEmployees.length > 0
              ? EMPLOYEE_START_Y + (sortedEmployees.length - 1) * EMPLOYEE_SPACING_Y + CARD_HEIGHT / 2
              : HEAD_Y + CARD_HEIGHT;


            return (
              <path
                key={`v-${dept.id}`}
                d={`M ${deptCenterX} ${MAIN_LINE_Y} V ${bottomY}`}
                stroke={CONNECTOR_COLOR}
                strokeWidth={5}
                fill="none"
                strokeLinecap="round"
                filter="drop-shadow(0 2px 4px rgba(156, 163, 175, 0.25))"
              />
            );
          })}
        </svg>

        {data.departments.map((dept, deptIndex) => {
          const deptX = startX + (deptIndex * DEPT_SPACING);
          const deptCenterX = deptX + DEPT_WIDTH / 2;


          const sortedEmployees = [...dept.employees].sort((a, b) => a.id - b.id);


          return (
            <div key={dept.id}>

              <div
                className="org-chart-dept-card"
                style={{
                  left: deptX,
                  top: DEPT_Y,
                  width: DEPT_WIDTH,

                  ['--dept-color' as any]: dept.color
                }}
                onClick={() => onDepartmentClick?.({
                  id: dept.id,
                  name: dept.name,
                  color: dept.color,
                  headOfDepartment: dept.headOfDepartment
                    ? {
                        id: dept.headOfDepartment.id,
                        firstName: dept.headOfDepartment.firstName,
                        lastName: dept.headOfDepartment.lastName,
                        email: dept.headOfDepartment.email,
                        title: { name: dept.headOfDepartment.title.name },
                        profilePictureUrl: dept.headOfDepartment.profilePictureUrl ?? undefined,
                      } as unknown as PersonModel
                    : null,
                  employees: dept.employees.map(e => ({
                    id: e.id,
                    firstName: e.firstName,
                    lastName: e.lastName,
                    email: e.email,
                    title: { name: e.title.name },
                    profilePictureUrl: e.profilePictureUrl ?? undefined,
                  } as unknown as PersonModel)),
                })}
                role="button"
                tabIndex={0}
              >
                {dept.name}
              </div>


              {dept.headOfDepartment && (
                <>

                  <div
                    className="org-chart-head-card"
                    style={{
                      left: deptX,
                      top: HEAD_Y,
                      ['--dept-color' as any]: dept.color,
                      ['--card-width' as any]: `${CARD_WIDTH}px`
                    }}
                    onClick={() => onPersonClick?.(dept.headOfDepartment as unknown as PersonModel)}
                    role="button"
                    tabIndex={0}
                  >
                    <Avatar
                      url={dept.headOfDepartment.profilePictureUrl}
                      color={dept.color}
                      alt={`${dept.headOfDepartment.firstName} ${dept.headOfDepartment.lastName}`}
                    />
                    <div className="org-chart-person-info">
                      <div className="org-chart-person-name">
                        {dept.headOfDepartment.firstName} {dept.headOfDepartment.lastName}
                      </div>
                      <div className="org-chart-person-title">
                        {dept.headOfDepartment.title.name}
                      </div>
                    </div>
                  </div>


                  <svg className="org-chart-svg" width={svgWidth} height={20000}>
                    <path
                      d={`M ${deptCenterX} ${HEAD_Y + CARD_HEIGHT / 2} H ${deptX + CARD_WIDTH / 2}`}
                      stroke={CONNECTOR_COLOR}
                      strokeWidth={4}
                      fill="none"
                      strokeLinecap="round"
                      filter="drop-shadow(0 1px 2px rgba(156, 163, 175, 0.2))"
                    />
                  </svg>


                  {sortedEmployees.map((employee, empIndex) => {
                    const empY = EMPLOYEE_START_Y + (empIndex * EMPLOYEE_SPACING_Y);
                    
                    return (
                      <div key={employee.id}>

                        <svg className="org-chart-svg" width={svgWidth} height={20000}>

                          <path
                            d={`M ${deptCenterX} ${empY + CARD_HEIGHT / 2} H ${deptX + (DEPT_WIDTH - CARD_WIDTH)}`}
                            stroke={CONNECTOR_COLOR}
                            strokeWidth={4}
                            fill="none"
                            strokeLinecap="round"
                            filter="drop-shadow(0 1px 2px rgba(156, 163, 175, 0.2))"
                          />
                        </svg>


                        <div
                          className="org-chart-employee-card"
                          style={{
                            left: deptX + (DEPT_WIDTH - CARD_WIDTH),
                            top: empY,
                            ['--card-width' as any]: `${CARD_WIDTH}px`
                          }}
                          onClick={() => onPersonClick?.(employee as unknown as PersonModel)}
                          role="button"
                          tabIndex={0}
                        >
                          <Avatar
                            url={employee.profilePictureUrl}
                            color={dept.color}
                            alt={`${employee.firstName} ${employee.lastName}`}
                          />
                          <div className="org-chart-person-info">
                            <div className="org-chart-person-name">
                              {employee.firstName} {employee.lastName}
                            </div>
                            <div className="org-chart-person-title">
                              {employee.title.name}
                            </div>
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

import { useState, useEffect } from 'react';
import OrganizationChart from '../components/OrganizationChart';
import PersonProfileView from '../components/PersonProfileView';
import DepartmentProfileView from '../components/DepartmentProfileView';
import type { Person, Department } from '../types/models';
import { useAuth } from '../context/AuthContext';
import '../styles/organization.css';

export default function Organization() {
  const { token } = useAuth();
  const [selectedPerson, setSelectedPerson] = useState<Person | null>(null);
  const [selectedDepartment, setSelectedDepartment] = useState<Department | null>(null);
  const [navigationStack, setNavigationStack] = useState<Array<'chart' | 'department' | 'person'>>(['chart']);
  const [fullPersonData, setFullPersonData] = useState<Person | null>(null);
  
  useEffect(() => {
    if (selectedPerson && token) {
      const fetchFullPersonData = async () => {
        try {
          const response = await fetch(`http://localhost:8081/api/people/${selectedPerson.id}`, {
            headers: { Authorization: `Bearer ${token}` }
          });
          
          if (response.ok) {
            const fullPerson = await response.json();
            setFullPersonData(fullPerson);
          } else {
            setFullPersonData(selectedPerson);
          }
        } catch (error) {
          setFullPersonData(selectedPerson);
        }
      };
      
      fetchFullPersonData();
    } else {
      setFullPersonData(null);
    }
  }, [selectedPerson, token]);
  
  return (
    <div className="org-container">
      {selectedPerson ? (
        <PersonProfileView 
          person={fullPersonData || selectedPerson} 
          onBack={() => {
            if (navigationStack.includes('department')) {
              setSelectedPerson(null);
              setNavigationStack(['chart', 'department']);
            } else {
              setSelectedPerson(null);
              setNavigationStack(['chart']);
            }
          }} 
          onPersonUpdate={(updatedPerson) => {
            setSelectedPerson(updatedPerson);
            setFullPersonData(updatedPerson);
          }}
        />
      ) : selectedDepartment ? (
        <DepartmentProfileView 
          department={selectedDepartment} 
          onBack={() => {
            setSelectedDepartment(null);
            setNavigationStack(['chart']);
          }} 
          onDepartmentUpdate={(updatedDepartment) => setSelectedDepartment(updatedDepartment)}
          onPersonClick={(person) => {
            setSelectedPerson(person);
            setNavigationStack(['chart', 'department', 'person']);
          }}
        />
      ) : (
        <OrganizationChart 
          onPersonClick={(person) => {
            setSelectedPerson(person);
            setNavigationStack(['chart', 'person']);
          }} 
          onDepartmentClick={(department) => {
            setSelectedDepartment(department);
            setNavigationStack(['chart', 'department']);
          }} 
        />
      )}
    </div>
  );
} 
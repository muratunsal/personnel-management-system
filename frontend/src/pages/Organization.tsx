import { useState } from 'react';
import OrganizationChart from '../components/OrganizationChart';
import PersonProfileView from '../components/PersonProfileView';
import DepartmentProfileView from '../components/DepartmentProfileView';
import type { Person, Department } from '../types/models';

export default function Organization() {
  const [selectedPerson, setSelectedPerson] = useState<Person | null>(null);
  const [selectedDepartment, setSelectedDepartment] = useState<Department | null>(null);

  return (
    <div style={{ width: '100%', height: '100%' }}>
      {selectedDepartment ? (
        <DepartmentProfileView department={selectedDepartment} onBack={() => setSelectedDepartment(null)} />
      ) : selectedPerson ? (
        <PersonProfileView person={selectedPerson} onBack={() => setSelectedPerson(null)} />
      ) : (
        <OrganizationChart onPersonClick={setSelectedPerson} onDepartmentClick={setSelectedDepartment} />
      )}
    </div>
  );
} 
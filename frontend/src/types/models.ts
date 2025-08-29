export type Department = {
  id: number;
  name: string;
  color?: string;
  headOfDepartment?: Person | null;
  employees?: Person[];
  titles?: Title[];
};

export type Title = {
  id?: number;
  name: string;
  department?: Department | null;
};

export type Person = {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber?: string;
  department?: Department | null;
  title?: Title | null;
  gender?: string;
  profilePictureUrl?: string | null;

  address?: string;
  birthDate?: string;
  contractStartDate?: string;
  salary?: number;
  nationalId?: string;
  bankAccount?: string;
  insuranceNumber?: string;
  contractType?: string;
  contractEndDate?: string;
};

export type Task = {
  id: number;
  title: string;
  description?: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  status: 'ASSIGNED' | 'IN_PROGRESS' | 'COMPLETED' | 'CLOSED';
  department?: Department | null;
  createdBy: Person | null;
  assignee: Person;
  closedAt?: string;
  createdAt: string;
};

export type Meeting = {
  id: number;
  title: string;
  description?: string;
  department?: Department | null;
  organizer: Person | null;
  participants: Person[];
  day: string;
  startTime: string;
  endTime: string;
  finalized: boolean;
  createdAt: string;
};



export type Department = {
  id: number;
  name: string;
  color?: string;
  headOfDepartment?: Person | null;
  employees?: Person[];
};

export type TitleEntity = {
  id?: number;
  name: string;
};

export type Person = {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber?: string;
  department?: Department | null;
  title?: TitleEntity | null;
  startDate?: string;
  birthDate?: string;
  gender?: string;
  address?: string;
  profilePictureUrl?: string | null;
};



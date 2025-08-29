export const canViewSensitiveInfo = (userRole: string, userEmail: string, personEmail: string, userDeptId: number | null, personDeptId: number | null) => {
  if (userRole === 'ADMIN' || userRole === 'HR') return true;
  if (userRole === 'HEAD' && userDeptId === personDeptId) return true;
  if (userRole === 'EMPLOYEE' && userEmail === personEmail) return true;
  return false;
};

export const canEdit = (userRole: string) => {
  return userRole === 'ADMIN' || userRole === 'HR';
};

export const canDelete = (userRole: string) => {
  return userRole === 'ADMIN' || userRole === 'HR';
};

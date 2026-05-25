export function getToken() {
  return localStorage.getItem('redmind_token');
}

export function getNickname() {
  return localStorage.getItem('redmind_nickname') || '创作者';
}

export function getRole() {
  return localStorage.getItem('redmind_role') || 'USER';
}

export function getRoleCode() {
  return localStorage.getItem('redmind_role_code') || 'USER';
}

export function getPermissions() {
  const raw = localStorage.getItem('redmind_permissions');
  if (!raw) {
    return [] as string[];
  }
  try {
    return JSON.parse(raw) as string[];
  } catch {
    return [];
  }
}

export function hasPermission(permission: string) {
  return getPermissions().includes(permission);
}

export function setAuthProfile(payload: { nickname?: string; role?: string; roleCode?: string; permissions?: string[] }) {
  if (payload.nickname) {
    localStorage.setItem('redmind_nickname', payload.nickname);
  }
  if (payload.role) {
    localStorage.setItem('redmind_role', payload.role);
  }
  if (payload.roleCode) {
    localStorage.setItem('redmind_role_code', payload.roleCode);
  }
  if (payload.permissions) {
    localStorage.setItem('redmind_permissions', JSON.stringify(payload.permissions));
  }
  window.dispatchEvent(new Event('redmind-auth-changed'));
}

export function isLoggedIn() {
  return Boolean(getToken());
}

export function isAdmin() {
  return getRole() === 'ADMIN';
}

export function logout() {
  localStorage.removeItem('redmind_token');
  localStorage.removeItem('redmind_nickname');
  localStorage.removeItem('redmind_role');
  localStorage.removeItem('redmind_role_code');
  localStorage.removeItem('redmind_permissions');
  window.dispatchEvent(new Event('redmind-auth-changed'));
  window.location.href = '/login';
}

export async function refreshAuthProfile() {
  const token = getToken();
  if (!token) {
    return;
  }
  const { getCurrentUser } = await import('../api/user');
  const { data } = await getCurrentUser();
  setAuthProfile({
    nickname: data.data.nickname,
    role: data.data.role,
    roleCode: data.data.roleCode,
    permissions: data.data.permissions,
  });
}

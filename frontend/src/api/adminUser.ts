import { http } from './http';
import { AdminRoleItem, AdminUserOverview, ApiResponse, UserManagePayload } from '../types';

export function getAdminUsers(memberType?: string) {
  return http.get<ApiResponse<AdminUserOverview[]>>('/users/admin', {
    params: { memberType },
  });
}

export function updateAdminUser(id: number, payload: UserManagePayload) {
  return http.put<ApiResponse<null>>(`/users/admin/${id}`, payload);
}

export function getAssignableRoles() {
  return http.get<ApiResponse<AdminRoleItem[]>>('/admin/roles/assignable');
}

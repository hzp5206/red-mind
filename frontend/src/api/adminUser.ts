import { http } from './http';
import { AdminRoleItem, AdminUserOverview, ApiResponse, PaginatedResponse, UserManagePayload } from '../types';

export function getAdminUsers(params?: {
  keyword?: string;
  memberType?: string;
  role?: string;
  roleCode?: string;
  page?: number;
  pageSize?: number;
}) {
  return http.get<ApiResponse<PaginatedResponse<AdminUserOverview>>>('/users/admin', {
    params,
  });
}

export function updateAdminUser(id: number, payload: UserManagePayload) {
  return http.put<ApiResponse<null>>(`/users/admin/${id}`, payload);
}

export function getAssignableRoles() {
  return http.get<ApiResponse<AdminRoleItem[]>>('/admin/roles/assignable');
}

export function exportAdminUsers(params?: {
  keyword?: string;
  memberType?: string;
  role?: string;
  roleCode?: string;
}) {
  return http.get<ApiResponse<string>>('/users/admin/export', {
    params,
  });
}

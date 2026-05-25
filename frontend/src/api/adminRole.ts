import { AdminPermissionItem, AdminRoleItem, ApiResponse } from '../types';
import { http } from './http';

export function getAdminRoles() {
  return http.get<ApiResponse<AdminRoleItem[]>>('/admin/roles');
}

export function getAdminPermissions() {
  return http.get<ApiResponse<AdminPermissionItem[]>>('/admin/roles/permissions');
}

export function saveAdminRole(payload: {
  id?: number;
  roleCode: string;
  roleName: string;
  descriptionText?: string;
  isActive?: boolean;
  permissions?: string[];
}) {
  return http.post<ApiResponse<AdminRoleItem>>('/admin/roles', payload);
}

export function deleteAdminRole(id: number) {
  return http.delete<ApiResponse<null>>(`/admin/roles/${id}`);
}

export function copyAdminRole(id: number, payload: {
  roleCode: string;
  roleName: string;
  descriptionText?: string;
}) {
  return http.post<ApiResponse<AdminRoleItem>>(`/admin/roles/${id}/copy`, payload);
}

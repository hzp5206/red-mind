export interface GenerateRequest {
  mode: string;
  coreDescription: string;
  style: string;
  targetAudience?: string[];
  tone?: string;
  wordCount: number;
  requiredKeywords?: string[];
  referenceUrl?: string;
  styleSample?: string;
}

export interface QualityScores {
  titleAttraction: number;
  keywordDensity: string;
  complianceIssues: string[];
}

export interface GeneratedVersion {
  verNum: number;
  title: string;
  content: string;
  tags: string[];
  qualityScores: QualityScores;
}

export interface GenerateResponse {
  generationId: string;
  versions: GeneratedVersion[];
  creditsUsed: number;
}

export interface ApiResponse<T> {
  code: number;
  msg: string;
  data: T;
}

export interface TemplateItem {
  id: number;
  category: string;
  title: string;
  contentExample: string;
  tags: string;
  style: string;
  isActive?: boolean;
}

export interface HistoryRecord {
  id: number;
  coreInput: string;
  style: string;
  persona: string;
  wordCount: number;
  results: string;
  isCollected: boolean;
  createdAt: string;
}

export interface LibraryItem {
  id: number;
  historyId: number;
  customTags: string;
  coreInput: string;
  style: string;
  results: string;
  createdAt: string;
}

export interface UserProfile {
  id: number;
  email: string;
  nickname: string;
  role?: string;
  roleCode?: string;
  permissions?: string[];
  memberType: string;
  dailyGenCount: number;
  dailyLimit: number;
  remainingCount: number;
  pro: boolean;
}

export interface TemplateSavePayload {
  category: string;
  title: string;
  contentExample: string;
  tags: string;
  style: string;
  isActive: boolean;
}

export interface SensitiveWordItem {
  id?: number;
  word: string;
  replacement: string;
  isActive?: boolean;
}

export interface AdminUserOverview {
  id: number;
  email: string;
  nickname: string;
  role: string;
  roleCode?: string;
  memberType: string;
  dailyGenCount: number;
  lastGenDate: string | null;
}

export interface UserManagePayload {
  role?: string;
  roleCode?: string;
  memberType?: string;
  memberExpireAt?: string;
  dailyGenCount?: number;
}

export interface DashboardSummary {
  templateCount: number;
  userCount: number;
  adminCount: number;
  todayGenerationCount: number;
  sensitiveWordCount: number;
  todayOperationCount: number;
}

export interface PaginatedResponse<T> {
  total: number;
  records: T[];
}

export interface GenerationLogItem {
  id: number;
  userId: number;
  coreInput: string;
  style: string;
  persona: string;
  wordCount: number;
  collected: boolean;
  createdAt: string;
}

export interface OperationLogItem {
  id: number;
  operatorId: number;
  operatorNickname: string;
  moduleName: string;
  actionName: string;
  targetType: string;
  targetId?: number;
  detailText: string;
  createdAt: string;
}

export interface SelectOptionItem {
  id?: number;
  label: string;
  value: string;
  sortOrder?: number;
  isActive?: boolean;
}

export interface AdminRoleItem {
  id: number;
  roleCode: string;
  roleName: string;
  descriptionText?: string;
  isActive: boolean;
  permissions: string[];
}

export interface AdminPermissionItem {
  permissionCode: string;
  permissionName: string;
  moduleName: string;
}

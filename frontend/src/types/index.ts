export interface GenerateRequest {
  mode: string;
  productName?: string;
  coreDescription: string;
  style: string;
  targetAudience?: string[];
  coreSellingPoints?: string[];
  useScenarios?: string[];
  tone?: string;
  conversionGoal?: string;
  contentGoal?: string;
  hookPreference?: string;
  noteStructure?: string;
  wordCount: number;
  requiredKeywords?: string[];
  forbiddenExpressions?: string[];
  referenceUrl?: string;
  styleSample?: string;
}

export interface TitleCandidate {
  title: string;
  reason?: string;
  score?: number;
}

export interface PrePublishCheckItem {
  label: string;
  status: string;
  detail: string;
}

export interface QualityScores {
  overallScore?: number;
  titleAttraction?: number;
  hookStrength?: number;
  sellingPointClarity?: number;
  emotionalAppeal?: number;
  collectIntent?: number;
  interactionPotential?: number;
  authenticity?: number;
  aiFlavorRisk?: number;
  keywordCoverage?: string;
  riskLevel?: string;
  strengths?: string[];
  complianceIssues?: string[];
}

export interface GeneratedVersion {
  verNum: number;
  angleLabel?: string;
  hookType?: string;
  strategySummary?: string;
  opening?: string;
  cta?: string;
  title: string;
  titleCandidates?: TitleCandidate[];
  content: string;
  tags: string[];
  trendingReferenceTitles?: string[];
  referenceTakeaways?: string[];
  differentiationTips?: string[];
  publishSuggestions?: string[];
  prePublishChecks?: PrePublishCheckItem[];
  optimizationActions?: string[];
  qualityScores?: QualityScores;
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
  finalTitle?: string;
  finalResult?: string;
  finalScore?: number;
  isCollected: boolean;
  lastModifiedAt?: string;
  createdAt: string;
}

export interface LibraryItem {
  id: number;
  historyId?: number | null;
  trendingItemId?: number | null;
  sourceType?: string;
  sourceTitle?: string;
  customTags: string;
  productName?: string;
  coreInput: string;
  previewText?: string;
  style: string;
  tone?: string;
  results: string;
  noteUrl?: string;
  styleSample?: string;
  requiredKeywords?: string[];
  hookPreference?: string;
  noteStructure?: string;
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
  recentOperations: RecentOperationItem[];
  adminRoleDistribution: SummaryBucketItem[];
  operationModuleDistribution: SummaryBucketItem[];
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
  snapshotBefore?: string;
  snapshotAfter?: string;
  createdAt: string;
}

export interface RecentOperationItem {
  id: number;
  operatorNickname: string;
  moduleName: string;
  actionName: string;
  detailText: string;
  createdAt: string;
}

export interface SummaryBucketItem {
  label: string;
  value: number;
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

export interface AiSetting {
  provider: string;
  baseUrl: string;
  model: string;
  chatPath: string;
  apiKeyValue?: string;
  apiKeyMasked?: string | null;
  apiKeyConfigured: boolean;
  providerOptions: string[];
  modelOptions: string[];
}

export interface AiConnectivityTestResult {
  success: boolean;
  provider: string;
  model: string;
  baseUrl: string;
  message: string;
}

export interface AiRuntimeInfo {
  provider: string;
  model: string;
  baseUrl: string;
}

export interface TrendingTask {
  id: number;
  taskName: string;
  platformCode: string;
  keywords: string;
  fetchLimit: number;
  cronExpr: string;
  providerCode: string;
  enabled: boolean;
  lastRunAt?: string;
  lastFetchedCount?: number;
  lastStatus?: string;
  lastMessage?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface TrendingItem {
  id: number;
  taskId: number;
  platformCode: string;
  sourceId: string;
  keyword?: string;
  title: string;
  contentText?: string;
  authorName?: string;
  noteUrl?: string;
  likesCount?: number;
  collectsCount?: number;
  commentsCount?: number;
  heatScore?: number;
  tags: string[];
  publishedAt?: string;
  fetchedAt?: string;
}

export interface TrendingAnalysis {
  itemId: number;
  titleType: string;
  hookType: string;
  structureSummary: string;
  interactionCta: string;
  collectPoints: string[];
  keywords: string[];
  tone: string;
  recommendedStyle: string;
  recommendedTone: string;
  recommendedHook: string;
  recommendedStructure: string;
  productHint: string;
  coreDescription: string;
  styleSample: string;
  requiredKeywords: string[];
  summary: string;
  adaptationTips: string[];
}

export interface TrendingDashboard {
  taskCount: number;
  enabledTaskCount: number;
  totalItemCount: number;
  todayFetchedCount: number;
  tasks: TrendingTask[];
  latestItems: TrendingItem[];
}

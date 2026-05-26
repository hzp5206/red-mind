import {
  AppstoreOutlined,
  FireOutlined,
  RocketOutlined,
  StarOutlined,
  ThunderboltOutlined,
} from '@ant-design/icons';
import {
  Alert,
  Badge,
  Button,
  Card,
  Col,
  Collapse,
  Empty,
  Form,
  Input,
  InputNumber,
  Progress,
  Row,
  Select,
  Space,
  Statistic,
  Tabs,
  Tag,
  Typography,
  message,
} from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { generateCopyStream, getAiRuntimeInfo } from '../api/generate';
import { collectHistory, finalizeHistoryVersion } from '../api/history';
import { getCurrentUser } from '../api/user';
import { EditorModal } from '../components/EditorModal';
import { VersionCard } from '../components/VersionCard';
import { AiRuntimeInfo, GenerateRequest, GeneratedVersion, QualityScores, UserProfile } from '../types';

const modeOptions = [
  { label: '智能生成', value: 'smart' },
  { label: '拆解生成', value: 'framework' },
  { label: '笔风仿写', value: 'style_clone' },
];

const styleOptions = [
  { label: '好物种草', value: 'good_item' },
  { label: '探店打卡', value: 'visit' },
  { label: '实用教程', value: 'tutorial' },
  { label: 'Vlog 日常', value: 'vlog' },
  { label: '合集清单', value: 'collection' },
  { label: '成分测评', value: 'ingredient' },
  { label: '知识分享', value: 'knowledge' },
  { label: '情感故事', value: 'story' },
];

const toneOptions = ['真诚种草', '专业理性', '幽默风趣', '温柔治愈', '急切按头安利', '冷静分析'];
const conversionGoalOptions = ['提升点击', '促进收藏', '促进评论', '引导私信', '辅助下单'];
const contentGoalOptions = ['点赞', '收藏', '评论', '私信', '下单转化'];
const hookOptions = ['反差开场', '第一人称体验', '清单利益点', '问题切入', '结论先行'];
const structureOptions = [
  '痛点 → 体验 → 结论',
  '问题 → 方案 → CTA',
  '清单 → 场景 → 建议',
  '对比 → 选择 → 互动',
];

const defaultValues: Partial<GenerateRequest> = {
  mode: 'smart',
  style: 'good_item',
  tone: '真诚种草',
  conversionGoal: '促进收藏',
  contentGoal: '收藏',
  hookPreference: '反差开场',
  noteStructure: '痛点 → 体验 → 结论',
  wordCount: 400,
  requiredKeywords: [],
  forbiddenExpressions: [],
  coreSellingPoints: [],
  useScenarios: [],
  targetAudience: [],
};

const demoValues: GenerateRequest = {
  mode: 'smart',
  style: 'good_item',
  tone: '真诚种草',
  conversionGoal: '促进收藏',
  contentGoal: '收藏',
  hookPreference: '反差开场',
  noteStructure: '痛点 → 体验 → 结论',
  wordCount: 400,
  requiredKeywords: ['熬夜急救', '提亮', '清爽'],
  forbiddenExpressions: ['全网第一', '绝对有效'],
  coreSellingPoints: ['第二天肤色不蜡黄', '肤感清爽不黏腻', '适合熬夜党急救'],
  useScenarios: ['熬夜后护肤', '妆前急救', '换季维稳'],
  targetAudience: ['职场女生', '经常熬夜的人', '追求高效率护肤的人'],
  productName: '熬夜急救精华',
  coreDescription:
    '一款主打熬夜后快速提亮、维稳和改善暗沉的修护精华，肤感清爽不黏，适合上班族和经常晚睡人群。',
};

interface PrefillState {
  sourceLabel?: string;
  productName?: string;
  coreDescription?: string;
  style?: string;
  tone?: string;
  styleSample?: string;
  requiredKeywords?: string[];
  coreSellingPoints?: string[];
  useScenarios?: string[];
  targetAudience?: string[];
  hookPreference?: string;
  noteStructure?: string;
  conversionGoal?: string;
  contentGoal?: string;
}

type StreamStatus = 'idle' | 'streaming' | 'done' | 'error';

const createEmptyScores = (): QualityScores => ({
  overallScore: 0,
  titleAttraction: 0,
  hookStrength: 0,
  sellingPointClarity: 0,
  emotionalAppeal: 0,
  collectIntent: 0,
  interactionPotential: 0,
  authenticity: 0,
  aiFlavorRisk: 0,
  keywordCoverage: 'analyzing',
  riskLevel: 'analyzing',
  strengths: ['正在分析策略亮点'],
  complianceIssues: ['正在检查内容风险'],
});

const createDraftVersion = (verNum: number): GeneratedVersion => ({
  verNum,
  angleLabel: '正在生成策略',
  hookType: '等待生成',
  strategySummary: '系统正在推演更适合当前主题的爆文切入角度。',
  opening: '',
  cta: '',
  title: '',
  titleCandidates: [],
  content: '',
  tags: [],
  publishSuggestions: [],
  prePublishChecks: [],
  optimizationActions: [],
  qualityScores: createEmptyScores(),
});

function safeParse<T>(raw: string) {
  try {
    return JSON.parse(raw) as T;
  } catch (error) {
    return null;
  }
}

function getStatusMeta(status: StreamStatus, versionsCount: number, streamHint: string) {
  if (status === 'streaming') {
    return {
      color: 'processing' as const,
      title: '正在生成中',
      description: streamHint || `已产出 ${versionsCount} 个候选版本，系统仍在继续打磨。`,
      percent: Math.min(92, 18 + versionsCount * 24),
    };
  }
  if (status === 'done') {
    return {
      color: 'success' as const,
      title: '生成完成',
      description: streamHint || '你可以继续优化、改写、收藏或定稿当前版本。',
      percent: 100,
    };
  }
  if (status === 'error') {
    return {
      color: 'error' as const,
      title: '生成中断',
      description: streamHint || '本次生成未完成，请检查配置后重试。',
      percent: versionsCount ? 55 : 20,
    };
  }
  return {
    color: 'default' as const,
    title: '准备开始',
    description: '先补充左侧素材与策略，系统会输出更稳的多版本文案。',
    percent: 0,
  };
}

export function CreatePage() {
  const [form] = Form.useForm<GenerateRequest>();
  const watchedValues = Form.useWatch([], form) as Partial<GenerateRequest> | undefined;
  const currentMode = Form.useWatch('mode', form);
  const [versions, setVersions] = useState<GeneratedVersion[]>([]);
  const [loading, setLoading] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();
  const [historyId, setHistoryId] = useState<number | null>(null);
  const [editorOpen, setEditorOpen] = useState(false);
  const [editingVersion, setEditingVersion] = useState<GeneratedVersion | undefined>();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [selectedVersion, setSelectedVersion] = useState<number | null>(null);
  const [activeVersionKey, setActiveVersionKey] = useState<string>();
  const [prefillSource, setPrefillSource] = useState<string>('');
  const [runtimeInfo, setRuntimeInfo] = useState<AiRuntimeInfo | null>(null);
  const [streamStatus, setStreamStatus] = useState<StreamStatus>('idle');
  const [streamHint, setStreamHint] = useState('');
  const location = useLocation();

  useEffect(() => {
    getCurrentUser().then(({ data }) => setProfile(data.data));
    getAiRuntimeInfo().then(({ data }) => setRuntimeInfo(data.data));
  }, []);

  useEffect(() => {
    const state = location.state as PrefillState | null;
    if (!state) {
      return;
    }
    setPrefillSource(state.sourceLabel || '');
    form.setFieldsValue({
      ...defaultValues,
      productName: state.productName,
      coreDescription: state.coreDescription,
      style: state.style || defaultValues.style,
      tone: state.tone || defaultValues.tone,
      styleSample: state.styleSample,
      requiredKeywords: state.requiredKeywords || [],
      coreSellingPoints: state.coreSellingPoints || [],
      useScenarios: state.useScenarios || [],
      targetAudience: state.targetAudience || [],
      hookPreference: state.hookPreference || defaultValues.hookPreference,
      noteStructure: state.noteStructure || defaultValues.noteStructure,
      conversionGoal: state.conversionGoal || defaultValues.conversionGoal,
      contentGoal: state.contentGoal || defaultValues.contentGoal,
    });
  }, [form, location.state]);

  useEffect(() => {
    if (versions.length && !activeVersionKey) {
      setActiveVersionKey(String(versions[0].verNum));
    }
  }, [activeVersionKey, versions]);

  const briefingScore = useMemo(() => {
    const values = watchedValues || {};
    let score = 0;
    if (values.productName) {
      score += 12;
    }
    if (values.coreDescription && values.coreDescription.trim().length >= 10) {
      score += 22;
    }
    if ((values.coreSellingPoints || []).length) {
      score += 15;
    }
    if ((values.targetAudience || []).length) {
      score += 12;
    }
    if ((values.useScenarios || []).length) {
      score += 12;
    }
    if ((values.requiredKeywords || []).length) {
      score += 10;
    }
    if (values.hookPreference) {
      score += 7;
    }
    if (values.noteStructure) {
      score += 5;
    }
    if (values.styleSample) {
      score += 5;
    }
    return Math.min(score, 100);
  }, [watchedValues]);

  const statusMeta = useMemo(
    () => getStatusMeta(streamStatus, versions.length, streamHint),
    [streamHint, streamStatus, versions.length],
  );

  const updateVersion = (nextVersion: GeneratedVersion) => {
    setVersions((prev) => prev.map((item) => (item.verNum === nextVersion.verNum ? nextVersion : item)));
    setEditingVersion((prev) => (prev?.verNum === nextVersion.verNum ? nextVersion : prev));
  };

  const handleFinalize = async (version: GeneratedVersion) => {
    if (!historyId) {
      messageApi.warning('请先完成一次生成后，再设置最终采用版本。');
      return;
    }
    await finalizeHistoryVersion(historyId, {
      ...version,
      optimizationActions: Array.from(new Set([...(version.optimizationActions || []), 'finalized'])),
    });
    setSelectedVersion(version.verNum);
    setActiveVersionKey(String(version.verNum));
    messageApi.success(`版本 ${version.verNum} 已设为最终采用版本`);
  };

  const tabItems = useMemo(
    () =>
      versions.map((version) => ({
        key: String(version.verNum),
        label: (
          <div className="create-version-tab">
            <span>版本 {version.verNum}</span>
            <Space size={6}>
              <Tag color="blue">{version.qualityScores?.overallScore?.toFixed(1) ?? '--'} 分</Tag>
              {selectedVersion === version.verNum ? <Badge status="success" text="已定稿" /> : null}
            </Space>
          </div>
        ),
        children: (
          <VersionCard
            request={form.getFieldsValue(true)}
            version={version}
            onOpenEditor={(currentVersion) => {
              setEditingVersion(currentVersion);
              setEditorOpen(true);
            }}
            onCollect={async () => {
              if (!historyId) {
                messageApi.warning('请先完成一次生成后，再收藏到灵感库。');
                return;
              }
              await collectHistory(historyId);
              messageApi.success('已收藏到灵感库');
            }}
            onFinalize={handleFinalize}
            onVersionChange={updateVersion}
          />
        ),
      })),
    [form, historyId, messageApi, selectedVersion, versions],
  );

  const handleFillDemo = () => {
    form.setFieldsValue(demoValues);
    messageApi.success('已填入一套示例素材，你可以直接生成或继续微调。');
  };

  const handleReset = () => {
    form.setFieldsValue(defaultValues);
    form.resetFields();
    form.setFieldsValue(defaultValues);
    setPrefillSource('');
    messageApi.success('已重置为默认状态。');
  };

  const handleGenerate = async () => {
    const values = await form.validateFields();
    setLoading(true);
    setVersions([]);
    setHistoryId(null);
    setSelectedVersion(null);
    setActiveVersionKey(undefined);
    setStreamStatus('streaming');
    setStreamHint('系统正在拆解主题、寻找角度并生成首批文案。');

    const drafts: Record<number, GeneratedVersion> = {};
    let streamFailed = false;

    try {
      await generateCopyStream(values, (event, rawData) => {
        if (event === 'strategy') {
          const parsed = safeParse<Partial<GeneratedVersion> & { ver: number }>(rawData);
          if (!parsed) {
            return;
          }
          const current = drafts[parsed.ver] || createDraftVersion(parsed.ver);
          drafts[parsed.ver] = {
            ...current,
            angleLabel: parsed.angleLabel || current.angleLabel,
            hookType: parsed.hookType || current.hookType,
            strategySummary: parsed.strategySummary || current.strategySummary,
            opening: parsed.opening || current.opening,
            cta: parsed.cta || current.cta,
          };
          setStreamHint(`版本 ${parsed.ver} 的策略方向已生成，正在补全文案内容。`);
          setVersions(Object.values(drafts).sort((a, b) => a.verNum - b.verNum));
          setActiveVersionKey(String(parsed.ver));
        }

        if (event === 'version') {
          const parsed = safeParse<GeneratedVersion>(rawData);
          if (!parsed) {
            return;
          }
          drafts[parsed.verNum] = {
            ...createDraftVersion(parsed.verNum),
            ...drafts[parsed.verNum],
            ...parsed,
          };
          setStreamHint(`版本 ${parsed.verNum} 已完成，系统正在继续生成后续版本。`);
          setVersions(Object.values(drafts).sort((a, b) => a.verNum - b.verNum));
          setActiveVersionKey(String(parsed.verNum));
        }

        if (event === 'done') {
          const parsed = safeParse<{ history_id?: number }>(rawData);
          if (parsed?.history_id) {
            setHistoryId(parsed.history_id);
          }
          setStreamStatus('done');
          setStreamHint(`本次已生成 ${Object.keys(drafts).length || versions.length} 个候选版本，可以开始筛选和优化。`);
          messageApi.success('文案生成完成');
          getCurrentUser().then(({ data }) => setProfile(data.data));
          getAiRuntimeInfo().then(({ data }) => setRuntimeInfo(data.data));
        }

        if (event === 'error') {
          streamFailed = true;
          setStreamStatus('error');
          setStreamHint(rawData);
          messageApi.error(rawData);
        }
      });

      if (!streamFailed) {
        setStreamStatus('done');
      }
    } catch (error) {
      streamFailed = true;
      const nextMessage = error instanceof Error ? error.message : '流式生成失败，请稍后重试';
      setStreamStatus('error');
      setStreamHint(nextMessage);
      messageApi.error(nextMessage);
    } finally {
      setLoading(false);
    }
  };

  const emptyTips = [
    {
      icon: <AppstoreOutlined />,
      title: '先给系统喂清楚素材',
      description: '产品、场景、人群越具体，生成的文案越像一个懂转化的内容策划。',
    },
    {
      icon: <ThunderboltOutlined />,
      title: '再锁定爆文策略',
      description: '提前设定钩子、结构、目标动作，能显著减少“看起来像 AI”的空泛表达。',
    },
    {
      icon: <RocketOutlined />,
      title: '最后筛选并定稿',
      description: '从多个版本里挑最能打的一个，再做标题、开头、CTA 的精修。',
    },
  ];

  return (
    <div className="create-page">
      {contextHolder}
      <Card className="create-hero-card" bordered={false}>
        <Row gutter={[24, 24]} align="middle">
          <Col xs={24} lg={15}>
            <Space direction="vertical" size={12} style={{ width: '100%' }}>
              <Space wrap>
                <Tag color="magenta">文案生成工作台</Tag>
                <Tag color="gold">{profile?.pro ? '会员模式' : '标准模式'}</Tag>
                <Tag color="blue">当前模型：{runtimeInfo?.model || '-'}</Tag>
              </Space>
              <Typography.Title level={2} style={{ margin: 0 }}>
                用更清晰的策略输入，换更能打的爆款文案
              </Typography.Title>
              <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
                这不是一个只会“堆字”的生成器。它会同时输出策略角度、标题候选、正文版本、质量评分和发布前检查，
                帮你更快从素材走到可发文案。
              </Typography.Paragraph>
            </Space>
          </Col>
          <Col xs={24} lg={9}>
            <div className="create-hero-metrics">
              <div className="create-hero-metric">
                <Statistic title="今日剩余次数" value={profile?.pro ? '∞' : profile?.remainingCount ?? '-'} />
              </div>
              <div className="create-hero-metric">
                <Statistic title="已生成版本" value={versions.length} prefix={<FireOutlined />} />
              </div>
              <div className="create-hero-metric">
                <Statistic title="Brief 完整度" value={briefingScore} suffix="%" prefix={<StarOutlined />} />
              </div>
            </div>
          </Col>
        </Row>
      </Card>

      <Row gutter={[24, 24]} align="top">
        <Col xs={24} xl={9}>
          <Card className="create-form-card sticky-card" bordered={false}>
            <div className="create-card-header">
              <div>
                <Typography.Title level={4} style={{ margin: 0 }}>
                  素材与策略输入
                </Typography.Title>
                <Typography.Text type="secondary">
                  把输入写具体，系统才会给你更稳、更像真人策划的输出。
                </Typography.Text>
              </div>
              <Tag color="purple">{modeOptions.find((item) => item.value === currentMode)?.label || '智能生成'}</Tag>
            </div>

            <div className="create-form-summary">
              <div className="create-form-summary-item">
                <span>当前账号</span>
                <strong>{profile?.nickname || '创作者'}</strong>
              </div>
              <div className="create-form-summary-item">
                <span>今日已用</span>
                <strong>{profile?.dailyGenCount ?? 0} 次</strong>
              </div>
              <div className="create-form-summary-item">
                <span>运行模型</span>
                <strong>{runtimeInfo?.provider || '-'} / {runtimeInfo?.model || '-'}</strong>
              </div>
            </div>

            <div className="create-brief-score">
              <div className="create-brief-score-head">
                <span>输入完整度</span>
                <strong>{briefingScore}%</strong>
              </div>
              <Progress percent={briefingScore} showInfo={false} strokeColor="#ff6b8a" />
              <Typography.Text type="secondary">
                建议至少补充产品描述、卖点、人群、场景和关键词，生成质量会明显更稳。
              </Typography.Text>
            </div>

            {prefillSource ? (
              <Alert
                type="info"
                showIcon
                style={{ marginBottom: 16 }}
                message="已带入外部灵感"
                description={prefillSource}
              />
            ) : null}

            <Alert
              type="success"
              showIcon
              style={{ marginBottom: 16 }}
              message="建议工作流"
              description="先填素材，再定策略，最后生成多个候选版本。生成后优先看评分、标题候选和发布前检查。"
            />

            <Form form={form} layout="vertical" initialValues={defaultValues}>
              <Collapse
                defaultActiveKey={['basic', 'audience', 'strategy']}
                ghost
                items={[
                  {
                    key: 'basic',
                    label: '基础素材',
                    children: (
                      <Row gutter={16}>
                        <Col span={24}>
                          <Form.Item name="mode" label="创作模式" rules={[{ required: true }]}>
                            <Select options={modeOptions} />
                          </Form.Item>
                        </Col>
                        <Col span={24}>
                          <Form.Item name="productName" label="产品 / 主题">
                            <Input placeholder="例如：熬夜急救精华 / 复古咖啡店 / 防晒测评" />
                          </Form.Item>
                        </Col>
                        <Col span={24}>
                          <Form.Item
                            name="coreDescription"
                            label="核心描述"
                            extra="建议直接写清楚卖点、使用感受、适用场景或真实体验。"
                            rules={[{ required: true, min: 10, message: '至少输入 10 个字' }]}
                          >
                            <Input.TextArea
                              rows={5}
                              placeholder="例如：主打熬夜后快速提亮和维稳，肤感清爽不黏，适合第二天要见人但状态差的人。"
                            />
                          </Form.Item>
                        </Col>
                        <Col span={12}>
                          <Form.Item name="style" label="笔记风格" rules={[{ required: true }]}>
                            <Select options={styleOptions} />
                          </Form.Item>
                        </Col>
                        <Col span={12}>
                          <Form.Item name="tone" label="语气人设">
                            <Select options={toneOptions.map((item) => ({ label: item, value: item }))} />
                          </Form.Item>
                        </Col>
                        <Col span={24}>
                          <Form.Item name="coreSellingPoints" label="核心卖点">
                            <Select mode="tags" placeholder="例如：提亮快、肤感轻、不闷痘、性价比高" />
                          </Form.Item>
                        </Col>
                      </Row>
                    ),
                  },
                  {
                    key: 'audience',
                    label: '人群与场景',
                    children: (
                      <Row gutter={16}>
                        <Col span={24}>
                          <Form.Item name="targetAudience" label="目标人群">
                            <Select mode="tags" placeholder="例如：学生党、宝妈、职场通勤人群" />
                          </Form.Item>
                        </Col>
                        <Col span={24}>
                          <Form.Item name="useScenarios" label="使用场景">
                            <Select mode="tags" placeholder="例如：熬夜后、妆前、出差途中、周末探店" />
                          </Form.Item>
                        </Col>
                        <Col span={12}>
                          <Form.Item name="conversionGoal" label="转化目标">
                            <Select options={conversionGoalOptions.map((item) => ({ label: item, value: item }))} />
                          </Form.Item>
                        </Col>
                        <Col span={12}>
                          <Form.Item name="contentGoal" label="内容目标">
                            <Select options={contentGoalOptions.map((item) => ({ label: item, value: item }))} />
                          </Form.Item>
                        </Col>
                      </Row>
                    ),
                  },
                  {
                    key: 'strategy',
                    label: '爆文策略',
                    children: (
                      <Row gutter={16}>
                        <Col span={12}>
                          <Form.Item name="hookPreference" label="钩子偏好">
                            <Select options={hookOptions.map((item) => ({ label: item, value: item }))} />
                          </Form.Item>
                        </Col>
                        <Col span={12}>
                          <Form.Item name="noteStructure" label="结构偏好">
                            <Select options={structureOptions.map((item) => ({ label: item, value: item }))} />
                          </Form.Item>
                        </Col>
                        <Col span={12}>
                          <Form.Item name="wordCount" label="字数要求">
                            <InputNumber min={100} max={1000} style={{ width: '100%' }} />
                          </Form.Item>
                        </Col>
                        <Col span={24}>
                          <Form.Item name="requiredKeywords" label="必须包含关键词">
                            <Select mode="tags" placeholder="每个词按回车确认，例如：提亮、通勤、显白" />
                          </Form.Item>
                        </Col>
                      </Row>
                    ),
                  },
                  {
                    key: 'advanced',
                    label: '高级约束',
                    children: (
                      <Row gutter={16}>
                        <Col span={24}>
                          <Form.Item name="forbiddenExpressions" label="禁用表达 / 避雷词">
                            <Select mode="tags" placeholder="例如：全网第一、闭眼冲、绝对有效" />
                          </Form.Item>
                        </Col>
                        <Col span={24}>
                          <Form.Item name="referenceUrl" label="参考笔记链接">
                            <Input placeholder="可粘贴参考链接，帮助系统理解你想要的内容方向" />
                          </Form.Item>
                        </Col>
                        {currentMode === 'style_clone' ? (
                          <Col span={24}>
                            <Form.Item
                              name="styleSample"
                              label="仿写范文"
                              extra="仅用于学习表达节奏和语气，不建议直接照搬内容。"
                            >
                              <Input.TextArea rows={4} placeholder="贴入你喜欢的笔记表达方式，帮助系统学习语感。" />
                            </Form.Item>
                          </Col>
                        ) : null}
                      </Row>
                    ),
                  },
                ]}
              />

              <div className="create-form-actions">
                <Space wrap>
                  <Button type="primary" size="large" loading={loading} icon={<ThunderboltOutlined />} onClick={handleGenerate}>
                    {loading ? '正在生成中...' : '开始生成'}
                  </Button>
                  <Button size="large" onClick={handleFillDemo}>
                    一键填入示例
                  </Button>
                  <Button size="large" onClick={handleReset}>
                    重置输入
                  </Button>
                </Space>
                <Typography.Text type="secondary">
                  小建议：如果你想压低 AI 味，尽量把“真实场景 + 具体感受 + 避雷要求”写清楚。
                </Typography.Text>
              </div>
            </Form>
          </Card>
        </Col>

        <Col xs={24} xl={15}>
          <Card
            className="create-result-card"
            bordered={false}
            title={
              <div className="create-card-header">
                <div>
                  <Typography.Title level={4} style={{ margin: 0 }}>
                    生成结果与优化工作台
                  </Typography.Title>
                  <Typography.Text type="secondary">
                    这里会按版本展示策略、标题、正文、评分和发布建议。
                  </Typography.Text>
                </div>
                <Space wrap>
                  <Badge status={statusMeta.color} text={statusMeta.title} />
                  <Button onClick={handleGenerate} loading={loading}>
                    重新生成
                  </Button>
                </Space>
              </div>
            }
          >
            <div className="create-status-panel">
              <div className="create-status-top">
                <div>
                  <Typography.Text strong>{statusMeta.title}</Typography.Text>
                  <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
                    {statusMeta.description}
                  </Typography.Paragraph>
                </div>
                <Tag color={streamStatus === 'error' ? 'error' : streamStatus === 'done' ? 'success' : 'processing'}>
                  {versions.length ? `${versions.length} 个版本` : '等待生成'}
                </Tag>
              </div>
              <Progress percent={statusMeta.percent} showInfo={false} strokeColor="#ff6b8a" />
            </div>

            <Row gutter={[16, 16]} className="create-metrics-row">
              <Col xs={24} md={8}>
                <Card className="create-mini-card" bordered={false}>
                  <Statistic title="已生成版本" value={versions.length} prefix={<FireOutlined />} />
                </Card>
              </Col>
              <Col xs={24} md={8}>
                <Card className="create-mini-card" bordered={false}>
                  <Statistic title="最终采用版本" value={selectedVersion ?? '-'} prefix={<StarOutlined />} />
                </Card>
              </Col>
              <Col xs={24} md={8}>
                <Card className="create-mini-card" bordered={false}>
                  <Statistic title="历史记录 ID" value={historyId ?? '-'} prefix={<AppstoreOutlined />} />
                </Card>
              </Col>
            </Row>

            {selectedVersion ? (
              <Alert
                type="success"
                showIcon
                style={{ marginBottom: 16 }}
                message={`当前最终采用版本：版本 ${selectedVersion}`}
                description="系统会将该版本的标题、正文、评分和优化动作同步沉淀到历史记录中。"
              />
            ) : null}

            {versions.length ? (
              <Tabs activeKey={activeVersionKey} items={tabItems} onChange={setActiveVersionKey} />
            ) : (
              <div className="create-empty-state">
                <Empty
                  image={Empty.PRESENTED_IMAGE_SIMPLE}
                  description="还没有生成结果。先填写左侧素材，系统会在这里实时输出多个候选版本。"
                />
                <div className="create-empty-grid">
                  {emptyTips.map((item) => (
                    <Card key={item.title} className="create-empty-tip-card" bordered={false}>
                      <div className="create-empty-tip-icon">{item.icon}</div>
                      <Typography.Title level={5}>{item.title}</Typography.Title>
                      <Typography.Text type="secondary">{item.description}</Typography.Text>
                    </Card>
                  ))}
                </div>
              </div>
            )}
          </Card>
        </Col>
      </Row>

      <EditorModal
        open={editorOpen}
        request={form.getFieldsValue(true)}
        version={editingVersion}
        onClose={() => setEditorOpen(false)}
        onSave={(nextVersion) => {
          updateVersion(nextVersion);
          setEditorOpen(false);
        }}
      />
    </div>
  );
}

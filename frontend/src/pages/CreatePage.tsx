import {
  Alert,
  Button,
  Card,
  Col,
  Divider,
  Form,
  Input,
  InputNumber,
  Row,
  Select,
  Space,
  Tabs,
  Tag,
  Typography,
  message,
} from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { generateCopyStream } from '../api/generate';
import { collectHistory } from '../api/history';
import { getCurrentUser } from '../api/user';
import { EditorModal } from '../components/EditorModal';
import { VersionCard } from '../components/VersionCard';
import { GenerateRequest, GeneratedVersion, QualityScores, UserProfile } from '../types';

const styleOptions = [
  { label: '好物推荐', value: 'good_item' },
  { label: '探店打卡', value: 'visit' },
  { label: '实用教程', value: 'tutorial' },
  { label: 'Vlog日常', value: 'vlog' },
  { label: '合集清单', value: 'collection' },
  { label: '成分测评', value: 'ingredient' },
  { label: '知识分享', value: 'knowledge' },
  { label: '情感故事', value: 'story' },
];

const toneOptions = ['真诚种草', '专业严谨', '幽默风趣', '温柔治愈', '急切按头安利', '冷静分析'];
const conversionGoalOptions = ['提升点击', '促进收藏', '促进评论', '引导私信', '辅助下单'];
const contentGoalOptions = ['点赞', '收藏', '评论', '私信', '下单转化'];
const hookOptions = ['反差开场', '第一人称体验', '清单利益点', '问题切入', '结论先行'];
const structureOptions = ['痛点 → 体验 → 结论', '问题 → 方案 → CTA', '清单 → 场景 → 建议', '对比 → 选择 → 互动'];

interface PrefillState {
  productName?: string;
  coreDescription?: string;
  style?: string;
  tone?: string;
  styleSample?: string;
  requiredKeywords?: string[];
}

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
  strengths: ['策略生成中'],
  complianceIssues: ['检测中'],
});

const createDraftVersion = (verNum: number): GeneratedVersion => ({
  verNum,
  angleLabel: '策略生成中',
  hookType: '待生成',
  strategySummary: '正在分析更适合的爆文切入角度',
  opening: '',
  cta: '',
  title: '',
  titleCandidates: [],
  content: '',
  tags: [],
  publishSuggestions: [],
  prePublishChecks: [],
  qualityScores: createEmptyScores(),
});

export function CreatePage() {
  const [form] = Form.useForm<GenerateRequest>();
  const [versions, setVersions] = useState<GeneratedVersion[]>([]);
  const [loading, setLoading] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();
  const [historyId, setHistoryId] = useState<number | null>(null);
  const [editorOpen, setEditorOpen] = useState(false);
  const [editingVersion, setEditingVersion] = useState<GeneratedVersion | undefined>();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const location = useLocation();

  useEffect(() => {
    getCurrentUser().then(({ data }) => setProfile(data.data));
  }, []);

  useEffect(() => {
    const state = location.state as PrefillState | null;
    if (!state) {
      return;
    }
    form.setFieldsValue({
      productName: state.productName,
      coreDescription: state.coreDescription,
      style: state.style || 'good_item',
      tone: state.tone || '真诚种草',
      styleSample: state.styleSample,
      requiredKeywords: state.requiredKeywords || [],
    });
  }, [form, location.state]);

  const updateVersion = (nextVersion: GeneratedVersion) => {
    setVersions((prev) => prev.map((item) => (item.verNum === nextVersion.verNum ? nextVersion : item)));
  };

  const tabItems = useMemo(
    () =>
      versions.map((version) => ({
        key: String(version.verNum),
        label: `版本 ${version.verNum}`,
        children: (
          <VersionCard
            version={version}
            onOpenEditor={(currentVersion) => {
              setEditingVersion(currentVersion);
              setEditorOpen(true);
            }}
            onCollect={async () => {
              if (!historyId) {
                messageApi.warning('请先完成一次生成后再收藏');
                return;
              }
              await collectHistory(historyId);
              messageApi.success('已收藏到灵感库');
            }}
            onVersionChange={updateVersion}
          />
        ),
      })),
    [historyId, messageApi, versions],
  );

  const handleGenerate = async () => {
    const values = await form.validateFields();
    setLoading(true);
    setVersions([]);
    setHistoryId(null);

    const drafts: Record<number, GeneratedVersion> = {};
    try {
      await generateCopyStream(values, (event, rawData) => {
        if (event === 'strategy') {
          const parsed = JSON.parse(rawData) as Partial<GeneratedVersion> & { ver: number };
          const current = drafts[parsed.ver] || createDraftVersion(parsed.ver);
          drafts[parsed.ver] = {
            ...current,
            angleLabel: parsed.angleLabel || current.angleLabel,
            hookType: parsed.hookType || current.hookType,
            strategySummary: parsed.strategySummary || current.strategySummary,
            opening: parsed.opening || current.opening,
            cta: parsed.cta || current.cta,
          };
          setVersions(Object.values(drafts).sort((a, b) => a.verNum - b.verNum));
        }

        if (event === 'version') {
          const parsed = JSON.parse(rawData) as GeneratedVersion;
          drafts[parsed.verNum] = {
            ...createDraftVersion(parsed.verNum),
            ...drafts[parsed.verNum],
            ...parsed,
          };
          setVersions(Object.values(drafts).sort((a, b) => a.verNum - b.verNum));
        }

        if (event === 'done') {
          const parsed = JSON.parse(rawData) as { history_id?: number };
          if (parsed.history_id) {
            setHistoryId(parsed.history_id);
          }
          messageApi.success('生成完成');
          getCurrentUser().then(({ data }) => setProfile(data.data));
        }

        if (event === 'error') {
          messageApi.error(rawData);
        }
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      {contextHolder}
      <Row gutter={[24, 24]} align="top">
        <Col span={8}>
          <Card title="灵感输入与策略控制台" className="sticky-card">
            <Space direction="vertical" style={{ width: '100%', marginBottom: 16 }}>
              <Space wrap>
                <Tag color={profile?.pro ? 'gold' : 'default'}>
                  {profile?.pro ? '会员用户' : '免费用户'}
                </Tag>
                <Tag color="magenta">
                  {profile?.pro ? '今日不限次数' : `今日剩余 ${profile?.remainingCount ?? '-'} 次`}
                </Tag>
              </Space>
              <Typography.Text type="secondary">
                当前账号：{profile?.nickname || '创作者'}，已使用 {profile?.dailyGenCount ?? 0} 次。
              </Typography.Text>
            </Space>

            <Alert
              type="success"
              showIcon
              style={{ marginBottom: 16 }}
              message="这轮升级为“爆文内容工作台”"
              description="现在不只生成正文，还会同步给出策略切口、标题候选、质量评分和发布前检查。"
            />

            <Form
              form={form}
              layout="vertical"
              initialValues={{
                mode: 'smart',
                style: 'good_item',
                tone: '真诚种草',
                conversionGoal: '促进收藏',
                contentGoal: '收藏',
                hookPreference: '反差开场',
                noteStructure: '痛点 → 体验 → 结论',
                wordCount: 400,
              }}
            >
              <Form.Item name="mode" label="创作模式" rules={[{ required: true }]}>
                <Select
                  options={[
                    { label: '智能生成', value: 'smart' },
                    { label: '拆解生成', value: 'framework' },
                    { label: '笔风仿写', value: 'style_clone' },
                  ]}
                />
              </Form.Item>

              <Divider orientation="left">基础信息</Divider>

              <Form.Item name="productName" label="产品 / 主题">
                <Input placeholder="例如：VC精华 / 探店下午茶 / 护肤懒人流程" />
              </Form.Item>

              <Form.Item
                name="coreDescription"
                label="核心描述"
                rules={[{ required: true, min: 10, message: '最少输入 10 个字' }]}
              >
                <Input.TextArea
                  rows={5}
                  placeholder="输入产品名、卖点、场景，例如：XX精华液，提亮抗暗沉，适合熬夜后快速恢复气色。"
                />
              </Form.Item>

              <Form.Item name="style" label="笔记风格" rules={[{ required: true }]}>
                <Select options={styleOptions} />
              </Form.Item>

              <Form.Item name="coreSellingPoints" label="核心卖点">
                <Select mode="tags" placeholder="例如：提亮快 / 不黏腻 / 性价比高" />
              </Form.Item>

              <Form.Item name="useScenarios" label="使用场景">
                <Select mode="tags" placeholder="例如：通勤前 / 熬夜后 / 出门补妆前" />
              </Form.Item>

              <Form.Item name="targetAudience" label="目标人群">
                <Select mode="tags" placeholder="学生党 / 宝妈 / 职场白领" />
              </Form.Item>

              <Form.Item name="tone" label="语气人设">
                <Select options={toneOptions.map((item) => ({ label: item, value: item }))} />
              </Form.Item>

              <Divider orientation="left">爆文策略</Divider>

              <Form.Item name="conversionGoal" label="转化目标">
                <Select options={conversionGoalOptions.map((item) => ({ label: item, value: item }))} />
              </Form.Item>

              <Form.Item name="contentGoal" label="内容目标">
                <Select options={contentGoalOptions.map((item) => ({ label: item, value: item }))} />
              </Form.Item>

              <Form.Item name="hookPreference" label="钩子偏好">
                <Select options={hookOptions.map((item) => ({ label: item, value: item }))} />
              </Form.Item>

              <Form.Item name="noteStructure" label="结构偏好">
                <Select options={structureOptions.map((item) => ({ label: item, value: item }))} />
              </Form.Item>

              <Form.Item name="wordCount" label="字数要求">
                <InputNumber min={100} max={1000} style={{ width: '100%' }} />
              </Form.Item>

              <Form.Item name="requiredKeywords" label="必须包含关键词">
                <Select mode="tags" placeholder="每个词按回车确认" />
              </Form.Item>

              <Form.Item name="forbiddenExpressions" label="禁用表达 / 避雷词">
                <Select mode="tags" placeholder="例如：全网第一 / 闭眼冲 / 绝对有效" />
              </Form.Item>

              <Form.Item name="referenceUrl" label="参考笔记链接">
                <Input placeholder="粘贴小红书笔记 URL" />
              </Form.Item>

              <Form.Item name="styleSample" label="仿写范文">
                <Input.TextArea rows={4} placeholder="仅在笔风仿写模式下使用" />
              </Form.Item>

              <Space direction="vertical" style={{ width: '100%' }}>
                <Typography.Text type="secondary">
                  左侧配置越具体，右侧产出的策略和文案就越稳定。
                </Typography.Text>
                <Button block type="primary" loading={loading} onClick={handleGenerate}>
                  {loading ? '正在为你策划爆文...' : '开始生成'}
                </Button>
              </Space>
            </Form>
          </Card>
        </Col>

        <Col span={16}>
          <Card
            title="内容生成与爆文工作台"
            extra={
              <Button onClick={handleGenerate} loading={loading}>
                重新生成
              </Button>
            }
          >
            {versions.length ? (
              <Tabs items={tabItems} />
            ) : (
              <div className="empty-panel">
                <Typography.Title level={4}>先把策略信息喂给系统，再拿到更稳的爆文文案</Typography.Title>
                <Typography.Text type="secondary">
                  左侧信息越具体，右侧就越像一位懂转化的内容策划，而不是只会堆字的生成器。
                </Typography.Text>
              </div>
            )}
          </Card>
        </Col>
      </Row>
      <EditorModal open={editorOpen} version={editingVersion} onClose={() => setEditorOpen(false)} />
    </>
  );
}

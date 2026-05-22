import {
  Button,
  Card,
  Col,
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
import { collectHistory } from '../api/history';
import { generateCopyStream } from '../api/generate';
import { getCurrentUser } from '../api/user';
import { EditorModal } from '../components/EditorModal';
import { VersionCard } from '../components/VersionCard';
import { GenerateRequest, GeneratedVersion, UserProfile } from '../types';

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

interface PrefillState {
  coreDescription?: string;
  style?: string;
  tone?: string;
  styleSample?: string;
  requiredKeywords?: string[];
}

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
        if (event === 'title' || event === 'content') {
          const parsed = JSON.parse(rawData) as { ver: number; text: string };
          drafts[parsed.ver] = drafts[parsed.ver] || {
            verNum: parsed.ver,
            title: '',
            content: '',
            tags: [],
            qualityScores: {
              titleAttraction: 4.2,
              keywordDensity: 'good',
              complianceIssues: ['检测中'],
            },
          };
          if (event === 'title') {
            drafts[parsed.ver].title = parsed.text;
          }
          if (event === 'content') {
            drafts[parsed.ver].content = parsed.text.split('\\n').join('\n');
          }
          setVersions(Object.values(drafts).sort((a, b) => a.verNum - b.verNum));
        }
        if (event === 'tags') {
          const parsed = JSON.parse(rawData) as { ver: number; text: string };
          drafts[parsed.ver] = drafts[parsed.ver] || {
            verNum: parsed.ver,
            title: '',
            content: '',
            tags: [],
            qualityScores: {
              titleAttraction: 4.2,
              keywordDensity: 'good',
              complianceIssues: ['检测中'],
            },
          };
          drafts[parsed.ver].tags = parsed.text.split(',').filter(Boolean);
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
          <Card title="灵感输入与操控台" className="sticky-card">
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
            <Form
              form={form}
              layout="vertical"
              initialValues={{
                mode: 'smart',
                style: 'good_item',
                tone: '真诚种草',
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
              <Form.Item
                name="coreDescription"
                label="核心描述"
                rules={[{ required: true, min: 10, message: '最少输入 10 个字' }]}
              >
                <Input.TextArea
                  rows={5}
                  placeholder="输入产品名、卖点、场景，例如：XX精华液，去黄提亮，适合熬夜党"
                />
              </Form.Item>
              <Form.Item name="style" label="笔记风格" rules={[{ required: true }]}>
                <Select options={styleOptions} />
              </Form.Item>
              <Form.Item name="targetAudience" label="目标人群">
                <Select mode="tags" placeholder="学生党 / 宝妈 / 职场白领" />
              </Form.Item>
              <Form.Item name="tone" label="语气人设">
                <Select options={toneOptions.map((item) => ({ label: item, value: item }))} />
              </Form.Item>
              <Form.Item name="wordCount" label="字数要求">
                <InputNumber min={100} max={1000} style={{ width: '100%' }} />
              </Form.Item>
              <Form.Item name="requiredKeywords" label="必须包含关键词">
                <Select mode="tags" placeholder="每个词按回车确认" />
              </Form.Item>
              <Form.Item name="referenceUrl" label="参考笔记链接">
                <Input placeholder="粘贴小红书笔记 URL" />
              </Form.Item>
              <Form.Item name="styleSample" label="仿写范文">
                <Input.TextArea rows={4} placeholder="仅在笔风仿写模式下使用" />
              </Form.Item>
              <Space direction="vertical" style={{ width: '100%' }}>
                <Typography.Text type="secondary">
                  参数变更后，点击生成即可拿到新的 3 版文案。
                </Typography.Text>
                <Button block type="primary" loading={loading} onClick={handleGenerate}>
                  {loading ? '正在为您撰写...' : '开始生成'}
                </Button>
              </Space>
            </Form>
          </Card>
        </Col>
        <Col span={16}>
          <Card title="内容生成与展示台" extra={<Button onClick={handleGenerate}>重新生成</Button>}>
            {versions.length ? (
              <Tabs items={tabItems} />
            ) : (
              <div className="empty-panel">
                <Typography.Title level={4}>输入产品卖点，让 AI 帮你写出爆款笔记</Typography.Title>
                <Typography.Text type="secondary">
                  左侧配置越具体，右侧生成越稳定。
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

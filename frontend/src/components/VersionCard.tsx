import { DownOutlined } from '@ant-design/icons';
import { Alert, Button, Card, Divider, Dropdown, Progress, Space, Tag, Typography, message } from 'antd';
import { purifyContent } from '../api/content';
import { optimizeCopy, reviewGeneratedVersion } from '../api/generate';
import { GenerateRequest, GeneratedVersion } from '../types';

interface Props {
  request?: GenerateRequest;
  version: GeneratedVersion;
  onOpenEditor: (version: GeneratedVersion) => void;
  onCollect: () => void;
  onFinalize?: (version: GeneratedVersion) => void;
  onVersionChange: (version: GeneratedVersion) => void;
}

export function VersionCard({ request, version, onOpenEditor, onCollect, onFinalize, onVersionChange }: Props) {
  const [messageApi, contextHolder] = message.useMessage();

  const qualityMetrics = [
    { label: '标题吸引力', value: version.qualityScores?.titleAttraction },
    { label: '开头钩子', value: version.qualityScores?.hookStrength },
    { label: '卖点清晰度', value: version.qualityScores?.sellingPointClarity },
    { label: '情绪感染力', value: version.qualityScores?.emotionalAppeal },
    { label: '收藏意愿', value: version.qualityScores?.collectIntent },
    { label: '互动潜力', value: version.qualityScores?.interactionPotential },
    { label: '真人感', value: version.qualityScores?.authenticity },
  ];

  const copyPlainText = async () => {
    await navigator.clipboard.writeText(
      `${version.title}\n\n${version.content}\n\n${version.tags.join(' ')}`,
    );
    messageApi.success('复制成功');
  };

  const refreshReview = async (nextVersion: GeneratedVersion) => {
    if (!request) {
      onVersionChange(nextVersion);
      return;
    }
    const { data } = await reviewGeneratedVersion({
      request,
      version: nextVersion,
    });
    onVersionChange(data.data);
  };

  const purify = async () => {
    const { data } = await purifyContent(version.content);
    await refreshReview({
      ...version,
      content: data.data.cleanContent,
      optimizationActions: Array.from(new Set([...(version.optimizationActions || []), 'purify'])),
    });
    messageApi.success(`净化完成，替换 ${data.data.replacedWords.length} 处`);
  };

  const optimize = async (
    option:
      | 'concise'
      | 'rich'
      | 'emoji'
      | 'rewrite_opening'
      | 'stronger_hook'
      | 'more_emotional'
      | 'more_collectible'
      | 'more_natural'
      | 'stronger_cta',
  ) => {
    const { data } = await optimizeCopy({
      option,
      title: version.title,
      content: version.content,
      tags: version.tags,
    });
    await refreshReview({
      ...version,
      title: data.data.title,
      content: data.data.content,
      tags: data.data.tags,
      optimizationActions: Array.from(new Set([...(version.optimizationActions || []), option])),
    });
    messageApi.success('优化完成');
  };

  const applyTitleCandidate = async (title: string) => {
    await refreshReview({
      ...version,
      title,
      optimizationActions: Array.from(new Set([...(version.optimizationActions || []), 'adopt_title_candidate'])),
    });
    messageApi.success('已采用标题候选并重新评分');
  };

  const manualReview = async () => {
    await refreshReview({
      ...version,
      optimizationActions: Array.from(new Set([...(version.optimizationActions || []), 'manual_review'])),
    });
    messageApi.success('发布前检查已刷新');
  };

  return (
    <>
      {contextHolder}
      <Card
        title={`版本 ${version.verNum}`}
        extra={
          <Space size="small" wrap>
            <Tag color="magenta">综合分 {version.qualityScores?.overallScore?.toFixed(1) ?? '-'}</Tag>
            <Tag color="geekblue">关键词 {version.qualityScores?.keywordCoverage ?? '-'}</Tag>
            <Tag
              color={
                version.qualityScores?.riskLevel === 'high'
                  ? 'red'
                  : version.qualityScores?.riskLevel === 'medium'
                    ? 'orange'
                    : 'green'
              }
            >
              风险 {version.qualityScores?.riskLevel ?? '-'}
            </Tag>
          </Space>
        }
      >
        <Space wrap style={{ marginBottom: 12 }}>
          {version.angleLabel ? <Tag color="purple">{version.angleLabel}</Tag> : null}
          {version.hookType ? <Tag color="blue">{version.hookType}</Tag> : null}
          {version.optimizationActions?.length ? (
            <Tag color="gold">已优化 {version.optimizationActions.length} 次</Tag>
          ) : null}
        </Space>

        {version.strategySummary ? (
          <Alert
            type="info"
            showIcon
            message="策略摘要"
            description={version.strategySummary}
            style={{ marginBottom: 16 }}
          />
        ) : null}

        <Typography.Title level={4}>{version.title}</Typography.Title>

        {version.titleCandidates?.length ? (
          <Space direction="vertical" style={{ width: '100%', marginBottom: 16 }}>
            <Typography.Text strong>标题候选</Typography.Text>
            {version.titleCandidates.map((candidate) => (
              <Card
                key={candidate.title}
                size="small"
                extra={
                  <Button type="link" onClick={() => applyTitleCandidate(candidate.title)}>
                    采用并重评
                  </Button>
                }
              >
                <Space direction="vertical" size={4} style={{ width: '100%' }}>
                  <Space wrap>
                    <Tag color="magenta">{candidate.score?.toFixed(1) ?? '-'}</Tag>
                    <Typography.Text strong>{candidate.title}</Typography.Text>
                  </Space>
                  {candidate.reason ? (
                    <Typography.Text type="secondary">{candidate.reason}</Typography.Text>
                  ) : null}
                </Space>
              </Card>
            ))}
          </Space>
        ) : null}

        <Typography.Paragraph style={{ whiteSpace: 'pre-wrap' }}>
          {version.content}
        </Typography.Paragraph>

        {version.cta ? (
          <Alert
            type="success"
            showIcon
            message="互动收口"
            description={version.cta}
            style={{ marginBottom: 16 }}
          />
        ) : null}

        <Space wrap>
          {version.tags.map((tag) => (
            <Tag key={tag}>{tag}</Tag>
          ))}
        </Space>

        <Divider />

        {qualityMetrics.some((item) => typeof item.value === 'number') ? (
          <>
            <Typography.Text strong>质量评分</Typography.Text>
            <div style={{ marginTop: 12, marginBottom: 16 }}>
              {qualityMetrics.map((item) =>
                typeof item.value === 'number' ? (
                  <div key={item.label} style={{ marginBottom: 10 }}>
                    <Typography.Text>{item.label}</Typography.Text>
                    <Progress
                      percent={Math.round((item.value ?? 0) * 20)}
                      size="small"
                      format={() => (item.value ?? 0).toFixed(1)}
                    />
                  </div>
                ) : null,
              )}
            </div>
          </>
        ) : null}

        {version.qualityScores?.strengths?.length ? (
          <>
            <Typography.Text strong>亮点总结</Typography.Text>
            <div style={{ marginTop: 12, marginBottom: 16 }}>
              <Space wrap>
                {version.qualityScores.strengths.map((strength) => (
                  <Tag color="green" key={strength}>
                    {strength}
                  </Tag>
                ))}
              </Space>
            </div>
          </>
        ) : null}

        {version.publishSuggestions?.length ? (
          <>
            <Typography.Text strong>发布建议</Typography.Text>
            <div style={{ marginTop: 12, marginBottom: 16 }}>
              <Space direction="vertical" size={6} style={{ width: '100%' }}>
                {version.publishSuggestions.map((suggestion) => (
                  <Typography.Text key={suggestion}>- {suggestion}</Typography.Text>
                ))}
              </Space>
            </div>
          </>
        ) : null}

        {version.prePublishChecks?.length ? (
          <>
            <Typography.Text strong>发布前检查</Typography.Text>
            <div style={{ marginTop: 12, marginBottom: 16 }}>
              <Space direction="vertical" size={8} style={{ width: '100%' }}>
                {version.prePublishChecks.map((check) => (
                  <Card key={check.label} size="small">
                    <Space direction="vertical" size={4} style={{ width: '100%' }}>
                      <Space wrap>
                        <Tag color={check.status === 'pass' ? 'green' : check.status === 'warn' ? 'orange' : 'red'}>
                          {check.status}
                        </Tag>
                        <Typography.Text strong>{check.label}</Typography.Text>
                      </Space>
                      <Typography.Text type="secondary">{check.detail}</Typography.Text>
                    </Space>
                  </Card>
                ))}
              </Space>
            </div>
          </>
        ) : null}

        {version.qualityScores?.complianceIssues?.length ? (
          <>
            <Typography.Text strong>合规提示</Typography.Text>
            <div style={{ marginTop: 12, marginBottom: 16 }}>
              <Space wrap>
                {version.qualityScores.complianceIssues.map((issue) => (
                  <Tag color={issue.includes('未发现') ? 'green' : 'orange'} key={issue}>
                    {issue}
                  </Tag>
                ))}
              </Space>
            </div>
          </>
        ) : null}

        <Space wrap>
          <Button type="primary" onClick={copyPlainText}>
            一键复制
          </Button>
          <Dropdown
            menu={{
              items: [
                { key: 'concise', label: '精简版（200字内）', onClick: () => optimize('concise') },
                { key: 'rich', label: '丰富版（扩展内容）', onClick: () => optimize('rich') },
                { key: 'emoji', label: '增加 Emoji', onClick: () => optimize('emoji') },
                { key: 'rewrite_opening', label: '重写开头', onClick: () => optimize('rewrite_opening') },
                { key: 'stronger_hook', label: '强化开头钩子', onClick: () => optimize('stronger_hook') },
                { key: 'more_emotional', label: '提升情绪感染力', onClick: () => optimize('more_emotional') },
                { key: 'more_collectible', label: '增强收藏价值', onClick: () => optimize('more_collectible') },
                { key: 'more_natural', label: '降低 AI 味', onClick: () => optimize('more_natural') },
                { key: 'stronger_cta', label: '强化互动收口', onClick: () => optimize('stronger_cta') },
              ],
            }}
          >
            <Button>
              帮我优化 <DownOutlined />
            </Button>
          </Dropdown>
          <Button onClick={manualReview}>一键体检</Button>
          <Button onClick={purify}>内容净化</Button>
          <Button onClick={() => onOpenEditor(version)}>在编辑区打开</Button>
          {onFinalize ? <Button onClick={() => onFinalize(version)}>设为最终采用</Button> : null}
          <Button onClick={onCollect}>收藏至灵感库</Button>
        </Space>
      </Card>
    </>
  );
}

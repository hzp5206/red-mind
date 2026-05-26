import {
  CopyOutlined,
  DownOutlined,
  EditOutlined,
  EyeOutlined,
  LikeOutlined,
  RocketOutlined,
  ScissorOutlined,
  StarOutlined,
} from '@ant-design/icons';
import {
  Alert,
  Button,
  Card,
  Col,
  Divider,
  Dropdown,
  Progress,
  Row,
  Space,
  Statistic,
  Tag,
  Typography,
  message,
} from 'antd';
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

function getRiskColor(riskLevel?: string) {
  if (riskLevel === 'high') {
    return 'red';
  }
  if (riskLevel === 'medium') {
    return 'orange';
  }
  return 'green';
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
    await navigator.clipboard.writeText(`${version.title}\n\n${version.content}\n\n${version.tags.join(' ')}`);
    messageApi.success('已复制文案到剪贴板');
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
    messageApi.success(`内容净化完成，替换 ${data.data.replacedWords.length} 处敏感词`);
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
    messageApi.success('已采用该标题并刷新评分');
  };

  const manualReview = async () => {
    await refreshReview({
      ...version,
      optimizationActions: Array.from(new Set([...(version.optimizationActions || []), 'manual_review'])),
    });
    messageApi.success('评分与发布检查已刷新');
  };

  return (
    <>
      {contextHolder}
      <Card className="version-workspace-card" bordered={false}>
        <div className="version-hero">
          <div className="version-hero-main">
            <Space wrap style={{ marginBottom: 12 }}>
              <Tag color="purple">版本 {version.verNum}</Tag>
              {version.angleLabel ? <Tag color="magenta">{version.angleLabel}</Tag> : null}
              {version.hookType ? <Tag color="blue">{version.hookType}</Tag> : null}
              {version.optimizationActions?.length ? (
                <Tag color="gold">已优化 {version.optimizationActions.length} 次</Tag>
              ) : null}
              <Tag color={getRiskColor(version.qualityScores?.riskLevel)}>
                风险 {version.qualityScores?.riskLevel || 'low'}
              </Tag>
            </Space>
            <Typography.Title level={3} style={{ marginTop: 0, marginBottom: 10 }}>
              {version.title || `版本 ${version.verNum}`}
            </Typography.Title>
            <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
              {version.strategySummary || '系统已根据你的 Brief 组合出当前版本的策略方向。'}
            </Typography.Paragraph>
          </div>
          <div className="version-hero-stats">
            <Statistic title="综合评分" value={version.qualityScores?.overallScore ?? 0} precision={1} suffix="/ 5" />
            <Statistic title="关键词覆盖" value={version.qualityScores?.keywordCoverage || '-'} />
          </div>
        </div>

        <Row gutter={[20, 20]}>
          <Col xs={24} xl={16}>
            <Space direction="vertical" size="middle" style={{ width: '100%' }}>
              <Card className="version-section-card" bordered={false} title="正文与结构">
                {version.opening ? (
                  <Alert
                    type="info"
                    showIcon
                    style={{ marginBottom: 16 }}
                    message="推荐开头"
                    description={version.opening}
                  />
                ) : null}
                <Typography.Paragraph className="version-content-block">
                  {version.content || '正文还在生成中，请稍候。'}
                </Typography.Paragraph>
                {version.cta ? (
                  <Alert
                    type="success"
                    showIcon
                    style={{ marginBottom: 16 }}
                    message="互动收口"
                    description={version.cta}
                  />
                ) : null}
                <Space wrap>
                  {version.tags.map((tag) => (
                    <Tag key={tag}>{tag}</Tag>
                  ))}
                </Space>
              </Card>

              {version.titleCandidates?.length ? (
                <Card className="version-section-card" bordered={false} title="标题候选">
                  <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                    {version.titleCandidates.map((candidate) => (
                      <div key={candidate.title} className="version-title-candidate">
                        <div>
                          <Space wrap style={{ marginBottom: 6 }}>
                            <Tag color="magenta">{candidate.score?.toFixed(1) ?? '--'} 分</Tag>
                            <Typography.Text strong>{candidate.title}</Typography.Text>
                          </Space>
                          {candidate.reason ? (
                            <Typography.Text type="secondary">{candidate.reason}</Typography.Text>
                          ) : null}
                        </div>
                        <Button type="link" onClick={() => applyTitleCandidate(candidate.title)}>
                          采用并重评
                        </Button>
                      </div>
                    ))}
                  </Space>
                </Card>
              ) : null}

              {(version.trendingReferenceTitles?.length ||
                version.referenceTakeaways?.length ||
                version.differentiationTips?.length) ? (
                <Card className="version-section-card" bordered={false} title="爆款参考与差异化建议">
                  {version.trendingReferenceTitles?.length ? (
                    <>
                      <Typography.Text strong>参考样本</Typography.Text>
                      <div style={{ marginTop: 10, marginBottom: 16 }}>
                        <Space wrap>
                          {version.trendingReferenceTitles.map((title) => (
                            <Tag color="volcano" key={title}>
                              {title}
                            </Tag>
                          ))}
                        </Space>
                      </div>
                    </>
                  ) : null}

                  {version.referenceTakeaways?.length ? (
                    <>
                      <Typography.Text strong>可借鉴点</Typography.Text>
                      <div className="version-bullet-list">
                        {version.referenceTakeaways.map((item) => (
                          <Typography.Text key={item}>• {item}</Typography.Text>
                        ))}
                      </div>
                    </>
                  ) : null}

                  {version.differentiationTips?.length ? (
                    <>
                      <Divider />
                      <Typography.Text strong>差异化建议</Typography.Text>
                      <div className="version-bullet-list">
                        {version.differentiationTips.map((item) => (
                          <Typography.Text key={item}>• {item}</Typography.Text>
                        ))}
                      </div>
                    </>
                  ) : null}
                </Card>
              ) : null}

              {version.publishSuggestions?.length ? (
                <Card className="version-section-card" bordered={false} title="发布建议">
                  <div className="version-bullet-list">
                    {version.publishSuggestions.map((suggestion) => (
                      <Typography.Text key={suggestion}>• {suggestion}</Typography.Text>
                    ))}
                  </div>
                </Card>
              ) : null}
            </Space>
          </Col>

          <Col xs={24} xl={8}>
            <Space direction="vertical" size="middle" style={{ width: '100%' }}>
              <Card className="version-section-card" bordered={false} title="快捷操作">
                <Space direction="vertical" size="small" style={{ width: '100%' }}>
                  <Button type="primary" icon={<CopyOutlined />} block onClick={copyPlainText}>
                    一键复制
                  </Button>
                  <Dropdown
                    menu={{
                      items: [
                        { key: 'concise', label: '精简版（200字内）', onClick: () => optimize('concise') },
                        { key: 'rich', label: '丰富版（扩展内容）', onClick: () => optimize('rich') },
                        { key: 'emoji', label: '增加 Emoji', onClick: () => optimize('emoji') },
                        { key: 'rewrite_opening', label: '重写开头', onClick: () => optimize('rewrite_opening') },
                        { key: 'stronger_hook', label: '强化钩子', onClick: () => optimize('stronger_hook') },
                        { key: 'more_emotional', label: '增强情绪感染力', onClick: () => optimize('more_emotional') },
                        { key: 'more_collectible', label: '增强收藏价值', onClick: () => optimize('more_collectible') },
                        { key: 'more_natural', label: '降低 AI 味', onClick: () => optimize('more_natural') },
                        { key: 'stronger_cta', label: '强化互动收口', onClick: () => optimize('stronger_cta') },
                      ],
                    }}
                  >
                    <Button block>
                      帮我优化 <DownOutlined />
                    </Button>
                  </Dropdown>
                  <Button block icon={<EyeOutlined />} onClick={manualReview}>
                    一键体检
                  </Button>
                  <Button block icon={<ScissorOutlined />} onClick={purify}>
                    内容净化
                  </Button>
                  <Button block icon={<EditOutlined />} onClick={() => onOpenEditor(version)}>
                    打开编辑器
                  </Button>
                  {onFinalize ? (
                    <Button block icon={<RocketOutlined />} onClick={() => onFinalize(version)}>
                      设为最终采用
                    </Button>
                  ) : null}
                  <Button block icon={<LikeOutlined />} onClick={onCollect}>
                    收藏到灵感库
                  </Button>
                </Space>
              </Card>

              <Card className="version-section-card" bordered={false} title="质量评分">
                <Space direction="vertical" size="small" style={{ width: '100%' }}>
                  {qualityMetrics.map((item) =>
                    typeof item.value === 'number' ? (
                      <div key={item.label}>
                        <div className="version-metric-head">
                          <Typography.Text>{item.label}</Typography.Text>
                          <Typography.Text type="secondary">{item.value.toFixed(1)} / 5</Typography.Text>
                        </div>
                        <Progress percent={Math.round(item.value * 20)} showInfo={false} strokeColor="#ff6b8a" />
                      </div>
                    ) : null,
                  )}
                </Space>
              </Card>

              {version.prePublishChecks?.length ? (
                <Card className="version-section-card" bordered={false} title="发布前检查">
                  <Space direction="vertical" size="small" style={{ width: '100%' }}>
                    {version.prePublishChecks.map((check) => (
                      <Alert
                        key={check.label}
                        type={check.status === 'pass' ? 'success' : check.status === 'warn' ? 'warning' : 'error'}
                        showIcon
                        message={check.label}
                        description={check.detail}
                      />
                    ))}
                  </Space>
                </Card>
              ) : null}

              {(version.qualityScores?.strengths?.length || version.qualityScores?.complianceIssues?.length) ? (
                <Card className="version-section-card" bordered={false} title="亮点与风险">
                  {version.qualityScores?.strengths?.length ? (
                    <>
                      <Typography.Text strong>当前亮点</Typography.Text>
                      <div style={{ marginTop: 10, marginBottom: 16 }}>
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

                  {version.qualityScores?.complianceIssues?.length ? (
                    <>
                      <Typography.Text strong>合规提示</Typography.Text>
                      <div style={{ marginTop: 10 }}>
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
                </Card>
              ) : null}
            </Space>
          </Col>
        </Row>
      </Card>
    </>
  );
}

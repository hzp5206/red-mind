import { Alert, Button, Card, Col, Input, Modal, Row, Space, Tag, Typography, message } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { reviewGeneratedVersion } from '../api/generate';
import { GenerateRequest, GeneratedVersion } from '../types';

interface Props {
  open: boolean;
  request?: GenerateRequest;
  version?: GeneratedVersion;
  onClose: () => void;
  onSave: (version: GeneratedVersion) => void;
}

export function EditorModal({ open, request, version, onClose, onSave }: Props) {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [tags, setTags] = useState('');
  const [reviewing, setReviewing] = useState(false);
  const [draftVersion, setDraftVersion] = useState<GeneratedVersion | undefined>();
  const [messageApi, contextHolder] = message.useMessage();

  useEffect(() => {
    setTitle(version?.title || '');
    setContent(version?.content || '');
    setTags((version?.tags || []).join(' '));
    setDraftVersion(version);
  }, [version]);

  const wordCount = useMemo(() => content.replace(/\s+/g, '').length, [content]);

  const currentVersion = useMemo<GeneratedVersion | undefined>(() => {
    if (!draftVersion) {
      return undefined;
    }
    return {
      ...draftVersion,
      title,
      content,
      tags: tags.split(/\s+/).filter(Boolean),
    };
  }, [content, draftVersion, tags, title]);

  const applyCandidateTitle = (nextTitle: string) => {
    setTitle(nextTitle);
  };

  const handleReview = async () => {
    if (!request || !currentVersion) {
      return;
    }
    setReviewing(true);
    try {
      const { data } = await reviewGeneratedVersion({
        request,
        version: {
          ...currentVersion,
          optimizationActions: [...(currentVersion.optimizationActions || []), 'manual_edit', 'review_refresh'],
        },
      });
      setDraftVersion(data.data);
      setTitle(data.data.title);
      setContent(data.data.content);
      setTags((data.data.tags || []).join(' '));
      messageApi.success('评分、发布建议和检查项已刷新');
    } finally {
      setReviewing(false);
    }
  };

  const handleSave = () => {
    if (!currentVersion) {
      return;
    }
    const mergedVersion: GeneratedVersion = {
      ...currentVersion,
      optimizationActions: Array.from(
        new Set([...(currentVersion.optimizationActions || []), 'manual_edit', 'saved_from_editor']),
      ),
    };
    onSave(mergedVersion);
    messageApi.success('当前修改已同步到该版本');
  };

  return (
    <Modal
      open={open}
      onCancel={onClose}
      onOk={handleSave}
      width={1280}
      title="沉浸式编辑器"
      okText="保存当前修改"
      cancelText="关闭"
      destroyOnClose
    >
      {contextHolder}
      <Row gutter={24}>
        <Col span={14}>
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            <Card className="version-section-card" bordered={false}>
              <div className="create-card-header">
                <div>
                  <Typography.Title level={5} style={{ margin: 0 }}>
                    手动精修区
                  </Typography.Title>
                  <Typography.Text type="secondary">
                    修改标题、正文和标签后，可以一键刷新评分与发布建议。
                  </Typography.Text>
                </div>
                <Button onClick={handleReview} loading={reviewing}>
                  刷新评分
                </Button>
              </div>

              {currentVersion?.titleCandidates?.length ? (
                <Card size="small" title="标题候选" style={{ marginBottom: 16 }}>
                  <Space direction="vertical" style={{ width: '100%' }}>
                    {currentVersion.titleCandidates.map((candidate) => (
                      <Card
                        size="small"
                        key={candidate.title}
                        extra={
                          <Button type="link" onClick={() => applyCandidateTitle(candidate.title)}>
                            采用
                          </Button>
                        }
                      >
                        <Space direction="vertical" size={4} style={{ width: '100%' }}>
                          <Space wrap>
                            <Tag color="magenta">{candidate.score?.toFixed(1) ?? '--'} 分</Tag>
                            <Typography.Text strong>{candidate.title}</Typography.Text>
                          </Space>
                          {candidate.reason ? <Typography.Text type="secondary">{candidate.reason}</Typography.Text> : null}
                        </Space>
                      </Card>
                    ))}
                  </Space>
                </Card>
              ) : null}

              <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                <div>
                  <Typography.Text strong>标题</Typography.Text>
                  <Input value={title} onChange={(event) => setTitle(event.target.value)} />
                </div>

                <div>
                  <Typography.Text strong>正文</Typography.Text>
                  <Input.TextArea rows={18} value={content} onChange={(event) => setContent(event.target.value)} />
                </div>

                <div>
                  <Typography.Text strong>标签</Typography.Text>
                  <Input
                    value={tags}
                    onChange={(event) => setTags(event.target.value)}
                    placeholder="#职场护肤 #熬夜急救 #妆前提亮"
                  />
                </div>

                <Typography.Text type="secondary">当前字数：{wordCount}</Typography.Text>
              </Space>
            </Card>
          </Space>
        </Col>

        <Col span={10}>
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            <Card className="version-section-card" bordered={false} title="实时预览">
              <Typography.Title level={4} style={{ marginTop: 0 }}>
                {title || '标题预览'}
              </Typography.Title>
              <Typography.Paragraph style={{ whiteSpace: 'pre-wrap' }}>
                {content || '正文预览区域'}
              </Typography.Paragraph>
              <Space wrap>
                {tags.split(/\s+/).filter(Boolean).map((tag) => (
                  <Tag key={tag}>{tag}</Tag>
                ))}
              </Space>
            </Card>

            {currentVersion?.qualityScores ? (
              <Alert
                type="info"
                showIcon
                message={`综合评分：${currentVersion.qualityScores.overallScore?.toFixed(1) ?? '-'}`}
                description={`风险等级：${currentVersion.qualityScores.riskLevel ?? '-'}；关键词覆盖：${currentVersion.qualityScores.keywordCoverage ?? '-'}`}
              />
            ) : null}

            {currentVersion?.publishSuggestions?.length ? (
              <Card className="version-section-card" bordered={false} title="发布建议">
                <Space direction="vertical" size={6} style={{ width: '100%' }}>
                  {currentVersion.publishSuggestions.map((item) => (
                    <Typography.Text key={item}>• {item}</Typography.Text>
                  ))}
                </Space>
              </Card>
            ) : null}

            {currentVersion?.prePublishChecks?.length ? (
              <Card className="version-section-card" bordered={false} title="发布前检查">
                <Space direction="vertical" size={8} style={{ width: '100%' }}>
                  {currentVersion.prePublishChecks.map((check) => (
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
          </Space>
        </Col>
      </Row>
    </Modal>
  );
}

import { Col, Input, Modal, Row, Space, Tag, Typography } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { GeneratedVersion } from '../types';

interface Props {
  open: boolean;
  version?: GeneratedVersion;
  onClose: () => void;
}

export function EditorModal({ open, version, onClose }: Props) {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');

  useEffect(() => {
    setTitle(version?.title || '');
    setContent(version?.content || '');
  }, [version]);

  const wordCount = useMemo(() => content.replace(/\s+/g, '').length, [content]);

  return (
    <Modal
      open={open}
      onCancel={onClose}
      onOk={onClose}
      width={1200}
      title="沉浸式编辑器"
      okText="完成编辑"
      cancelText="关闭"
    >
      <Row gutter={24}>
        <Col span={12}>
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            <div>
              <Typography.Text strong>标题</Typography.Text>
              <Input value={title} onChange={(event) => setTitle(event.target.value)} />
            </div>
            <div>
              <Typography.Text strong>Markdown / 文案源码</Typography.Text>
              <Input.TextArea
                rows={20}
                value={content}
                onChange={(event) => setContent(event.target.value)}
              />
            </div>
            <Typography.Text type="secondary">当前字数：{wordCount}</Typography.Text>
          </Space>
        </Col>
        <Col span={12}>
          <div className="preview-panel">
            <Typography.Title level={4} style={{ marginTop: 0 }}>
              {title || '标题预览'}
            </Typography.Title>
            <div className="preview-image-placeholder">图片占位区</div>
            <Typography.Paragraph style={{ whiteSpace: 'pre-wrap' }}>
              {content || '正文预览区域'}
            </Typography.Paragraph>
            <Space wrap>
              {(version?.tags || []).map((tag) => (
                <Tag key={tag}>{tag}</Tag>
              ))}
            </Space>
          </div>
        </Col>
      </Row>
    </Modal>
  );
}

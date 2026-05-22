import { DownOutlined } from '@ant-design/icons';
import { Button, Card, Divider, Dropdown, Space, Tag, Typography, message } from 'antd';
import { purifyContent } from '../api/content';
import { optimizeCopy } from '../api/generate';
import { GeneratedVersion } from '../types';

interface Props {
  version: GeneratedVersion;
  onOpenEditor: (version: GeneratedVersion) => void;
  onCollect: () => void;
  onVersionChange: (version: GeneratedVersion) => void;
}

export function VersionCard({ version, onOpenEditor, onCollect, onVersionChange }: Props) {
  const [messageApi, contextHolder] = message.useMessage();

  const copyPlainText = async () => {
    await navigator.clipboard.writeText(
      `${version.title}\n\n${version.content}\n\n${version.tags.join(' ')}`,
    );
    messageApi.success('复制成功');
  };

  const purify = async () => {
    const { data } = await purifyContent(version.content);
    onVersionChange({
      ...version,
      content: data.data.cleanContent,
    });
    messageApi.success(`净化完成，替换 ${data.data.replacedWords.length} 处`);
  };

  const optimize = async (option: 'concise' | 'rich' | 'emoji' | 'rewrite_opening') => {
    const { data } = await optimizeCopy({
      option,
      title: version.title,
      content: version.content,
      tags: version.tags,
    });
    onVersionChange({
      ...version,
      title: data.data.title,
      content: data.data.content,
      tags: data.data.tags,
    });
    messageApi.success('优化完成');
  };

  return (
    <>
      {contextHolder}
      <Card
        title={`版本 ${version.verNum}`}
        extra={
          <Space size="small">
            <Tag color="magenta">标题分 {version.qualityScores?.titleAttraction ?? '-'}</Tag>
            <Tag color="geekblue">关键词 {version.qualityScores?.keywordDensity ?? '-'}</Tag>
          </Space>
        }
      >
        <Typography.Title level={4}>{version.title}</Typography.Title>
        <Typography.Paragraph style={{ whiteSpace: 'pre-wrap' }}>
          {version.content}
        </Typography.Paragraph>
        <Space wrap>
          {version.tags.map((tag) => (
            <Tag key={tag}>{tag}</Tag>
          ))}
        </Space>
        <Divider />
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
                { key: 'rewrite_opening', label: '更换开头', onClick: () => optimize('rewrite_opening') },
              ],
            }}
          >
            <Button>
              帮我优化 <DownOutlined />
            </Button>
          </Dropdown>
          <Button onClick={purify}>内容净化</Button>
          <Button onClick={() => onOpenEditor(version)}>在编辑区打开</Button>
          <Button onClick={onCollect}>收藏至灵感库</Button>
        </Space>
      </Card>
    </>
  );
}

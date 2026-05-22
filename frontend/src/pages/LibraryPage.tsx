import { Button, Card, Col, Empty, Input, Modal, Popconfirm, Row, Segmented, Space, Tag, Typography, message } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { deleteCollection, getMyCollections } from '../api/library';
import { getTemplates } from '../api/template';
import { getTemplateCategories } from '../api/templateCategory';
import { GeneratedVersion, LibraryItem, TemplateItem } from '../types';

function extractPreview(results: string) {
  try {
    const versions = JSON.parse(results) as GeneratedVersion[];
    return versions[0]?.content || results;
  } catch {
    return results;
  }
}

export function LibraryPage() {
  const [templates, setTemplates] = useState<TemplateItem[]>([]);
  const [collections, setCollections] = useState<LibraryItem[]>([]);
  const [categoryMap, setCategoryMap] = useState<Record<string, string>>({});
  const [mode, setMode] = useState<'templates' | 'collections'>('templates');
  const [keyword, setKeyword] = useState('');
  const [messageApi, contextHolder] = message.useMessage();
  const [detail, setDetail] = useState<LibraryItem | null>(null);
  const navigate = useNavigate();

  const loadCollections = () => {
    getMyCollections().then(({ data }) => setCollections(data.data));
  };

  useEffect(() => {
    getTemplates().then(({ data }) => setTemplates(data.data));
    getTemplateCategories().then(({ data }) =>
      setCategoryMap(
        data.data.reduce<Record<string, string>>((acc, item) => {
          acc[item.value] = item.label;
          return acc;
        }, {}),
      ),
    );
    loadCollections();
  }, []);

  const currentList = useMemo(() => {
    if (mode === 'templates') {
      return templates.filter((item) =>
        [item.title, item.contentExample, item.tags].join(' ').toLowerCase().includes(keyword.toLowerCase()),
      );
    }
    return collections.filter((item) =>
      [item.coreInput, item.customTags, item.style].join(' ').toLowerCase().includes(keyword.toLowerCase()),
    );
  }, [collections, keyword, mode, templates]);

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      {contextHolder}
      <Space style={{ width: '100%', justifyContent: 'space-between' }} wrap>
        <Typography.Title level={3} style={{ marginBottom: 0 }}>
          文案灵感库
        </Typography.Title>
        <Space wrap>
          <Input.Search
            allowClear
            placeholder="搜索模板、标签或收藏内容"
            style={{ width: 260 }}
            onSearch={setKeyword}
            onChange={(event) => setKeyword(event.target.value)}
          />
          <Segmented
            value={mode}
            options={[
              { label: '系统模板', value: 'templates' },
              { label: '我的收藏', value: 'collections' },
            ]}
            onChange={(value) => setMode(value as 'templates' | 'collections')}
          />
        </Space>
      </Space>

      {!currentList.length ? (
        <Empty description={mode === 'templates' ? '暂无系统模板' : '你还没有收藏内容'} />
      ) : (
        <Row gutter={[16, 16]}>
          {mode === 'templates' &&
            (currentList as TemplateItem[]).map((item) => (
              <Col span={8} key={item.id}>
                <Card
                  title={item.title}
                  extra={
                    <Button
                      type="link"
                      onClick={() =>
                        navigate('/create', {
                          state: {
                            coreDescription: item.contentExample,
                            style: item.style,
                            requiredKeywords: item.tags.split(',').filter(Boolean),
                          },
                        })
                      }
                    >
                      引用生成
                    </Button>
                  }
                >
                  <Typography.Paragraph ellipsis={{ rows: 4 }}>
                    {item.contentExample}
                  </Typography.Paragraph>
                  <Space wrap>
                    <Tag color="magenta">{categoryMap[item.category] || item.category}</Tag>
                    <Tag>{item.style}</Tag>
                    {item.tags.split(',').map((tag) => (
                      <Tag key={tag}>{tag}</Tag>
                    ))}
                  </Space>
                </Card>
              </Col>
            ))}

          {mode === 'collections' &&
            (currentList as LibraryItem[]).map((item) => (
              <Col span={8} key={item.id}>
                <Card
                  title={item.coreInput || '已收藏文案'}
                  extra={
                    <Space>
                      <Button
                        type="link"
                        onClick={() =>
                          navigate('/create', {
                            state: {
                              coreDescription: item.coreInput,
                              style: item.style,
                            },
                          })
                        }
                      >
                        使用此模板
                      </Button>
                      <Button type="link" onClick={() => setDetail(item)}>
                        详情
                      </Button>
                    </Space>
                  }
                >
                  <Typography.Paragraph type="secondary">
                    收藏时间：{item.createdAt}
                  </Typography.Paragraph>
                  <Typography.Paragraph ellipsis={{ rows: 4 }}>
                    {extractPreview(item.results)}
                  </Typography.Paragraph>
                  <Space wrap>
                    <Tag color="purple">{item.customTags}</Tag>
                    <Tag>{item.style || '未分类'}</Tag>
                  </Space>
                  <div style={{ marginTop: 12 }}>
                    <Popconfirm
                      title="确认删除这条收藏吗？"
                      onConfirm={async () => {
                        await deleteCollection(item.id);
                        messageApi.success('删除成功');
                        loadCollections();
                      }}
                    >
                      <Button danger type="link" style={{ paddingLeft: 0 }}>
                        删除收藏
                      </Button>
                    </Popconfirm>
                  </div>
                </Card>
              </Col>
            ))}
        </Row>
      )}

      <Modal
        open={!!detail}
        onCancel={() => setDetail(null)}
        footer={null}
        title="收藏详情"
        width={820}
      >
        <Typography.Title level={5}>{detail?.coreInput}</Typography.Title>
        <Typography.Paragraph style={{ whiteSpace: 'pre-wrap' }}>
          {detail ? extractPreview(detail.results) : ''}
        </Typography.Paragraph>
      </Modal>
    </Space>
  );
}

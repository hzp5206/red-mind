import { Alert, Button, DatePicker, Modal, Popconfirm, Select, Space, Table, Tag, Typography, message } from 'antd';
import { ColumnsType } from 'antd/es/table';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { collectHistory, deleteHistory, getHistory } from '../api/history';
import { GeneratedVersion, HistoryRecord } from '../types';

const { RangePicker } = DatePicker;

const styleOptions = [
  { label: '全部风格', value: '' },
  { label: '好物推荐', value: 'good_item' },
  { label: '探店打卡', value: 'visit' },
  { label: '实用教程', value: 'tutorial' },
  { label: 'Vlog日常', value: 'vlog' },
  { label: '合集清单', value: 'collection' },
];

function parseVersions(results?: string) {
  if (!results) {
    return [] as GeneratedVersion[];
  }
  try {
    return JSON.parse(results) as GeneratedVersion[];
  } catch {
    return [];
  }
}

export function HistoryPage() {
  const [records, setRecords] = useState<HistoryRecord[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [detail, setDetail] = useState<HistoryRecord | null>(null);
  const [messageApi, contextHolder] = message.useMessage();
  const [style, setStyle] = useState('');
  const [dateRange, setDateRange] = useState<[string, string] | null>(null);
  const navigate = useNavigate();

  const fetchData = async () => {
    setLoading(true);
    try {
      const { data } = await getHistory({
        page: 1,
        limit: 20,
        style: style || undefined,
        startDate: dateRange?.[0],
        endDate: dateRange?.[1],
      });
      setRecords(data.data.records);
      setTotal(data.data.total);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [style, dateRange]);

  const columns: ColumnsType<HistoryRecord> = useMemo(
    () => [
      { title: '生成时间', dataIndex: 'createdAt', width: 180 },
      { title: '核心描述摘要', dataIndex: 'coreInput', ellipsis: true },
      { title: '风格', dataIndex: 'style', width: 140, render: (value) => <Tag>{value}</Tag> },
      {
        title: '最终采用',
        width: 120,
        render: (_, record) =>
          record.finalTitle ? <Tag color="green">已沉淀</Tag> : <Tag color="default">未设置</Tag>,
      },
      { title: '字数', dataIndex: 'wordCount', width: 100 },
      {
        title: '操作',
        width: 320,
        render: (_, record) => (
          <Space>
            <Button type="link" onClick={() => setDetail(record)}>
              查看详情
            </Button>
            <Button
              type="link"
              onClick={() =>
                navigate('/create', {
                  state: {
                    coreDescription: record.coreInput,
                    style: record.style,
                    tone: record.persona,
                  },
                })
              }
            >
              再次生成
            </Button>
            <Button
              type="link"
              onClick={async () => {
                await collectHistory(record.id);
                messageApi.success('收藏成功');
                fetchData();
              }}
            >
              收藏
            </Button>
            <Popconfirm
              title="确认删除这条记录吗？"
              onConfirm={async () => {
                await deleteHistory(record.id);
                messageApi.success('删除成功');
                fetchData();
              }}
            >
              <Button type="link" danger>
                删除
              </Button>
            </Popconfirm>
          </Space>
        ),
      },
    ],
    [messageApi, navigate],
  );

  const versions: GeneratedVersion[] = parseVersions(detail?.results);
  const finalVersion: GeneratedVersion | null = detail?.finalResult ? JSON.parse(detail.finalResult) : null;

  const copyAll = async () => {
    const text = versions
      .map((item) => `${item.title}\n\n${item.content}\n\n${item.tags.join(' ')}`)
      .join('\n\n-----------------\n\n');
    await navigator.clipboard.writeText(text);
    messageApi.success('复制成功');
  };

  return (
    <>
      {contextHolder}
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <Typography.Title level={3}>我的历史</Typography.Title>
        <Space wrap>
          <Select style={{ width: 180 }} value={style} options={styleOptions} onChange={setStyle} />
          <RangePicker
            onChange={(dates) =>
              setDateRange(
                dates ? [dates[0]?.format('YYYY-MM-DD') || '', dates[1]?.format('YYYY-MM-DD') || ''] : null,
              )
            }
          />
          <Button
            onClick={() => {
              setStyle('');
              setDateRange(null);
            }}
          >
            重置筛选
          </Button>
        </Space>
        <Table
          rowKey="id"
          columns={columns}
          dataSource={records}
          loading={loading}
          pagination={{ total, pageSize: 20 }}
        />
      </Space>
      <Modal
        open={!!detail}
        onCancel={() => setDetail(null)}
        footer={null}
        width={960}
        title="生成详情"
      >
        <Space style={{ marginBottom: 16 }} wrap>
          <Button onClick={copyAll}>复制全部</Button>
          <Button
            onClick={() =>
              detail &&
              navigate('/create', {
                state: {
                  coreDescription: detail.coreInput,
                  style: detail.style,
                  tone: detail.persona,
                },
              })
            }
          >
            再次生成
          </Button>
        </Space>

        {finalVersion ? (
          <Alert
            type="success"
            showIcon
            style={{ marginBottom: 16 }}
            message={`最终采用版本：${detail?.finalTitle || finalVersion.title}`}
            description={`最终评分：${detail?.finalScore?.toFixed(1) ?? '-'}${detail?.lastModifiedAt ? `；最后更新：${detail.lastModifiedAt}` : ''}`}
          />
        ) : null}

        <Space direction="vertical" style={{ width: '100%' }}>
          {versions.map((version) => (
            <div key={version.verNum} className="history-version-block">
              <Typography.Title level={5}>
                {version.title}
                {finalVersion?.verNum === version.verNum ? <Tag color="green" style={{ marginLeft: 8 }}>最终采用</Tag> : null}
              </Typography.Title>
              <Typography.Paragraph style={{ whiteSpace: 'pre-wrap' }}>
                {version.content}
              </Typography.Paragraph>
              <Space wrap>
                {version.tags.map((tag) => (
                  <Tag key={tag}>{tag}</Tag>
                ))}
              </Space>
            </div>
          ))}
        </Space>
      </Modal>
    </>
  );
}

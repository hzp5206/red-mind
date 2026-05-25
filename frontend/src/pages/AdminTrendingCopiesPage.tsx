import {
  Button,
  Card,
  Col,
  Descriptions,
  Form,
  Input,
  InputNumber,
  Modal,
  Popconfirm,
  Row,
  Select,
  Space,
  Statistic,
  Switch,
  Table,
  Tag,
  Typography,
  message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  collectTrendingItem,
  deleteTrendingTask,
  getTrendingAnalysis,
  getTrendingDashboard,
  getTrendingItems,
  getTrendingTasks,
  saveTrendingTask,
  triggerTrendingTask,
} from '../api/adminTrendingCopy';
import { TrendingAnalysis, TrendingDashboard, TrendingItem, TrendingTask } from '../types';
import { hasPermission } from '../utils/auth';

const platformOptions = [{ label: '小红书', value: 'xiaohongshu' }];
const providerOptions = [{ label: 'Mock Provider', value: 'mock' }];

export function AdminTrendingCopiesPage() {
  const [dashboard, setDashboard] = useState<TrendingDashboard | null>(null);
  const [tasks, setTasks] = useState<TrendingTask[]>([]);
  const [items, setItems] = useState<TrendingItem[]>([]);
  const [itemsTotal, setItemsTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [itemLoading, setItemLoading] = useState(false);
  const [taskOpen, setTaskOpen] = useState(false);
  const [analysisOpen, setAnalysisOpen] = useState(false);
  const [analysisLoading, setAnalysisLoading] = useState(false);
  const [analysis, setAnalysis] = useState<TrendingAnalysis | null>(null);
  const [editingTask, setEditingTask] = useState<TrendingTask | null>(null);
  const [keyword, setKeyword] = useState('');
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [form] = Form.useForm();
  const [messageApi, contextHolder] = message.useMessage();
  const navigate = useNavigate();

  const loadDashboard = async () => {
    const { data } = await getTrendingDashboard();
    setDashboard(data.data);
  };

  const loadTasks = async () => {
    const { data } = await getTrendingTasks();
    setTasks(data.data);
  };

  const loadItems = async () => {
    setItemLoading(true);
    try {
      const { data } = await getTrendingItems({
        keyword: keyword || undefined,
        platformCode: 'xiaohongshu',
        page,
        pageSize,
      });
      setItems(data.data.records);
      setItemsTotal(data.data.total);
    } finally {
      setItemLoading(false);
    }
  };

  const refreshAll = async () => {
    setLoading(true);
    try {
      await Promise.all([loadDashboard(), loadTasks(), loadItems()]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refreshAll();
  }, []);

  useEffect(() => {
    loadItems();
  }, [keyword, page, pageSize]);

  const openCreate = () => {
    setEditingTask(null);
    form.resetFields();
    form.setFieldsValue({
      platformCode: 'xiaohongshu',
      providerCode: 'mock',
      fetchLimit: 12,
      cronExpr: '0 0 8 * * ?',
      enabled: true,
    });
    setTaskOpen(true);
  };

  const openEdit = (task: TrendingTask) => {
    setEditingTask(task);
    form.setFieldsValue(task);
    setTaskOpen(true);
  };

  const submitTask = async () => {
    const values = await form.validateFields();
    await saveTrendingTask({
      ...values,
      id: editingTask?.id,
    });
    messageApi.success(editingTask ? '采集任务已更新' : '采集任务已创建');
    setTaskOpen(false);
    refreshAll();
  };

  const fetchAnalysis = async (item: TrendingItem) => {
    setAnalysisLoading(true);
    try {
      const { data } = await getTrendingAnalysis(item.id);
      setAnalysis(data.data);
      return data.data;
    } finally {
      setAnalysisLoading(false);
    }
  };

  const handleQuoteGenerate = async (item: TrendingItem) => {
    const itemAnalysis = await fetchAnalysis(item);
    navigate('/create', {
      state: {
        sourceLabel: `来自爆文采集：${item.title}`,
        productName: itemAnalysis.productHint || item.keyword || item.title,
        coreDescription: itemAnalysis.coreDescription,
        style: itemAnalysis.recommendedStyle,
        tone: itemAnalysis.recommendedTone,
        styleSample: itemAnalysis.styleSample,
        requiredKeywords: itemAnalysis.requiredKeywords,
        coreSellingPoints: itemAnalysis.collectPoints,
        hookPreference: itemAnalysis.recommendedHook,
        noteStructure: itemAnalysis.recommendedStructure,
        contentGoal: '收藏',
        conversionGoal: '促进收藏',
      },
    });
  };

  const taskColumns: ColumnsType<TrendingTask> = useMemo(
    () => [
      { title: '任务名称', dataIndex: 'taskName', width: 180 },
      { title: '平台', dataIndex: 'platformCode', width: 120, render: (value) => <Tag>{value}</Tag> },
      { title: '关键词', dataIndex: 'keywords', ellipsis: true },
      { title: '数量', dataIndex: 'fetchLimit', width: 90 },
      { title: 'Provider', dataIndex: 'providerCode', width: 120 },
      {
        title: '状态',
        width: 120,
        render: (_, record) => (
          <Tag color={record.lastStatus === 'success' ? 'green' : record.lastStatus === 'failed' ? 'red' : 'default'}>
            {record.lastStatus || 'idle'}
          </Tag>
        ),
      },
      { title: '上次执行', dataIndex: 'lastRunAt', width: 180 },
      {
        title: '启用',
        dataIndex: 'enabled',
        width: 100,
        render: (value) => <Switch checked={value} disabled />,
      },
      {
        title: '操作',
        width: 260,
        render: (_, record) => (
          <Space>
            <Button type="link" disabled={!hasPermission('trending_copy:manage')} onClick={() => openEdit(record)}>
              编辑
            </Button>
            <Button
              type="link"
              disabled={!hasPermission('trending_copy:manage')}
              onClick={async () => {
                const { data } = await triggerTrendingTask(record.id);
                messageApi.success(`执行成功，本次新增 ${data.data} 条样本`);
                refreshAll();
              }}
            >
              立即执行
            </Button>
            <Popconfirm
              title="确认删除这个采集任务吗？"
              onConfirm={async () => {
                await deleteTrendingTask(record.id);
                messageApi.success('删除成功');
                refreshAll();
              }}
            >
              <Button type="link" danger disabled={!hasPermission('trending_copy:manage')}>
                删除
              </Button>
            </Popconfirm>
          </Space>
        ),
      },
    ],
    [messageApi],
  );

  const itemColumns: ColumnsType<TrendingItem> = useMemo(
    () => [
      { title: '标题', dataIndex: 'title', width: 260 },
      { title: '关键词', dataIndex: 'keyword', width: 120, render: (value) => <Tag color="blue">{value || '未标注'}</Tag> },
      { title: '作者', dataIndex: 'authorName', width: 120 },
      { title: '点赞', dataIndex: 'likesCount', width: 90 },
      { title: '收藏', dataIndex: 'collectsCount', width: 90 },
      { title: '评论', dataIndex: 'commentsCount', width: 90 },
      { title: '热度', dataIndex: 'heatScore', width: 100 },
      { title: '抓取时间', dataIndex: 'fetchedAt', width: 180 },
      {
        title: '操作',
        key: 'actions',
        width: 260,
        fixed: 'right',
        render: (_, record) => (
          <Space wrap>
            <Button
              type="link"
              onClick={async () => {
                await fetchAnalysis(record);
                setAnalysisOpen(true);
              }}
            >
              查看拆解
            </Button>
            <Button type="link" onClick={() => handleQuoteGenerate(record)}>
              引用生成
            </Button>
            <Button
              type="link"
              onClick={async () => {
                await collectTrendingItem(record.id);
                messageApi.success('已加入灵感库');
              }}
            >
              加入灵感库
            </Button>
          </Space>
        ),
      },
    ],
    [messageApi],
  );

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      {contextHolder}
      <Space style={{ width: '100%', justifyContent: 'space-between' }} wrap>
        <Typography.Title level={3} style={{ marginBottom: 0 }}>
          爆文采集中心
        </Typography.Title>
        <Space wrap>
          <Input.Search
            allowClear
            placeholder="搜索标题、关键词"
            style={{ width: 240 }}
            onSearch={(value) => {
              setKeyword(value);
              setPage(1);
            }}
            onChange={(event) => {
              setKeyword(event.target.value);
              setPage(1);
            }}
          />
          <Button onClick={refreshAll} loading={loading}>
            刷新数据
          </Button>
          <Button type="primary" onClick={openCreate} disabled={!hasPermission('trending_copy:manage')}>
            新建采集任务
          </Button>
        </Space>
      </Space>

      <Row gutter={[16, 16]}>
        <Col span={6}>
          <Card><Statistic title="任务总数" value={dashboard?.taskCount ?? 0} /></Card>
        </Col>
        <Col span={6}>
          <Card><Statistic title="启用任务" value={dashboard?.enabledTaskCount ?? 0} /></Card>
        </Col>
        <Col span={6}>
          <Card><Statistic title="样本总数" value={dashboard?.totalItemCount ?? 0} /></Card>
        </Col>
        <Col span={6}>
          <Card><Statistic title="今日新增" value={dashboard?.todayFetchedCount ?? 0} /></Card>
        </Col>
      </Row>

      <Card title="采集任务列表">
        <Table rowKey="id" dataSource={tasks} columns={taskColumns} pagination={false} scroll={{ x: 1100 }} />
      </Card>

      <Card title="最新爆文样本">
        <Table
          rowKey="id"
          dataSource={items}
          columns={itemColumns}
          loading={itemLoading}
          scroll={{ x: 1450 }}
          pagination={{
            current: page,
            pageSize,
            total: itemsTotal,
            onChange: (nextPage, nextPageSize) => {
              setPage(nextPage);
              setPageSize(nextPageSize);
            },
          }}
          expandable={{
            expandedRowRender: (record) => (
              <Space direction="vertical" style={{ width: '100%' }}>
                <Typography.Text strong>{record.title}</Typography.Text>
                <Typography.Paragraph style={{ whiteSpace: 'pre-wrap', marginBottom: 0 }}>
                  {record.contentText || '暂无正文样本'}
                </Typography.Paragraph>
                <Space wrap>
                  {record.tags.map((tag) => (
                    <Tag key={tag}>{tag}</Tag>
                  ))}
                </Space>
                {record.noteUrl ? (
                  <Typography.Link href={record.noteUrl} target="_blank" rel="noreferrer">
                    查看来源
                  </Typography.Link>
                ) : null}
              </Space>
            ),
          }}
        />
      </Card>

      <Modal
        open={taskOpen}
        title={editingTask ? '编辑采集任务' : '新建采集任务'}
        onCancel={() => setTaskOpen(false)}
        onOk={submitTask}
        width={720}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="taskName" label="任务名称" rules={[{ required: true }]}>
            <Input placeholder="例如：每日护肤赛道爆文采集" />
          </Form.Item>
          <Form.Item name="platformCode" label="平台" rules={[{ required: true }]}>
            <Select options={platformOptions} />
          </Form.Item>
          <Form.Item name="keywords" label="关键词" rules={[{ required: true }]}>
            <Input placeholder="多个关键词用逗号分隔，例如：护肤,美妆,穿搭" />
          </Form.Item>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="fetchLimit" label="单次采集数量" rules={[{ required: true }]}>
                <InputNumber min={1} max={50} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="providerCode" label="Provider" rules={[{ required: true }]}>
                <Select options={providerOptions} />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item name="cronExpr" label="Cron 表达式" rules={[{ required: true }]}>
            <Input placeholder="例如：0 0 8 * * ?" />
          </Form.Item>
          <Form.Item name="enabled" label="启用状态" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        open={analysisOpen}
        title="爆文拆解分析"
        onCancel={() => setAnalysisOpen(false)}
        footer={null}
        width={900}
      >
        {analysis ? (
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            <Typography.Paragraph>{analysis.summary}</Typography.Paragraph>
            <Descriptions column={2} bordered size="small">
              <Descriptions.Item label="标题类型">{analysis.titleType}</Descriptions.Item>
              <Descriptions.Item label="开场钩子">{analysis.hookType}</Descriptions.Item>
              <Descriptions.Item label="内容结构">{analysis.structureSummary}</Descriptions.Item>
              <Descriptions.Item label="互动方式">{analysis.interactionCta}</Descriptions.Item>
              <Descriptions.Item label="语气风格">{analysis.tone}</Descriptions.Item>
              <Descriptions.Item label="建议笔记风格">{analysis.recommendedStyle}</Descriptions.Item>
              <Descriptions.Item label="建议语气">{analysis.recommendedTone}</Descriptions.Item>
              <Descriptions.Item label="建议结构">{analysis.recommendedStructure}</Descriptions.Item>
            </Descriptions>

            <Card size="small" title="可借鉴亮点">
              <Space direction="vertical">
                {analysis.collectPoints.map((item) => (
                  <Typography.Text key={item}>- {item}</Typography.Text>
                ))}
              </Space>
            </Card>

            <Card size="small" title="迁移建议">
              <Space direction="vertical">
                {analysis.adaptationTips.map((item) => (
                  <Typography.Text key={item}>- {item}</Typography.Text>
                ))}
              </Space>
            </Card>

            <Card size="small" title="推荐生成预填">
              <Descriptions column={1} size="small">
                <Descriptions.Item label="主题建议">{analysis.productHint}</Descriptions.Item>
                <Descriptions.Item label="核心描述">{analysis.coreDescription}</Descriptions.Item>
                <Descriptions.Item label="保留关键词">{analysis.requiredKeywords.join('、') || '无'}</Descriptions.Item>
              </Descriptions>
            </Card>
          </Space>
        ) : (
          <Typography.Text type="secondary">{analysisLoading ? '正在分析中...' : '暂无分析结果'}</Typography.Text>
        )}
      </Modal>
    </Space>
  );
}

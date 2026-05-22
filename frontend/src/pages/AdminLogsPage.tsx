import { Button, DatePicker, Form, Input, Space, Table, Tabs, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { TabsProps } from 'antd';
import dayjs from 'dayjs';
import { useEffect, useMemo, useState } from 'react';
import { getGenerationLogs, getOperationLogs } from '../api/adminLog';
import { GenerationLogItem, OperationLogItem } from '../types';
import { hasPermission } from '../utils/auth';

const moduleLabelMap: Record<string, string> = {
  template: '模板',
  category: '分类',
  sensitive_word: '敏感词',
  user: '用户',
  role: '角色',
};

const actionLabelMap: Record<string, string> = {
  create: '新增',
  update: '编辑',
  delete: '删除',
  toggle: '启停',
  import: '导入',
  export: '导出',
  grant: '授权',
};

export function AdminLogsPage() {
  const [generationRecords, setGenerationRecords] = useState<GenerationLogItem[]>([]);
  const [operationRecords, setOperationRecords] = useState<OperationLogItem[]>([]);
  const [generationPage, setGenerationPage] = useState(1);
  const [operationPage, setOperationPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [generationTotal, setGenerationTotal] = useState(0);
  const [operationTotal, setOperationTotal] = useState(0);
  const [generationForm] = Form.useForm();
  const [operationForm] = Form.useForm();

  const loadGenerationLogs = async (page = generationPage, currentPageSize = pageSize) => {
    const values = generationForm.getFieldsValue();
    const { data } = await getGenerationLogs({
      page,
      pageSize: currentPageSize,
      userId: values.userId,
      style: values.style,
      startDate: values.range?.[0] ? dayjs(values.range[0]).format('YYYY-MM-DD') : undefined,
      endDate: values.range?.[1] ? dayjs(values.range[1]).format('YYYY-MM-DD') : undefined,
    });
    setGenerationRecords(data.data.records);
    setGenerationTotal(data.data.total);
  };

  const loadOperationLogs = async (page = operationPage, currentPageSize = pageSize) => {
    const values = operationForm.getFieldsValue();
    const { data } = await getOperationLogs({
      page,
      pageSize: currentPageSize,
      operatorId: values.operatorId,
      moduleName: values.moduleName,
      actionName: values.actionName,
      startDate: values.range?.[0] ? dayjs(values.range[0]).format('YYYY-MM-DD') : undefined,
      endDate: values.range?.[1] ? dayjs(values.range[1]).format('YYYY-MM-DD') : undefined,
    });
    setOperationRecords(data.data.records);
    setOperationTotal(data.data.total);
  };

  useEffect(() => {
    if (hasPermission('generation_log:view')) {
      loadGenerationLogs();
    }
  }, [generationPage, pageSize]);

  useEffect(() => {
    if (hasPermission('operation_log:view')) {
      loadOperationLogs();
    }
  }, [operationPage, pageSize]);

  const generationColumns: ColumnsType<GenerationLogItem> = useMemo(
    () => [
      { title: '日志ID', dataIndex: 'id', width: 100 },
      { title: '用户ID', dataIndex: 'userId', width: 100 },
      { title: '核心描述', dataIndex: 'coreInput', ellipsis: true },
      { title: '风格', dataIndex: 'style', width: 120 },
      { title: '人设', dataIndex: 'persona', width: 120 },
      { title: '字数', dataIndex: 'wordCount', width: 100 },
      {
        title: '已收藏',
        dataIndex: 'collected',
        width: 100,
        render: (value) => <Tag color={value ? 'green' : 'default'}>{value ? '是' : '否'}</Tag>,
      },
      { title: '生成时间', dataIndex: 'createdAt', width: 180 },
    ],
    [],
  );

  const operationColumns: ColumnsType<OperationLogItem> = useMemo(
    () => [
      { title: '日志ID', dataIndex: 'id', width: 100 },
      { title: '操作人', dataIndex: 'operatorNickname', width: 120 },
      { title: '操作人ID', dataIndex: 'operatorId', width: 100 },
      { title: '模块', dataIndex: 'moduleName', width: 120, render: (value) => moduleLabelMap[value] || value },
      { title: '动作', dataIndex: 'actionName', width: 120, render: (value) => actionLabelMap[value] || value },
      { title: '目标类型', dataIndex: 'targetType', width: 120 },
      { title: '目标ID', dataIndex: 'targetId', width: 100 },
      { title: '详情', dataIndex: 'detailText', ellipsis: true },
      { title: '时间', dataIndex: 'createdAt', width: 180 },
    ],
    [],
  );

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Typography.Title level={3}>日志中心</Typography.Title>
      {(() => {
        const items: TabsProps['items'] = [
          hasPermission('generation_log:view')
            ? {
                key: 'generation',
                label: '生成日志',
                children: (
                  <Space direction="vertical" style={{ width: '100%' }}>
                    <Form form={generationForm} layout="inline">
                      <Form.Item name="userId" label="用户ID">
                        <Input placeholder="输入用户ID" style={{ width: 140 }} />
                      </Form.Item>
                      <Form.Item name="style" label="风格">
                        <Input placeholder="如 good_item" style={{ width: 160 }} />
                      </Form.Item>
                      <Form.Item name="range" label="时间范围">
                        <DatePicker.RangePicker />
                      </Form.Item>
                      <Form.Item>
                        <Button
                          type="primary"
                          onClick={() => {
                            setGenerationPage(1);
                            loadGenerationLogs(1, pageSize);
                          }}
                        >
                          筛选
                        </Button>
                      </Form.Item>
                    </Form>
                    <Table
                      rowKey="id"
                      dataSource={generationRecords}
                      columns={generationColumns}
                      pagination={{
                        current: generationPage,
                        pageSize,
                        total: generationTotal,
                        onChange: (nextPage, nextPageSize) => {
                          setGenerationPage(nextPage);
                          setPageSize(nextPageSize);
                        },
                      }}
                    />
                  </Space>
                ),
              }
            : null,
          hasPermission('operation_log:view')
            ? {
                key: 'operation',
                label: '操作日志',
                children: (
                  <Space direction="vertical" style={{ width: '100%' }}>
                    <Form form={operationForm} layout="inline">
                      <Form.Item name="operatorId" label="操作人ID">
                        <Input placeholder="输入操作人ID" style={{ width: 140 }} />
                      </Form.Item>
                      <Form.Item name="moduleName" label="模块">
                        <Input placeholder="如 template" style={{ width: 140 }} />
                      </Form.Item>
                      <Form.Item name="actionName" label="动作">
                        <Input placeholder="如 update" style={{ width: 140 }} />
                      </Form.Item>
                      <Form.Item name="range" label="时间范围">
                        <DatePicker.RangePicker />
                      </Form.Item>
                      <Form.Item>
                        <Button
                          type="primary"
                          onClick={() => {
                            setOperationPage(1);
                            loadOperationLogs(1, pageSize);
                          }}
                        >
                          筛选
                        </Button>
                      </Form.Item>
                    </Form>
                    <Table
                      rowKey="id"
                      dataSource={operationRecords}
                      columns={operationColumns}
                      pagination={{
                        current: operationPage,
                        pageSize,
                        total: operationTotal,
                        onChange: (nextPage, nextPageSize) => {
                          setOperationPage(nextPage);
                          setPageSize(nextPageSize);
                        },
                      }}
                    />
                  </Space>
                ),
              }
            : null,
        ].filter(Boolean) as TabsProps['items'];
        return <Tabs items={items} />;
      })()}
    </Space>
  );
}

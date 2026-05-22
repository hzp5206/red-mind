import { Button, Form, Input, InputNumber, Modal, Popconfirm, Select, Space, Switch, Table, Tag, Typography, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useEffect, useMemo, useState } from 'react';
import { createTemplate, deleteTemplate, getAdminTemplates, toggleTemplate, updateTemplate } from '../api/adminTemplate';
import { deleteTemplateCategory, getAdminTemplateCategories, saveTemplateCategory } from '../api/templateCategory';
import { SelectOptionItem, TemplateItem, TemplateSavePayload } from '../types';
import { hasPermission } from '../utils/auth';

const styleOptions = [
  { label: '好物推荐', value: 'good_item' },
  { label: '探店打卡', value: 'visit' },
  { label: '实用教程', value: 'tutorial' },
  { label: 'Vlog日常', value: 'vlog' },
  { label: '合集清单', value: 'collection' },
];

export function AdminTemplatesPage() {
  const [records, setRecords] = useState<TemplateItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [keyword, setKeyword] = useState('');
  const [category, setCategory] = useState<string>();
  const [editing, setEditing] = useState<TemplateItem | null>(null);
  const [open, setOpen] = useState(false);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [form] = Form.useForm<TemplateSavePayload>();
  const [messageApi, contextHolder] = message.useMessage();
  const [preview, setPreview] = useState<TemplateItem | null>(null);
  const [categoryOptions, setCategoryOptions] = useState<SelectOptionItem[]>([]);
  const [categoryManageOpen, setCategoryManageOpen] = useState(false);
  const [categoryEditing, setCategoryEditing] = useState<SelectOptionItem | null>(null);
  const [categoryForm] = Form.useForm();

  const loadData = async () => {
    setLoading(true);
    try {
      const { data } = await getAdminTemplates({ keyword, category, page, pageSize });
      setRecords(data.data.records);
      setTotal(data.data.total);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [keyword, category, page, pageSize]);

  const loadCategories = async () => {
    const { data } = await getAdminTemplateCategories();
    setCategoryOptions(data.data);
  };

  useEffect(() => {
    loadCategories();
  }, []);

  const openCreate = () => {
    setEditing(null);
    form.resetFields();
    form.setFieldsValue({ isActive: true, style: 'good_item', category: categoryOptions[0]?.value });
    setOpen(true);
  };

  const openEdit = (record: TemplateItem) => {
    setEditing(record);
    form.setFieldsValue({
      category: record.category,
      title: record.title,
      contentExample: record.contentExample,
      tags: record.tags,
      style: record.style,
      isActive: record.isActive ?? true,
    });
    setOpen(true);
  };

  const submit = async () => {
    const values = await form.validateFields();
    if (editing?.id) {
      await updateTemplate(editing.id, values);
      messageApi.success('模板已更新');
    } else {
      await createTemplate(values);
      messageApi.success('模板已创建');
    }
    setOpen(false);
    loadData();
  };

  const submitCategory = async () => {
    const values = await categoryForm.validateFields();
    await saveTemplateCategory({
      id: categoryEditing?.id,
      categoryCode: values.categoryCode,
      categoryName: values.categoryName,
      sortOrder: values.sortOrder,
      isActive: values.isActive ?? true,
    });
    messageApi.success('分类已保存');
    setCategoryManageOpen(false);
    setCategoryEditing(null);
    categoryForm.resetFields();
    loadCategories();
  };

  const columns: ColumnsType<TemplateItem> = useMemo(
    () => [
      { title: '标题', dataIndex: 'title', width: 220 },
      { title: '分类', dataIndex: 'category', width: 120, render: (value) => <Tag>{value}</Tag> },
      { title: '风格', dataIndex: 'style', width: 120 },
      { title: '标签', dataIndex: 'tags', ellipsis: true },
      {
        title: '状态',
        dataIndex: 'isActive',
        width: 120,
        render: (_, record) => (
          <Switch
            checked={record.isActive}
            disabled={!hasPermission('template:manage')}
            onChange={async (checked) => {
              await toggleTemplate(record.id, checked);
              messageApi.success('状态已更新');
              loadData();
            }}
          />
        ),
      },
      {
        title: '操作',
        width: 180,
        render: (_, record) => (
          <Space>
            <Button type="link" onClick={() => openEdit(record)} disabled={!hasPermission('template:manage')}>
              编辑
            </Button>
            <Button type="link" onClick={() => setPreview(record)}>
              详情
            </Button>
            <Popconfirm
              title="确认删除这个模板吗？"
              onConfirm={async () => {
                await deleteTemplate(record.id);
                messageApi.success('删除成功');
                loadData();
              }}
            >
              <Button type="link" danger disabled={!hasPermission('template:manage')}>
                删除
              </Button>
            </Popconfirm>
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
          模板管理
        </Typography.Title>
        <Space wrap>
          <Input.Search
            allowClear
            placeholder="搜索标题、内容、标签"
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
          <Select
            allowClear
            placeholder="筛选分类"
            style={{ width: 160 }}
            options={categoryOptions}
            onChange={(value) => {
              setCategory(value);
              setPage(1);
            }}
          />
          <Button
            disabled={!hasPermission('category:manage')}
            onClick={() => {
              setCategoryEditing(null);
              categoryForm.resetFields();
              categoryForm.setFieldsValue({ isActive: true, sortOrder: 0 });
              setCategoryManageOpen(true);
            }}
          >
            分类字典
          </Button>
          <Button type="primary" onClick={openCreate} disabled={!hasPermission('template:manage')}>
            新建模板
          </Button>
        </Space>
      </Space>

      <Table
        rowKey="id"
        loading={loading}
        dataSource={records}
        columns={columns}
        pagination={{
          current: page,
          pageSize,
          total,
          onChange: (nextPage, nextPageSize) => {
            setPage(nextPage);
            setPageSize(nextPageSize);
          },
        }}
      />

      <Modal
        open={open}
        title={editing ? '编辑模板' : '新建模板'}
        onCancel={() => setOpen(false)}
        onOk={submit}
        width={760}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="title" label="标题" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="category" label="分类" rules={[{ required: true }]}>
            <Select options={categoryOptions} />
          </Form.Item>
          <Form.Item name="style" label="风格" rules={[{ required: true }]}>
            <Select options={styleOptions} />
          </Form.Item>
          <Form.Item name="tags" label="标签" rules={[{ required: true }]}>
            <Input placeholder="多个标签用英文逗号分隔" />
          </Form.Item>
          <Form.Item name="contentExample" label="示例内容" rules={[{ required: true }]}>
            <Input.TextArea rows={6} />
          </Form.Item>
          <Form.Item name="isActive" label="启用状态" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
      <Modal
        open={categoryManageOpen}
        title={categoryEditing ? '编辑分类' : '新增分类'}
        onCancel={() => setCategoryManageOpen(false)}
        onOk={submitCategory}
      >
        <Form form={categoryForm} layout="vertical">
          <Form.Item name="categoryCode" label="分类编码" rules={[{ required: true }]}>
            <Input placeholder="如 beauty" />
          </Form.Item>
          <Form.Item name="categoryName" label="分类名称" rules={[{ required: true }]}>
            <Input placeholder="如 美妆" />
          </Form.Item>
          <Form.Item name="sortOrder" label="排序">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="isActive" label="启用状态" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
        <Table
          rowKey="value"
          size="small"
          style={{ marginTop: 16 }}
          dataSource={categoryOptions}
          pagination={false}
          columns={[
            { title: '名称', dataIndex: 'label' },
            { title: '编码', dataIndex: 'value' },
            { title: '排序', dataIndex: 'sortOrder', width: 80 },
            {
              title: '状态',
              dataIndex: 'isActive',
              width: 80,
              render: (value: boolean) => <Tag color={value ? 'green' : 'default'}>{value ? '启用' : '停用'}</Tag>,
            },
            {
              title: '操作',
              width: 140,
              render: (_, record: SelectOptionItem) => (
                <Space>
                  <Button
                    type="link"
                    disabled={!hasPermission('category:manage')}
                    onClick={() => {
                      setCategoryEditing(record);
                      categoryForm.setFieldsValue({
                        categoryCode: record.value,
                        categoryName: record.label,
                        sortOrder: record.sortOrder,
                        isActive: record.isActive,
                      });
                    }}
                  >
                    编辑
                  </Button>
                  <Popconfirm
                    title="确认删除该分类吗？"
                    onConfirm={async () => {
                      if (!record.id) {
                        return;
                      }
                      await deleteTemplateCategory(record.id);
                      messageApi.success('分类已删除');
                      loadCategories();
                    }}
                  >
                    <Button type="link" danger disabled={!hasPermission('category:manage')}>
                      删除
                    </Button>
                  </Popconfirm>
                </Space>
              ),
            },
          ]}
        />
      </Modal>
      <Modal
        open={!!preview}
        title="模板详情"
        footer={null}
        onCancel={() => setPreview(null)}
        width={760}
      >
        <Typography.Title level={4}>{preview?.title}</Typography.Title>
        <Space wrap style={{ marginBottom: 12 }}>
          <Tag>{preview?.category}</Tag>
          <Tag color="blue">{preview?.style}</Tag>
          {(preview?.tags || '').split(',').filter(Boolean).map((tag) => (
            <Tag key={tag}>{tag}</Tag>
          ))}
        </Space>
        <Typography.Paragraph style={{ whiteSpace: 'pre-wrap' }}>
          {preview?.contentExample}
        </Typography.Paragraph>
      </Modal>
    </Space>
  );
}

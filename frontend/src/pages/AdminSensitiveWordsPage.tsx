import { Button, Form, Input, Modal, Popconfirm, Space, Table, Tag, Typography, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useEffect, useMemo, useState } from 'react';
import { deleteSensitiveWord, exportSensitiveWords, getSensitiveWords, importSensitiveWords, saveSensitiveWord } from '../api/adminSensitiveWord';
import { SensitiveWordItem } from '../types';
import { hasPermission } from '../utils/auth';

export function AdminSensitiveWordsPage() {
  const [records, setRecords] = useState<SensitiveWordItem[]>([]);
  const [open, setOpen] = useState(false);
  const [form] = Form.useForm<SensitiveWordItem>();
  const [editing, setEditing] = useState<SensitiveWordItem | null>(null);
  const [messageApi, contextHolder] = message.useMessage();
  const [importOpen, setImportOpen] = useState(false);
  const [importContent, setImportContent] = useState('');

  const loadData = async () => {
    const { data } = await getSensitiveWords();
    setRecords(data.data);
  };

  useEffect(() => {
    loadData();
  }, []);

  const submit = async () => {
    const values = await form.validateFields();
    await saveSensitiveWord({
      ...values,
      id: editing?.id,
      isActive: editing?.isActive ?? true,
    });
    messageApi.success('敏感词已保存');
    setOpen(false);
    form.resetFields();
    setEditing(null);
    loadData();
  };

  const columns: ColumnsType<SensitiveWordItem> = useMemo(
    () => [
      { title: '敏感词', dataIndex: 'word', width: 240 },
      { title: '替换词', dataIndex: 'replacement' },
      {
        title: '状态',
        dataIndex: 'isActive',
        width: 120,
        render: (value) => <Tag color={value ? 'green' : 'default'}>{value ? '启用' : '停用'}</Tag>,
      },
      {
        title: '操作',
        width: 180,
        render: (_, record) => (
          <Space>
            <Button
              type="link"
              disabled={!hasPermission('sensitive_word:manage')}
              onClick={() => {
                setEditing(record);
                form.setFieldsValue(record);
                setOpen(true);
              }}
            >
              编辑
            </Button>
            <Popconfirm
              title="确认删除这个敏感词吗？"
              onConfirm={async () => {
                await deleteSensitiveWord(record.id!);
                messageApi.success('删除成功');
                loadData();
              }}
            >
              <Button type="link" danger disabled={!hasPermission('sensitive_word:manage')}>
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
      <Space style={{ width: '100%', justifyContent: 'space-between' }}>
        <Typography.Title level={3} style={{ marginBottom: 0 }}>
          敏感词管理
        </Typography.Title>
        <Space>
          <Button onClick={() => setImportOpen(true)} disabled={!hasPermission('sensitive_word:manage')}>批量导入</Button>
          <Button
            disabled={!hasPermission('sensitive_word:manage')}
            onClick={async () => {
              const { data } = await exportSensitiveWords();
              await navigator.clipboard.writeText(data.data || '');
              messageApi.success('词库内容已复制到剪贴板');
            }}
          >
            导出词库
          </Button>
          <Button
            type="primary"
            disabled={!hasPermission('sensitive_word:manage')}
            onClick={() => {
              setEditing(null);
              form.resetFields();
              setOpen(true);
            }}
          >
            新增敏感词
          </Button>
        </Space>
      </Space>
      <Table rowKey="id" dataSource={records} columns={columns} />
      <Modal
        open={open}
        title={editing ? '编辑敏感词' : '新增敏感词'}
        onCancel={() => setOpen(false)}
        onOk={submit}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="word" label="敏感词" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="replacement" label="替换词" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
        </Form>
      </Modal>
      <Modal
        open={importOpen}
        title="批量导入敏感词"
        onCancel={() => setImportOpen(false)}
        onOk={async () => {
          const { data } = await importSensitiveWords(importContent);
          messageApi.success(`已导入 ${data.data} 条敏感词`);
          setImportOpen(false);
          setImportContent('');
          loadData();
        }}
      >
        <Typography.Paragraph type="secondary">
          每行一条，格式为 `敏感词=替换词`，例如：绝对=更稳妥
        </Typography.Paragraph>
        <Input.TextArea rows={10} value={importContent} onChange={(event) => setImportContent(event.target.value)} />
      </Modal>
    </Space>
  );
}

import { Button, DatePicker, Form, Input, InputNumber, Modal, Select, Space, Table, Tag, Typography, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import { useEffect, useMemo, useState } from 'react';
import { exportAdminUsers, getAdminUsers, getAssignableRoles, updateAdminUser } from '../api/adminUser';
import { AdminRoleItem, AdminUserOverview, UserManagePayload } from '../types';
import { hasPermission, refreshAuthProfile } from '../utils/auth';

export function AdminUsersPage() {
  const [records, setRecords] = useState<AdminUserOverview[]>([]);
  const [roles, setRoles] = useState<AdminRoleItem[]>([]);
  const [keyword, setKeyword] = useState('');
  const [memberType, setMemberType] = useState<string>();
  const [role, setRole] = useState<string>();
  const [roleCode, setRoleCode] = useState<string>();
  const [editing, setEditing] = useState<AdminUserOverview | null>(null);
  const [open, setOpen] = useState(false);
  const [form] = Form.useForm<UserManagePayload>();
  const [messageApi, contextHolder] = message.useMessage();
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);

  const loadData = async () => {
    const { data } = await getAdminUsers({
      keyword,
      memberType,
      role,
      roleCode,
      page,
      pageSize,
    });
    setRecords(data.data.records);
    setTotal(data.data.total);
  };

  useEffect(() => {
    loadData();
    getAssignableRoles().then(({ data }) => setRoles(data.data.filter((item) => item.isActive)));
  }, [keyword, memberType, role, roleCode, page, pageSize]);

  const columns: ColumnsType<AdminUserOverview> = useMemo(
    () => [
      { title: '用户ID', dataIndex: 'id', width: 100 },
      { title: '邮箱', dataIndex: 'email', width: 240 },
      { title: '昵称', dataIndex: 'nickname', width: 180 },
      {
        title: '后台身份',
        dataIndex: 'role',
        width: 120,
        render: (value) => <Tag color={value === 'ADMIN' ? 'red' : 'default'}>{value}</Tag>,
      },
      {
        title: '后台角色',
        dataIndex: 'roleCode',
        width: 140,
        render: (value) => <Tag color="blue">{value || 'USER'}</Tag>,
      },
      {
        title: '会员类型',
        dataIndex: 'memberType',
        width: 120,
        render: (value) => <Tag color={value === 'PRO' ? 'gold' : 'default'}>{value}</Tag>,
      },
      { title: '今日使用次数', dataIndex: 'dailyGenCount', width: 140 },
      { title: '最近生成日期', dataIndex: 'lastGenDate' },
      {
        title: '操作',
        width: 120,
        render: (_, record) => (
          <Button
            type="link"
            disabled={!hasPermission('user:manage')}
            onClick={() => {
              setEditing(record);
              form.setFieldsValue({
                role: record.role,
                roleCode: record.roleCode,
                memberType: record.memberType,
                dailyGenCount: record.dailyGenCount,
                memberExpireAt: undefined,
              });
              setOpen(true);
            }}
          >
            调整
          </Button>
        ),
      },
    ],
    [form],
  );

  const submit = async () => {
    const values = await form.validateFields();
    if (!editing) {
      return;
    }
    await updateAdminUser(editing.id, values);
    await refreshAuthProfile();
    messageApi.success('用户信息已更新');
    setOpen(false);
    setEditing(null);
    form.resetFields();
    loadData();
  };

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      {contextHolder}
      <Space style={{ width: '100%', justifyContent: 'space-between' }} wrap>
        <Typography.Title level={3} style={{ marginBottom: 0 }}>
          用户概览
        </Typography.Title>
        <Space wrap>
          <Input.Search
            allowClear
            placeholder="搜索邮箱或昵称"
            style={{ width: 220 }}
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
            placeholder="筛选会员类型"
            style={{ width: 180 }}
            options={[
              { label: '免费用户', value: 'FREE' },
              { label: '会员用户', value: 'PRO' },
            ]}
            onChange={(value) => {
              setMemberType(value);
              setPage(1);
            }}
          />
          <Select
            allowClear
            placeholder="筛选后台身份"
            style={{ width: 180 }}
            options={[
              { label: '普通用户', value: 'USER' },
              { label: '管理员', value: 'ADMIN' },
            ]}
            onChange={(value) => {
              setRole(value);
              setPage(1);
            }}
          />
          <Select
            allowClear
            placeholder="筛选管理员角色"
            style={{ width: 220 }}
            options={roles.map((item) => ({ label: item.roleName, value: item.roleCode }))}
            onChange={(value) => {
              setRoleCode(value);
              setPage(1);
            }}
          />
          <Button
            disabled={!hasPermission('user:manage')}
            onClick={async () => {
              const { data } = await exportAdminUsers({ keyword, memberType, role, roleCode });
              const blob = new Blob([`\uFEFF${data.data || ''}`], { type: 'text/csv;charset=utf-8;' });
              const url = window.URL.createObjectURL(blob);
              const link = document.createElement('a');
              link.href = url;
              link.download = `users-${dayjs().format('YYYYMMDD-HHmmss')}.csv`;
              link.click();
              window.URL.revokeObjectURL(url);
              messageApi.success('用户列表已导出');
            }}
          >
            导出列表
          </Button>
        </Space>
      </Space>
      <Table
        rowKey="id"
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
        title="调整用户信息"
        onCancel={() => {
          setOpen(false);
          setEditing(null);
          form.resetFields();
        }}
        onOk={submit}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="role" label="后台身份">
            <Select
              onChange={(value) => {
                if (value === 'USER') {
                  form.setFieldValue('roleCode', 'USER');
                }
                if (value === 'ADMIN' && !form.getFieldValue('roleCode')) {
                  form.setFieldValue('roleCode', 'ADMIN');
                }
              }}
              options={[
                { label: '普通用户', value: 'USER' },
                { label: '管理员', value: 'ADMIN' },
              ]}
            />
          </Form.Item>
          <Form.Item name="roleCode" label="管理员角色">
            <Select
              allowClear
              placeholder="仅管理员需要选择角色"
              options={roles.map((item) => ({ label: item.roleName, value: item.roleCode }))}
            />
          </Form.Item>
          <Form.Item name="memberType" label="会员类型">
            <Select
              options={[
                { label: '免费用户', value: 'FREE' },
                { label: '会员用户', value: 'PRO' },
              ]}
            />
          </Form.Item>
          <Form.Item name="memberExpireAt" label="会员到期时间">
            <DatePicker
              showTime
              style={{ width: '100%' }}
              onChange={(value) => {
                form.setFieldValue('memberExpireAt', value ? dayjs(value).format('YYYY-MM-DDTHH:mm:ss') : undefined);
              }}
            />
          </Form.Item>
          <Form.Item name="dailyGenCount" label="今日使用次数">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  );
}

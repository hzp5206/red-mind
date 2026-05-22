import { Button, Checkbox, Form, Input, Modal, Popconfirm, Space, Switch, Table, Tag, Typography, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useEffect, useMemo, useState } from 'react';
import { deleteAdminRole, getAdminPermissions, getAdminRoles, saveAdminRole } from '../api/adminRole';
import { AdminPermissionItem, AdminRoleItem } from '../types';
import { hasPermission } from '../utils/auth';

export function AdminRolesPage() {
  const [records, setRecords] = useState<AdminRoleItem[]>([]);
  const [permissions, setPermissions] = useState<AdminPermissionItem[]>([]);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<AdminRoleItem | null>(null);
  const [form] = Form.useForm();
  const [messageApi, contextHolder] = message.useMessage();

  const loadData = async () => {
    const [{ data: roleData }, { data: permissionData }] = await Promise.all([getAdminRoles(), getAdminPermissions()]);
    setRecords(roleData.data);
    setPermissions(permissionData.data);
  };

  useEffect(() => {
    loadData();
  }, []);

  const columns: ColumnsType<AdminRoleItem> = useMemo(
    () => [
      { title: '角色名', dataIndex: 'roleName', width: 180 },
      { title: '角色编码', dataIndex: 'roleCode', width: 140 },
      { title: '说明', dataIndex: 'descriptionText' },
      {
        title: '状态',
        dataIndex: 'isActive',
        width: 100,
        render: (value) => <Tag color={value ? 'green' : 'default'}>{value ? '启用' : '停用'}</Tag>,
      },
      {
        title: '权限点',
        dataIndex: 'permissions',
        render: (value: string[]) => (
          <Space wrap>
            {value?.map((item) => (
              <Tag key={item}>{item}</Tag>
            ))}
          </Space>
        ),
      },
      {
        title: '操作',
        width: 160,
        render: (_, record) => (
          <Space>
            <Button
              type="link"
              disabled={!hasPermission('role:manage')}
              onClick={() => {
                setEditing(record);
                form.setFieldsValue({
                  roleCode: record.roleCode,
                  roleName: record.roleName,
                  descriptionText: record.descriptionText,
                  isActive: record.isActive,
                  permissions: record.permissions,
                });
                setOpen(true);
              }}
            >
              编辑
            </Button>
            <Popconfirm
              title="确认删除该角色吗？"
              onConfirm={async () => {
                await deleteAdminRole(record.id);
                messageApi.success('角色已删除');
                loadData();
              }}
            >
              <Button type="link" danger disabled={!hasPermission('role:manage')}>
                删除
              </Button>
            </Popconfirm>
          </Space>
        ),
      },
    ],
    [form, messageApi],
  );

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      {contextHolder}
      <Space style={{ width: '100%', justifyContent: 'space-between' }}>
        <Typography.Title level={3} style={{ marginBottom: 0 }}>
          角色权限管理
        </Typography.Title>
        <Button
          type="primary"
          disabled={!hasPermission('role:manage')}
          onClick={() => {
            setEditing(null);
            form.resetFields();
            form.setFieldsValue({ isActive: true, permissions: [] });
            setOpen(true);
          }}
        >
          新建角色
        </Button>
      </Space>

      <Table rowKey="id" dataSource={records} columns={columns} />

      <Modal
        open={open}
        title={editing ? '编辑角色' : '新建角色'}
        width={760}
        onCancel={() => setOpen(false)}
        onOk={async () => {
          const values = await form.validateFields();
          await saveAdminRole({
            id: editing?.id,
            roleCode: values.roleCode,
            roleName: values.roleName,
            descriptionText: values.descriptionText,
            isActive: values.isActive,
            permissions: values.permissions,
          });
          messageApi.success('角色已保存');
          setOpen(false);
          loadData();
        }}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="roleCode" label="角色编码" rules={[{ required: true }]}>
            <Input placeholder="如 OPS_MANAGER" disabled={!!editing} />
          </Form.Item>
          <Form.Item name="roleName" label="角色名称" rules={[{ required: true }]}>
            <Input placeholder="如 运营管理员" />
          </Form.Item>
          <Form.Item name="descriptionText" label="角色说明">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item name="isActive" label="启用状态" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item name="permissions" label="权限点">
            <Checkbox.Group
              style={{ width: '100%' }}
              options={permissions.map((item) => ({
                label: `${item.moduleName} / ${item.permissionName}（${item.permissionCode}）`,
                value: item.permissionCode,
              }))}
            />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  );
}

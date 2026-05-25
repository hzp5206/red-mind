import { Button, Checkbox, Collapse, Form, Input, Modal, Popconfirm, Space, Switch, Table, Tag, Typography, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useEffect, useMemo, useState } from 'react';
import { copyAdminRole, deleteAdminRole, getAdminPermissions, getAdminRoles, saveAdminRole } from '../api/adminRole';
import { AdminPermissionItem, AdminRoleItem } from '../types';
import { hasPermission, refreshAuthProfile } from '../utils/auth';

export function AdminRolesPage() {
  const [records, setRecords] = useState<AdminRoleItem[]>([]);
  const [permissions, setPermissions] = useState<AdminPermissionItem[]>([]);
  const [open, setOpen] = useState(false);
  const [copyOpen, setCopyOpen] = useState(false);
  const [editing, setEditing] = useState<AdminRoleItem | null>(null);
  const [copying, setCopying] = useState<AdminRoleItem | null>(null);
  const [form] = Form.useForm();
  const [copyForm] = Form.useForm();
  const [messageApi, contextHolder] = message.useMessage();

  const groupedPermissions = permissions.reduce<Record<string, AdminPermissionItem[]>>((acc, item) => {
    acc[item.moduleName] = acc[item.moduleName] || [];
    acc[item.moduleName].push(item);
    return acc;
  }, {});

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
              <Tag key={item} color={item.startsWith('dashboard') ? 'blue' : item.startsWith('user') ? 'green' : item.startsWith('role') ? 'purple' : 'default'}>
                {item}
              </Tag>
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
            <Button
              type="link"
              disabled={!hasPermission('role:manage')}
              onClick={() => {
                setCopying(record);
                copyForm.resetFields();
                copyForm.setFieldsValue({
                  roleCode: `${record.roleCode}_COPY`,
                  roleName: `${record.roleName}-副本`,
                  descriptionText: record.descriptionText,
                });
                setCopyOpen(true);
              }}
            >
              复制
            </Button>
            <Popconfirm
              title="确认删除该角色吗？"
              onConfirm={async () => {
                await deleteAdminRole(record.id);
                await refreshAuthProfile();
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
        onCancel={() => {
          setOpen(false);
          setEditing(null);
          form.resetFields();
        }}
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
          await refreshAuthProfile();
          messageApi.success('角色已保存');
          setOpen(false);
          setEditing(null);
          form.resetFields();
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
            <Form.Item noStyle shouldUpdate>
              {() => {
                const selectedPermissions = (form.getFieldValue('permissions') || []) as string[];
                return (
                  <Collapse
                    items={Object.entries(groupedPermissions).map(([moduleName, modulePermissions]) => ({
                      key: moduleName,
                      label: `${moduleName}（${modulePermissions.length}）`,
                      children: (
                        <Checkbox.Group
                          value={selectedPermissions}
                          onChange={(value) => form.setFieldValue('permissions', value)}
                          options={modulePermissions.map((item) => ({
                            label: `${item.permissionName}（${item.permissionCode}）`,
                            value: item.permissionCode,
                          }))}
                        />
                      ),
                    }))}
                  />
                );
              }}
            </Form.Item>
          </Form.Item>
        </Form>
      </Modal>
      <Modal
        open={copyOpen}
        title="复制角色"
        onCancel={() => {
          setCopyOpen(false);
          setCopying(null);
          copyForm.resetFields();
        }}
        onOk={async () => {
          if (!copying) {
            return;
          }
          const values = await copyForm.validateFields();
          await copyAdminRole(copying.id, values);
          await refreshAuthProfile();
          messageApi.success('角色副本已创建');
          setCopyOpen(false);
          setCopying(null);
          copyForm.resetFields();
          loadData();
        }}
      >
        <Form form={copyForm} layout="vertical">
          <Form.Item name="roleCode" label="新角色编码" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="roleName" label="新角色名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="descriptionText" label="角色说明">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  );
}

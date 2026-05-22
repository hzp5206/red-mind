import { Button, Card, Form, Input, Space, Tabs, Typography, message } from 'antd';
import { login, register } from '../api/auth';

export function LoginPage() {
  const [messageApi, contextHolder] = message.useMessage();

  const handleLogin = async (values: { email: string; password: string }) => {
    const { data } = await login(values);
    localStorage.setItem('redmind_token', data.data.token);
    localStorage.setItem('redmind_nickname', data.data.nickname);
    localStorage.setItem('redmind_role', data.data.role || 'USER');
    localStorage.setItem('redmind_role_code', data.data.roleCode || data.data.role || 'USER');
    localStorage.setItem('redmind_permissions', JSON.stringify(data.data.permissions || []));
    messageApi.success('登录成功');
    window.location.href = '/create';
  };

  const handleRegister = async (values: { email: string; nickname: string; password: string }) => {
    const { data } = await register(values);
    localStorage.setItem('redmind_token', data.data.token);
    localStorage.setItem('redmind_nickname', data.data.nickname);
    localStorage.setItem('redmind_role', data.data.role || 'USER');
    localStorage.setItem('redmind_role_code', data.data.roleCode || data.data.role || 'USER');
    localStorage.setItem('redmind_permissions', JSON.stringify(data.data.permissions || []));
    messageApi.success('注册成功');
    window.location.href = '/create';
  };

  return (
    <div className="login-page">
      {contextHolder}
      <Card className="login-card">
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <div>
            <Typography.Title level={2}>欢迎来到 Red Mind</Typography.Title>
            <Typography.Text type="secondary">
              让小红书内容生产，从灵感焦虑变成稳定产出。
            </Typography.Text>
          </div>
          <Tabs
            items={[
              {
                key: 'login',
                label: '登录',
                children: (
                  <Form layout="vertical" onFinish={handleLogin}>
                    <Form.Item name="email" label="邮箱" rules={[{ required: true }]}>
                      <Input placeholder="请输入邮箱" />
                    </Form.Item>
                    <Form.Item name="password" label="密码" rules={[{ required: true }]}>
                      <Input.Password placeholder="请输入密码" />
                    </Form.Item>
                    <Button block type="primary" htmlType="submit">
                      登录
                    </Button>
                  </Form>
                ),
              },
              {
                key: 'register',
                label: '注册',
                children: (
                  <Form layout="vertical" onFinish={handleRegister}>
                    <Form.Item name="email" label="邮箱" rules={[{ required: true }]}>
                      <Input placeholder="请输入邮箱" />
                    </Form.Item>
                    <Form.Item name="nickname" label="昵称" rules={[{ required: true }]}>
                      <Input placeholder="请输入昵称" />
                    </Form.Item>
                    <Form.Item name="password" label="密码" rules={[{ required: true }]}>
                      <Input.Password placeholder="请输入密码" />
                    </Form.Item>
                    <Button block type="primary" htmlType="submit">
                      注册并开始创作
                    </Button>
                  </Form>
                ),
              },
            ]}
          />
        </Space>
      </Card>
    </div>
  );
}

import {
  AlertOutlined,
  BulbOutlined,
  CrownOutlined,
  DashboardOutlined,
  HistoryOutlined,
  FileTextOutlined,
  LogoutOutlined,
  TeamOutlined,
  AppstoreOutlined,
  RocketOutlined,
  SafetyCertificateOutlined,
} from '@ant-design/icons';
import { Avatar, Dropdown, Layout, Menu, Space, Typography } from 'antd';
import type { MenuProps } from 'antd';
import { useEffect, useState } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { getNickname, getRole, hasPermission, logout } from '../utils/auth';

const { Header, Sider, Content } = Layout;

const profileMenu: MenuProps['items'] = [
  { key: 'logout', icon: <LogoutOutlined />, label: '退出登录', onClick: logout },
];

export function AppLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const [, setVersion] = useState(0);

  useEffect(() => {
    const handler = () => setVersion((current) => current + 1);
    window.addEventListener('redmind-auth-changed', handler);
    return () => window.removeEventListener('redmind-auth-changed', handler);
  }, []);

  const isAdminUser = getRole() === 'ADMIN';
  const adminItems = [
    hasPermission('dashboard:view') ? { key: '/admin/dashboard', icon: <DashboardOutlined />, label: '后台首页' } : null,
    hasPermission('template:manage') ? { key: '/admin/templates', icon: <AppstoreOutlined />, label: '模板管理' } : null,
    hasPermission('trending_copy:manage') ? { key: '/admin/trending-copies', icon: <BulbOutlined />, label: '爆文采集中心' } : null,
    hasPermission('sensitive_word:manage') ? { key: '/admin/sensitive-words', icon: <AlertOutlined />, label: '敏感词管理' } : null,
    hasPermission('user:manage') ? { key: '/admin/users', icon: <TeamOutlined />, label: '用户概览' } : null,
    hasPermission('role:manage') ? { key: '/admin/roles', icon: <SafetyCertificateOutlined />, label: '角色权限' } : null,
    hasPermission('generation_log:view') || hasPermission('operation_log:view')
      ? { key: '/admin/logs', icon: <FileTextOutlined />, label: '日志中心' }
      : null,
  ].filter(Boolean) as NonNullable<MenuProps['items']>[number][];

  const items = [
    { key: '/create', icon: <RocketOutlined />, label: '文案生成' },
    { key: '/library', icon: <BulbOutlined />, label: '文案灵感库' },
    { key: '/history', icon: <HistoryOutlined />, label: '我的历史' },
    { key: '/settings/billing', icon: <CrownOutlined />, label: '会员中心' },
    ...(isAdminUser ? adminItems : []),
  ];

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider width={240} theme="light" className="app-sider">
        <div className="brand">
          <Typography.Title level={3} style={{ margin: 0 }}>
            Red Mind
          </Typography.Title>
          <Typography.Text type="secondary">小红书创作工作台</Typography.Text>
        </div>
        <Menu
          mode="inline"
          selectedKeys={[location.pathname]}
          items={items}
          onClick={({ key }) => navigate(key)}
          style={{ borderInlineEnd: 'none' }}
        />
      </Sider>
      <Layout>
        <Header className="app-header">
          <Space style={{ width: '100%', justifyContent: 'space-between' }}>
            <Typography.Text strong>高转化内容生成中台</Typography.Text>
            <Dropdown menu={{ items: profileMenu }} trigger={['click']}>
              <Space className="profile-entry">
                <Avatar style={{ backgroundColor: '#FE2C55' }}>
                  {getNickname().slice(0, 1).toUpperCase()}
                </Avatar>
                <Typography.Text>{getNickname()}</Typography.Text>
              </Space>
            </Dropdown>
          </Space>
        </Header>
        <Content className="app-content">
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}

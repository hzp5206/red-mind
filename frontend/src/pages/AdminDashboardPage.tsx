import { Card, Col, List, Row, Statistic, Tag, Typography } from 'antd';
import { useEffect, useState } from 'react';
import { getDashboardSummary } from '../api/adminDashboard';
import { DashboardSummary } from '../types';

function BucketList({ title, items }: { title: string; items: { label: string; value: number }[] }) {
  const max = Math.max(...items.map((item) => item.value), 1);
  return (
    <Card title={title}>
      <List
        dataSource={items}
        locale={{ emptyText: '暂无数据' }}
        renderItem={(item) => (
          <List.Item>
            <div style={{ width: '100%' }}>
              <Row justify="space-between">
                <Col>
                  <Typography.Text>{item.label}</Typography.Text>
                </Col>
                <Col>
                  <Typography.Text strong>{item.value}</Typography.Text>
                </Col>
              </Row>
              <div style={{ marginTop: 8, background: '#f5f5f5', borderRadius: 999, height: 8 }}>
                <div
                  style={{
                    width: `${(item.value / max) * 100}%`,
                    background: '#FE2C55',
                    borderRadius: 999,
                    height: 8,
                  }}
                />
              </div>
            </div>
          </List.Item>
        )}
      />
    </Card>
  );
}

export function AdminDashboardPage() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);

  useEffect(() => {
    getDashboardSummary().then(({ data }) => setSummary(data.data));
  }, []);

  return (
    <>
      <Typography.Title level={3}>后台首页</Typography.Title>
      <Row gutter={[16, 16]}>
        <Col span={6}>
          <Card>
            <Statistic title="模板总数" value={summary?.templateCount ?? 0} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="用户总数" value={summary?.userCount ?? 0} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="管理员人数" value={summary?.adminCount ?? 0} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="今日生成次数" value={summary?.todayGenerationCount ?? 0} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="启用敏感词数" value={summary?.sensitiveWordCount ?? 0} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="今日后台操作数" value={summary?.todayOperationCount ?? 0} />
          </Card>
        </Col>
        <Col span={24}>
          <Card title="最近操作动态">
            <List
              dataSource={summary?.recentOperations || []}
              locale={{ emptyText: '暂无最近操作' }}
              renderItem={(item) => (
                <List.Item>
                  <List.Item.Meta
                    title={
                      <Row justify="space-between" style={{ width: '100%' }}>
                        <Col>
                          <Tag color="blue">{item.moduleName}</Tag>
                          <Tag color="purple">{item.actionName}</Tag>
                          <Typography.Text strong>{item.operatorNickname}</Typography.Text>
                        </Col>
                        <Col>
                          <Typography.Text type="secondary">{item.createdAt}</Typography.Text>
                        </Col>
                      </Row>
                    }
                    description={item.detailText}
                  />
                </List.Item>
              )}
            />
          </Card>
        </Col>
        <Col span={12}>
          <BucketList title="管理员角色分布" items={summary?.adminRoleDistribution || []} />
        </Col>
        <Col span={12}>
          <BucketList title="操作模块分布" items={summary?.operationModuleDistribution || []} />
        </Col>
      </Row>
    </>
  );
}

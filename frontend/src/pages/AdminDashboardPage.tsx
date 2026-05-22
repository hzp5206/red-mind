import { Card, Col, Row, Statistic, Typography } from 'antd';
import { useEffect, useState } from 'react';
import { getDashboardSummary } from '../api/adminDashboard';
import { DashboardSummary } from '../types';

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
      </Row>
    </>
  );
}

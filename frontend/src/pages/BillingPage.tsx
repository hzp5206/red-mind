import { Button, Card, Col, Row, Space, Tag, Typography } from 'antd';

const plans = [
  { title: '免费版', price: '¥0', desc: '每日 10 次，适合体验', tags: ['智能生成', '历史最近 20 条'] },
  { title: '月度会员', price: '¥49', desc: '不限生成次数', tags: ['笔风仿写', '内容净化', '历史永久保留'] },
  { title: '年度会员', price: '¥399', desc: '适合长期团队运营', tags: ['不限次数', '优先模型', '后续多账号能力'] },
];

export function BillingPage() {
  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Typography.Title level={3}>会员中心</Typography.Title>
      <Row gutter={16}>
        {plans.map((plan) => (
          <Col span={8} key={plan.title}>
            <Card title={plan.title}>
              <Typography.Title level={2}>{plan.price}</Typography.Title>
              <Typography.Paragraph type="secondary">{plan.desc}</Typography.Paragraph>
              <Space wrap>
                {plan.tags.map((tag) => (
                  <Tag color="magenta" key={tag}>
                    {tag}
                  </Tag>
                ))}
              </Space>
              <div style={{ marginTop: 20 }}>
                <Button type="primary" block>
                  立即开通
                </Button>
              </div>
            </Card>
          </Col>
        ))}
      </Row>
    </Space>
  );
}

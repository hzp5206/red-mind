import { Alert, Button, Card, Form, Input, Select, Space, Tag, Typography, message } from 'antd';
import { useEffect, useState } from 'react';
import { getAiSetting, saveAiSetting, testAiSetting } from '../api/adminAiSetting';
import { AiConnectivityTestResult, AiSetting } from '../types';
import { hasPermission } from '../utils/auth';

const providerTips: Record<string, { baseUrl: string; chatPath: string; model: string }> = {
  mock: { baseUrl: 'https://mock.local', chatPath: '/chat/completions', model: 'mock' },
  openai: { baseUrl: 'https://api.openai.com', chatPath: '/v1/chat/completions', model: 'gpt-4o-mini' },
  deepseek: { baseUrl: 'https://api.deepseek.com', chatPath: '/chat/completions', model: 'deepseek-v4-flash' },
};

export function AdminAiSettingsPage() {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [testing, setTesting] = useState(false);
  const [current, setCurrent] = useState<AiSetting | null>(null);
  const [testResult, setTestResult] = useState<AiConnectivityTestResult | null>(null);
  const [messageApi, contextHolder] = message.useMessage();

  const loadData = async () => {
    setLoading(true);
    try {
      const { data } = await getAiSetting();
      setCurrent(data.data);
      form.setFieldsValue({
        provider: data.data.provider,
        baseUrl: data.data.baseUrl,
        model: data.data.model,
        chatPath: data.data.chatPath,
        apiKey: '',
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const submit = async () => {
    const values = await form.validateFields();
    setSaving(true);
    try {
      const { data } = await saveAiSetting(values);
      setCurrent(data.data);
      form.setFieldValue('apiKey', '');
      messageApi.success('AI 配置已保存');
    } finally {
      setSaving(false);
    }
  };

  const runTest = async () => {
    setTesting(true);
    try {
      const { data } = await testAiSetting();
      setTestResult(data.data);
      if (data.data.success) {
        messageApi.success('AI 连通性测试成功');
      } else {
        messageApi.warning('AI 连通性测试未通过');
      }
    } finally {
      setTesting(false);
    }
  };

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      {contextHolder}
      <Space style={{ width: '100%', justifyContent: 'space-between' }} wrap>
        <Typography.Title level={3} style={{ marginBottom: 0 }}>
          AI 配置中心
        </Typography.Title>
        <Space>
          <Button onClick={loadData} loading={loading}>
            刷新配置
          </Button>
          <Button onClick={runTest} loading={testing} disabled={!hasPermission('ai_setting:manage')}>
            连通性测试
          </Button>
        </Space>
      </Space>

      <Alert
        type="info"
        showIcon
        message="运行时生效"
        description="这里保存后会直接更新后端当前 AI Provider、模型和接口地址。API Key 只有你重新填写时才会覆盖。"
      />

      {testResult ? (
        <Alert
          type={testResult.success ? 'success' : 'error'}
          showIcon
          message={testResult.success ? 'AI 连通成功' : 'AI 连通失败'}
          description={`${testResult.provider} / ${testResult.model}：${testResult.message}`}
        />
      ) : null}

      <Card loading={loading}>
        <Form
          form={form}
          layout="vertical"
          onValuesChange={(changedValues) => {
            if (changedValues.provider) {
              const preset = providerTips[changedValues.provider];
              if (preset) {
                form.setFieldsValue({
                  baseUrl: preset.baseUrl,
                  chatPath: preset.chatPath,
                  model: preset.model,
                });
              }
            }
          }}
        >
          <Form.Item name="provider" label="AI Provider" rules={[{ required: true }]}>
            <Select
              disabled={!hasPermission('ai_setting:manage')}
              options={(current?.providerOptions || []).map((item) => ({ label: item, value: item }))}
            />
          </Form.Item>
          <Form.Item name="baseUrl" label="Base URL" rules={[{ required: true }]}>
            <Input disabled={!hasPermission('ai_setting:manage')} />
          </Form.Item>
          <Form.Item name="chatPath" label="Chat Path" rules={[{ required: true }]}>
            <Input disabled={!hasPermission('ai_setting:manage')} />
          </Form.Item>
          <Form.Item name="model" label="Model" rules={[{ required: true }]}>
            <Select
              showSearch
              disabled={!hasPermission('ai_setting:manage')}
              options={(current?.modelOptions || []).map((item) => ({ label: item, value: item }))}
            />
          </Form.Item>
          <Form.Item name="apiKey" label="API Key">
            <Input.Password
              placeholder={current?.apiKeyMasked ? `当前已配置：${current.apiKeyMasked}` : '输入新的 API Key'}
              disabled={!hasPermission('ai_setting:manage')}
            />
          </Form.Item>
        </Form>

        <Space direction="vertical" style={{ width: '100%', marginBottom: 16 }}>
          <Space wrap>
            <Tag color="blue">当前 Provider：{current?.provider || '-'}</Tag>
            <Tag color="purple">当前 Model：{current?.model || '-'}</Tag>
          </Space>
          <Typography.Text type="secondary">
            当前状态：{current?.apiKeyConfigured ? '已配置 API Key' : '未配置 API Key'}
          </Typography.Text>
          {current?.provider === 'deepseek' ? (
            <Alert
              type="success"
              showIcon
              message="DeepSeek 推荐配置"
              description="推荐使用 https://api.deepseek.com + /chat/completions，模型优先 deepseek-v4-flash 或 deepseek-v4-pro。"
            />
          ) : null}
        </Space>

        <Button type="primary" onClick={submit} loading={saving} disabled={!hasPermission('ai_setting:manage')}>
          保存 AI 配置
        </Button>
      </Card>
    </Space>
  );
}

import { useState } from 'react';
import { Card, Button, Modal, Form, Input, InputNumber, List, Space, Tag, message, Popconfirm } from 'antd';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchMeals, createMeal, updateMeal, deleteMeal } from '../api/mealApi';
import { EditOutlined, DeleteOutlined, ClockCircleOutlined, DollarOutlined } from '@ant-design/icons';

const MealsPage = () => {
  const queryClient = useQueryClient();
  const [form] = Form.useForm();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingMeal, setEditingMeal] = useState(null);

  const { data: meals, isLoading, error } = useQuery({
    queryKey: ['meals'],
    queryFn: fetchMeals,
    onError: (error) => {
      message.error(`Failed to load meals: ${error.message}`);
    }
  });

  const createMutation = useMutation({
    mutationFn: createMeal,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['meals'] });
      message.success('Meal created successfully!');
      setIsModalOpen(false);
      form.resetFields();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, ...data }) => updateMeal(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['meals'] });
      message.success('Meal updated successfully!');
      setIsModalOpen(false);
      form.resetFields();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteMeal,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['meals'] });
      message.success('Meal deleted successfully!');
    },
  });

  const handleSubmit = (values) => {
    if (editingMeal) {
      updateMutation.mutate({ ...values, id: editingMeal.id });
    } else {
      createMutation.mutate(values);
    }
  };

  if (error) {
    return <div style={{ padding: 24 }}>Error loading meals. Please try again later.</div>;
  }

  return (
    <div style={{ padding: 24 }}>
      <Card
        title="Menu Management"
        extra={
          <Button 
            type="primary" 
            onClick={() => {
              setEditingMeal(null);
              setIsModalOpen(true);
            }}
          >
            Add New Meal
          </Button>
        }
      >
        <List
          grid={{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 3, xl: 4, xxl: 4 }}
          dataSource={meals || []}
          loading={isLoading}
          renderItem={(meal) => (
            <List.Item>
              <Card
                actions={[
                  <EditOutlined 
                    key="edit" 
                    onClick={() => {
                      setEditingMeal(meal);
                      form.setFieldsValue({
                        ...meal,
                        price: Number(meal.price)
                      });
                      setIsModalOpen(true);
                    }}
                  />,
                  <Popconfirm
                    title="Delete this meal?"
                    onConfirm={() => deleteMutation.mutate(meal.id)}
                    okText="Yes"
                    cancelText="No"
                  >
                    <DeleteOutlined 
                      key="delete" 
                      style={{ color: '#ff4d4f' }}
                    />
                  </Popconfirm>
                ]}
              >
                <Card.Meta
                  title={meal.name}
                  description={
                    <Space direction="vertical" size="small">
                      <div>
                        <Tag icon={<DollarOutlined />} color="green">
                          ${meal.price.toFixed(2)}
                        </Tag>
                      </div>
                      <div>
                        <Tag icon={<ClockCircleOutlined />} color="blue">
                          {meal.cookingTime} min
                        </Tag>
                      </div>
                      <Tag>ID: {meal.id}</Tag>
                    </Space>
                  }
                />
              </Card>
            </List.Item>
          )}
        />
      </Card>

      <Modal
        title={editingMeal ? 'Edit Meal' : 'Add New Meal'}
        open={isModalOpen}
        onCancel={() => {
          setIsModalOpen(false);
          form.resetFields();
        }}
        footer={null}
        destroyOnClose
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{
            price: 0,
            cookingTime: 1
          }}
        >
          <Form.Item
            name="name"
            label="Meal Name"
            rules={[
              { required: true, message: 'Please input meal name!' },
              { min: 2, message: 'Name must be at least 2 characters' }
            ]}
          >
            <Input placeholder="Enter meal name" />
          </Form.Item>

          <Form.Item
            name="price"
            label="Price ($)"
            rules={[
              { required: true, message: 'Please input price!' },
              { type: 'number', min: 0.01, message: 'Price must be at least $0.01' }
            ]}
          >
            <InputNumber
              min={0.01}
              step={0.01}
              style={{ width: '100%' }}
              formatter={(value) => `$ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
              parser={(value) => value.replace(/\$\s?|(,*)/g, '')}
            />
          </Form.Item>

          <Form.Item
            name="cookingTime"
            label="Cooking Time (minutes)"
            rules={[
              { required: true, message: 'Please input cooking time!' },
              { type: 'number', min: 1, max: 1440, message: 'Must be between 1-1440 minutes' }
            ]}
          >
            <InputNumber min={1} max={1440} style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item>
            <Button 
              type="primary" 
              htmlType="submit"
              loading={createMutation.isLoading || updateMutation.isLoading}
              block
            >
              {editingMeal ? 'Update Meal' : 'Add Meal'}
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default MealsPage;
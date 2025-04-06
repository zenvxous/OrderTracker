import { useState } from "react";
import { Card, Button, Modal, Form, Input, List, Space, Tag, message, Popconfirm } from "antd";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { 
  fetchCustomers, 
  createCustomer, 
  updateCustomer, 
  deleteCustomer 
} from "../api/customerApi";
import { EditOutlined, DeleteOutlined, UserOutlined, PhoneOutlined } from '@ant-design/icons';

const CustomersPage = () => {
  const queryClient = useQueryClient();
  const [form] = Form.useForm();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingCustomer, setEditingCustomer] = useState(null);

  const { data: customers, isLoading } = useQuery({
    queryKey: ['customers'],
    queryFn: fetchCustomers
  });

  const createMutation = useMutation({
    mutationFn: createCustomer,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['customers'] });
      message.success("Customer created!");
      setIsModalOpen(false);
      form.resetFields();
    },
  });

  const updateMutation = useMutation({
    mutationFn: (data) => updateCustomer(data.id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['customers'] });
      message.success("Customer updated!");
      setIsModalOpen(false);
      form.resetFields();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteCustomer,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['customers'] });
      message.success("Customer deleted!");
    },
  });

  const handleSubmit = (values) => {
    if (editingCustomer) {
      updateMutation.mutate({ ...values, id: editingCustomer.id });
    } else {
      createMutation.mutate(values);
    }
  };

  return (
    <div style={{ padding: 24 }}>
      <Card
        title="Customers Management"
        extra={
          <Button 
            type="primary" 
            onClick={() => {
              setEditingCustomer(null);
              setIsModalOpen(true);
            }}
            icon={<UserOutlined />}
          >
            Add Customer
          </Button>
        }
      >
        <List
          grid={{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 3, xl: 4, xxl: 4 }}
          dataSource={customers}
          loading={isLoading}
          renderItem={(customer) => (
            <List.Item>
              <Card
                actions={[
                  <EditOutlined 
                    key="edit" 
                    onClick={() => {
                      setEditingCustomer(customer);
                      form.setFieldsValue(customer);
                      setIsModalOpen(true);
                    }}
                  />,
                  <Popconfirm
                    title="Delete this customer?"
                    onConfirm={() => deleteMutation.mutate(customer.id)}
                    okText="Yes"
                    cancelText="No"
                  >
                    <DeleteOutlined key="delete" style={{ color: '#ff4d4f' }} />
                  </Popconfirm>
                ]}
              >
                <Card.Meta
                  avatar={
                    <div style={{
                      backgroundColor: '#1890ff',
                      borderRadius: '50%',
                      width: 32,
                      height: 32,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      color: 'white'
                    }}>
                      {customer.name.charAt(0)}
                    </div>
                  }
                  title={customer.name}
                  description={
                    <Space direction="vertical">
                      <div>
                        <PhoneOutlined /> {customer.phoneNumber}
                      </div>
                      <Tag color="blue">ID: {customer.id}</Tag>
                    </Space>
                  }
                />
              </Card>
            </List.Item>
          )}
        />
      </Card>

      <Modal
        title={
          <span>
            {editingCustomer ? (
              <>
                <EditOutlined /> Edit Customer
              </>
            ) : (
              <>
                <UserOutlined /> Add New Customer
              </>
            )}
          </span>
        }
        open={isModalOpen}
        onCancel={() => {
          setIsModalOpen(false);
          form.resetFields();
        }}
        footer={null}
      >
        <Form 
          form={form} 
          onFinish={handleSubmit}
          layout="vertical"
        >
          <Form.Item
            name="name"
            label="Full Name"
            rules={[{ required: true, message: "Please input customer name!" }]}
          >
            <Input prefix={<UserOutlined />} placeholder="John Doe" />
          </Form.Item>

          <Form.Item
            name="phoneNumber"
            label="Phone Number"
            rules={[{ 
              required: true, 
              message: "Please input phone number!" 
            }]}
          >
            <Input prefix={<PhoneOutlined />} placeholder="+1234567890" />
          </Form.Item>

          <Form.Item>
            <Button 
              type="primary" 
              htmlType="submit"
              loading={createMutation.isLoading || updateMutation.isLoading}
              block
            >
              {editingCustomer ? 'Update Customer' : 'Add Customer'}
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default CustomersPage;
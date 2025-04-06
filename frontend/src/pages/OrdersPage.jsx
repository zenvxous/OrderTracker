import { useState } from "react";
import { Card, Button, Modal, Form, Select, List, Space, Tag, message, Popconfirm } from "antd";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { fetchOrders, createOrder, updateOrderStatus, deleteOrder } from "../api/orderApi";
import { fetchCustomers } from "../api/customerApi";
import { CheckOutlined, CloseOutlined, FireOutlined, UserOutlined } from '@ant-design/icons';

const OrdersPage = () => {
  const queryClient = useQueryClient();
  const [form] = Form.useForm();
  const [isModalOpen, setIsModalOpen] = useState(false);

  const { data: orders, isLoading, error } = useQuery({
    queryKey: ['orders'],
    queryFn: fetchOrders
  });

  const { data: customers } = useQuery({
    queryKey: ['customers'],
    queryFn: fetchCustomers
  });

  const createOrderMutation = useMutation({
    mutationFn: (customerId) => createOrder(customerId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      message.success("Order created successfully!");
    },
  });

  const deleteOrderMutation = useMutation({
    mutationFn: deleteOrder,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      message.success("Order deleted successfully!");
    },
  });

  const updateStatusMutation = useMutation({
    mutationFn: ({ id, status }) => updateOrderStatus(id, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      message.success("Status updated!");
    },
  });

  if (error) return <div>Error loading orders: {error.message}</div>;

  const getStatusTag = (status) => {
    switch(status) {
      case 'READY':
        return <Tag icon={<CheckOutlined />} color="green">READY</Tag>;
      case 'ACCEPTED':
        return <Tag icon={<UserOutlined />} color="blue">ACCEPTED</Tag>;
      default:
        return <Tag icon={<FireOutlined />} color="orange">COOKING</Tag>;
    }
  };

  return (
    <div style={{ padding: 24 }}>
      <Card
        title="Orders Management"
        extra={
          <Button 
            type="primary" 
            onClick={() => setIsModalOpen(true)}
            icon={<UserOutlined />}
          >
            Create Order
          </Button>
        }
      >
        <List
          grid={{ gutter: 16, xs: 1, sm: 2, md: 2, lg: 3, xl: 3 }}
          dataSource={orders}
          loading={isLoading}
          renderItem={(order) => (
            <List.Item>
              <Card
                title={`Order #${order.id}`}
                actions={[
                  <Select
                    defaultValue={order.status}
                    style={{ width: '100%' }}
                    onChange={(value) => updateStatusMutation.mutate({
                      id: order.id,
                      status: value
                    })}
                    options={[
                      { value: 'COOKING', label: 'Cooking' },
                      { value: 'READY', label: 'Ready' },
                      { value: 'ACCEPTED', label: 'Accepted' },
                    ]}
                  />,
                  <Popconfirm
                    title="Delete this order?"
                    onConfirm={() => deleteOrderMutation.mutate(order.id)}
                    okText="Yes"
                    cancelText="No"
                    icon={<CloseOutlined style={{ color: 'red' }} />}
                  >
                    <Button 
                      danger 
                      loading={deleteOrderMutation.isPending && deleteOrderMutation.variables === order.id}
                    >
                      Delete Order
                    </Button>
                  </Popconfirm>
                ]}
              >
                <Space direction="vertical" size="middle">
                  <div>
                    <strong>Status:</strong> {getStatusTag(order.status)}
                  </div>
                  {order.customer && (
                    <div>
                      <strong>Customer:</strong> {order.customer.name} (ID: {order.customer.id})
                    </div>
                  )}
                  {order.meals?.length > 0 && (
                    <div>
                      <strong>Meals:</strong> {order.meals.length}
                    </div>
                  )}
                </Space>
              </Card>
            </List.Item>
          )}
        />
      </Card>

      <Modal
        title="Create New Order"
        open={isModalOpen}
        onCancel={() => {
          setIsModalOpen(false);
          form.resetFields();
        }}
        footer={null}
      >
        <Form
          form={form}
          onFinish={(values) => {
            createOrderMutation.mutate(values.customerId);
            setIsModalOpen(false);
            form.resetFields();
          }}
        >
          <Form.Item
            name="customerId"
            label="Customer"
            rules={[{ required: true, message: 'Please select a customer!' }]}
          >
            <Select
              placeholder="Select a customer"
              options={customers?.map(c => ({
                value: c.id,
                label: `${c.name} (ID: ${c.id})`
              }))}
            />
          </Form.Item>
          <Form.Item>
            <Button 
              type="primary" 
              htmlType="submit"
              loading={createOrderMutation.isLoading}
              block
            >
              Create Order
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default OrdersPage;
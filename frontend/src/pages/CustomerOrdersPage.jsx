import { useState } from 'react';
import { Table, Button, Modal, Form, Select, Space, Tag, message, Card, Descriptions } from 'antd';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchCustomers, fetchCustomerOrders, createOrderForCustomer } from '../api/customerApi';
import { fetchMeals } from '../api/mealApi';

const CustomersOrdersPage = () => {
  const queryClient = useQueryClient();
  const [selectedCustomer, setSelectedCustomer] = useState(null);
  const [selectedMeals, setSelectedMeals] = useState([]);
  const [isOrderModalOpen, setIsOrderModalOpen] = useState(false);

  const { data: customers, isLoading: customersLoading } = useQuery({
    queryKey: ['customers'],
    queryFn: fetchCustomers
  });

  const { data: meals } = useQuery({
    queryKey: ['meals'],
    queryFn: fetchMeals
  });

  const { data: customerOrders, isLoading: ordersLoading } = useQuery({
    queryKey: ['customer-orders', selectedCustomer?.id],
    queryFn: () => fetchCustomerOrders(selectedCustomer?.id),
    enabled: !!selectedCustomer
  });

  const createOrderMutation = useMutation({
    mutationFn: ({ customerId, mealIds }) => 
      createOrderForCustomer(customerId, mealIds),
    onSuccess: () => {
      queryClient.invalidateQueries(['customer-orders']);
      message.success('Order created successfully!');
      setIsOrderModalOpen(false);
      setSelectedMeals([]);
    },
    onError: (error) => {
      message.error(error.response?.data?.message || 'Failed to create order');
    }
  });

  const columns = [
    {
      title: 'Order ID',
      dataIndex: 'id',
      key: 'id',
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: (status) => (
        <Tag color={
          status === 'COMPLETED' ? 'green' : 
          status === 'CANCELLED' ? 'red' : 'orange'
        }>
          {status}
        </Tag>
      ),
    },
    {
      title: 'Meals',
      dataIndex: 'meals',
      key: 'meals',
      render: (meals) => (
        <div>
          {meals?.map(meal => (
            <Tag key={meal.id}>{meal.name} (${meal.price})</Tag>
          ))}
        </div>
      ),
    },
    {
      title: 'Total',
      key: 'total',
      render: (_, record) => (
        <span>
          ${record.meals?.reduce((sum, meal) => sum + meal.price, 0).toFixed(2)}
        </span>
      ),
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <Card title="Select Customer" style={{ marginBottom: 24 }}>
        <Select
          style={{ width: '100%' }}
          placeholder="Select a customer"
          onChange={(value) => setSelectedCustomer(
            customers.find(c => c.id === value)
          )}
          options={customers?.map(customer => ({
            value: customer.id,
            label: `${customer.name} (ID: ${customer.id})`
          }))}
        />
      </Card>

      {selectedCustomer && (
        <>
          <Card 
            title={`Orders for ${selectedCustomer.name}`}
            extra={
              <Button 
                type="primary" 
                onClick={() => setIsOrderModalOpen(true)}
              >
                Create New Order
              </Button>
            }
          >
            <Descriptions bordered column={1}>
              <Descriptions.Item label="Phone">
                {selectedCustomer.phoneNumber}
              </Descriptions.Item>
              <Descriptions.Item label="Total Orders">
                {customerOrders?.length || 0}
              </Descriptions.Item>
            </Descriptions>

            <Table
              columns={columns}
              dataSource={customerOrders}
              rowKey="id"
              loading={ordersLoading}
              style={{ marginTop: 16 }}
            />
          </Card>
        </>
      )}

      {/* Модальное окно создания заказа */}
      <Modal
        title={`Create Order for ${selectedCustomer?.name}`}
        open={isOrderModalOpen}
        onCancel={() => {
          setIsOrderModalOpen(false);
          setSelectedMeals([]);
        }}
        footer={null}
        width={800}
      >
        <Form
          onFinish={() => {
            createOrderMutation.mutate({
              customerId: selectedCustomer.id,
              mealIds: selectedMeals
            });
          }}
        >
          <Form.Item
            label="Select Meals"
            rules={[{ required: true, message: 'Please select at least one meal!' }]}
          >
            <Select
              mode="multiple"
              placeholder="Select meals"
              value={selectedMeals}
              onChange={setSelectedMeals}
              options={meals?.map(meal => ({
                value: meal.id,
                label: `${meal.name} ($${meal.price})`
              }))}
              style={{ width: '100%' }}
            />
          </Form.Item>
          
          <div style={{ marginTop: 16 }}>
            <h4>Selected Meals:</h4>
            <ul>
              {selectedMeals.map(mealId => {
                const meal = meals?.find(m => m.id === mealId);
                return meal ? (
                  <li key={meal.id}>
                    {meal.name} (${meal.price})
                  </li>
                ) : null;
              })}
            </ul>
            <p>
              <strong>Total:</strong> $
              {selectedMeals.reduce((sum, mealId) => {
                const meal = meals?.find(m => m.id === mealId);
                return sum + (meal?.price || 0);
              }, 0).toFixed(2)}
            </p>
          </div>

          <Form.Item style={{ marginTop: 24 }}>
            <Button 
              type="primary" 
              htmlType="submit"
              loading={createOrderMutation.isLoading}
              disabled={selectedMeals.length === 0}
            >
              Create Order
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default CustomersOrdersPage;
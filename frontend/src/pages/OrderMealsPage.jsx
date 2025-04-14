import { useState } from 'react';
import { Card, Button, Modal, Form, Select, List, Tag, message, Popconfirm, Divider } from 'antd';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchMeals } from '../api/mealApi';
import { 
  fetchOrders, 
  addMealToOrder, 
  removeMealFromOrder,
  addMultipleMealsToOrder,
  updateOrderStatus,
  deleteOrder
} from '../api/orderApi';
import { PlusOutlined, DeleteOutlined, ShoppingOutlined, EditOutlined } from '@ant-design/icons';

const OrdersMealsPage = () => {
  const queryClient = useQueryClient();
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [selectedMeals, setSelectedMeals] = useState([]);
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [isBulkAddModalOpen, setIsBulkAddModalOpen] = useState(false);
  const [isStatusModalOpen, setIsStatusModalOpen] = useState(false);

  // Загрузка данных
  const { data: orders, isLoading } = useQuery({
    queryKey: ['orders-with-meals'],
    queryFn: fetchOrders,
    select: data => data.map(order => ({
      ...order,
      key: order.id
    }))
  });

  const { data: meals } = useQuery({
    queryKey: ['all-meals'],
    queryFn: fetchMeals
  });

  // Мутации
  const addMealMutation = useMutation({
    mutationFn: ({ orderId, mealId }) => addMealToOrder(orderId, mealId),
    onSuccess: () => {
      queryClient.invalidateQueries(['orders-with-meals']);
      message.success('Meal added!');
      setIsAddModalOpen(false);
    }
  });

  const removeMealMutation = useMutation({
    mutationFn: ({ orderId, mealId }) => removeMealFromOrder(orderId, mealId),
    onSuccess: () => {
      queryClient.invalidateQueries(['orders-with-meals']);
      message.success('Meal removed!');
    }
  });

  const addMultipleMealsMutation = useMutation({
    mutationFn: ({ orderId, mealIds }) => addMultipleMealsToOrder(orderId, mealIds),
    onSuccess: () => {
      queryClient.invalidateQueries(['orders-with-meals']);
      message.success('Meals added!');
      setSelectedMeals([]);
      setIsBulkAddModalOpen(false);
    }
  });

  const updateStatusMutation = useMutation({
    mutationFn: ({ orderId, status }) => updateOrderStatus(orderId, status),
    onSuccess: () => {
      queryClient.invalidateQueries(['orders-with-meals']);
      message.success('Status updated!');
      setIsStatusModalOpen(false);
    }
  });

  const deleteOrderMutation = useMutation({
    mutationFn: (orderId) => deleteOrder(orderId),
    onSuccess: () => {
      queryClient.invalidateQueries(['orders-with-meals']);
      message.success('Order deleted!');
    }
  });

  const statusColors = {
    'READY': '#52c41a',
    'COOKING': '#fa8c16',
    'ACCEPTED': '#1890ff'
  };

  const statusOptions = [
    { value: 'READY', label: 'Ready' },
    { value: 'COOKING', label: 'Cooking' },
    { value: 'ACCEPTED', label: 'Accepted' }
  ];

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto', padding: 24 }}>
      <Card
        bordered={false}
        bodyStyle={{ padding: 0 }}
      >
        <div style={{ 
          display: 'flex', 
          justifyContent: 'space-between', 
          alignItems: 'center',
          marginBottom: 24,
          padding: '0 16px',
          paddingTop: 16 // Добавлен отступ сверху
        }}>
          <h2 style={{ margin: 0 }}>Order Management</h2>
          <Button 
            type="primary" 
            icon={<ShoppingOutlined />}
            onClick={() => orders?.length > 0 && setIsBulkAddModalOpen(true)}
            style={{ marginTop: 16 }} // Добавлен отступ сверху для кнопки
          >
            Add Meals
          </Button>
        </div>

        <Divider style={{ margin: 0 }} />

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: 16, padding: 16 }}>
          {orders?.map(order => (
            <Card
              key={order.id}
              bordered={false}
              style={{ 
                boxShadow: '0 1px 2px rgba(0,0,0,0.1)',
                borderRadius: 8,
                transition: 'all 0.3s',
                display: 'flex',
                flexDirection: 'column',
                height: '100%', // Фиксированная высота карточки
                ':hover': {
                  boxShadow: '0 4px 12px rgba(0,0,0,0.15)'
                }
              }}
              bodyStyle={{
                display: 'flex',
                flexDirection: 'column',
                flexGrow: 1,
                padding: 16
              }}
              title={
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>Order #{order.id}</span>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <Tag 
                      color={statusColors[order.status]} 
                      style={{ 
                        border: 'none',
                        fontWeight: 500,
                        borderRadius: 12,
                        padding: '0 12px'
                      }}
                    >
                      {order.status}
                    </Tag>
                    <Button 
                      size="small" 
                      type="text" 
                      icon={<EditOutlined />} 
                      onClick={() => {
                        setSelectedOrder(order);
                        setIsStatusModalOpen(true);
                      }}
                    />
                  </div>
                </div>
              }
            >
              <div style={{ marginBottom: 16, flexGrow: 1 }}>
                <div style={{ 
                  display: 'flex', 
                  justifyContent: 'space-between',
                  marginBottom: 8
                }}>
                  <span style={{ color: '#595959' }}>Total meals</span>
                  <span>{order.meals?.length || 0}</span>
                </div>
                <div style={{ 
                  display: 'flex', 
                  justifyContent: 'space-between',
                  marginBottom: 8
                }}>
                  <span style={{ color: '#595959' }}>Total price</span>
                  <span style={{ fontWeight: 500 }}>
                    ${order.meals?.reduce((sum, meal) => sum + meal.price, 0).toFixed(2)}
                  </span>
                </div>

                <Divider style={{ margin: '16px 0' }} />

                <div style={{ maxHeight: 200, overflowY: 'auto', marginBottom: 16 }}>
                  {order.meals?.map(meal => (
                    <div 
                      key={meal.id} 
                      style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        padding: '8px 0',
                        borderBottom: '1px solid #f0f0f0'
                      }}
                    >
                      <div>
                        <div style={{ fontWeight: 500 }}>{meal.name}</div>
                        <div style={{ color: '#8c8c8c', fontSize: 12 }}>
                          ${meal.price.toFixed(2)}
                        </div>
                      </div>
                      <Popconfirm
                        title="Remove this meal?"
                        onConfirm={() => removeMealMutation.mutate({ 
                          orderId: order.id, 
                          mealId: meal.id 
                        })}
                      >
                        <Button 
                          size="small" 
                          type="text" 
                          danger 
                          icon={<DeleteOutlined />}
                          style={{ color: '#ff4d4f' }}
                        />
                      </Popconfirm>
                    </div>
                  ))}
                </div>
              </div>

              <div style={{ marginTop: 'auto' }}> {/* Кнопки всегда внизу */}
                <div style={{ display: 'flex', gap: 8, marginBottom: 8 }}>
                  <Button 
                    type="text"
                    icon={<PlusOutlined />}
                    onClick={() => {
                      setSelectedOrder(order);
                      setIsAddModalOpen(true);
                    }}
                    style={{ flex: 1 }}
                  >
                    Add meal
                  </Button>
                  <Button 
                    type="text"
                    icon={<PlusOutlined />}
                    onClick={() => {
                      setSelectedOrder(order);
                      setIsBulkAddModalOpen(true);
                    }}
                    style={{ flex: 1 }}
                  >
                    Add multiple
                  </Button>
                </div>
                <Popconfirm
                  title="Are you sure to delete this order?"
                  onConfirm={() => deleteOrderMutation.mutate(order.id)}
                  okText="Yes"
                  cancelText="No"
                >
                  <Button 
                    danger
                    type="text"
                    icon={<DeleteOutlined />}
                    style={{ width: '100%' }}
                  >
                    Delete Order
                  </Button>
                </Popconfirm>
              </div>
            </Card>
          ))}
        </div>
      </Card>

      {/* Модальное окно для добавления одного блюда */}
      <Modal
        title={
          <span style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <PlusOutlined />
            Add meal to order #{selectedOrder?.id}
          </span>
        }
        open={isAddModalOpen}
        onCancel={() => setIsAddModalOpen(false)}
        footer={null}
        width={480}
        bodyStyle={{ padding: '24px 24px 0' }}
      >
        <Form
          onFinish={(values) => {
            addMealMutation.mutate({
              orderId: selectedOrder.id,
              mealId: values.mealId
            });
          }}
        >
          <Form.Item
            name="mealId"
            rules={[{ required: true, message: 'Please select a meal!' }]}
          >
            <Select
              placeholder="Select meal"
              options={meals?.map(meal => ({
                value: meal.id,
                label: `${meal.name} ($${meal.price.toFixed(2)})`
              }))}
              style={{ width: '100%' }}
              showSearch
              optionFilterProp="label"
            />
          </Form.Item>
          <Form.Item style={{ marginBottom: 0 }}>
            <Button 
              type="primary" 
              htmlType="submit"
              loading={addMealMutation.isLoading}
              block
              size="large"
            >
              Add meal
            </Button>
          </Form.Item>
        </Form>
      </Modal>

      {/* Модальное окно для добавления нескольких блюд */}
      <Modal
        title={
          <span style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <PlusOutlined />
            Add multiple meals to order #{selectedOrder?.id}
          </span>
        }
        open={isBulkAddModalOpen}
        onCancel={() => {
          setIsBulkAddModalOpen(false);
          setSelectedMeals([]);
        }}
        footer={null}
        width={640}
        bodyStyle={{ padding: '24px 24px 0' }}
      >
        <Form
          onFinish={() => {
            addMultipleMealsMutation.mutate({
              orderId: selectedOrder.id,
              mealIds: selectedMeals
            });
          }}
        >
          <Form.Item
            rules={[{ required: true, message: 'Please select at least one meal!' }]}
          >
            <Select
              mode="multiple"
              placeholder="Select meals"
              value={selectedMeals}
              onChange={setSelectedMeals}
              options={meals?.map(meal => ({
                value: meal.id,
                label: `${meal.name} ($${meal.price.toFixed(2)})`
              }))}
              style={{ width: '100%' }}
              showSearch
              optionFilterProp="label"
              size="large"
            />
          </Form.Item>

          {selectedMeals.length > 0 && (
            <div style={{ 
              margin: '16px 0 24px',
              padding: 16,
              border: '1px solid #f0f0f0',
              borderRadius: 8
            }}>
              <div style={{ color: '#8c8c8c', marginBottom: 8 }}>Selected meals:</div>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                {selectedMeals.map(id => {
                  const meal = meals?.find(m => m.id === id);
                  return meal && (
                    <Tag key={meal.id} closable onClose={() => {
                      setSelectedMeals(selectedMeals.filter(mId => mId !== id));
                    }}>
                      {meal.name}
                    </Tag>
                  );
                })}
              </div>
            </div>
          )}

          <Form.Item style={{ marginBottom: 0 }}>
            <Button 
              type="primary" 
              htmlType="submit"
              loading={addMultipleMealsMutation.isLoading}
              block
              size="large"
              disabled={selectedMeals.length === 0}
            >
              Add {selectedMeals.length} meal{selectedMeals.length !== 1 ? 's' : ''}
            </Button>
          </Form.Item>
        </Form>
      </Modal>

      {/* Модальное окно для изменения статуса заказа */}
      <Modal
        title={`Change status for order #${selectedOrder?.id}`}
        open={isStatusModalOpen}
        onCancel={() => setIsStatusModalOpen(false)}
        footer={null}
        width={480}
        bodyStyle={{ padding: '24px 24px 0' }}
      >
        <Form
          initialValues={{ status: selectedOrder?.status }}
          onFinish={(values) => {
            updateStatusMutation.mutate({
              orderId: selectedOrder.id,
              status: values.status
            });
          }}
        >
          <Form.Item
            name="status"
            rules={[{ required: true, message: 'Please select a status!' }]}
          >
            <Select
              placeholder="Select status"
              options={statusOptions}
              style={{ width: '100%' }}
            />
          </Form.Item>
          <Form.Item style={{ marginBottom: 0 }}>
            <Button 
              type="primary" 
              htmlType="submit"
              loading={updateStatusMutation.isLoading}
              block
              size="large"
            >
              Update Status
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default OrdersMealsPage;
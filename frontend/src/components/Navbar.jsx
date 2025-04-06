import { Menu } from "antd";
import { Link } from "react-router-dom";

const Navbar = () => {
  const items = [
    { key: "customers", label: <Link to="/customers">Customers</Link> },
    { key: "meals", label: <Link to="/meals">Meals</Link> },
    { key: "orders", label: <Link to="/orders">Orders</Link> },
    { key: "ordermeals", label: <Link to="/ordermeals">Order-meals</Link> },
    { key: "customerorders", label: <Link to="/customerorders">Customer-orders</Link> },
  ];

  return <Menu mode="horizontal" items={items} theme="dark" />;
};

export default Navbar;
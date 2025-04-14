import { Menu } from "antd";
import { Link } from "react-router-dom";

const Navbar = () => {
  const items = [
    { key: "customers", label: <Link to="/customers">Customers</Link> },
    { key: "customer", label: <Link to="/customer">Customer</Link> },
    { key: "orders", label: <Link to="/orders">Orders</Link> },
    { key: "meals", label: <Link to="/meals">Meals</Link> }
  ];

  return <Menu mode="horizontal" items={items} theme="dark" />;
};

export default Navbar;
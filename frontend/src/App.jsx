import { BrowserRouter, Routes, Route } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { Layout } from "antd";
import Navbar from "./components/navbar";
import CustomersPage from "./pages/CustomersPage";
import MealsPage from "./pages/MealsPage";
import OrdersMealsPage from "./pages/OrderMealsPage";
import CustomersOrdersPage from "./pages/CustomerOrdersPage";

const { Header, Content } = Layout;
const queryClient = new QueryClient();

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Layout style={{ minHeight: "100vh" }}>
          <Header>
            <Navbar />
          </Header>
          <Content style={{ padding: "20px" }}>
            <Routes>
              <Route path="/customers" element={<CustomersPage />} />
              <Route path="/meals" element={<MealsPage />} />
              <Route path="/orders" element={<OrdersMealsPage />} />
              <Route path="/customer" element={<CustomersOrdersPage />} />
            </Routes>
          </Content>
        </Layout>
      </BrowserRouter>
    </QueryClientProvider>
  );
}

export default App;
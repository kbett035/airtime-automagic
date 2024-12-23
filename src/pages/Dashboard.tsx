import { useEffect } from "react";
import { useNavigate, Routes, Route } from "react-router-dom";
import { supabase } from "@/integrations/supabase/client";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Power, List, Menu } from "lucide-react";
import BotSettings from "@/components/BotSettings";
import TransactionHistory from "@/components/TransactionHistory";
import USSDPatterns from "@/components/USSDPatterns";
import MobileNav from "@/components/MobileNav";

const DashboardOverview = () => (
  <>
    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 md:gap-8">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-base md:text-lg">
            <Power className="h-4 w-4 md:h-5 md:w-5" />
            Bot Settings
          </CardTitle>
        </CardHeader>
        <CardContent>
          <BotSettings />
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-base md:text-lg">
            <List className="h-4 w-4 md:h-5 md:w-5" />
            Recent Transactions
          </CardTitle>
        </CardHeader>
        <CardContent>
          <TransactionHistory />
        </CardContent>
      </Card>
    </div>
  </>
);

const Dashboard = () => {
  const navigate = useNavigate();

  useEffect(() => {
    const checkUser = async () => {
      const { data: { session } } = await supabase.auth.getSession();
      if (!session) {
        navigate("/auth");
      }
    };

    checkUser();
  }, [navigate]);

  const handleLogout = async () => {
    await supabase.auth.signOut();
    navigate("/auth");
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="p-4 md:p-8 pb-20 md:pb-8">
        <div className="max-w-7xl mx-auto space-y-4 md:space-y-8">
          <div className="flex justify-between items-center">
            <h1 className="text-xl md:text-3xl font-bold">Airtime Bot Dashboard</h1>
            <Button variant="outline" size="sm" className="md:size-default" onClick={handleLogout}>
              <span className="hidden md:inline">Logout</span>
              <Power className="h-4 w-4 md:hidden" />
            </Button>
          </div>

          <Routes>
            <Route index element={<DashboardOverview />} />
            <Route path="patterns" element={
              <Card>
                <CardHeader>
                  <CardTitle className="text-base md:text-lg">USSD Patterns</CardTitle>
                </CardHeader>
                <CardContent>
                  <USSDPatterns />
                </CardContent>
              </Card>
            } />
            <Route path="settings" element={
              <Card>
                <CardHeader>
                  <CardTitle className="text-base md:text-lg">Bot Settings</CardTitle>
                </CardHeader>
                <CardContent>
                  <BotSettings />
                </CardContent>
              </Card>
            } />
          </Routes>
        </div>
      </div>
      <MobileNav />
    </div>
  );
};

export default Dashboard;
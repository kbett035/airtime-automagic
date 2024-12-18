import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { supabase } from "@/integrations/supabase/client";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Power, List, MessageSquare } from "lucide-react";
import { useToast } from "@/hooks/use-toast";

const Dashboard = () => {
  const navigate = useNavigate();
  const { toast } = useToast();
  const [settings, setSettings] = useState({ is_enabled: false, ussd_format: "*544*4*6*{phone}#" });
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const checkUser = async () => {
      const { data: { session } } = await supabase.auth.getSession();
      if (!session) {
        navigate("/auth");
        return;
      }
      fetchData();
    };

    checkUser();
  }, [navigate]);

  const fetchData = async () => {
    try {
      // Fetch bot settings
      const { data: settingsData, error: settingsError } = await supabase
        .from("bot_settings")
        .select("*")
        .maybeSingle();

      if (settingsError && settingsError.code !== "PGRST116") {
        console.error("Error fetching settings:", settingsError);
        toast({
          title: "Error",
          description: "Failed to load bot settings",
          variant: "destructive",
        });
      }

      // If no settings exist, create default settings
      if (!settingsData) {
        const { data: newSettings, error: createError } = await supabase
          .from("bot_settings")
          .insert([{
            is_enabled: false,
            ussd_format: "*544*4*6*{phone}#",
            user_id: (await supabase.auth.getUser()).data.user?.id
          }])
          .select()
          .single();

        if (createError) {
          console.error("Error creating settings:", createError);
          toast({
            title: "Error",
            description: "Failed to create default settings",
            variant: "destructive",
          });
        } else if (newSettings) {
          setSettings(newSettings);
        }
      } else {
        setSettings(settingsData);
      }

      // Fetch recent transactions
      const { data: transactionsData, error: transactionsError } = await supabase
        .from("transaction_logs")
        .select("*")
        .order("created_at", { ascending: false })
        .limit(10);

      if (transactionsError) {
        console.error("Error fetching transactions:", transactionsError);
        toast({
          title: "Error",
          description: "Failed to load transactions",
          variant: "destructive",
        });
      } else {
        setTransactions(transactionsData || []);
      }
    } finally {
      setLoading(false);
    }
  };

  const updateSettings = async (newSettings) => {
    try {
      const { error } = await supabase
        .from("bot_settings")
        .upsert({ 
          ...settings, 
          ...newSettings,
          user_id: (await supabase.auth.getUser()).data.user?.id 
        });

      if (error) throw error;

      setSettings(prev => ({ ...prev, ...newSettings }));
      toast({
        title: "Success",
        description: "Bot settings updated successfully",
      });
    } catch (error) {
      console.error("Error updating settings:", error);
      toast({
        title: "Error",
        description: "Failed to update bot settings",
        variant: "destructive",
      });
    }
  };

  const handleLogout = async () => {
    await supabase.auth.signOut();
    navigate("/auth");
  };

  if (loading) {
    return <div className="flex items-center justify-center min-h-screen">Loading...</div>;
  }

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <div className="max-w-7xl mx-auto space-y-8">
        <div className="flex justify-between items-center">
          <h1 className="text-3xl font-bold">Airtime Bot Dashboard</h1>
          <Button variant="outline" onClick={handleLogout}>Logout</Button>
        </div>

        <div className="grid md:grid-cols-2 gap-8">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Power className="h-5 w-5" />
                Bot Settings
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="flex items-center justify-between">
                <Label htmlFor="bot-status">Bot Status</Label>
                <Switch
                  id="bot-status"
                  checked={settings.is_enabled}
                  onCheckedChange={(checked) => updateSettings({ is_enabled: checked })}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="ussd-format">USSD Format</Label>
                <Input
                  id="ussd-format"
                  value={settings.ussd_format}
                  onChange={(e) => updateSettings({ ussd_format: e.target.value })}
                  placeholder="Enter USSD format e.g. *544*4*6*{phone}#"
                />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <List className="h-5 w-5" />
                Recent Transactions
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="relative overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Amount</TableHead>
                      <TableHead>Phone</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead>Date</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {transactions.map((tx) => (
                      <TableRow key={tx.id}>
                        <TableCell>{tx.amount}</TableCell>
                        <TableCell>{tx.sender_phone}</TableCell>
                        <TableCell>{tx.status}</TableCell>
                        <TableCell>{new Date(tx.created_at).toLocaleDateString()}</TableCell>
                      </TableRow>
                    ))}
                    {transactions.length === 0 && (
                      <TableRow>
                        <TableCell colSpan={4} className="text-center py-4">
                          <div className="flex flex-col items-center gap-2 text-gray-500">
                            <MessageSquare className="h-8 w-8" />
                            <p>No transactions yet</p>
                          </div>
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
import { useQuery } from "@tanstack/react-query";
import { supabase } from "@/integrations/supabase/client";
import { MessageSquare } from "lucide-react";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

const TransactionHistory = () => {
  const { data: transactions } = useQuery({
    queryKey: ["transactions"],
    queryFn: async () => {
      const { data, error } = await supabase
        .from("transaction_logs")
        .select("*")
        .order("created_at", { ascending: false })
        .limit(10);

      if (error) throw error;
      return data || [];
    },
  });

  return (
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
          {transactions?.map((tx) => (
            <TableRow key={tx.id}>
              <TableCell>{tx.amount}</TableCell>
              <TableCell>{tx.sender_phone}</TableCell>
              <TableCell>{tx.status}</TableCell>
              <TableCell>{new Date(tx.created_at).toLocaleDateString()}</TableCell>
            </TableRow>
          ))}
          {(!transactions || transactions.length === 0) && (
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
  );
};

export default TransactionHistory;
import { useState } from "react";
import { Plus, Trash } from "lucide-react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { supabase } from "@/integrations/supabase/client";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useToast } from "@/hooks/use-toast";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

type PatternType = "regex" | "exact";

interface Pattern {
  id: string;
  pattern: string;
  amount: number;
  pattern_type: PatternType;
}

interface NewPattern {
  pattern: string;
  amount: string;
  pattern_type: PatternType;
}

const USSDPatterns = () => {
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const [newPattern, setNewPattern] = useState<NewPattern>({ 
    pattern: "", 
    amount: "", 
    pattern_type: "regex" 
  });

  const { data: patterns, isLoading } = useQuery({
    queryKey: ["ussd-patterns"],
    queryFn: async () => {
      const { data, error } = await supabase
        .from("ussd_patterns")
        .select("*")
        .order("created_at", { ascending: false });

      if (error) throw error;
      return data as Pattern[];
    },
  });

  const addPattern = useMutation({
    mutationFn: async () => {
      const { error } = await supabase.from("ussd_patterns").insert([
        {
          pattern: newPattern.pattern,
          amount: parseFloat(newPattern.amount),
          pattern_type: newPattern.pattern_type,
        },
      ]);
      if (error) throw error;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["ussd-patterns"] });
      setNewPattern({ pattern: "", amount: "", pattern_type: "regex" });
      toast({ title: "Success", description: "Pattern added successfully" });
    },
    onError: (error) => {
      toast({
        title: "Error",
        description: "Failed to add pattern: " + error.message,
        variant: "destructive",
      });
    },
  });

  const deletePattern = useMutation({
    mutationFn: async (id: string) => {
      const { error } = await supabase.from("ussd_patterns").delete().eq("id", id);
      if (error) throw error;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["ussd-patterns"] });
      toast({ title: "Success", description: "Pattern deleted successfully" });
    },
    onError: (error) => {
      toast({
        title: "Error",
        description: "Failed to delete pattern: " + error.message,
        variant: "destructive",
      });
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newPattern.pattern || !newPattern.amount) {
      toast({
        title: "Error",
        description: "Please fill in all fields",
        variant: "destructive",
      });
      return;
    }
    addPattern.mutate();
  };

  if (isLoading) return <div>Loading patterns...</div>;

  return (
    <div className="space-y-4">
      <form onSubmit={handleSubmit} className="flex gap-4 items-end">
        <div className="space-y-2">
          <label className="text-sm font-medium">Pattern</label>
          <Input
            value={newPattern.pattern}
            onChange={(e) =>
              setNewPattern((prev) => ({ ...prev, pattern: e.target.value }))
            }
            placeholder="Enter pattern"
          />
        </div>
        <div className="space-y-2">
          <label className="text-sm font-medium">Amount</label>
          <Input
            type="number"
            value={newPattern.amount}
            onChange={(e) =>
              setNewPattern((prev) => ({ ...prev, amount: e.target.value }))
            }
            placeholder="Enter amount"
          />
        </div>
        <div className="space-y-2">
          <label className="text-sm font-medium">Type</label>
          <Select
            value={newPattern.pattern_type}
            onValueChange={(value: PatternType) =>
              setNewPattern((prev) => ({ ...prev, pattern_type: value }))
            }
          >
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="regex">Regex</SelectItem>
              <SelectItem value="exact">Exact Match</SelectItem>
            </SelectContent>
          </Select>
        </div>
        <Button type="submit">
          <Plus className="w-4 h-4 mr-2" />
          Add Pattern
        </Button>
      </form>

      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Pattern</TableHead>
            <TableHead>Type</TableHead>
            <TableHead>Amount</TableHead>
            <TableHead>Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {patterns?.map((pattern) => (
            <TableRow key={pattern.id}>
              <TableCell>{pattern.pattern}</TableCell>
              <TableCell>{pattern.pattern_type}</TableCell>
              <TableCell>{pattern.amount}</TableCell>
              <TableCell>
                <Button
                  variant="ghost"
                  size="icon"
                  onClick={() => deletePattern.mutate(pattern.id)}
                >
                  <Trash className="w-4 h-4" />
                </Button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
};

export default USSDPatterns;
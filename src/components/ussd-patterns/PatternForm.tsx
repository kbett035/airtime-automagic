import { useState } from "react";
import { Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useToast } from "@/hooks/use-toast";

interface PatternFormProps {
  onSubmit: (pattern: NewPattern) => void;
}

export interface NewPattern {
  pattern: string;
  amount: string;
  pattern_type: "regex" | "exact";
  ussd_format: string;
  description?: string;
}

export const PatternForm = ({ onSubmit }: PatternFormProps) => {
  const { toast } = useToast();
  const [newPattern, setNewPattern] = useState<NewPattern>({
    pattern: "",
    amount: "",
    pattern_type: "exact",
    ussd_format: "*180*5*2*{phone}*1*1#",
    description: "",
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newPattern.pattern || !newPattern.amount || !newPattern.ussd_format) {
      toast({
        title: "Error",
        description: "Please fill in all required fields",
        variant: "destructive",
      });
      return;
    }
    onSubmit(newPattern);
    setNewPattern({
      pattern: "",
      amount: "",
      pattern_type: "exact",
      ussd_format: "*180*5*2*{phone}*1*1#",
      description: "",
    });
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="pattern">Bundle Name</Label>
          <Input
            id="pattern"
            value={newPattern.pattern}
            onChange={(e) =>
              setNewPattern((prev) => ({ ...prev, pattern: e.target.value }))
            }
            placeholder="e.g., 1.5GB Bundle - 50 KES"
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="amount">Amount (KES)</Label>
          <Input
            id="amount"
            type="number"
            value={newPattern.amount}
            onChange={(e) =>
              setNewPattern((prev) => ({ ...prev, amount: e.target.value }))
            }
            placeholder="e.g., 50"
          />
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="ussd-format">USSD Format</Label>
          <Input
            id="ussd-format"
            value={newPattern.ussd_format}
            onChange={(e) =>
              setNewPattern((prev) => ({ ...prev, ussd_format: e.target.value }))
            }
            placeholder="e.g., *180*5*2*{phone}*1*1#"
            className="font-mono"
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="description">Description (Optional)</Label>
          <Input
            id="description"
            value={newPattern.description}
            onChange={(e) =>
              setNewPattern((prev) => ({ ...prev, description: e.target.value }))
            }
            placeholder="e.g., 1.5GB valid for 30 days"
          />
        </div>
      </div>

      <div className="flex justify-end">
        <Button type="submit">
          <Plus className="w-4 h-4 mr-2" />
          Add Pattern
        </Button>
      </div>
    </form>
  );
};
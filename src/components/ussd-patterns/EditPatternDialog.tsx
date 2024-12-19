import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useToast } from "@/hooks/use-toast";

interface Pattern {
  id: string;
  pattern: string;
  amount: number;
  pattern_type: "regex" | "exact";
  ussd_format: string;
  description?: string;
}

interface EditPatternDialogProps {
  pattern: Pattern | null;
  isOpen: boolean;
  onClose: () => void;
  onSave: (pattern: Pattern) => void;
}

export const EditPatternDialog = ({
  pattern,
  isOpen,
  onClose,
  onSave,
}: EditPatternDialogProps) => {
  const { toast } = useToast();

  if (!pattern) return null;

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const formData = new FormData(e.currentTarget);
    const updatedPattern = {
      ...pattern,
      pattern: formData.get("pattern") as string,
      amount: Number(formData.get("amount")),
      ussd_format: formData.get("ussd_format") as string,
      description: formData.get("description") as string,
    };

    if (!updatedPattern.pattern || !updatedPattern.amount || !updatedPattern.ussd_format) {
      toast({
        title: "Error",
        description: "Please fill in all required fields",
        variant: "destructive",
      });
      return;
    }

    onSave(updatedPattern);
    onClose();
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Edit Bundle Pattern</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="edit-pattern">Bundle Name</Label>
            <Input
              id="edit-pattern"
              name="pattern"
              defaultValue={pattern.pattern}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="edit-amount">Amount (KES)</Label>
            <Input
              id="edit-amount"
              name="amount"
              type="number"
              defaultValue={pattern.amount}
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="edit-ussd">USSD Format</Label>
            <Input
              id="edit-ussd"
              name="ussd_format"
              defaultValue={pattern.ussd_format}
              className="font-mono"
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="edit-description">Description (Optional)</Label>
            <Input
              id="edit-description"
              name="description"
              defaultValue={pattern.description}
            />
          </div>
          <div className="flex justify-end space-x-2">
            <Button type="button" variant="outline" onClick={onClose}>
              Cancel
            </Button>
            <Button type="submit">Save Changes</Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};
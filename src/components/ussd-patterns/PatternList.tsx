import { Trash, Edit } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Pattern } from "@/hooks/useUSSDPatterns";

interface PatternListProps {
  patterns: Pattern[];
  onDelete: (id: string) => void;
  onEdit: (pattern: Pattern) => void;
}

export const PatternList = ({ patterns, onDelete, onEdit }: PatternListProps) => {
  return (
    <div className="overflow-x-auto">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Bundle Name</TableHead>
            <TableHead className="w-24">Amount</TableHead>
            <TableHead>USSD Format</TableHead>
            <TableHead>Description</TableHead>
            <TableHead className="w-24">Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {patterns?.map((pattern) => (
            <TableRow key={pattern.id}>
              <TableCell>{pattern.pattern}</TableCell>
              <TableCell>{pattern.amount} KES</TableCell>
              <TableCell className="font-mono text-sm">
                {pattern.ussd_format}
              </TableCell>
              <TableCell>{pattern.description || "-"}</TableCell>
              <TableCell>
                <div className="flex space-x-2">
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => onEdit(pattern)}
                  >
                    <Edit className="w-4 h-4" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => onDelete(pattern.id)}
                  >
                    <Trash className="w-4 h-4" />
                  </Button>
                </div>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
};
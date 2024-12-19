import { useState } from "react";
import { PatternForm } from "./ussd-patterns/PatternForm";
import { PatternList } from "./ussd-patterns/PatternList";
import { EditPatternDialog } from "./ussd-patterns/EditPatternDialog";
import { useUSSDPatterns, Pattern } from "@/hooks/useUSSDPatterns";

const USSDPatterns = () => {
  const [editingPattern, setEditingPattern] = useState<Pattern | null>(null);
  const { patterns, isLoading, userId, addPattern, updatePattern, deletePattern } = useUSSDPatterns();

  if (!userId) return <div>Loading user...</div>;
  if (isLoading) return <div>Loading patterns...</div>;

  return (
    <div className="space-y-8">
      <PatternForm onSubmit={(pattern) => addPattern.mutate(pattern)} />
      <PatternList
        patterns={patterns || []}
        onDelete={(id) => deletePattern.mutate(id)}
        onEdit={(pattern) => setEditingPattern(pattern)}
      />
      <EditPatternDialog
        pattern={editingPattern}
        isOpen={!!editingPattern}
        onClose={() => setEditingPattern(null)}
        onSave={(pattern) => updatePattern.mutate(pattern)}
      />
    </div>
  );
};

export default USSDPatterns;
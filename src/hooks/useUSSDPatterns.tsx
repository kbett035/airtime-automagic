import { useState, useEffect } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { supabase } from "@/integrations/supabase/client";
import { useToast } from "@/hooks/use-toast";

export interface Pattern {
  id: string;
  pattern: string;
  amount: number;
  pattern_type: "regex" | "exact";
  ussd_format: string;
  description?: string;
}

export interface NewPattern {
  pattern: string;
  amount: string;
  pattern_type: "regex" | "exact";
  ussd_format: string;
  description?: string;
}

export const useUSSDPatterns = () => {
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const [userId, setUserId] = useState<string | null>(null);

  useEffect(() => {
    const getUserId = async () => {
      const { data: { session } } = await supabase.auth.getSession();
      if (session?.user?.id) {
        setUserId(session.user.id);
      }
    };
    getUserId();
  }, []);

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
    enabled: !!userId,
  });

  const addPattern = useMutation({
    mutationFn: async (newPattern: NewPattern) => {
      const { error } = await supabase.from("ussd_patterns").insert([
        {
          pattern: newPattern.pattern,
          amount: parseFloat(newPattern.amount),
          pattern_type: newPattern.pattern_type,
          ussd_format: newPattern.ussd_format,
          description: newPattern.description,
          user_id: userId,
        },
      ]);
      if (error) throw error;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["ussd-patterns"] });
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

  const updatePattern = useMutation({
    mutationFn: async (pattern: Pattern) => {
      const { error } = await supabase
        .from("ussd_patterns")
        .update({
          pattern: pattern.pattern,
          amount: pattern.amount,
          ussd_format: pattern.ussd_format,
          description: pattern.description,
        })
        .eq("id", pattern.id);
      if (error) throw error;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["ussd-patterns"] });
      toast({ title: "Success", description: "Pattern updated successfully" });
    },
    onError: (error) => {
      toast({
        title: "Error",
        description: "Failed to update pattern: " + error.message,
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

  return {
    patterns,
    isLoading,
    userId,
    addPattern,
    updatePattern,
    deletePattern,
  };
};
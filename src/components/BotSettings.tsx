import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { supabase } from "@/integrations/supabase/client";
import { Power } from "lucide-react";
import { Switch } from "@/components/ui/switch";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useToast } from "@/hooks/use-toast";

const BotSettings = () => {
  const { toast } = useToast();
  const queryClient = useQueryClient();

  const { data: settings, isLoading } = useQuery({
    queryKey: ["bot-settings"],
    queryFn: async () => {
      const { data, error } = await supabase
        .from("bot_settings")
        .select("*")
        .maybeSingle();

      if (error && error.code !== "PGRST116") throw error;

      if (!data) {
        const { data: newSettings, error: createError } = await supabase
          .from("bot_settings")
          .insert([
            {
              is_enabled: false,
              ussd_format: "*544*4*6*{phone}#",
            },
          ])
          .select()
          .single();

        if (createError) throw createError;
        return newSettings;
      }

      return data;
    },
  });

  const updateSettings = useMutation({
    mutationFn: async (newSettings: any) => {
      const { error } = await supabase
        .from("bot_settings")
        .upsert({ ...settings, ...newSettings });

      if (error) throw error;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["bot-settings"] });
      toast({ title: "Success", description: "Bot settings updated successfully" });
    },
    onError: (error) => {
      toast({
        title: "Error",
        description: "Failed to update settings: " + error.message,
        variant: "destructive",
      });
    },
  });

  if (isLoading) return <div>Loading settings...</div>;

  return (
    <div className="space-y-4 md:space-y-6">
      <div className="flex items-center justify-between">
        <Label htmlFor="bot-status" className="text-sm md:text-base">Bot Status</Label>
        <Switch
          id="bot-status"
          checked={settings?.is_enabled}
          onCheckedChange={(checked) => updateSettings.mutate({ is_enabled: checked })}
        />
      </div>
      <div className="space-y-2">
        <Label htmlFor="ussd-format" className="text-sm md:text-base">USSD Format</Label>
        <Input
          id="ussd-format"
          value={settings?.ussd_format}
          onChange={(e) => updateSettings.mutate({ ussd_format: e.target.value })}
          placeholder="Enter USSD format e.g. *544*4*6*{phone}#"
          className="font-mono text-sm"
        />
      </div>
    </div>
  );
};

export default BotSettings;
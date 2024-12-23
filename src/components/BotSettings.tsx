import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Power } from "lucide-react";
import { Switch } from "@/components/ui/switch";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { BotSettingsRepository } from "@/utils/supabase/repositories/BotSettingsRepository";

const BotSettings = () => {
  const queryClient = useQueryClient();

  const { data: settings, isLoading } = useQuery({
    queryKey: ["bot-settings"],
    queryFn: BotSettingsRepository.getBotSettings,
  });

  const updateSettings = useMutation({
    mutationFn: BotSettingsRepository.updateBotSettings,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["bot-settings"] });
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
          onCheckedChange={(checked) => updateSettings.mutate({ 
            ...settings,
            is_enabled: checked 
          })}
        />
      </div>
      <div className="space-y-2">
        <Label htmlFor="ussd-format" className="text-sm md:text-base">USSD Format</Label>
        <Input
          id="ussd-format"
          value={settings?.ussd_format}
          onChange={(e) => updateSettings.mutate({ 
            ...settings,
            ussd_format: e.target.value 
          })}
          placeholder="Enter USSD format e.g. *544*4*6*{phone}#"
          className="font-mono text-sm"
        />
      </div>
    </div>
  );
};

export default BotSettings;
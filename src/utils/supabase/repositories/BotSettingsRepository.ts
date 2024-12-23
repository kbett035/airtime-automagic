import { supabase, getCurrentUserId } from '@/integrations/supabase/client';
import { toast } from '@/hooks/use-toast';

export class BotSettingsRepository {
  static async getBotSettings() {
    const userId = await getCurrentUserId();
    if (!userId) {
      toast({
        title: "Authentication Error",
        description: "Please sign in to access bot settings",
        variant: "destructive",
      });
      return null;
    }

    const { data, error } = await supabase
      .from('bot_settings')
      .select('*')
      .eq('user_id', userId)
      .maybeSingle();

    if (error) {
      console.error('Error fetching bot settings:', error);
      toast({
        title: "Error",
        description: "Failed to fetch bot settings",
        variant: "destructive",
      });
      return null;
    }

    return data;
  }

  static async updateBotSettings(settings: any) {
    const userId = await getCurrentUserId();
    if (!userId) {
      toast({
        title: "Authentication Error",
        description: "Please sign in to update settings",
        variant: "destructive",
      });
      return false;
    }

    const { error } = await supabase
      .from('bot_settings')
      .upsert({
        ...settings,
        user_id: userId,
        updated_at: new Date().toISOString(),
      });

    if (error) {
      console.error('Error updating bot settings:', error);
      toast({
        title: "Error",
        description: "Failed to update bot settings",
        variant: "destructive",
      });
      return false;
    }

    toast({
      title: "Success",
      description: "Bot settings updated successfully",
    });
    return true;
  }
}
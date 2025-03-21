export type Json =
  | string
  | number
  | boolean
  | null
  | { [key: string]: Json | undefined }
  | Json[]

export type Database = {
  public: {
    Tables: {
      bot_settings: {
        Row: {
          created_at: string | null
          id: string
          is_enabled: boolean | null
          updated_at: string | null
          user_id: string | null
          ussd_format: string | null
        }
        Insert: {
          created_at?: string | null
          id?: string
          is_enabled?: boolean | null
          updated_at?: string | null
          user_id?: string | null
          ussd_format?: string | null
        }
        Update: {
          created_at?: string | null
          id?: string
          is_enabled?: boolean | null
          updated_at?: string | null
          user_id?: string | null
          ussd_format?: string | null
        }
        Relationships: []
      }
      daily_transactions: {
        Row: {
          amount: number
          created_at: string | null
          id: string
          scheduled_for: string | null
          sender_phone: string
          status: Database["public"]["Enums"]["scheduled_status"] | null
          updated_at: string | null
          user_id: string | null
        }
        Insert: {
          amount: number
          created_at?: string | null
          id?: string
          scheduled_for?: string | null
          sender_phone: string
          status?: Database["public"]["Enums"]["scheduled_status"] | null
          updated_at?: string | null
          user_id?: string | null
        }
        Update: {
          amount?: number
          created_at?: string | null
          id?: string
          scheduled_for?: string | null
          sender_phone?: string
          status?: Database["public"]["Enums"]["scheduled_status"] | null
          updated_at?: string | null
          user_id?: string | null
        }
        Relationships: []
      }
      transaction_logs: {
        Row: {
          amount: number
          created_at: string | null
          id: string
          message_text: string
          sender_phone: string
          status: Database["public"]["Enums"]["transaction_status"] | null
          updated_at: string | null
          user_id: string | null
          ussd_string: string
        }
        Insert: {
          amount: number
          created_at?: string | null
          id?: string
          message_text: string
          sender_phone: string
          status?: Database["public"]["Enums"]["transaction_status"] | null
          updated_at?: string | null
          user_id?: string | null
          ussd_string: string
        }
        Update: {
          amount?: number
          created_at?: string | null
          id?: string
          message_text?: string
          sender_phone?: string
          status?: Database["public"]["Enums"]["transaction_status"] | null
          updated_at?: string | null
          user_id?: string | null
          ussd_string?: string
        }
        Relationships: []
      }
      ussd_patterns: {
        Row: {
          amount: number
          created_at: string | null
          description: string | null
          id: string
          is_active: boolean | null
          pattern: string
          pattern_type:
            | Database["public"]["Enums"]["message_pattern_type"]
            | null
          updated_at: string | null
          user_id: string | null
          ussd_format: string
        }
        Insert: {
          amount: number
          created_at?: string | null
          description?: string | null
          id?: string
          is_active?: boolean | null
          pattern: string
          pattern_type?:
            | Database["public"]["Enums"]["message_pattern_type"]
            | null
          updated_at?: string | null
          user_id?: string | null
          ussd_format?: string
        }
        Update: {
          amount?: number
          created_at?: string | null
          description?: string | null
          id?: string
          is_active?: boolean | null
          pattern?: string
          pattern_type?:
            | Database["public"]["Enums"]["message_pattern_type"]
            | null
          updated_at?: string | null
          user_id?: string | null
          ussd_format?: string
        }
        Relationships: []
      }
    }
    Views: {
      [_ in never]: never
    }
    Functions: {
      [_ in never]: never
    }
    Enums: {
      message_pattern_type: "regex" | "exact"
      scheduled_status: "pending" | "scheduled" | "completed"
      transaction_status: "pending" | "completed" | "failed"
    }
    CompositeTypes: {
      [_ in never]: never
    }
  }
}

type PublicSchema = Database[Extract<keyof Database, "public">]

export type Tables<
  PublicTableNameOrOptions extends
    | keyof (PublicSchema["Tables"] & PublicSchema["Views"])
    | { schema: keyof Database },
  TableName extends PublicTableNameOrOptions extends { schema: keyof Database }
    ? keyof (Database[PublicTableNameOrOptions["schema"]]["Tables"] &
        Database[PublicTableNameOrOptions["schema"]]["Views"])
    : never = never,
> = PublicTableNameOrOptions extends { schema: keyof Database }
  ? (Database[PublicTableNameOrOptions["schema"]]["Tables"] &
      Database[PublicTableNameOrOptions["schema"]]["Views"])[TableName] extends {
      Row: infer R
    }
    ? R
    : never
  : PublicTableNameOrOptions extends keyof (PublicSchema["Tables"] &
        PublicSchema["Views"])
    ? (PublicSchema["Tables"] &
        PublicSchema["Views"])[PublicTableNameOrOptions] extends {
        Row: infer R
      }
      ? R
      : never
    : never

export type TablesInsert<
  PublicTableNameOrOptions extends
    | keyof PublicSchema["Tables"]
    | { schema: keyof Database },
  TableName extends PublicTableNameOrOptions extends { schema: keyof Database }
    ? keyof Database[PublicTableNameOrOptions["schema"]]["Tables"]
    : never = never,
> = PublicTableNameOrOptions extends { schema: keyof Database }
  ? Database[PublicTableNameOrOptions["schema"]]["Tables"][TableName] extends {
      Insert: infer I
    }
    ? I
    : never
  : PublicTableNameOrOptions extends keyof PublicSchema["Tables"]
    ? PublicSchema["Tables"][PublicTableNameOrOptions] extends {
        Insert: infer I
      }
      ? I
      : never
    : never

export type TablesUpdate<
  PublicTableNameOrOptions extends
    | keyof PublicSchema["Tables"]
    | { schema: keyof Database },
  TableName extends PublicTableNameOrOptions extends { schema: keyof Database }
    ? keyof Database[PublicTableNameOrOptions["schema"]]["Tables"]
    : never = never,
> = PublicTableNameOrOptions extends { schema: keyof Database }
  ? Database[PublicTableNameOrOptions["schema"]]["Tables"][TableName] extends {
      Update: infer U
    }
    ? U
    : never
  : PublicTableNameOrOptions extends keyof PublicSchema["Tables"]
    ? PublicSchema["Tables"][PublicTableNameOrOptions] extends {
        Update: infer U
      }
      ? U
      : never
    : never

export type Enums<
  PublicEnumNameOrOptions extends
    | keyof PublicSchema["Enums"]
    | { schema: keyof Database },
  EnumName extends PublicEnumNameOrOptions extends { schema: keyof Database }
    ? keyof Database[PublicEnumNameOrOptions["schema"]]["Enums"]
    : never = never,
> = PublicEnumNameOrOptions extends { schema: keyof Database }
  ? Database[PublicEnumNameOrOptions["schema"]]["Enums"][EnumName]
  : PublicEnumNameOrOptions extends keyof PublicSchema["Enums"]
    ? PublicSchema["Enums"][PublicEnumNameOrOptions]
    : never

export type CompositeTypes<
  PublicCompositeTypeNameOrOptions extends
    | keyof PublicSchema["CompositeTypes"]
    | { schema: keyof Database },
  CompositeTypeName extends PublicCompositeTypeNameOrOptions extends {
    schema: keyof Database
  }
    ? keyof Database[PublicCompositeTypeNameOrOptions["schema"]]["CompositeTypes"]
    : never = never,
> = PublicCompositeTypeNameOrOptions extends { schema: keyof Database }
  ? Database[PublicCompositeTypeNameOrOptions["schema"]]["CompositeTypes"][CompositeTypeName]
  : PublicCompositeTypeNameOrOptions extends keyof PublicSchema["CompositeTypes"]
    ? PublicSchema["CompositeTypes"][PublicCompositeTypeNameOrOptions]
    : never

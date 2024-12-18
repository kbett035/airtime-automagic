import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

interface Message {
  text: string;
  phone: string;
  userId: string;
}

Deno.serve(async (req) => {
  if (req.method === 'OPTIONS') {
    return new Response(null, { headers: corsHeaders })
  }

  try {
    const supabase = createClient(
      Deno.env.get('SUPABASE_URL') ?? '',
      Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? ''
    )

    const { text, phone, userId } = await req.json() as Message

    // Get user's patterns and settings
    const { data: patterns } = await supabase
      .from('ussd_patterns')
      .select('*')
      .eq('user_id', userId)
      .eq('is_active', true)

    const { data: settings } = await supabase
      .from('bot_settings')
      .select('*')
      .eq('user_id', userId)
      .single()

    if (!settings?.is_enabled) {
      return new Response(
        JSON.stringify({ error: 'Bot is disabled' }),
        { headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
      )
    }

    // Try to match the message with patterns
    let matchedAmount: number | null = null
    for (const pattern of patterns || []) {
      if (pattern.pattern_type === 'regex') {
        try {
          const regex = new RegExp(pattern.pattern)
          if (regex.test(text)) {
            matchedAmount = pattern.amount
            break
          }
        } catch (e) {
          console.error('Invalid regex pattern:', e)
        }
      } else {
        if (text.includes(pattern.pattern)) {
          matchedAmount = pattern.amount
          break
        }
      }
    }

    if (!matchedAmount) {
      return new Response(
        JSON.stringify({ error: 'No matching pattern found' }),
        { headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
      )
    }

    // Generate USSD string
    const ussdString = settings.ussd_format.replace('{phone}', phone)

    // Log the transaction
    const { error: logError } = await supabase
      .from('transaction_logs')
      .insert([
        {
          user_id: userId,
          amount: matchedAmount,
          sender_phone: phone,
          message_text: text,
          ussd_string: ussdString,
          status: 'pending'
        }
      ])

    if (logError) throw logError

    return new Response(
      JSON.stringify({ 
        success: true,
        ussd: ussdString,
        amount: matchedAmount
      }),
      { headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
    )

  } catch (error) {
    return new Response(
      JSON.stringify({ error: error.message }),
      { headers: { ...corsHeaders, 'Content-Type': 'application/json' } }
    )
  }
})
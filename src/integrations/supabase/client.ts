import { createClient } from '@supabase/supabase-js';
import type { Database } from './types';

const SUPABASE_URL = "https://xtntoxbhnlpcpsotltiz.supabase.co";
const SUPABASE_PUBLISHABLE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inh0bnRveGJobmxwY3Bzb3RsdGl6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzQ1MTg3MjIsImV4cCI6MjA1MDA5NDcyMn0.lYDCOPI0mkkRcxdsnwoRc5NAnwpYbH-lnHL4mUuugn4";

export const supabase = createClient<Database>(
  SUPABASE_URL,
  SUPABASE_PUBLISHABLE_KEY,
  {
    auth: {
      persistSession: true,
      autoRefreshToken: true,
    },
  }
);

// Helper function to get current user id
export const getCurrentUserId = async () => {
  const { data: { session } } = await supabase.auth.getSession();
  return session?.user?.id;
};
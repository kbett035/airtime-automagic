export const parseAmount = (message: string, patterns: any[]): number | null => {
  for (const pattern of patterns) {
    if (pattern.pattern_type === 'regex') {
      try {
        const regex = new RegExp(pattern.pattern);
        if (regex.test(message)) {
          return pattern.amount;
        }
      } catch (e) {
        console.error('Invalid regex pattern:', e);
      }
    } else {
      // Exact match
      if (message.includes(pattern.pattern)) {
        return pattern.amount;
      }
    }
  }
  return null;
};

export const formatUSSD = (format: string, phone: string): string => {
  return format.replace('{phone}', phone);
};
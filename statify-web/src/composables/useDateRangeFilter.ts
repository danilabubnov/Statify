import { computed, type Ref } from 'vue';
import type { TimeRange } from '../types/filters';

export const useDateRangeFilter = (timeRangeFilter: Ref<TimeRange>) => {
  const dateRange = computed<{ from?: string; to?: string }>(() => {
    if (timeRangeFilter.value === 'all-time') {
      return {};
    }

    const now = new Date();
    const to = now.toISOString().split('T')[0];

    let from: Date;

    if (timeRangeFilter.value === 'month') {
      from = new Date(now);
      from.setMonth(now.getMonth() - 1);
    } else {
      from = new Date(now);
      from.setFullYear(now.getFullYear() - 1);
    }

    return {
      from: from.toISOString().split('T')[0],
      to,
    };
  });

  return { dateRange };
};

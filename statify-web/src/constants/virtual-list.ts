// Default virtual list configuration
export const VIRTUAL_LIST_DEFAULTS = {
  BUFFER: 5,
  KEY_FIELD: 'id',
  SCROLL_TOP: 0,
  CONTAINER_HEIGHT: 800,
  TOTAL_COUNT: 0,
} as const;

// Virtual list CSS classes
export const VIRTUAL_LIST_CLASSES = {
  WRAPPER: 'virtual-list-wrapper',
  ITEM: 'virtual-list-item',
} as const;

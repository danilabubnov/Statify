export const SCROLLBAR_DEFAULTS = {
  THUMB_COLOR: '#FFFFFF80',
  THUMB_HOVER_COLOR: '#FFFFFFA0',
  THUMB_WIDTH: 12,
  THUMB_HEIGHT: 30,
  TRANSITION_DURATION: '0.2s',
  POSITION_RIGHT: '0px',
  Z_INDEX: 50,
} as const;

export const SCROLLBAR_CLASSES = {
  WRAPPER: 'custom-scrollbar-wrapper',
  CONTENT: 'scrollable-content',
  SCROLLBAR: 'custom-scrollbar',
  THUMB: 'custom-scrollbar-thumb',
  FORCE_VISIBLE: 'force-visible',
} as const;

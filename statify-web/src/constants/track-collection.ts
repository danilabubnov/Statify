// Layout constants
export const GRID_LAYOUTS = {
  MOBILE: '[1fr]',
  TABLET: '[50px_1fr]',
  DESKTOP: '[50px_1fr_1fr_120px_80px]',
} as const;

// Scroll and pagination
export const SCROLL_CONFIG = {
  ITEM_HEIGHT: 56,
  PAGE_SIZE: 100,
  DEBOUNCE_MS: 100,
  DEFAULT_CONTAINER_HEIGHT: 800,
  VIRTUAL_LIST_BUFFER: 10,
} as const;

// Fade effects
export const FADE_CONFIG = {
  HEADER: {
    START: 50,
    END: 200,
    MAX_OPACITY: 0.95,
    MAX_BLUR: 4,
  },
  GRADIENT: {
    START: 0,
    END: 230,
  },
} as const;

// Gradient colors
export const GRADIENT_COLORS = {
  TOP: { r: 80, g: 56, b: 160, a: 0.9 },
  MID: { r: 64, g: 44, b: 128, a: 0.8 },
  BOTTOM: { r: 48, g: 32, b: 96, a: 0.5 },
  STOPS: {
    TOP: '0',
    MID: '80px',
    BOTTOM: '150px',
    END: '230px',
  },
} as const;

// Header background color
export const HEADER_BG_COLOR = {
  l: 0.23,
  c: 0.015,
  h: 264.66,
} as const;

// Theme colors
export const THEME_COLORS = {
  EXPLICIT_BADGE_BG: '#B3B3B3',
  HOVER_BG: 'oklch(1_0_0_/_0.298)',
} as const;

// Dimensions
export const DIMENSIONS = {
  COVER_SIZE: 40,
  HEADER_ICON_SIZE: 130,
  EXPLICIT_BADGE_SIZE: 16,
  CONTENT_MAX_WIDTH_PERCENT: 80,
  ALBUM_PADDING_RIGHT_PERCENT: 20,
} as const;

// Transition durations
export const TRANSITIONS = {
  BACKGROUND: '0.3s ease-out',
  HOVER: 'duration-200',
  HEADER: 'duration-300',
} as const;

// Image URLs
export const ASSETS = {
  FAVORITE_TRACKS_ICON: 'https://misc.scdn.co/liked-songs/liked-songs-300.jpg',
} as const;

// Text labels
export const LABELS = {
  PLAYLIST_TYPE: 'Playlist',
  FAVORITE_TRACKS: 'Favorite tracks',
  COLUMNS: {
    NUMBER: '#',
    NAME: 'Name',
    ALBUM: 'Album',
    ADDED_AT: 'Added At',
    DURATION: 'Duration',
  },
} as const;

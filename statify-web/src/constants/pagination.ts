export const PAGINATION = {
  ELLIPSIS: '…',
  SHAKE_DURATION: 1000,
  CONTAINER_OFFSET: 4,
  TRANSITION_TIMING: 'cubic-bezier(.2,.7,.2,1)',
  TRANSITION_DURATION: 220,
  MIN_PAGES_FOR_ELLIPSIS: 3,
} as const;

export const DEFAULT_PAGE = 0;

export const DEFAULT_SIZE = {
  TRACKS: 27,
  ALBUMS: 27,
  ARTISTS: 50,
} as const;

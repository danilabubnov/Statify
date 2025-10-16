export interface PageInfo {
  page: number;
  pageCount: number;
  hasPrev: boolean;
  hasNext: boolean;
}

export interface NavigationActions {
  prev: () => void;
  next: () => void;
  follow: (page: number) => void;
}

export interface IndicatorPosition {
  x: number;
  w: number;
}

export interface InputState {
  value: string;
  isInvalid: boolean;
  isShaking: boolean;
  activeIndex: number | null;
}

export type PageItem = number | string;

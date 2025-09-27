export type ButtonVariant = 'primary' | 'secondary' | 'outline' | 'link' | 'oauth' | 'carousel-arrow';
export type ButtonSize = 'sm' | 'md' | 'lg' | 'icon-sm';
export type ButtonShape = 'none' | 'rounded' | 'pill';

export const SHAPE_CLASSES: Record<ButtonShape, string> = {
  none: '',
  rounded: 'rounded-lg',
  pill: 'rounded-full',
};

export const VARIANT_CLASSES: Record<ButtonVariant, string> = {
  primary: 'btn btn-hoverable btn-primary',
  secondary: 'btn btn-hoverable btn-secondary',
  outline: 'btn btn-hoverable btn-outline border-contrast hover:border-white',
  link: 'font-bold underline hover:text-secondary bg-transparent rounded-none px-0',
  oauth: 'btn btn-hoverable btn-outline border-contrast hover:border-white',
  'carousel-arrow': 'flex items-center justify-center bg-base-100',
};

export const SIZE_CLASSES: Record<ButtonSize, string> = {
  sm: 'btn-sm',
  md: 'btn-md',
  lg: 'btn-lg',
  'icon-sm': 'w-8 h-8',
};

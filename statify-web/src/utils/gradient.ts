import { GRADIENT_COLORS } from '../constants/track-collection';

interface GradientColor {
  r: number;
  g: number;
  b: number;
  a: number;
}

/**
 * Create gradient background style with dynamic opacity
 */
export const createGradientBackground = (opacity: number): string => {
  if (opacity <= 0) return 'transparent';

  const { TOP, MID, BOTTOM } = GRADIENT_COLORS;
  const { STOPS } = GRADIENT_COLORS;

  const formatColor = (color: GradientColor) =>
    `rgba(${color.r}, ${color.g}, ${color.b}, ${color.a * opacity})`;

  return `linear-gradient(
    to bottom,
    ${formatColor(TOP)} ${STOPS.TOP},
    ${formatColor(MID)} ${STOPS.MID},
    ${formatColor(BOTTOM)} ${STOPS.BOTTOM},
    transparent ${STOPS.END}
  )`;
};

/**
 * Calculate fade progress between start and end points
 */
export const calculateFadeProgress = (
  scrollTop: number,
  startFade: number,
  endFade: number
): number => {
  return Math.min(
    Math.max((scrollTop - startFade) / (endFade - startFade), 0),
    1
  );
};

/**
 * Calculate gradient opacity based on scroll position
 */
export const calculateGradientOpacity = (
  scrollTop: number,
  fadeEnd: number
): number => {
  return Math.max(1 - scrollTop / fadeEnd, 0);
};

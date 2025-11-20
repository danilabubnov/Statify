import { GRID_LAYOUTS, DIMENSIONS, THEME_COLORS } from '../constants/track-collection';

/**
 * Get grid column classes for track rows
 */
export const useGridLayout = () => {
  const gridClasses = `grid-cols-${GRID_LAYOUTS.MOBILE} md:grid-cols-${GRID_LAYOUTS.TABLET} lg:grid-cols-${GRID_LAYOUTS.DESKTOP}`;

  return {
    gridClasses,
    GRID_LAYOUTS,
    DIMENSIONS,
    THEME_COLORS,
  };
};

import { gql } from 'graphql-tag';

export const OffsetPageInfoFields = gql`
  fragment OffsetPageInfoFields on OffsetPageInfo {
    page
    size
    hasNextPage
    hasPreviousPage
  }
`;

/* ========== ALBUMS ========== */
export const TopAlbumsByPopularity = gql`
  fragment AlbumPreviewFields on AlbumPreview {
    id
    name
    artists {
      id
      name
    }
    covers {
      imageUrl
      imageHeight
      imageWidth
    }
  }

  query TopAlbumsByPopularity(
    $page: Int!
    $size: Int
    $from: Date
    $to: Date
    $albumType: AlbumType
    $withTotal: Boolean = false
  ) {
    topAlbumsByPopularity(page: $page, size: $size, from: $from, to: $to, albumType: $albumType) {
      items {
        ...AlbumPreviewFields
      }
      pageInfo {
        ...OffsetPageInfoFields
      }
      totalCount @include(if: $withTotal)
    }
  }
`;

/* ========== TRACKS ========== */
export const TopTracksByPopularity = gql`
  fragment TrackPreviewFields on TrackPreview {
    id
    name
    artists {
      id
      name
    }
    covers {
      imageUrl
      imageHeight
      imageWidth
    }
  }

  query TopTracksByPopularity(
    $page: Int!
    $size: Int
    $from: Date
    $to: Date
    $withTotal: Boolean = false
  ) {
    topTracksByPopularity(page: $page, size: $size, from: $from, to: $to) {
      items {
        ...TrackPreviewFields
      }
      pageInfo {
        ...OffsetPageInfoFields
      }
      totalCount @include(if: $withTotal)
    }
  }
`;

/* ========== ARTISTS ========== */
export const TopArtistsByPopularity = gql`
  fragment ArtistPreviewFields on ArtistPreview {
    id
    name
    images {
      imageUrl
      imageHeight
      imageWidth
    }
  }

  query TopArtistsByPopularity(
    $page: Int!
    $size: Int
    $withTotal: Boolean = false
  ) {
    topArtistsByPopularity(page: $page, size: $size) {
      items {
        ...ArtistPreviewFields
      }
      pageInfo {
        ...OffsetPageInfoFields
      }
      totalCount @include(if: $withTotal)
    }
  }
`;

export { TopAlbumsByPopularityDocument as TOP_ALBUMS_QUERY } from './generated/graphql';
export { TopTracksByPopularityDocument as TOP_TRACKS_QUERY } from './generated/graphql';
export { TopArtistsByPopularityDocument as TOP_ARTISTS_QUERY } from './generated/graphql';

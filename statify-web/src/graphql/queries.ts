import { gql } from 'graphql-tag';

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

  query TopAlbumsByPopularity($page: Int!, $size: Int, $year: Int, $albumType: AlbumType) {
    topAlbumsByPopularity(page: $page, size: $size, year: $year, albumType: $albumType) {
      ...AlbumPreviewFields
    }
  }
`;

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

  query TopTracksByPopularity($page: Int!, $size: Int, $year: Int) {
    topTracksByPopularity(page: $page, size: $size, year: $year) {
      ...TrackPreviewFields
    }
  }
`;

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

  query TopArtistsByPopularity($page: Int!, $size: Int) {
    topArtistsByPopularity(page: $page, size: $size) {
      ...ArtistPreviewFields
    }
  }
`;

export { TopAlbumsByPopularityDocument as TOP_ALBUMS_QUERY } from './generated/graphql';
export { TopTracksByPopularityDocument as TOP_TRACKS_QUERY } from './generated/graphql';
export { TopArtistsByPopularityDocument as TOP_ARTISTS_QUERY } from './generated/graphql';

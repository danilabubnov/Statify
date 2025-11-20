import type { TypedDocumentNode as DocumentNode } from '@graphql-typed-document-node/core';
export type Maybe<T> = T | null;
export type InputMaybe<T> = Maybe<T>;
export type Exact<T extends { [key: string]: unknown }> = { [K in keyof T]: T[K] };
export type MakeOptional<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]?: Maybe<T[SubKey]> };
export type MakeMaybe<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]: Maybe<T[SubKey]> };
export type MakeEmpty<T extends { [key: string]: unknown }, K extends keyof T> = { [_ in K]?: never };
export type Incremental<T> = T | { [P in keyof T]?: P extends ' $fragmentName' | '__typename' ? T[P] : never };
/** All built-in and custom scalars, mapped to their actual values */
export type Scalars = {
  ID: { input: string; output: string };
  String: { input: string; output: string };
  Boolean: { input: boolean; output: boolean };
  Int: { input: number; output: number };
  Float: { input: number; output: number };
  Date: { input: unknown; output: unknown };
  Long: { input: number; output: number };
};

export type AlbumPreview = {
  __typename?: 'AlbumPreview';
  artists: Array<ArtistSimple>;
  covers: Array<Image>;
  id: Scalars['ID']['output'];
  name: Scalars['String']['output'];
};

export type AlbumPreviewPage = {
  __typename?: 'AlbumPreviewPage';
  items: Array<AlbumPreview>;
  pageInfo?: Maybe<OffsetPageInfo>;
  totalCount?: Maybe<Scalars['Long']['output']>;
};

export type AlbumType = 'ALBUM' | 'COMPILATION' | 'SINGLE';

export type ArtistPreview = {
  __typename?: 'ArtistPreview';
  id: Scalars['ID']['output'];
  images: Array<Image>;
  name: Scalars['String']['output'];
};

export type ArtistPreviewPage = {
  __typename?: 'ArtistPreviewPage';
  items: Array<ArtistPreview>;
  pageInfo?: Maybe<OffsetPageInfo>;
  totalCount?: Maybe<Scalars['Long']['output']>;
};

export type ArtistSimple = {
  __typename?: 'ArtistSimple';
  followersTotal?: Maybe<Scalars['Int']['output']>;
  id: Scalars['ID']['output'];
  name: Scalars['String']['output'];
  popularity?: Maybe<Scalars['Int']['output']>;
};

export type FavAlbum = {
  __typename?: 'FavAlbum';
  addedAt: Scalars['String']['output'];
  albumType: Scalars['String']['output'];
  id: Scalars['ID']['output'];
  label?: Maybe<Scalars['String']['output']>;
  name: Scalars['String']['output'];
  popularity?: Maybe<Scalars['Int']['output']>;
  releaseDate: Scalars['String']['output'];
  totalTracks: Scalars['Int']['output'];
};

export type FavAlbumConnection = {
  __typename?: 'FavAlbumConnection';
  edges: Array<FavAlbumEdge>;
  pageInfo: PageInfo;
};

export type FavAlbumEdge = {
  __typename?: 'FavAlbumEdge';
  cursor: Scalars['ID']['output'];
  node: FavAlbum;
};

export type FavTrack = {
  __typename?: 'FavTrack';
  addedAt: Scalars['String']['output'];
  album: FavTrackAlbum;
  artists: Array<ArtistSimple>;
  covers: Array<Image>;
  durationMs: Scalars['Int']['output'];
  explicit: Scalars['Boolean']['output'];
  id: Scalars['ID']['output'];
  name: Scalars['String']['output'];
};

export type FavTrackAlbum = {
  __typename?: 'FavTrackAlbum';
  id: Scalars['ID']['output'];
  name: Scalars['String']['output'];
};

export type FavTrackConnection = {
  __typename?: 'FavTrackConnection';
  hasMore: Scalars['Boolean']['output'];
  items: Array<FavTrack>;
  totalCount?: Maybe<Scalars['Int']['output']>;
};

export type Image = {
  __typename?: 'Image';
  imageHeight: Scalars['Int']['output'];
  imageUrl: Scalars['String']['output'];
  imageWidth: Scalars['Int']['output'];
};

export type OffsetPageInfo = {
  __typename?: 'OffsetPageInfo';
  hasNextPage: Scalars['Boolean']['output'];
  hasPreviousPage: Scalars['Boolean']['output'];
  page: Scalars['Int']['output'];
  size: Scalars['Int']['output'];
};

export type PageInfo = {
  __typename?: 'PageInfo';
  endCursor?: Maybe<Scalars['String']['output']>;
  hasNextPage: Scalars['Boolean']['output'];
  hasPreviousPage: Scalars['Boolean']['output'];
  startCursor?: Maybe<Scalars['String']['output']>;
};

export type Query = {
  __typename?: 'Query';
  getMyFavoriteAlbums: FavAlbumConnection;
  getMyFavoriteTracks: FavTrackConnection;
  topAlbumsByPopularity: AlbumPreviewPage;
  topArtistsByPopularity: ArtistPreviewPage;
  topTracksByPopularity: TrackPreviewPage;
};

export type QueryGetMyFavoriteAlbumsArgs = {
  after?: InputMaybe<Scalars['ID']['input']>;
  size?: InputMaybe<Scalars['Int']['input']>;
};

export type QueryGetMyFavoriteTracksArgs = {
  offset?: InputMaybe<Scalars['Int']['input']>;
  size?: InputMaybe<Scalars['Int']['input']>;
};

export type QueryTopAlbumsByPopularityArgs = {
  albumType?: InputMaybe<AlbumType>;
  from?: InputMaybe<Scalars['Date']['input']>;
  page: Scalars['Int']['input'];
  size?: InputMaybe<Scalars['Int']['input']>;
  to?: InputMaybe<Scalars['Date']['input']>;
};

export type QueryTopArtistsByPopularityArgs = {
  page: Scalars['Int']['input'];
  size?: InputMaybe<Scalars['Int']['input']>;
};

export type QueryTopTracksByPopularityArgs = {
  from?: InputMaybe<Scalars['Date']['input']>;
  page: Scalars['Int']['input'];
  size?: InputMaybe<Scalars['Int']['input']>;
  to?: InputMaybe<Scalars['Date']['input']>;
};

export type TrackPreview = {
  __typename?: 'TrackPreview';
  artists: Array<ArtistSimple>;
  covers: Array<Image>;
  id: Scalars['ID']['output'];
  name: Scalars['String']['output'];
};

export type TrackPreviewPage = {
  __typename?: 'TrackPreviewPage';
  items: Array<TrackPreview>;
  pageInfo?: Maybe<OffsetPageInfo>;
  totalCount?: Maybe<Scalars['Long']['output']>;
};

export type OffsetPageInfoFieldsFragment = {
  __typename?: 'OffsetPageInfo';
  page: number;
  size: number;
  hasNextPage: boolean;
  hasPreviousPage: boolean;
};

export type AlbumPreviewFieldsFragment = {
  __typename?: 'AlbumPreview';
  id: string;
  name: string;
  artists: Array<{ __typename?: 'ArtistSimple'; id: string; name: string }>;
  covers: Array<{ __typename?: 'Image'; imageUrl: string; imageHeight: number; imageWidth: number }>;
};

export type TopAlbumsByPopularityQueryVariables = Exact<{
  page: Scalars['Int']['input'];
  size?: InputMaybe<Scalars['Int']['input']>;
  from?: InputMaybe<Scalars['Date']['input']>;
  to?: InputMaybe<Scalars['Date']['input']>;
  albumType?: InputMaybe<AlbumType>;
  withTotal?: InputMaybe<Scalars['Boolean']['input']>;
}>;

export type TopAlbumsByPopularityQuery = {
  __typename?: 'Query';
  topAlbumsByPopularity: {
    __typename?: 'AlbumPreviewPage';
    totalCount?: number | null;
    items: Array<{
      __typename?: 'AlbumPreview';
      id: string;
      name: string;
      artists: Array<{ __typename?: 'ArtistSimple'; id: string; name: string }>;
      covers: Array<{ __typename?: 'Image'; imageUrl: string; imageHeight: number; imageWidth: number }>;
    }>;
    pageInfo?: {
      __typename?: 'OffsetPageInfo';
      page: number;
      size: number;
      hasNextPage: boolean;
      hasPreviousPage: boolean;
    } | null;
  };
};

export type TrackPreviewFieldsFragment = {
  __typename?: 'TrackPreview';
  id: string;
  name: string;
  artists: Array<{ __typename?: 'ArtistSimple'; id: string; name: string }>;
  covers: Array<{ __typename?: 'Image'; imageUrl: string; imageHeight: number; imageWidth: number }>;
};

export type TopTracksByPopularityQueryVariables = Exact<{
  page: Scalars['Int']['input'];
  size?: InputMaybe<Scalars['Int']['input']>;
  from?: InputMaybe<Scalars['Date']['input']>;
  to?: InputMaybe<Scalars['Date']['input']>;
  withTotal?: InputMaybe<Scalars['Boolean']['input']>;
}>;

export type TopTracksByPopularityQuery = {
  __typename?: 'Query';
  topTracksByPopularity: {
    __typename?: 'TrackPreviewPage';
    totalCount?: number | null;
    items: Array<{
      __typename?: 'TrackPreview';
      id: string;
      name: string;
      artists: Array<{ __typename?: 'ArtistSimple'; id: string; name: string }>;
      covers: Array<{ __typename?: 'Image'; imageUrl: string; imageHeight: number; imageWidth: number }>;
    }>;
    pageInfo?: {
      __typename?: 'OffsetPageInfo';
      page: number;
      size: number;
      hasNextPage: boolean;
      hasPreviousPage: boolean;
    } | null;
  };
};

export type ArtistPreviewFieldsFragment = {
  __typename?: 'ArtistPreview';
  id: string;
  name: string;
  images: Array<{ __typename?: 'Image'; imageUrl: string; imageHeight: number; imageWidth: number }>;
};

export type TopArtistsByPopularityQueryVariables = Exact<{
  page: Scalars['Int']['input'];
  size?: InputMaybe<Scalars['Int']['input']>;
  withTotal?: InputMaybe<Scalars['Boolean']['input']>;
}>;

export type TopArtistsByPopularityQuery = {
  __typename?: 'Query';
  topArtistsByPopularity: {
    __typename?: 'ArtistPreviewPage';
    totalCount?: number | null;
    items: Array<{
      __typename?: 'ArtistPreview';
      id: string;
      name: string;
      images: Array<{ __typename?: 'Image'; imageUrl: string; imageHeight: number; imageWidth: number }>;
    }>;
    pageInfo?: {
      __typename?: 'OffsetPageInfo';
      page: number;
      size: number;
      hasNextPage: boolean;
      hasPreviousPage: boolean;
    } | null;
  };
};

export type FavTrackFieldsFragment = {
  __typename?: 'FavTrack';
  id: string;
  name: string;
  durationMs: number;
  explicit: boolean;
  addedAt: string;
  artists: Array<{ __typename?: 'ArtistSimple'; id: string; name: string }>;
  covers: Array<{ __typename?: 'Image'; imageUrl: string; imageHeight: number; imageWidth: number }>;
  album: { __typename?: 'FavTrackAlbum'; id: string; name: string };
};

export type GetMyFavoriteTracksQueryVariables = Exact<{
  size?: InputMaybe<Scalars['Int']['input']>;
  offset?: InputMaybe<Scalars['Int']['input']>;
  withTotal?: InputMaybe<Scalars['Boolean']['input']>;
}>;

export type GetMyFavoriteTracksQuery = {
  __typename?: 'Query';
  getMyFavoriteTracks: {
    __typename?: 'FavTrackConnection';
    totalCount?: number | null;
    hasMore: boolean;
    items: Array<{
      __typename?: 'FavTrack';
      id: string;
      name: string;
      durationMs: number;
      explicit: boolean;
      addedAt: string;
      artists: Array<{ __typename?: 'ArtistSimple'; id: string; name: string }>;
      covers: Array<{ __typename?: 'Image'; imageUrl: string; imageHeight: number; imageWidth: number }>;
      album: { __typename?: 'FavTrackAlbum'; id: string; name: string };
    }>;
  };
};

export const OffsetPageInfoFieldsFragmentDoc = {
  kind: 'Document',
  definitions: [
    {
      kind: 'FragmentDefinition',
      name: { kind: 'Name', value: 'OffsetPageInfoFields' },
      typeCondition: { kind: 'NamedType', name: { kind: 'Name', value: 'OffsetPageInfo' } },
      selectionSet: {
        kind: 'SelectionSet',
        selections: [
          { kind: 'Field', name: { kind: 'Name', value: 'page' } },
          { kind: 'Field', name: { kind: 'Name', value: 'size' } },
          { kind: 'Field', name: { kind: 'Name', value: 'hasNextPage' } },
          { kind: 'Field', name: { kind: 'Name', value: 'hasPreviousPage' } },
        ],
      },
    },
  ],
} as unknown as DocumentNode<OffsetPageInfoFieldsFragment, unknown>;
export const AlbumPreviewFieldsFragmentDoc = {
  kind: 'Document',
  definitions: [
    {
      kind: 'FragmentDefinition',
      name: { kind: 'Name', value: 'AlbumPreviewFields' },
      typeCondition: { kind: 'NamedType', name: { kind: 'Name', value: 'AlbumPreview' } },
      selectionSet: {
        kind: 'SelectionSet',
        selections: [
          { kind: 'Field', name: { kind: 'Name', value: 'id' } },
          { kind: 'Field', name: { kind: 'Name', value: 'name' } },
          {
            kind: 'Field',
            name: { kind: 'Name', value: 'artists' },
            selectionSet: {
              kind: 'SelectionSet',
              selections: [
                { kind: 'Field', name: { kind: 'Name', value: 'id' } },
                { kind: 'Field', name: { kind: 'Name', value: 'name' } },
              ],
            },
          },
          {
            kind: 'Field',
            name: { kind: 'Name', value: 'covers' },
            selectionSet: {
              kind: 'SelectionSet',
              selections: [
                { kind: 'Field', name: { kind: 'Name', value: 'imageUrl' } },
                { kind: 'Field', name: { kind: 'Name', value: 'imageHeight' } },
                { kind: 'Field', name: { kind: 'Name', value: 'imageWidth' } },
              ],
            },
          },
        ],
      },
    },
  ],
} as unknown as DocumentNode<AlbumPreviewFieldsFragment, unknown>;
export const TrackPreviewFieldsFragmentDoc = {
  kind: 'Document',
  definitions: [
    {
      kind: 'FragmentDefinition',
      name: { kind: 'Name', value: 'TrackPreviewFields' },
      typeCondition: { kind: 'NamedType', name: { kind: 'Name', value: 'TrackPreview' } },
      selectionSet: {
        kind: 'SelectionSet',
        selections: [
          { kind: 'Field', name: { kind: 'Name', value: 'id' } },
          { kind: 'Field', name: { kind: 'Name', value: 'name' } },
          {
            kind: 'Field',
            name: { kind: 'Name', value: 'artists' },
            selectionSet: {
              kind: 'SelectionSet',
              selections: [
                { kind: 'Field', name: { kind: 'Name', value: 'id' } },
                { kind: 'Field', name: { kind: 'Name', value: 'name' } },
              ],
            },
          },
          {
            kind: 'Field',
            name: { kind: 'Name', value: 'covers' },
            selectionSet: {
              kind: 'SelectionSet',
              selections: [
                { kind: 'Field', name: { kind: 'Name', value: 'imageUrl' } },
                { kind: 'Field', name: { kind: 'Name', value: 'imageHeight' } },
                { kind: 'Field', name: { kind: 'Name', value: 'imageWidth' } },
              ],
            },
          },
        ],
      },
    },
  ],
} as unknown as DocumentNode<TrackPreviewFieldsFragment, unknown>;
export const ArtistPreviewFieldsFragmentDoc = {
  kind: 'Document',
  definitions: [
    {
      kind: 'FragmentDefinition',
      name: { kind: 'Name', value: 'ArtistPreviewFields' },
      typeCondition: { kind: 'NamedType', name: { kind: 'Name', value: 'ArtistPreview' } },
      selectionSet: {
        kind: 'SelectionSet',
        selections: [
          { kind: 'Field', name: { kind: 'Name', value: 'id' } },
          { kind: 'Field', name: { kind: 'Name', value: 'name' } },
          {
            kind: 'Field',
            name: { kind: 'Name', value: 'images' },
            selectionSet: {
              kind: 'SelectionSet',
              selections: [
                { kind: 'Field', name: { kind: 'Name', value: 'imageUrl' } },
                { kind: 'Field', name: { kind: 'Name', value: 'imageHeight' } },
                { kind: 'Field', name: { kind: 'Name', value: 'imageWidth' } },
              ],
            },
          },
        ],
      },
    },
  ],
} as unknown as DocumentNode<ArtistPreviewFieldsFragment, unknown>;
export const FavTrackFieldsFragmentDoc = {
  kind: 'Document',
  definitions: [
    {
      kind: 'FragmentDefinition',
      name: { kind: 'Name', value: 'FavTrackFields' },
      typeCondition: { kind: 'NamedType', name: { kind: 'Name', value: 'FavTrack' } },
      selectionSet: {
        kind: 'SelectionSet',
        selections: [
          { kind: 'Field', name: { kind: 'Name', value: 'id' } },
          { kind: 'Field', name: { kind: 'Name', value: 'name' } },
          { kind: 'Field', name: { kind: 'Name', value: 'durationMs' } },
          { kind: 'Field', name: { kind: 'Name', value: 'explicit' } },
          { kind: 'Field', name: { kind: 'Name', value: 'addedAt' } },
          {
            kind: 'Field',
            name: { kind: 'Name', value: 'artists' },
            selectionSet: {
              kind: 'SelectionSet',
              selections: [
                { kind: 'Field', name: { kind: 'Name', value: 'id' } },
                { kind: 'Field', name: { kind: 'Name', value: 'name' } },
              ],
            },
          },
          {
            kind: 'Field',
            name: { kind: 'Name', value: 'covers' },
            selectionSet: {
              kind: 'SelectionSet',
              selections: [
                { kind: 'Field', name: { kind: 'Name', value: 'imageUrl' } },
                { kind: 'Field', name: { kind: 'Name', value: 'imageHeight' } },
                { kind: 'Field', name: { kind: 'Name', value: 'imageWidth' } },
              ],
            },
          },
          {
            kind: 'Field',
            name: { kind: 'Name', value: 'album' },
            selectionSet: {
              kind: 'SelectionSet',
              selections: [
                { kind: 'Field', name: { kind: 'Name', value: 'id' } },
                { kind: 'Field', name: { kind: 'Name', value: 'name' } },
              ],
            },
          },
        ],
      },
    },
  ],
} as unknown as DocumentNode<FavTrackFieldsFragment, unknown>;
export const TopAlbumsByPopularityDocument = {
  kind: 'Document',
  definitions: [
    {
      kind: 'OperationDefinition',
      operation: 'query',
      name: { kind: 'Name', value: 'TopAlbumsByPopularity' },
      variableDefinitions: [
        {
          kind: 'VariableDefinition',
          variable: { kind: 'Variable', name: { kind: 'Name', value: 'page' } },
          type: { kind: 'NonNullType', type: { kind: 'NamedType', name: { kind: 'Name', value: 'Int' } } },
        },
        {
          kind: 'VariableDefinition',
          variable: { kind: 'Variable', name: { kind: 'Name', value: 'size' } },
          type: { kind: 'NamedType', name: { kind: 'Name', value: 'Int' } },
        },
        {
          kind: 'VariableDefinition',
          variable: { kind: 'Variable', name: { kind: 'Name', value: 'from' } },
          type: { kind: 'NamedType', name: { kind: 'Name', value: 'Date' } },
        },
        {
          kind: 'VariableDefinition',
          variable: { kind: 'Variable', name: { kind: 'Name', value: 'to' } },
          type: { kind: 'NamedType', name: { kind: 'Name', value: 'Date' } },
        },
        {
          kind: 'VariableDefinition',
          variable: { kind: 'Variable', name: { kind: 'Name', value: 'albumType' } },
          type: { kind: 'NamedType', name: { kind: 'Name', value: 'AlbumType' } },
        },
        {
          kind: 'VariableDefinition',
          variable: { kind: 'Variable', name: { kind: 'Name', value: 'withTotal' } },
          type: { kind: 'NamedType', name: { kind: 'Name', value: 'Boolean' } },
          defaultValue: { kind: 'BooleanValue', value: false },
        },
      ],
      selectionSet: {
        kind: 'SelectionSet',
        selections: [
          {
            kind: 'Field',
            name: { kind: 'Name', value: 'topAlbumsByPopularity' },
            arguments: [
              {
                kind: 'Argument',
                name: { kind: 'Name', value: 'page' },
                value: { kind: 'Variable', name: { kind: 'Name', value: 'page' } },
              },
              {
                kind: 'Argument',
                name: { kind: 'Name', value: 'size' },
                value: { kind: 'Variable', name: { kind: 'Name', value: 'size' } },
              },
              {
                kind: 'Argument',
                name: { kind: 'Name', value: 'from' },
                value: { kind: 'Variable', name: { kind: 'Name', value: 'from' } },
              },
              {
                kind: 'Argument',
                name: { kind: 'Name', value: 'to' },
                value: { kind: 'Variable', name: { kind: 'Name', value: 'to' } },
              },
              {
                kind: 'Argument',
                name: { kind: 'Name', value: 'albumType' },
                value: { kind: 'Variable', name: { kind: 'Name', value: 'albumType' } },
              },
            ],
            selectionSet: {
              kind: 'SelectionSet',
              selections: [
                {
                  kind: 'Field',
                  name: { kind: 'Name', value: 'items' },
                  selectionSet: {
                    kind: 'SelectionSet',
                    selections: [{ kind: 'FragmentSpread', name: { kind: 'Name', value: 'AlbumPreviewFields' } }],
                  },
                },
                {
                  kind: 'Field',
                  name: { kind: 'Name', value: 'pageInfo' },
                  selectionSet: {
                    kind: 'SelectionSet',
                    selections: [{ kind: 'FragmentSpread', name: { kind: 'Name', value: 'OffsetPageInfoFields' } }],
                  },
                },
                {
                  kind: 'Field',
                  name: { kind: 'Name', value: 'totalCount' },
                  directives: [
                    {
                      kind: 'Directive',
                      name: { kind: 'Name', value: 'include' },
                      arguments: [
                        {
                          kind: 'Argument',
                          name: { kind: 'Name', value: 'if' },
                          value: { kind: 'Variable', name: { kind: 'Name', value: 'withTotal' } },
                        },
                      ],
                    },
                  ],
                },
              ],
            },
          },
        ],
      },
    },
    {
      kind: 'FragmentDefinition',
      name: { kind: 'Name', value: 'AlbumPreviewFields' },
      typeCondition: { kind: 'NamedType', name: { kind: 'Name', value: 'AlbumPreview' } },
      selectionSet: {
        kind: 'SelectionSet',
        selections: [
          { kind: 'Field', name: { kind: 'Name', value: 'id' } },
          { kind: 'Field', name: { kind: 'Name', value: 'name' } },
          {
            kind: 'Field',
            name: { kind: 'Name', value: 'artists' },
            selectionSet: {
              kind: 'SelectionSet',
              selections: [
                { kind: 'Field', name: { kind: 'Name', value: 'id' } },
                { kind: 'Field', name: { kind: 'Name', value: 'name' } },
              ],
            },
          },
          {
            kind: 'Field',
            name: { kind: 'Name', value: 'covers' },
            selectionSet: {
              kind: 'SelectionSet',
              selections: [
                { kind: 'Field', name: { kind: 'Name', value: 'imageUrl' } },
                { kind: 'Field', name: { kind: 'Name', value: 'imageHeight' } },
                { kind: 'Field', name: { kind: 'Name', value: 'imageWidth' } },
              ],
            },
          },
        ],
      },
    },
    {
      kind: 'FragmentDefinition',
      name: { kind: 'Name', value: 'OffsetPageInfoFields' },
      typeCondition: { kind: 'NamedType', name: { kind: 'Name', value: 'OffsetPageInfo' } },
      selectionSet: {
        kind: 'SelectionSet',
        selections: [
          { kind: 'Field', name: { kind: 'Name', value: 'page' } },
          { kind: 'Field', name: { kind: 'Name', value: 'size' } },
          { kind: 'Field', name: { kind: 'Name', value: 'hasNextPage' } },
          { kind: 'Field', name: { kind: 'Name', value: 'hasPreviousPage' } },
        ],
      },
    },
  ],
} as unknown as DocumentNode<TopAlbumsByPopularityQuery, TopAlbumsByPopularityQueryVariables>;
export const TopTracksByPopularityDocument = {
  kind: 'Document',
  definitions: [
    {
      kind: 'OperationDefinition',
      operation: 'query',
      name: { kind: 'Name', value: 'TopTracksByPopularity' },
      variableDefinitions: [
        {
          kind: 'VariableDefinition',
          variable: { kind: 'Variable', name: { kind: 'Name', value: 'page' } },
          type: { kind: 'NonNullType', type: { kind: 'NamedType', name: { kind: 'Name', value: 'Int' } } },
        },
        {
          kind: 'VariableDefinition',
          variable: { kind: 'Variable', name: { kind: 'Name', value: 'size' } },
          type: { kind: 'NamedType', name: { kind: 'Name', value: 'Int' } },
        },
        {
          kind: 'VariableDefinition',
          variable: { kind: 'Variable', name: { kind: 'Name', value: 'from' } },
          type: { kind: 'NamedType', name: { kind: 'Name', value: 'Date' } },
        },
        {
          kind: 'VariableDefinition',
          variable: { kind: 'Variable', name: { kind: 'Name', value: 'to' } },
          type: { kind: 'NamedType', name: { kind: 'Name', value: 'Date' } },
        },
        {
          kind: 'VariableDefinition',
          variable: { kind: 'Variable', name: { kind: 'Name', value: 'withTotal' } },
          type: { kind: 'NamedType', name: { kind: 'Name', value: 'Boolean' } },
          defaultValue: { kind: 'BooleanValue', value: false },
        },
      ],
      selectionSet: {
        kind: 'SelectionSet',
        selections: [
          {
            kind: 'Field',
            name: { kind: 'Name', value: 'topTracksByPopularity' },
            arguments: [
              {
                kind: 'Argument',
                name: { kind: 'Name', value: 'page' },
                value: { kind: 'Variable', name: { kind: 'Name', value: 'page' } },
              },
              {
                kind: 'Argument',
                name: { kind: 'Name', value: 'size' },
                value: { kind: 'Variable', name: { kind: 'Name', value: 'size' } },
              },
              {
                kind: 'Argument',
                name: { kind: 'Name', value: 'from' },
                value: { kind: 'Variable', name: { kind: 'Name', value: 'from' } },
              },
              {
                kind: 'Argument',
                name: { kind: 'Name', value: 'to' },
                value: { kind: 'Variable', name: { kind: 'Name', value: 'to' } },
              },
            ],
            selectionSet: {
              kind: 'SelectionSet',
              selections: [
                {
                  kind: 'Field',
                  name: { kind: 'Name', value: 'items' },
                  selectionSet: {
                    kind: 'SelectionSet',
                    selections: [{ kind: 'FragmentSpread', name: { kind: 'Name', value: 'TrackPreviewFields' } }],
                  },
                },
                {
                  kind: 'Field',
                  name: { kind: 'Name', value: 'pageInfo' },
                  selectionSet: {
                    kind: 'SelectionSet',
                    selections: [{ kind: 'FragmentSpread', name: { kind: 'Name', value: 'OffsetPageInfoFields' } }],
                  },
                },
                {
                  kind: 'Field',
                  name: { kind: 'Name', value: 'totalCount' },
                  directives: [
                    {
                      kind: 'Directive',
                      name: { kind: 'Name', value: 'include' },
                      arguments: [
                        {
                          kind: 'Argument',
                          name: { kind: 'Name', value: 'if' },
                          value: { kind: 'Variable', name: { kind: 'Name', value: 'withTotal' } },
                        },
                      ],
                    },
                  ],
                },
              ],
            },
          },
        ],
      },
    },
    {
      kind: 'FragmentDefinition',
      name: { kind: 'Name', value: 'TrackPreviewFields' },
      typeCondition: { kind: 'NamedType', name: { kind: 'Name', value: 'TrackPreview' } },
      selectionSet: {
        kind: 'SelectionSet',
        selections: [
          { kind: 'Field', name: { kind: 'Name', value: 'id' } },
          { kind: 'Field', name: { kind: 'Name', value: 'name' } },
          {
            kind: 'Field',
            name: { kind: 'Name', value: 'artists' },
            selectionSet: {
              kind: 'SelectionSet',
              selections: [
                { kind: 'Field', name: { kind: 'Name', value: 'id' } },
                { kind: 'Field', name: { kind: 'Name', value: 'name' } },
              ],
            },
          },
          {
            kind: 'Field',
            name: { kind: 'Name', value: 'covers' },
            selectionSet: {
              kind: 'SelectionSet',
              selections: [
                { kind: 'Field', name: { kind: 'Name', value: 'imageUrl' } },
                { kind: 'Field', name: { kind: 'Name', value: 'imageHeight' } },
                { kind: 'Field', name: { kind: 'Name', value: 'imageWidth' } },
              ],
            },
          },
        ],
      },
    },
    {
      kind: 'FragmentDefinition',
      name: { kind: 'Name', value: 'OffsetPageInfoFields' },
      typeCondition: { kind: 'NamedType', name: { kind: 'Name', value: 'OffsetPageInfo' } },
      selectionSet: {
        kind: 'SelectionSet',
        selections: [
          { kind: 'Field', name: { kind: 'Name', value: 'page' } },
          { kind: 'Field', name: { kind: 'Name', value: 'size' } },
          { kind: 'Field', name: { kind: 'Name', value: 'hasNextPage' } },
          { kind: 'Field', name: { kind: 'Name', value: 'hasPreviousPage' } },
        ],
      },
    },
  ],
} as unknown as DocumentNode<TopTracksByPopularityQuery, TopTracksByPopularityQueryVariables>;
export const TopArtistsByPopularityDocument = {
  kind: 'Document',
  definitions: [
    {
      kind: 'OperationDefinition',
      operation: 'query',
      name: { kind: 'Name', value: 'TopArtistsByPopularity' },
      variableDefinitions: [
        {
          kind: 'VariableDefinition',
          variable: { kind: 'Variable', name: { kind: 'Name', value: 'page' } },
          type: { kind: 'NonNullType', type: { kind: 'NamedType', name: { kind: 'Name', value: 'Int' } } },
        },
        {
          kind: 'VariableDefinition',
          variable: { kind: 'Variable', name: { kind: 'Name', value: 'size' } },
          type: { kind: 'NamedType', name: { kind: 'Name', value: 'Int' } },
        },
        {
          kind: 'VariableDefinition',
          variable: { kind: 'Variable', name: { kind: 'Name', value: 'withTotal' } },
          type: { kind: 'NamedType', name: { kind: 'Name', value: 'Boolean' } },
          defaultValue: { kind: 'BooleanValue', value: false },
        },
      ],
      selectionSet: {
        kind: 'SelectionSet',
        selections: [
          {
            kind: 'Field',
            name: { kind: 'Name', value: 'topArtistsByPopularity' },
            arguments: [
              {
                kind: 'Argument',
                name: { kind: 'Name', value: 'page' },
                value: { kind: 'Variable', name: { kind: 'Name', value: 'page' } },
              },
              {
                kind: 'Argument',
                name: { kind: 'Name', value: 'size' },
                value: { kind: 'Variable', name: { kind: 'Name', value: 'size' } },
              },
            ],
            selectionSet: {
              kind: 'SelectionSet',
              selections: [
                {
                  kind: 'Field',
                  name: { kind: 'Name', value: 'items' },
                  selectionSet: {
                    kind: 'SelectionSet',
                    selections: [{ kind: 'FragmentSpread', name: { kind: 'Name', value: 'ArtistPreviewFields' } }],
                  },
                },
                {
                  kind: 'Field',
                  name: { kind: 'Name', value: 'pageInfo' },
                  selectionSet: {
                    kind: 'SelectionSet',
                    selections: [{ kind: 'FragmentSpread', name: { kind: 'Name', value: 'OffsetPageInfoFields' } }],
                  },
                },
                {
                  kind: 'Field',
                  name: { kind: 'Name', value: 'totalCount' },
                  directives: [
                    {
                      kind: 'Directive',
                      name: { kind: 'Name', value: 'include' },
                      arguments: [
                        {
                          kind: 'Argument',
                          name: { kind: 'Name', value: 'if' },
                          value: { kind: 'Variable', name: { kind: 'Name', value: 'withTotal' } },
                        },
                      ],
                    },
                  ],
                },
              ],
            },
          },
        ],
      },
    },
    {
      kind: 'FragmentDefinition',
      name: { kind: 'Name', value: 'ArtistPreviewFields' },
      typeCondition: { kind: 'NamedType', name: { kind: 'Name', value: 'ArtistPreview' } },
      selectionSet: {
        kind: 'SelectionSet',
        selections: [
          { kind: 'Field', name: { kind: 'Name', value: 'id' } },
          { kind: 'Field', name: { kind: 'Name', value: 'name' } },
          {
            kind: 'Field',
            name: { kind: 'Name', value: 'images' },
            selectionSet: {
              kind: 'SelectionSet',
              selections: [
                { kind: 'Field', name: { kind: 'Name', value: 'imageUrl' } },
                { kind: 'Field', name: { kind: 'Name', value: 'imageHeight' } },
                { kind: 'Field', name: { kind: 'Name', value: 'imageWidth' } },
              ],
            },
          },
        ],
      },
    },
    {
      kind: 'FragmentDefinition',
      name: { kind: 'Name', value: 'OffsetPageInfoFields' },
      typeCondition: { kind: 'NamedType', name: { kind: 'Name', value: 'OffsetPageInfo' } },
      selectionSet: {
        kind: 'SelectionSet',
        selections: [
          { kind: 'Field', name: { kind: 'Name', value: 'page' } },
          { kind: 'Field', name: { kind: 'Name', value: 'size' } },
          { kind: 'Field', name: { kind: 'Name', value: 'hasNextPage' } },
          { kind: 'Field', name: { kind: 'Name', value: 'hasPreviousPage' } },
        ],
      },
    },
  ],
} as unknown as DocumentNode<TopArtistsByPopularityQuery, TopArtistsByPopularityQueryVariables>;
export const GetMyFavoriteTracksDocument = {
  kind: 'Document',
  definitions: [
    {
      kind: 'OperationDefinition',
      operation: 'query',
      name: { kind: 'Name', value: 'GetMyFavoriteTracks' },
      variableDefinitions: [
        {
          kind: 'VariableDefinition',
          variable: { kind: 'Variable', name: { kind: 'Name', value: 'size' } },
          type: { kind: 'NamedType', name: { kind: 'Name', value: 'Int' } },
        },
        {
          kind: 'VariableDefinition',
          variable: { kind: 'Variable', name: { kind: 'Name', value: 'offset' } },
          type: { kind: 'NamedType', name: { kind: 'Name', value: 'Int' } },
        },
        {
          kind: 'VariableDefinition',
          variable: { kind: 'Variable', name: { kind: 'Name', value: 'withTotal' } },
          type: { kind: 'NamedType', name: { kind: 'Name', value: 'Boolean' } },
          defaultValue: { kind: 'BooleanValue', value: false },
        },
      ],
      selectionSet: {
        kind: 'SelectionSet',
        selections: [
          {
            kind: 'Field',
            name: { kind: 'Name', value: 'getMyFavoriteTracks' },
            arguments: [
              {
                kind: 'Argument',
                name: { kind: 'Name', value: 'size' },
                value: { kind: 'Variable', name: { kind: 'Name', value: 'size' } },
              },
              {
                kind: 'Argument',
                name: { kind: 'Name', value: 'offset' },
                value: { kind: 'Variable', name: { kind: 'Name', value: 'offset' } },
              },
            ],
            selectionSet: {
              kind: 'SelectionSet',
              selections: [
                {
                  kind: 'Field',
                  name: { kind: 'Name', value: 'items' },
                  selectionSet: {
                    kind: 'SelectionSet',
                    selections: [{ kind: 'FragmentSpread', name: { kind: 'Name', value: 'FavTrackFields' } }],
                  },
                },
                {
                  kind: 'Field',
                  name: { kind: 'Name', value: 'totalCount' },
                  directives: [
                    {
                      kind: 'Directive',
                      name: { kind: 'Name', value: 'include' },
                      arguments: [
                        {
                          kind: 'Argument',
                          name: { kind: 'Name', value: 'if' },
                          value: { kind: 'Variable', name: { kind: 'Name', value: 'withTotal' } },
                        },
                      ],
                    },
                  ],
                },
                { kind: 'Field', name: { kind: 'Name', value: 'hasMore' } },
              ],
            },
          },
        ],
      },
    },
    {
      kind: 'FragmentDefinition',
      name: { kind: 'Name', value: 'FavTrackFields' },
      typeCondition: { kind: 'NamedType', name: { kind: 'Name', value: 'FavTrack' } },
      selectionSet: {
        kind: 'SelectionSet',
        selections: [
          { kind: 'Field', name: { kind: 'Name', value: 'id' } },
          { kind: 'Field', name: { kind: 'Name', value: 'name' } },
          { kind: 'Field', name: { kind: 'Name', value: 'durationMs' } },
          { kind: 'Field', name: { kind: 'Name', value: 'explicit' } },
          { kind: 'Field', name: { kind: 'Name', value: 'addedAt' } },
          {
            kind: 'Field',
            name: { kind: 'Name', value: 'artists' },
            selectionSet: {
              kind: 'SelectionSet',
              selections: [
                { kind: 'Field', name: { kind: 'Name', value: 'id' } },
                { kind: 'Field', name: { kind: 'Name', value: 'name' } },
              ],
            },
          },
          {
            kind: 'Field',
            name: { kind: 'Name', value: 'covers' },
            selectionSet: {
              kind: 'SelectionSet',
              selections: [
                { kind: 'Field', name: { kind: 'Name', value: 'imageUrl' } },
                { kind: 'Field', name: { kind: 'Name', value: 'imageHeight' } },
                { kind: 'Field', name: { kind: 'Name', value: 'imageWidth' } },
              ],
            },
          },
          {
            kind: 'Field',
            name: { kind: 'Name', value: 'album' },
            selectionSet: {
              kind: 'SelectionSet',
              selections: [
                { kind: 'Field', name: { kind: 'Name', value: 'id' } },
                { kind: 'Field', name: { kind: 'Name', value: 'name' } },
              ],
            },
          },
        ],
      },
    },
  ],
} as unknown as DocumentNode<GetMyFavoriteTracksQuery, GetMyFavoriteTracksQueryVariables>;

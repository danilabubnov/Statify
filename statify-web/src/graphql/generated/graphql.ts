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
  /** An RFC-3339 compliant Full Date Scalar */
  Date: { input: unknown; output: unknown };
  /** A 64-bit signed integer */
  Long: { input: number; output: number };
  _FieldSet: { input: unknown; output: unknown };
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

export type ErrorDetail =
  /**
   * The deadline expired before the operation could complete.
   *
   * For operations that change the state of the system, this error
   * may be returned even if the operation has completed successfully.
   * For example, a successful response from a server could have been
   * delayed long enough for the deadline to expire.
   *
   * HTTP Mapping: 504 Gateway Timeout
   * Error Type: UNAVAILABLE
   */
  | 'DEADLINE_EXCEEDED'
  /**
   * The server detected that the client is exhibiting a behavior that
   * might be generating excessive load.
   *
   * HTTP Mapping: 420 Enhance Your Calm
   * Error Type: UNAVAILABLE
   */
  | 'ENHANCE_YOUR_CALM'
  /**
   * The requested field is not found in the schema.
   *
   * This differs from `NOT_FOUND` in that `NOT_FOUND` should be used when a
   * query is valid, but is unable to return a result (if, for example, a
   * specific video id doesn't exist). `FIELD_NOT_FOUND` is intended to be
   * returned by the server to signify that the requested field is not known to exist.
   * This may be returned in lieu of failing the entire query.
   * See also `PERMISSION_DENIED` for cases where the
   * requested field is invalid only for the given user or class of users.
   *
   * HTTP Mapping: 404 Not Found
   * Error Type: BAD_REQUEST
   */
  | 'FIELD_NOT_FOUND'
  /**
   * The client specified an invalid argument.
   *
   * Note that this differs from `FAILED_PRECONDITION`.
   * `INVALID_ARGUMENT` indicates arguments that are problematic
   * regardless of the state of the system (e.g., a malformed file name).
   *
   * HTTP Mapping: 400 Bad Request
   * Error Type: BAD_REQUEST
   */
  | 'INVALID_ARGUMENT'
  /**
   * The provided cursor is not valid.
   *
   * The most common usage for this error is when a client is paginating
   * through a list that uses stateful cursors. In that case, the provided
   * cursor may be expired.
   *
   * HTTP Mapping: 404 Not Found
   * Error Type: NOT_FOUND
   */
  | 'INVALID_CURSOR'
  /**
   * Unable to perform operation because a required resource is missing.
   *
   * Example: Client is attempting to refresh a list, but the specified
   * list is expired. This requires an action by the client to get a new list.
   *
   * If the user is simply trying GET a resource that is not found,
   * use the NOT_FOUND error type. FAILED_PRECONDITION.MISSING_RESOURCE
   * is to be used particularly when the user is performing an operation
   * that requires a particular resource to exist.
   *
   * HTTP Mapping: 400 Bad Request or 500 Internal Server Error
   * Error Type: FAILED_PRECONDITION
   */
  | 'MISSING_RESOURCE'
  /**
   * Service Error.
   *
   * There is a problem with an upstream service.
   *
   * This may be returned if a gateway receives an unknown error from a service
   * or if a service is unreachable.
   * If a request times out which waiting on a response from a service,
   * `DEADLINE_EXCEEDED` may be returned instead.
   * If a service returns a more specific error Type, the specific error Type may
   * be returned instead.
   *
   * HTTP Mapping: 502 Bad Gateway
   * Error Type: UNAVAILABLE
   */
  | 'SERVICE_ERROR'
  /**
   * Request failed due to network errors.
   *
   * HTTP Mapping: 503 Unavailable
   * Error Type: UNAVAILABLE
   */
  | 'TCP_FAILURE'
  /**
   * Request throttled based on server concurrency limits.
   *
   * HTTP Mapping: 503 Unavailable
   * Error Type: UNAVAILABLE
   */
  | 'THROTTLED_CONCURRENCY'
  /**
   * Request throttled based on server CPU limits
   *
   * HTTP Mapping: 503 Unavailable.
   * Error Type: UNAVAILABLE
   */
  | 'THROTTLED_CPU'
  /**
   * The server detected that the client is exhibiting a behavior that
   * might be generating excessive load.
   *
   * HTTP Mapping: 429 Too Many Requests
   * Error Type: UNAVAILABLE
   */
  | 'TOO_MANY_REQUESTS'
  /**
   * The operation is not implemented or is not currently supported/enabled.
   *
   * HTTP Mapping: 501 Not Implemented
   * Error Type: BAD_REQUEST
   */
  | 'UNIMPLEMENTED'
  /**
   * Unknown error.
   *
   * This error should only be returned when no other error detail applies.
   * If a client sees an unknown errorDetail, it will be interpreted as UNKNOWN.
   *
   * HTTP Mapping: 500 Internal Server Error
   */
  | 'UNKNOWN';

export type ErrorType =
  /**
   * Bad Request.
   *
   * There is a problem with the request.
   * Retrying the same request is not likely to succeed.
   * An example would be a query or argument that cannot be deserialized.
   *
   * HTTP Mapping: 400 Bad Request
   */
  | 'BAD_REQUEST'
  /**
   * The operation was rejected because the system is not in a state
   * required for the operation's execution.  For example, the directory
   * to be deleted is non-empty, an rmdir operation is applied to
   * a non-directory, etc.
   *
   * Service implementers can use the following guidelines to decide
   * between `FAILED_PRECONDITION` and `UNAVAILABLE`:
   *
   * - Use `UNAVAILABLE` if the client can retry just the failing call.
   * - Use `FAILED_PRECONDITION` if the client should not retry until
   * the system state has been explicitly fixed.  E.g., if an "rmdir"
   *      fails because the directory is non-empty, `FAILED_PRECONDITION`
   * should be returned since the client should not retry unless
   * the files are deleted from the directory.
   *
   * HTTP Mapping: 400 Bad Request or 500 Internal Server Error
   */
  | 'FAILED_PRECONDITION'
  /**
   * Internal error.
   *
   * An unexpected internal error was encountered. This means that some
   * invariants expected by the underlying system have been broken.
   * This error code is reserved for serious errors.
   *
   * HTTP Mapping: 500 Internal Server Error
   */
  | 'INTERNAL'
  /**
   * The requested entity was not found.
   *
   * This could apply to a resource that has never existed (e.g. bad resource id),
   * or a resource that no longer exists (e.g. cache expired.)
   *
   * Note to server developers: if a request is denied for an entire class
   * of users, such as gradual feature rollout or undocumented allowlist,
   * `NOT_FOUND` may be used. If a request is denied for some users within
   * a class of users, such as user-based access control, `PERMISSION_DENIED`
   * must be used.
   *
   * HTTP Mapping: 404 Not Found
   */
  | 'NOT_FOUND'
  /**
   * The caller does not have permission to execute the specified
   * operation.
   *
   * `PERMISSION_DENIED` must not be used for rejections
   * caused by exhausting some resource or quota.
   * `PERMISSION_DENIED` must not be used if the caller
   * cannot be identified (use `UNAUTHENTICATED`
   * instead for those errors).
   *
   * This error Type does not imply the
   * request is valid or the requested entity exists or satisfies
   * other pre-conditions.
   *
   * HTTP Mapping: 403 Forbidden
   */
  | 'PERMISSION_DENIED'
  /**
   * The request does not have valid authentication credentials.
   *
   * This is intended to be returned only for routes that require
   * authentication.
   *
   * HTTP Mapping: 401 Unauthorized
   */
  | 'UNAUTHENTICATED'
  /**
   * Currently Unavailable.
   *
   * The service is currently unavailable.  This is most likely a
   * transient condition, which can be corrected by retrying with
   * a backoff.
   *
   * HTTP Mapping: 503 Unavailable
   */
  | 'UNAVAILABLE'
  /**
   * Unknown error.
   *
   * For example, this error may be returned when
   * an error code received from another address space belongs to
   * an error space that is not known in this address space.  Also
   * errors raised by APIs that do not return enough error information
   * may be converted to this error.
   *
   * If a client sees an unknown errorType, it will be interpreted as UNKNOWN.
   * Unknown errors MUST NOT trigger any special behavior. These MAY be treated
   * by an implementation as being equivalent to INTERNAL.
   *
   * When possible, a more specific error should be provided.
   *
   * HTTP Mapping: 520 Unknown Error
   */
  | 'UNKNOWN';

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
  albumId: Scalars['ID']['output'];
  durationMs: Scalars['Int']['output'];
  explicit: Scalars['Boolean']['output'];
  id: Scalars['ID']['output'];
  name: Scalars['String']['output'];
  popularity?: Maybe<Scalars['Int']['output']>;
  trackNumber: Scalars['Int']['output'];
};

export type FavTrackConnection = {
  __typename?: 'FavTrackConnection';
  edges: Array<FavTrackEdge>;
  pageInfo: PageInfo;
};

export type FavTrackEdge = {
  __typename?: 'FavTrackEdge';
  cursor: Scalars['ID']['output'];
  node: FavTrack;
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
  _service: _Service;
  getFavoriteAlbums: FavAlbumConnection;
  getFavoriteTracks: FavTrackConnection;
  topAlbumsByPopularity: AlbumPreviewPage;
  topArtistsByPopularity: ArtistPreviewPage;
  topTracksByPopularity: TrackPreviewPage;
};

export type QueryGetFavoriteAlbumsArgs = {
  after?: InputMaybe<Scalars['ID']['input']>;
  size?: InputMaybe<Scalars['Int']['input']>;
  userId: Scalars['ID']['input'];
};

export type QueryGetFavoriteTracksArgs = {
  after?: InputMaybe<Scalars['ID']['input']>;
  size?: InputMaybe<Scalars['Int']['input']>;
  userId: Scalars['ID']['input'];
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

export type _Service = {
  __typename?: '_Service';
  sdl: Scalars['String']['output'];
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

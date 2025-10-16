import type { AlbumPreview, ArtistPreview, TrackPreview } from "./generated/graphql";


export type Content =
  | AlbumPreview
  | ArtistPreview
  | TrackPreview;